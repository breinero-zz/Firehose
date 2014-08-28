package com.bryanreinero.firehose.metrics;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SampleSet {
	
	private final DelayQueue<Interval> intervals = new DelayQueue<Interval>();
	
	private final Cleanser cleanser = new Cleanser();
	private AtomicBoolean running = new AtomicBoolean(true);
	private Long timeToLive = 1000L;
	
	private class Cleanser extends Thread {

		@Override
		public void run() {
			try {
				while (true) {
					if (!running.get())
						throw new InterruptedException();
					intervals.take();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public void setTimeToLive( Long ttl ) {
		timeToLive = ttl;
	}
	
	public Long getTimeToLive() {
		return timeToLive;
	}
	
	public SampleSet(){
		cleanser.start();
	}
	
	public void stop(){
		running.set(false);
	}
	
	public Map<String, Aggregate> report() {
		
		Map<String, Aggregate> aggregates = new HashMap<String, Aggregate>();
		
		Iterator<Interval> iter = intervals.iterator();
	    while ( iter.hasNext() ) {
	    	
	    	Interval interval = iter.next();
	    	Aggregate aggregate;
	    	
	    	if( ( aggregate = aggregates.get( interval.getName() ) ) == null ) {
	    		aggregate = new Aggregate();
	    		aggregates.put( interval.getName(), aggregate );
			}
	    	aggregate.increment( interval.duration() );
	    }
		return aggregates;
	}
	
	public Interval set( String name ) {
		return new Interval( name, this );
	}

	void add(Interval interval) {
		intervals.add(interval);
	}
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer("{ units: \"microseconds\"");
		buf.append(", \"interval\": "+timeToLive+"000, ops: [ ");
		
		boolean first = true;
		for( Entry<String, Aggregate> aggregate : report().entrySet() ) {
			if( !first )
				buf.append(", ");
			else
				first = false;
			Aggregate agg = aggregate.getValue();
			buf.append("{ name: \""+aggregate.getKey()+"\", "+
				"count: "+agg.getCount()+", "
				+"average: "+agg.average()+" }"
			);
		}
		
		buf.append("] }");
		
		return buf.toString();
	}
}