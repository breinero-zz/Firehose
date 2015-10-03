package com.bryanreinero.firehose.dao;

import com.bryanreinero.util.RetryStrategy;

public class FailFast implements RetryStrategy {

	@Override
	public int getMaxRetries() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Long getWait() {
		// TODO Auto-generated method stub
		return null;
	}

}
