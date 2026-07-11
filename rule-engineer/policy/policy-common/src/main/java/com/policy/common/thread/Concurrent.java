package com.policy.common.thread;

@FunctionalInterface
public interface Concurrent<OUT> {
    OUT async() throws Exception;
}
