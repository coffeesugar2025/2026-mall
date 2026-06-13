package com.policy.common.thread;

public interface BatchExecutor<IN, OUT> {
    OUT async(IN paramIN) throws Exception;

    void onError(IN paramIN, Exception paramException) throws Exception;
}