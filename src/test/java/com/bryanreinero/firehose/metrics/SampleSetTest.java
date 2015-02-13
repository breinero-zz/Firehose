package com.bryanreinero.firehose.metrics;

import static org.junit.Assert.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

public class SampleSetTest {
	
	SampleSet set = new SampleSet();
	
	@Test
	public void test() {
		String name = "test";
		Interval interval;
		for ( int i = 1; i <= 100; i++ ) {
			set.set(name).mark();
			DescriptiveStatistics stats = set.report(name);
			assertEquals( i, stats.getN() );
		}
	}
}
