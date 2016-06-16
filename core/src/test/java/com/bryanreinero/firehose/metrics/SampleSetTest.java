package com.bryanreinero.firehose.metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SampleSetTest {
	
	SampleSet set = new SampleSet();
	
	@Test
	public void test() {
		String name = "test";
		
		for ( int i = 1; i <= 100; i++ ) {
			try (Interval interval = set.set(name) ) {}
			DescriptiveStatistics stats = set.report(name);
			assertEquals( i, stats.getN() );
		}
	}
}
