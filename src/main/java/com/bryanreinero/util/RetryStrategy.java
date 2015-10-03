package com.bryanreinero.util;

public interface RetryStrategy {
	public int getMaxRetries();
	public Long getWait();
}
