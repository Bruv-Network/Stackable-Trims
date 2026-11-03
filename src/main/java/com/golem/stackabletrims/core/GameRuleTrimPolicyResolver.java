package com.golem.stackabletrims.core;

import com.golem.stackabletrims.StackableTrims;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
//? if >=1.21.11
import net.minecraft.world.level.gamerules.GameRule;

public final class GameRuleTrimPolicyResolver {

    public static final int DEFAULT_MAX_TRIM_STACK = 32;

    // Registered by each loader's entrypoint. 1.21.11 replaced GameRules.Key with GameRule and renamed the rules to
    // snake_case ids (max_trim_stack / allow_duplicate_trims, formerly maxTrimStack / allowDuplicateTrims).
    //? if >=1.21.11 {
    public static GameRule<Integer> MAX_TRIM_STACK;
    public static GameRule<Boolean> ALLOW_DUPLICATE_TRIMS;
    //?} else {
    /*public static GameRules.Key<GameRules.IntegerValue> MAX_TRIM_STACK;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_DUPLICATE_TRIMS;
    *///?}

    public static void init() {
        StackableTrims.LOGGER.info("Registering game rules");
    }

    public static GameRules rulesOf(MinecraftServer server) {
        //? if >=26.1 || <1.21.11 {
        return server.getGameRules();
        //?} else {
        /*return server.getWorldData().getGameRules();
        *///?}
    }

    public static int maxStack(GameRules rules) {
        if (rules == null || MAX_TRIM_STACK == null) {
            return DEFAULT_MAX_TRIM_STACK;
        }
        //? if >=1.21.11 {
        return rules.get(MAX_TRIM_STACK);
        //?} else {
        /*return rules.getInt(MAX_TRIM_STACK);
        *///?}
    }

    public static boolean allowDuplicates(GameRules rules) {
        if (rules == null || ALLOW_DUPLICATE_TRIMS == null) {
            return false;
        }
        //? if >=1.21.11 {
        return rules.get(ALLOW_DUPLICATE_TRIMS);
        //?} else {
        /*return rules.getBoolean(ALLOW_DUPLICATE_TRIMS);
        *///?}
    }

    public TrimPolicy forLevel(Level level) {
        if (level == null || level.getServer() == null) {
            return new GameRuleTrimPolicy();
        }
        GameRules rules = rulesOf(level.getServer());
        final int max = maxStack(rules);
        final boolean allowDup = allowDuplicates(rules);
        return new TrimPolicy() {
            @Override
            public int getMaxStack() {
                return max;
            }
            @Override
            public boolean allowDuplicates() {
                return allowDup;
            }
        };
    }
}
