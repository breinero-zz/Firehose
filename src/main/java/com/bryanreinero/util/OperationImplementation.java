package com.bryanreinero.util;

import java.util.Map;
import java.util.concurrent.Callable;

import com.bryanreinero.firehose.circuitbreaker.CircuitBreaker;
import com.bryanreinero.firehose.dao.DAOException;
import com.bryanreinero.firehose.dao.DataAccessObject;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;

public abstract class OperationImplementation implements Callable<Result> {

	/*
	* An OperationImplementation Object
	* has a timeout from which it should consider it's request failed
	*/

	private final String name;
	private final SampleSet samples;
	private final DataAccessObject dao;
	private final Map<String, Object>request;
	
	private CircuitBreaker cb = null;
	private RetryStrategy strategy = null;
	private Long timeout = null;
	private Integer resultSetBound = null;
	
	public void setCb(CircuitBreaker cb) {
		this.cb = cb;
	}

	public void setStrategy(RetryStrategy strategy) {
		this.strategy = strategy;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public void setResultSetBound(Integer resultSetBound) {
		this.resultSetBound = resultSetBound;
	}

	// the operation need 
	public OperationImplementation(String name, SampleSet samples, DataAccessObject dao, Map<String, Object> request) {
		this.name = name;
		this.samples = samples;
		this.dao = dao;
		this.request = request;
	}

	@Override
	public Object call() throws Exception {
		Object o;
		try ( Interval i = samples.set(name) ) {
			o = dao.execute(request);
		}
		return o;
	}
	
	public abstract Object execute(Map<String, Object>request) throws DAOException;
	
	public abstract long getDelay();
	
	public abstract boolean needsRetry();
}
