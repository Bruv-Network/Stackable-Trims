package com.golem.stackabletrims.showcase;

import com.golem.stackabletrims.StackableTrims;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * {@code /stackabletrims showcase [distance]} — spawns a row of armor stands wearing armor with
 * increasing numbers of stacked trims in front of the player, for eyeballing the renderer.
 * {@code /stackabletrims stand <trims>} — spawns a single stand with N stacked trims.
 * {@code /stackabletrims clear} — removes every stand spawned by the above.
 */
public final class ShowcaseCommand {

    private ShowcaseCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(build());
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal(StackableTrims.MOD_ID)
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("showcase")
                        .executes(ctx -> showcase(ctx, 5.0))
                        .then(Commands.argument("distance", IntegerArgumentType.integer(1, 32))
                                .executes(ctx -> showcase(ctx, IntegerArgumentType.getInteger(ctx, "distance")))))
                .then(Commands.literal("stand")
                        .then(Commands.argument("trims", IntegerArgumentType.integer(0, 100))
                                .executes(ctx -> stand(ctx, IntegerArgumentType.getInteger(ctx, "trims")))))
                .then(Commands.literal("clear")
                        .executes(ShowcaseCommand::clear));
    }

    private static int showcase(CommandContext<CommandSourceStack> ctx, double distance) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        List<TrimShowcase.Entry> entries = TrimShowcase.defaultEntries();
        int spawned = TrimShowcase.spawnRow(level, src.getPosition(), src.getRotation().y, distance, 1.5, entries);

        src.sendSuccess(() -> Component.literal("Spawned " + spawned + " showcase armor stands. Use /"
                + StackableTrims.MOD_ID + " clear to remove them."), false);
        return spawned;
    }

    private static int stand(CommandContext<CommandSourceStack> ctx, int trims) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel level = src.getLevel();
        float yaw = src.getRotation().y;
        double rad = Math.toRadians(yaw);
        Vec3 pos = src.getPosition().add(-Math.sin(rad) * 3.0, 0, Math.cos(rad) * 3.0);

        ArmorStand stand = TrimShowcase.spawnStand(level, pos, yaw + 180.0F, Component.literal(trims + " trims"));
        ItemStack[] set = TrimShowcase.buildSet(level.registryAccess(),
                TrimShowcase.Entry.of(trims + " trims", TrimShowcase.DIAMOND, trims));
        TrimShowcase.equip(stand, set);

        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < trims; i++) {
            if (i > 0) desc.append(", ");
            desc.append(TrimShowcase.describe(TrimShowcase.trimAt(level.registryAccess(), i)));
        }
        src.sendSuccess(() -> Component.literal("Spawned stand with " + trims + " stacked trims: " + desc), false);
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> ctx) {
        int removed = TrimShowcase.clear(ctx.getSource().getLevel());
        ctx.getSource().sendSuccess(() -> Component.literal("Removed " + removed + " showcase armor stands."), false);
        return removed;
    }
}
