package com.bryanreinero.firehose.metrics;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class Statistics implements StatisticsMBean {

	private SampleSet set;
	
	public Statistics( SampleSet samples ) {
		this.set = samples;
	}
	
	@Override
	public String report() {
		
		StringBuffer buf = new StringBuffer("{\nunits: \"microseconds\",\n");
		buf.append("\"reporting interval ms\": ");
		buf.append( set.getTimeToLive() );
		
		for( Entry<String, DescriptiveStatistics> aggregate : set.report().entrySet() ) {
			buf.append(",\n");
			DescriptiveStatistics stat = aggregate.getValue();
			buf.append(aggregate.getKey());
			buf.append(": "+ formatStat( stat ) );

		}
		
		buf.append("\n}");
		
		return buf.toString();
	}
	
	public String formatStat( DescriptiveStatistics stat ) {
		
		StringBuffer buf = new StringBuffer();
		buf.append("{\n\tmean: "+stat.getMean()+", \n");
        buf.append("\tmedian: "+stat.getPercentile(50)+", \n");
        buf.append("\tstd: "+stat.getStandardDeviation()+", \n");
        buf.append("\tcount: "+stat.getN()+", \n");
        buf.append("\ttotal: "+stat.getSum());
        buf.append("\n}");
		return buf.toString();
	}

	@Override
	public String report(String metric) {
		StringBuffer buf = new StringBuffer("{\nunits: \"microseconds\",\n");
		buf.append("\"reporting interval ms\": "+set.getTimeToLive()+", \n");
		buf.append(metric+": ");
		buf.append( formatStat( set.report(metric) ) );
		buf.append("\n}");
		return buf.toString();
	}

	@Override
	public void setReportingInterval( Long milliseconds) {
		set.setTimeToLive( milliseconds );
	}

	@Override
	public Long getReportingInterval() {
		return set.getTimeToLive();
	}
}
