package com.bryanreinero.firehose.dao;

public interface RetryStrategy {
	public int getMaxRetries();
	public Long getWait();
}
