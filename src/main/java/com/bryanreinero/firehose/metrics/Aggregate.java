package com.bryanreinero.firehose.metrics;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

public class Aggregate {

	private AtomicLong sum = new AtomicLong(0);
	private AtomicLong count = new AtomicLong(0);
	
	public long increment( Long delta ) {
		count.incrementAndGet();
		return sum.addAndGet(delta);
	}
	
	public long getCount () {
		return count.longValue();
	}
	
	public long average() {
		if( count.intValue() <= 0 )
			return 0;
		
		return sum.get() / count.get();
	}
}
