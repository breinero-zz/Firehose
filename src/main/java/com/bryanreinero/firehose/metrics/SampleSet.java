package com.bryanreinero.firehose.metrics;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
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
			ObjectName name = new ObjectName("com.bryanreiner.firehose.metrics:type=Statistics");
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
	
	public static String formatStat( DescriptiveStatistics stat ) {
		StringBuffer buf = new StringBuffer("{\n");
		buf.append("\tmean: "+stat.getMean()+", \n");
        buf.append("\tmedian: "+stat.getPercentile(50)+", \n");
        buf.append("\tstd: "+stat.getStandardDeviation()+", \n");
        buf.append("\tcount: "+stat.getN()+", \n");
        buf.append("\ttotal: "+stat.getSum());
        buf.append("}");
		return buf.toString();
	}
	
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer("{ units: \"microseconds\"\n");
		buf.append(", \"interval\": "+timeToLive+"000, \n");
		
		boolean first = true;
		for( Entry<String, DescriptiveStatistics> aggregate : report().entrySet() ) {
			if( !first )
				buf.append(", ");
			else
				first = false;
			DescriptiveStatistics stat = aggregate.getValue();
			buf.append("name: \""+ formatStat( stat ) );
		}
		
		buf.append("\n }");
		
		return buf.toString();
	}
}