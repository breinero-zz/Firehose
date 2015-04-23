package com.bryanreinero.firehose.circuitbreaker;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class OpsPerSecond implements Threshold {

	private final Double max;
	private final BreakerType type = BreakerType.opsPerSec;
	
	public OpsPerSecond(Double max ) {
		this.max = max;
	}
	
	@Override
	public boolean isExceeded(DescriptiveStatistics stats) {
		return (stats.getN() >= max );
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
