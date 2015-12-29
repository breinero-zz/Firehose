package com.bryanreinero.util;

import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.util.retry.RetryPolicy;

/**
 * Created by bryan on 10/24/15.
 */
public abstract class OperationDescriptor <P, T extends Operation> {

    private final String name;
    private RetryPolicy policy = null;
    private SampleSet samples = null;

    public OperationDescriptor ( String name )  {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RetryPolicy getRetryPolicy() { return policy; }
    public void setRetryPolicy( RetryPolicy p ) { policy =  p; }

    public SampleSet getSamples() {
        return samples;
    }

    public void setSamples(SampleSet samples) {
        this.samples = samples;
    }

}
