package com.golem.stackabletrims;

import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//? if <1.20.5 {
/*import net.minecraft.core.RegistryAccess;
import java.util.function.Supplier;
*///?}

public class StackableTrims {
    public static final String MOD_ID = "stackabletrims";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static GameRules currentGameRules;
    public static boolean isBetterTrimTooltipsEnabled;

    public static void init() {
        LOGGER.info("Initializing Stackable Trims");
        GameRuleTrimPolicyResolver.init();
    }

    public static GameRules getCurrentGameRules() {
        return currentGameRules;
    }

    public static void setGameRules(GameRules gameRules) {
        currentGameRules = gameRules;
    }

    /** Called by every loader at the end of each server tick. */
    public static void onServerTick(MinecraftServer server) {
        setGameRules(GameRuleTrimPolicyResolver.rulesOf(server));
        //? if <1.20.5
        /*currentServer = server;*/
    }

    public static void setIsBetterTrimTooltipsEnabled(boolean value) {
        isBetterTrimTooltipsEnabled = value;
    }

    //? if <1.20.5 {
    /*// Before data components (1.20.5) a trim is NBT that only decodes against a registry. The renderer, tooltips and
    // the anvil have no registry at hand, so they read the one of the side they run on: the server's on its thread,
    // the client level's everywhere else. Keeping the sides apart matters because ArmorTrim compares its holders by
    // identity.
    private static MinecraftServer currentServer;
    private static Supplier<RegistryAccess> clientRegistries = () -> null;

    public static void setClientRegistries(Supplier<RegistryAccess> registries) {
        clientRegistries = registries;
    }

    public static RegistryAccess registries() {
        MinecraftServer server = currentServer;
        if (server != null && server.isSameThread()) {
            return server.registryAccess();
        }
        RegistryAccess client = clientRegistries.get();
        if (client != null) {
            return client;
        }
        return server != null ? server.registryAccess() : null;
    }
    *///?}
}
