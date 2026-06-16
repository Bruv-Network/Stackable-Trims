package com.golem.stackabletrims.core;

import com.golem.stackabletrims.StackableTrims;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;

public final class GameRuleTrimPolicyResolver {

    public static GameRule<Integer> MAX_TRIM_STACK;
    public static GameRule<Boolean> ALLOW_DUPLICATE_TRIMS;

    public static void init() {
        StackableTrims.LOGGER.info("Registering game rules");
    }

    public TrimPolicy forLevel(Level level) {
        if (level == null) {
            return new GameRuleTrimPolicy();
        }
        if (level.getServer() == null) {
            return new GameRuleTrimPolicy();
        }
        GameRules rules = level.getServer().getGameRules();
        int max = MAX_TRIM_STACK != null ? rules.get(MAX_TRIM_STACK) : 32;
        boolean allowDup = ALLOW_DUPLICATE_TRIMS != null ? rules.get(ALLOW_DUPLICATE_TRIMS) : false;
        final int fMax = max;
        final boolean fAllowDup = allowDup;
        return new TrimPolicy() {
            @Override
            public int getMaxStack() {
                return fMax;
            }
            @Override
            public boolean allowDuplicates() {
                return fAllowDup;
            }
        };
    }
}
