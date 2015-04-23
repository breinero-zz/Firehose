package com.bryanreinero.firehose.circuitbreaker;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ConcurrentRequests implements Threshold {

	private final Double max;
	private static final BreakerType type = BreakerType.concurrency;
	
	public ConcurrentRequests( Double max ) {
		this.max = max;
	}
	
	@Override
	public boolean isExceeded(DescriptiveStatistics stats) {
		return ( (stats.getN() * stats.getMean()) >= max );
	}

	@Override
	public BreakerType getType() {
		return type;
	}

	@Override
	public double getValue() {
		return max;
	}

}
