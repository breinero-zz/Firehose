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
	
	/**
	 * Check the sample set. Trip the breaker if the threshold is exceeded
	 * @param samples The sample set of operation metrics
	 */
	public void check( SampleSet samples ) {
		for( Threshold t : tresholds.values() ) {
			if( t.isExceeded( samples.report( key ) ) ) {
				isTripped.set(true);
				return;
			}
		}
	}
	
	/**
	 * 
	 * @return The set of thresholds this breaker is configured with
	 */
	public Set<Threshold> getThresholds() {
		Set<Threshold> set = new HashSet<Threshold>();
		for( Threshold t : tresholds.values()  ) 
			set.add( t );
		return set;
	}
	
	/**
	 * Set a threshold which can trip the breaker
	 * @param t
	 */
	public void setThreshold ( Threshold t ) {
		tresholds.put( t.getType(), t );
	}
	
	/**
	 * Reset the breaker if it is tripped
	 * @return
	 */
	public boolean reset () {
		return isTripped.compareAndSet(false, true);
	}
	
	/**
	 * Check if the breaker is in a tripped state
	 * @return
	 */
	public boolean isTripped() {
		return isTripped.get();
	}
	
	/**
	 *  get the unique id for this individual breaker
	 *  used for compare and swap modifications of the breaker
	 * @return
	 */
	public UUID getUUID() {
		return id;
	}
	
	/**
	 * 
	 * @param key the name of the operation this breaker is guarding
	 */
	public CircuitBreaker( String key ) {
		this.key = key;
		id = UUID.randomUUID();
	}
	
	/**
	 * Used for copy on write modifications of the circuit breaker
	 * @param id
	 * @param key
	 * @param tripped
	 */
	private CircuitBreaker( UUID id,  String key, Boolean tripped ) {
		this.key = key;
		this.id = id;
		this.isTripped.set( tripped );
	}
	
	/**
	 * Deep copy of this circuit breaker for copy-on-write modification 
	 */
	@Override 
	public CircuitBreaker clone() {
		CircuitBreaker clone = new CircuitBreaker( this.id, this.key, isTripped.get() );
		for ( Threshold t : tresholds.values() )
			clone.setThreshold( t );
		
		return clone;
	}
}
