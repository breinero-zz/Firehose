package com.bryanreinero.firehose.circuitbreaker;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.bryanreinero.firehose.metrics.SampleSet;

public class BreakerBox {
	
	private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<String, CircuitBreaker>();
	private final SampleSet samples;
	
	public BreakerBox( SampleSet set ) {
		this.samples = set;
	}
	
	public void setBreaker(String key, String type, Double value ) {
		Threshold threshold = null;
		try {
			switch ( BreakerType.getType(type) ) {
			case latency:
				threshold =  new Latency( value );
				break;
			case opsPerSec:
				threshold = new OpsPerSecond( value );
				break;
			case concurrency:
				threshold = new ConcurrentRequests( value );
				break;
			};

			while ( true ) {
				// Copy on write
				CircuitBreaker breaker = breakers.get(key);
				if( breaker == null ) 
					breaker = new CircuitBreaker( key );
				else 
					breaker = breaker.clone();

				breaker.setThreshold( threshold );
				
				// Compare and Swap
				synchronized ( breakers ) {
					if( ! breakers.containsKey(key) ) {
						breakers.put(key, breaker);
						return;
					}
					if( breakers.get(key).getUUID() == breaker.getUUID() ) {
						breakers.put(key, breaker);
						return;
					}
				}
			}

		} catch ( IllegalArgumentException e ) {
			throw new IllegalArgumentException( "Can't set breaker named "+key, e);
		}
	}
	
	/**
	 * Called periodically, checking all breakers
	 * to trip if needed
	 */
	public void check() {
		for ( Entry<String, CircuitBreaker> e : breakers.entrySet() )
			e.getValue().check( samples );
				
	}
	
	/**
	 * Reset the breaker if it exists
	 * @param key the name of the CircuitBreaker
	 */
	public void reset( String key  ) {
		if( ! breakers.containsKey(key) ) 
			return;
		
		while ( true ) {
			// Copy on write
			CircuitBreaker breaker = breakers.get(key).clone();
			breaker.reset();
			
			// Compare and swap
			synchronized ( breakers ) {
				if( ! breakers.containsKey(key) ) 
					return;
				
				if( breakers.get(key).getUUID() == breaker.getUUID() ) {
					breakers.put(key, breaker);
					return;
				}
			}
		}
	}
	
	public Map<String, CircuitBreaker> getState() {
		Map<String, CircuitBreaker> map = new HashMap<String, CircuitBreaker>();
		for ( Entry<String, CircuitBreaker> e : breakers.entrySet() )
			map.put(e.getKey(), e.getValue().clone() );
		
		return map;
	}
}
