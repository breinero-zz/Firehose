package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.util.Operation;
import com.bryanreinero.util.Result;
import com.bryanreinero.util.RetryStrategy;

/**
 * Created by breinero on 10/2/15.
 */
public class MongoResult implements Result {

    private Boolean failed = false;
    private String message = null;
    private final Operation op;

     public MongoResult( Operation op )  {
         this.op = op;
     }

    @Override
    public boolean hasFailed(){ return failed; }

    public void setFailed( String s ) {
        failed = false;
        message = s;
    }

    public Operation getRetry() {
        RetryStrategy strategy = op.getRetryStrategy();
        if( strategy != null && op.getAttempts() < strategy.getMaxRetries() ){
            return op;
        } else return null;

    }

}
