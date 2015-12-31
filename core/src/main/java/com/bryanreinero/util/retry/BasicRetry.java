package com.bryanreinero.util.retry;

import com.bryanreinero.util.Operation;

/**
 * Created by breinero on 10/12/15.
 */
public class BasicRetry extends RetryPolicy {

    public BasicRetry( int maxRetries, long delay, long maxDuration ) {
        super( maxRetries, delay, maxDuration );
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
