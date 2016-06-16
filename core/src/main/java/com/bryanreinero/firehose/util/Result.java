package com.bryanreinero.firehose.util;

import com.bryanreinero.firehose.util.retry.RetryRequest;

/**
 * Created by breinero on 9/27/15.
 */
public class Result <T> {

    private boolean failed;
    private RetryRequest retry = null;
    private String message = null;
    private Iterable it = null;

    private T result;

    public Result( boolean failed ) {
        this.failed = failed;
    }

    public boolean hasFailed(){ return failed; }

    public void setFailed( String s ) {
        failed = true;
        message = s;
    }

    public void setResults( T result ) { this.result = result; }

    public T getResults(){ return result; }

    public String getMessage() { return message; }

    public void setMessage( String s ){ message = s; }

    public RetryRequest getNextAttempt(){ return retry; }

    public void setNextAttempt( RetryRequest o ) {
        retry = o;
    }

}
