package com.bryanreinero.firehose.dao;

import com.bryanreinero.util.Operation;
import com.bryanreinero.util.RetryPolicy;
import com.bryanreinero.util.RetryRequest;

import java.util.concurrent.Callable;

/**
 * Created by breinero on 10/12/15.
 */
public class BasicRetry implements RetryPolicy {

    private final int maxRetries;
    private final long delay;
    private final long maxDuration;
    private final Callable callable;

    public BasicRetry(int maxRetries, long delay, long maxDuration, Callable callable) {
        this.maxRetries = maxRetries;
        this.delay = delay;
        this.maxDuration = maxDuration;
        this.callable = callable;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public RetryRequest getRetry(Operation o) {

        long duration = System.nanoTime() - o.getStartTime();
        if( duration >= maxDuration ||
                o.getAttempts() >= maxRetries   )
            return null;

        return new RetryRequest( delay );
    }
}
