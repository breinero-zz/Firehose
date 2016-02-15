package com.bryanreinero.firehose.util;

import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.util.retry.RetryPolicy;
import com.bryanreinero.firehose.util.retry.RetryQueue;
import com.bryanreinero.firehose.util.retry.RetryRequest;

import java.util.concurrent.Callable;

/**
 * Created by breinero on 9/26/15.
 */
public abstract class Operation implements Callable {

    private final OperationDescriptor descriptor;
    private final long start = System.nanoTime();

    private int attempts = 0;
    private RetryPolicy policy = null;
    private RetryQueue queue = null;
    protected SampleSet samples = null;

    public Operation ( OperationDescriptor d )  { descriptor = d; }

    public String getName() { return descriptor.getName(); }

    public RetryPolicy getRetryPolicy() { return policy; }

    public void setRetryPolicy( RetryPolicy p ) { policy =  p; }

    public void setRetryQueue( RetryQueue q ) { this.queue = q; }

    public int getAttempts() {
        return attempts;
    }

    public void incAttempts() {
        attempts++;
    }

    public long getStartTime() { return start; }

    public void setSamples( SampleSet s ) {
        this.samples = s;
    }

    public void AttemptRetry() {
        if (policy != null &&  queue != null ) {
            RetryRequest retry = policy.getRetry(this);
            if (retry != null) {
                retry.setCallable(this);
                queue.put(retry);
            }
        }
    }
}
