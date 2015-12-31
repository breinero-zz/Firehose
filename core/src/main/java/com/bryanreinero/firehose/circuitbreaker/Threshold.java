package com.bryanreinero.firehose.circuitbreaker;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public interface Threshold {
	public boolean isExceeded(DescriptiveStatistics stats);
	public BreakerType getType();
	public double getValue();
}
