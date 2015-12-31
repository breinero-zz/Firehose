package com.bryanreinero.firehose.circuitbreaker;

import com.bryanreinero.firehose.circuitbreaker.BreakerType;

enum BreakerType {
	latency("latency"), opsPerSec("opsPerSec"), concurrency("concurrency");
	
	private final String name;
	
	private BreakerType( String name ) {
		this.name = name;
	}
	
	public static BreakerType getType( String s ) {
		for(BreakerType t : BreakerType.values() )
			if( t.name.compareTo( s ) == 0 )
				return t;
		throw new IllegalArgumentException("Unsupported Threshold type: "+s );
	}
	
	@Override
	public String toString() {
		return name;
	}
}