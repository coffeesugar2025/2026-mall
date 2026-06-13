package com.policy.common;

import java.util.function.Supplier;


public final class Singleton<R>
        implements Supplier<R> {
    private boolean initialized = false;
    private volatile Supplier<R> instanceSupplier;

    public Singleton(Supplier<R> original) {
        synchronized (original) {
            this.instanceSupplier = (() -> {
                if (!this.initialized) {
                    this.instanceSupplier = original;
                    this.initialized = true;
                }
                return  this.instanceSupplier.get();
            });
        }
    }


    public R get() {
        return this.instanceSupplier.get();
    }
}
