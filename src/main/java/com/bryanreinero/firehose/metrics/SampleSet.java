package com.bryanreinero.firehose.metrics;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.ObjectName;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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

		try {
			ObjectName name = new ObjectName("com.bryanreinero.firehose:type=Statistics");
			Statistics mbean = new Statistics( this ); 
			ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, name); 
		} catch (Exception e) {
			e.printStackTrace();
		} 
        
		cleanser.start();
	}
	
	public void stop(){
		running.set(false);
	}
	
	public Map<String, DescriptiveStatistics> report() {
		
		Map<String, DescriptiveStatistics> stats = new HashMap<String, DescriptiveStatistics>();
		
		Iterator<Interval> iter = intervals.iterator();
	    while ( iter.hasNext() ) {
	    	
	    	Interval interval = iter.next();
	    	DescriptiveStatistics stat;
	    	
	    	if( ( stat = stats.get( interval.getName() ) ) == null ) 
	    		stats.put( interval.getName(), (stat = new DescriptiveStatistics() ) );
	
	    	stat.addValue( interval.duration() );
	    }
		return stats;
	}
	
	public DescriptiveStatistics report( String metric ) {
		
		DescriptiveStatistics stat = new DescriptiveStatistics();
		
		Iterator<Interval> iter = intervals.iterator();
		
	    while ( iter.hasNext() ) {
	    	Interval interval = iter.next();
	    	if( ! interval.getName().equals( metric ) ) continue;
	    	
	    	stat.addValue( interval.duration() );
	    }
		return stat;
	}
	
	public Interval set( String name ) {
		return new Interval( name, this );
	}

	void add(Interval interval) {
		intervals.add(interval);
	}
}