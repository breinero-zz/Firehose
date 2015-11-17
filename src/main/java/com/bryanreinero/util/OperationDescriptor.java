package com.bryanreinero.util;

import com.bryanreinero.util.retry.RetryPolicy;

/**
 * Created by bryan on 10/24/15.
 */
public abstract class OperationDescriptor <T extends Operation>{

    private final String name;
    private RetryPolicy policy= null;

    public OperationDescriptor ( String name )  {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RetryPolicy getRetryPolicy() { return policy; }
    public void setRetryPolicy( RetryPolicy p ) { policy =  p; }

    public abstract T getOperation();
}
