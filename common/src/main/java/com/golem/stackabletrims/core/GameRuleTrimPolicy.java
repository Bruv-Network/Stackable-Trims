package com.golem.stackabletrims.core;

import com.golem.stackabletrims.StackableTrims;
import net.minecraft.world.level.GameRules;

public class GameRuleTrimPolicy implements TrimPolicy {
    @Override
    public int getMaxStack() {
        GameRules rules = StackableTrims.getCurrentGameRules();
        if (rules != null && GameRuleTrimPolicyResolver.MAX_TRIM_STACK != null) {
            return rules.getInt(GameRuleTrimPolicyResolver.MAX_TRIM_STACK);
        }
        return 32;
    }

    @Override
    public boolean allowDuplicates() {
        GameRules rules = StackableTrims.getCurrentGameRules();
        if (rules != null && GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS != null) {
            return rules.getBoolean(GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS);
        }
        return false;
    }
}
