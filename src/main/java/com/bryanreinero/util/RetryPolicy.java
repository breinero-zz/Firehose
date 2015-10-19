package com.bryanreinero.util;

public interface RetryPolicy {
	public int getMaxRetries();
	public Long getWait();
}
