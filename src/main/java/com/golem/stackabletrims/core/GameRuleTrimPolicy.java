package com.golem.stackabletrims.core;

import com.golem.stackabletrims.StackableTrims;

/** The policy of the game rules last seen on a server tick, for code that has no level at hand. */
public class GameRuleTrimPolicy implements TrimPolicy {
    @Override
    public int getMaxStack() {
        return GameRuleTrimPolicyResolver.maxStack(StackableTrims.getCurrentGameRules());
    }

    @Override
    public boolean allowDuplicates() {
        return GameRuleTrimPolicyResolver.allowDuplicates(StackableTrims.getCurrentGameRules());
    }
}
