package com.bryanreinero.util;

import com.bryanreinero.util.retry.RetryRequest;

/**
 * Created by breinero on 9/27/15.
 */
public class Result {

    private boolean failed;
    private RetryRequest retry = null;
    private String message = null;


    public boolean hasFailed(){ return failed; }

    public void setFailed() { failed = true; }

    public void setFailed( String s ) {
        failed = true;
        message = s;
    }

    public String getMessage() { return message; }

    public RetryRequest getNextAttempt(){ return retry; }

    public void setNextAttempt( RetryRequest o ) {
        retry = o;
    }

}
