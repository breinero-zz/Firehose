package com.bryanreinero.util;

import java.util.concurrent.Callable;

/**
 * Created by breinero on 9/26/15.
 */
public abstract class Operation implements Callable<Result> {

    private final String name;
    private final long start = System.nanoTime();

    private int attempts = 0;
    private RetryPolicy policy= null;

    public Operation ( String s )  { name = s; }

    public String getName() {
        return name;
    }

    public RetryPolicy getRetryPolicy() { return policy; }

    public void setRetryPolicy( RetryPolicy p ) { policy =  p; }

    public int getAttempts() {
        return attempts;
    }

    public void incAttempts() {
        attempts++;
    }

    public long getStartTime() { return start; }
}
