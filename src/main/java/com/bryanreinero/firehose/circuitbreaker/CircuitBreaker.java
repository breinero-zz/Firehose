package com.bryanreinero.firehose.circuitbreaker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bryanreinero.firehose.metrics.SampleSet;

public class CircuitBreaker {
	
	private final String key;
	private final UUID id;
	private final AtomicBoolean isTripped = new AtomicBoolean(false);
	private final Map<BreakerType, Threshold> tresholds = new HashMap<BreakerType, Threshold>();
	
	public void check( SampleSet samples ) {
		for( Threshold t : tresholds.values() ) {
			if( t.isExceeded( samples.report( key ) ) ) {
				isTripped.set(true);
				return;
			}
		}
	}
	
	public Set<Threshold> getThresholds() {
		Set<Threshold> set = new HashSet<Threshold>();
		for( Threshold t : tresholds.values()  ) 
			set.add( t );
		return set;
	}
	
	public void setThreshold ( Threshold t ) {
		tresholds.put( t.getType(), t );
	}
	
	public boolean reset () {
		return isTripped.compareAndSet(false, true);
	}
	
	public boolean isTripped() {
		return isTripped.get();
	}
	
	public UUID getUUID() {
		return id;
	}
	
	public CircuitBreaker( String key ) {
		this.key = key;
		id = UUID.randomUUID();
	}
	
	private CircuitBreaker( UUID id,  String key, Boolean tripped ) {
		this.key = key;
		this.id = id;
		this.isTripped.set( tripped );
	}
	
	@Override 
	public CircuitBreaker clone() {
		CircuitBreaker clone = new CircuitBreaker( this.id, this.key, isTripped.get() );
		for ( Threshold t : tresholds.values() )
			clone.setThreshold( t );
		
		return clone;
	}
}
