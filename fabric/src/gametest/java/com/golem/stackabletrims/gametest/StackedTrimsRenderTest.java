package com.golem.stackabletrims.gametest;

import com.golem.stackabletrims.core.CoreServices;
import com.golem.stackabletrims.showcase.TrimShowcase;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Visual regression test for stacked trim rendering.
 *
 * <p>Spawns a single armor stand directly in front of the player in a deterministic flat world and
 * screenshots it wearing diamond armor with 0, 1, 2 and 4 stacked trims. Each additional trim must
 * change a meaningful number of pixels (proving the extra trims are actually drawn by the
 * {@code EquipmentLayerRenderer} mixin), while re-taking a screenshot with no change must be
 * pixel-identical (proving the comparison isn't just picking up noise).
 *
 * <p>Finally it runs {@code /stackabletrims showcase} and screenshots the whole row for humans.
 * Screenshots land in {@code fabric/build/run/clientGameTest/screenshots}.
 */
public class StackedTrimsRenderTest implements FabricClientGameTest {

    private static final Logger LOGGER = LoggerFactory.getLogger("stackabletrims-gametest");

    /** Minimum differing pixels for "a trim was added" to count as visible. */
    private static final int MIN_VISIBLE_DIFF = 150;
    /** Maximum differing pixels tolerated between two screenshots of an unchanged scene. */
    private static final int MAX_NOISE = 0;

    // Flat world surface is y = -60. Player looks south (+Z, yaw 0); the stand faces north (yaw 180).
    private static final Vec3 PLAYER_POS = new Vec3(0.5, -60, 0.5);
    private static final Vec3 STAND_POS = new Vec3(0.5, -60, 3.0);

    @Override
    public void runTest(ClientGameTestContext ctx) {
        ctx.runOnClient(mc -> {
            mc.options.cloudStatus().set(CloudStatus.OFF);
            setHudHidden(mc, true);
        });

        try (TestSingleplayerContext sp = ctx.worldBuilder()
                .adjustSettings(ui -> {
                    ui.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
                    ui.setAllowCommands(true);
                })
                .create()) {

            sp.getConnection().waitForChunksRender();

            // Fixed, bright, static lighting and a fixed camera.
            sp.getServer().runCommand("time set noon");
            sp.getServer().runCommand("weather clear");
            sp.getServer().runCommand("tp @a " + PLAYER_POS.x + " " + PLAYER_POS.y + " " + PLAYER_POS.z + " 0 0");
            sp.getConnection().waitForClientboundPackets();
            ctx.waitTicks(10);

            // One stand, re-dressed between screenshots so the camera and scene never move.
            int standId = sp.getServer().computeOnServer(server -> {
                ServerLevel level = server.overworld();
                ArmorStand stand = TrimShowcase.spawnStand(level, STAND_POS, 180.0F, null);
                return stand.getId();
            });

            Path shot0 = dressAndShoot(ctx, sp, standId, 0, "stand_0_trims");
            Path shot1 = dressAndShoot(ctx, sp, standId, 1, "stand_1_trim");
            Path shot2 = dressAndShoot(ctx, sp, standId, 2, "stand_2_trims_stacked");
            Path shot4 = dressAndShoot(ctx, sp, standId, 4, "stand_4_trims_stacked");
            Path shot4b = dressAndShoot(ctx, sp, standId, 4, "stand_4_trims_stacked_repeat");

            int d01 = diffPixels(shot0, shot1);
            int d12 = diffPixels(shot1, shot2);
            int d24 = diffPixels(shot2, shot4);
            int d44 = diffPixels(shot4, shot4b);
            LOGGER.info("Pixel diffs: 0->1 trim = {}, 1->2 trims = {}, 2->4 trims = {}, 4->4 (control) = {}",
                    d01, d12, d24, d44);

            assertTrue(d44 <= MAX_NOISE, "Control: unchanged scene should be pixel-identical but " + d44 + " pixels differ");
            assertTrue(d01 >= MIN_VISIBLE_DIFF, "Vanilla single trim not visible: only " + d01 + " pixels changed");
            assertTrue(d12 >= MIN_VISIBLE_DIFF, "Second stacked trim not rendered: only " + d12 + " pixels changed");
            assertTrue(d24 >= MIN_VISIBLE_DIFF, "3rd/4th stacked trims not rendered: only " + d24 + " pixels changed");

            // Sanity-check the component state the renderer is reading.
            int trimCount = sp.getServer().computeOnServer(server -> {
                ArmorStand stand = (ArmorStand) server.overworld().getEntity(standId);
                return CoreServices.trims().getTrimCount(stand.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST));
            });
            assertTrue(trimCount == 4, "Expected 4 stacked trims on the chestplate, found " + trimCount);

            // Human-readable showcase of the whole row (also exercises the command on the real dispatcher).
            sp.getServer().runOnServer(server -> TrimShowcase.clear(server.overworld()));
            ctx.runOnClient(mc -> mc.player.connection.sendCommand("stackabletrims showcase 6"));
            sp.getConnection().waitForClientboundEntityUpdates(EntityTypes.ARMOR_STAND);
            ctx.waitTicks(20);
            ctx.runOnClient(mc -> setHudHidden(mc, false));
            ctx.waitTicks(2);
            Path showcase = ctx.takeScreenshot("showcase_row");
            LOGGER.info("Showcase screenshot: {}", showcase);

            int standCount = sp.getServer().computeOnServer(server ->
                    server.overworld().getEntities(EntityTypes.ARMOR_STAND,
                            e -> e.entityTags().contains(TrimShowcase.STAND_TAG)).size());
            assertTrue(standCount == TrimShowcase.defaultEntries().size(),
                    "Showcase command spawned " + standCount + " stands, expected " + TrimShowcase.defaultEntries().size());
        } finally {
            ctx.runOnClient(mc -> setHudHidden(mc, false));
        }

        exitWhenClientStops(ctx);
    }

    /**
     * Architectury's dev-time transformer runtime leaves non-daemon thread pools alive, so the JVM
     * never exits after {@code Minecraft.run()} returns and the client shutdown watchdog eventually
     * reports a bogus crash (non-zero exit). Once this test has passed, exit cleanly as soon as the
     * render thread finishes — unless the runner recorded a failure after we returned.
     */
    private static void exitWhenClientStops(ClientGameTestContext ctx) {
        Thread renderThread = ctx.computeOnClient(mc -> Thread.currentThread());
        Thread exiter = new Thread(() -> {
            try {
                renderThread.join();
            } catch (InterruptedException ignored) {
                return;
            }
            if (net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl.testFailureException == null) {
                LOGGER.info("Client stopped after passing tests; exiting.");
                System.exit(0);
            }
        }, "stackabletrims-gametest-exit");
        exiter.setDaemon(true);
        exiter.start();
    }

    private static Path dressAndShoot(ClientGameTestContext ctx, TestSingleplayerContext sp, int standId,
                                      int trims, String name) {
        sp.getServer().runOnServer(server -> {
            ServerLevel level = server.overworld();
            ArmorStand stand = (ArmorStand) level.getEntity(standId);
            ItemStack[] set = TrimShowcase.buildSet(level.registryAccess(),
                    TrimShowcase.Entry.of(name, TrimShowcase.DIAMOND, trims));
            TrimShowcase.equip(stand, set);
        });
        sp.getConnection().waitForClientboundEntityUpdates(EntityTypes.ARMOR_STAND);
        ctx.waitTicks(5);
        Path path = ctx.takeScreenshot(name);
        LOGGER.info("Screenshot ({} trims): {}", trims, path);
        return path;
    }

    private static int diffPixels(Path a, Path b) {
        try (NativeImage ia = read(a); NativeImage ib = read(b)) {
            assertTrue(ia.getWidth() == ib.getWidth() && ia.getHeight() == ib.getHeight(),
                    "Screenshot sizes differ: " + a + " vs " + b);
            int diff = 0;
            for (int y = 0; y < ia.getHeight(); y++) {
                for (int x = 0; x < ia.getWidth(); x++) {
                    if (ia.getPixel(x, y) != ib.getPixel(x, y)) diff++;
                }
            }
            return diff;
        } catch (IOException e) {
            throw new AssertionError("Failed to read screenshots " + a + " / " + b, e);
        }
    }

    private static NativeImage read(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return NativeImage.read(in);
        }
    }

    private static void setHudHidden(net.minecraft.client.Minecraft mc, boolean hidden) {
        if (mc.gui.hud.isHidden() != hidden) mc.gui.hud.toggle();
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
