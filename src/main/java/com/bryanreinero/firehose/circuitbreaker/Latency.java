package com.bryanreinero.firehose.circuitbreaker;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Latency implements Threshold {

	private final Double max;
	private final static BreakerType type = BreakerType.latency;
	
	public Latency ( Double max ) {
		this.max = max;
	}
	
	@Override
	public boolean isExceeded(DescriptiveStatistics stats) {
		return ( stats.getMean() >= max );
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
