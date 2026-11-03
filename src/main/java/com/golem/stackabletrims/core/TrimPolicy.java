package com.golem.stackabletrims.core;

public interface TrimPolicy {
    int getMaxStack();
    boolean allowDuplicates();
}
