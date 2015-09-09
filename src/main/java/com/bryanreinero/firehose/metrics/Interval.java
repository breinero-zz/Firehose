package com.bryanreinero.firehose.metrics;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Interval implements Delayed, AutoCloseable {
	
	private final static Long microSecsPerMil = new Long(1000);
	private final Long start = System.nanoTime();
	private Long end;
	private final String name;
	private Long inception = System.currentTimeMillis();
	
	private SampleSet set = null ; 
	
	Interval ( String name, SampleSet set ) {
		this.name = name;
		this.set = set;
	}
	
	public String getName() {
		return name;
	}
	
	public Long getStart() {
		return start;
	}
	
	public Long duration(){ 
		return (end - start)/microSecsPerMil;
	}

	@Override
	public int compareTo(Delayed o) {
		if( o.getDelay(TimeUnit.MILLISECONDS) > this.getDelay(TimeUnit.MILLISECONDS) )
			return -1;
		if( o.getDelay(TimeUnit.MILLISECONDS) < this.getDelay(TimeUnit.MILLISECONDS) )
			return 1;
		return 0;
	}

	
	@Override
	public long getDelay( TimeUnit unit ) {
		return unit.convert( set.getTimeToLive() - ( System.currentTimeMillis() - inception ), TimeUnit.MILLISECONDS );
	}

	@Override
	public void close() {
		end = System.nanoTime();
		inception = System.currentTimeMillis();
		set.add(this);
	}
}