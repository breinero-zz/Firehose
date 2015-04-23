package com.bryanreinero.firehose.circuitbreaker;
public interface CircuitBreakerMBean {
	public String report();
	public void setBreaker( String name, String type, Double threshold );
	public void resetBreaker( String name );
}
