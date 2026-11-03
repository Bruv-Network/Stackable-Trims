package com.golem.stackabletrims.core;

public final class CoreServices {
    private static final TrimStackService TRIM_SERVICE = new DefaultTrimStackService();
    private static final GameRuleTrimPolicyResolver POLICY_RESOLVER = new GameRuleTrimPolicyResolver();

    private CoreServices() {}

    public static TrimStackService trims() {
        return TRIM_SERVICE;
    }

    public static GameRuleTrimPolicyResolver policyResolver() {
        return POLICY_RESOLVER;
    }
}
