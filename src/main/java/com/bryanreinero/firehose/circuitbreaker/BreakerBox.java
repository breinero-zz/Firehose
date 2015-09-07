package com.bryanreinero.firehose.circuitbreaker;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;

import com.bryanreinero.firehose.metrics.SampleSet;

public class BreakerBox implements BreakerBoxMBean {
	
	private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<String, CircuitBreaker>();
	
	public BreakerBox() {
		try {
			ObjectName name = new ObjectName("com.bryanreiner.firehose.circuitbreaker:type=CircuitBreaker");
			ManagementFactory.getPlatformMBeanServer().registerMBean(this, name);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
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
	public void check( SampleSet samples ) {
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
	
	public boolean isTripped( String key ) {
		if( !breakers.containsKey(key ))
			return false;
		return breakers.get(key).isTripped();
	}
	
	public Map<String, CircuitBreaker> getState() {
		Map<String, CircuitBreaker> map = new HashMap<String, CircuitBreaker>();
		for ( Entry<String, CircuitBreaker> e : breakers.entrySet() )
			map.put(e.getKey(), e.getValue().clone() );
		
		return map;
	}
	
	@Override
	public String report() {

		StringBuffer buf = new StringBuffer( "{ breakers: [ ");
		boolean firstB = true;
		for( Entry< String, CircuitBreaker> e : this.getState().entrySet() ) {
			
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
	public void resetBreaker(String name) {
		this.reset(name);
	}
}
