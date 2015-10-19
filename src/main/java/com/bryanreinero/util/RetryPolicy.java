package com.bryanreinero.util;

public interface RetryPolicy {
	int getMaxRetries();
	RetryRequest getRetry( Operation o );
}
