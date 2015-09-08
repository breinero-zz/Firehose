package com.bryanreinero.firehose.circuitbreaker;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.ObjectName;

import com.bryanreinero.firehose.metrics.SampleSet;

public class BreakerBox implements BreakerBoxMBean {
	
	private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<String, CircuitBreaker>();
	private final SampleSet samples;
	private final Monitor monitor = new Monitor();
	private AtomicBoolean running = new AtomicBoolean(true);
	
	private class Monitor extends Thread {

		@Override
		public void run() {
			try {
				while (true) {
					if (!running.get())
						throw new InterruptedException();
					for( Entry<String, CircuitBreaker> entry : breakers.entrySet() ) {
						entry.getValue().check(samples);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public BreakerBox( SampleSet samples ) {
		
		this.samples = samples;
		
		try {
			ObjectName name = new ObjectName("com.bryanreiner.firehose.circuitbreaker:type=CircuitBreaker");
			ManagementFactory.getPlatformMBeanServer().registerMBean(this, name);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void start() {
		running.set(true);
		monitor.start();
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

			CircuitBreaker breaker = new CircuitBreaker( key );
			breaker.setThreshold( threshold );
			breakers.put(key, breaker);		
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
		if( breakers.containsKey(key) ) 
			breakers.get(key).reset();
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
