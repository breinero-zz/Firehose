package com.bryanreinero.firehose.util.retry;

import com.bryanreinero.firehose.util.Result;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by breinero on 10/4/15.
 */
public class RetryRequest implements Delayed {

    private final Long inception;
    private Callable<Result> callable = null;
    private final Long delay;

    public RetryRequest( Long d ) {
        inception = System.currentTimeMillis();
        delay = d;
    }

    @Override
    public int compareTo(Delayed o) {
        if( o.getDelay(TimeUnit.MILLISECONDS) > this.getDelay(TimeUnit.MILLISECONDS) )
            return -1;
        if( o.getDelay(TimeUnit.MILLISECONDS) < this.getDelay(TimeUnit.MILLISECONDS) )
            return 1;
        return 0;
    }


    @Override
    public long getDelay( TimeUnit unit ) {
        return unit.convert( delay - ( System.currentTimeMillis() - inception ), TimeUnit.MILLISECONDS );
    }

    public Callable<Result> getCallable() {
        return callable;
    }

    public void setCallable( Callable c ){ callable = c; }
}
