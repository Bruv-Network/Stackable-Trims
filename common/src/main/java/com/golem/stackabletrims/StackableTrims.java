package com.golem.stackabletrims;

import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static void setIsBetterTrimTooltipsEnabled(boolean value) {
        isBetterTrimTooltipsEnabled = value;
    }

    protected static boolean isModLoaded(String modId) {
        return false;
    }
}
