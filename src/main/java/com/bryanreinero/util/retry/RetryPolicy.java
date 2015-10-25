package com.bryanreinero.util.retry;

import com.bryanreinero.util.Operation;

public abstract class RetryPolicy {

	protected final int maxRetries;
	protected final long delay, maxDuration;

	public RetryPolicy( int maxRetries, long delay, long maxDuration ){
		this.maxRetries = maxRetries;
		this.delay = delay;
		this.maxDuration = maxDuration;

	}
	abstract int getMaxRetries();
	abstract RetryRequest getRetry( Operation o );
}
