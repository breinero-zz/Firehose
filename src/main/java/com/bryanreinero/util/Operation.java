package com.bryanreinero.util;

import java.util.concurrent.Callable;

/**
 * Created by breinero on 9/26/15.
 */
public interface Operation extends Callable<Result> {

    public String getName();

    public RetryStrategy getRetryStrategy();

    public void complete();

    public int getAttempts();
}
