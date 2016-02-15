package com.bryanreinero.firehose.util;

import com.bryanreinero.firehose.util.retry.RetryRequest;

/**
 * Created by breinero on 9/27/15.
 */
public class Result {

    private boolean failed;
    private RetryRequest retry = null;
    private String message = null;
    private Iterable it = null;

    public Result( boolean failed ) {
        this.failed = failed;
    }

    public boolean hasFailed(){ return failed; }

    public void setFailed() { failed = true; }

    public void setFailed( String s ) {
        failed = true;
        message = s;
    }

    public void setResults( Iterable it ) { this.it = it; }

    public Iterable getResults(){ return it; }

    public String getMessage() { return message; }

    public RetryRequest getNextAttempt(){ return retry; }

    public void setNextAttempt( RetryRequest o ) {
        retry = o;
    }

}
