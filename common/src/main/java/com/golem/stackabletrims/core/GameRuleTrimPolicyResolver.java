package com.golem.stackabletrims.core;

import com.golem.stackabletrims.StackableTrims;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public final class GameRuleTrimPolicyResolver {

    public static GameRules.Key<GameRules.IntegerValue> MAX_TRIM_STACK;
    public static GameRules.Key<GameRules.BooleanValue> ALLOW_DUPLICATE_TRIMS;

    public static void init() {
        StackableTrims.LOGGER.info("Registering game rules");
    }

    public TrimPolicy forLevel(Level level) {
        if (level == null) {
            return new GameRuleTrimPolicy();
        }
        GameRules rules = level.getGameRules();
        int max = MAX_TRIM_STACK != null ? rules.getInt(MAX_TRIM_STACK) : 32;
        boolean allowDup = ALLOW_DUPLICATE_TRIMS != null ? rules.getBoolean(ALLOW_DUPLICATE_TRIMS) : false;
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
