package com.bryanreinero.firehose.util.retry;

import com.bryanreinero.firehose.util.ThreadPool;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by breinero on 10/12/15.
 */
public class RetryQueue extends Thread {

    private final DelayQueue queue;
    private final ThreadPool pool;
    private AtomicBoolean running = new AtomicBoolean( false );

    public RetryQueue( ThreadPool p ) {
        queue = new DelayQueue();
        pool = p;
    }

    public void put ( RetryRequest e ) {
        queue.put( e );
    }

    @Override
    public void run() {
        try {
            while (true) {

                if (!running.get())
                    throw new InterruptedException();

                RetryRequest r = (RetryRequest)queue.take();
                pool.submitTask( r.getCallable() );

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
