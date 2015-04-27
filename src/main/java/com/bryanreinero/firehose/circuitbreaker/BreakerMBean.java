package com.bryanreinero.firehose.circuitbreaker;

import java.util.Map.Entry;

import com.bryanreinero.firehose.metrics.SampleSet;

public class BreakerMBean implements CircuitBreakerMBean {

	private final BreakerBox box;
	
	public BreakerMBean(BreakerBox box ) {
		this.box = box;
	}
	
	@Override
	public String report() {

		StringBuffer buf = new StringBuffer( "{ breakers: [ ");
		boolean firstB = true;
		for( Entry< String, CircuitBreaker> e : box.getState().entrySet() ) {
			
			if ( !firstB ) buf.append(", "); else firstB = false;
			buf.append("{ name: "+e.getKey() );
			buf.append(", tripped: "+e.getValue().isTripped() );
			buf.append(", thresholds: [ ");
			boolean firstT = true;
			for ( Threshold t : e.getValue().getThresholds() ) {
				if ( !firstT ) buf.append(", "); else firstT = false;
				buf.append("{ "+t.getType()+": "+t.getValue()+" }" );
			}
			buf.append(" ] }");
		}
		buf.append(" ] }");
		return buf.toString();
	}

	@Override
	public void setBreaker(String name, String type, Double threshold) {
		box.setBreaker(name, type, threshold);
	}

	@Override
	public void resetBreaker(String name) {
		box.reset(name);
	}
}
