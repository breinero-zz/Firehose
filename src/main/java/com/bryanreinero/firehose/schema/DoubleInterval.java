package com.bryanreinero.firehose.schema;


public class DoubleInterval implements Interval<Double> {
	
	private final Double min;
	private final Double max;
	
	public DoubleInterval ( Double min, Double max ) {
		this.min = min;
		this.max = max;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
	
	public Double getMin() { return min; }
	public Double getMax() { return max; }

	@Override
	public boolean inRange(Double t) {
		return ( t >= min && t <= max );
	}
}
