package com.bryanreinero.firehose.metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Map;

import static com.bryanreinero.firehose.metrics.Format.formatStat;

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
		
		for( Map.Entry<String, DescriptiveStatistics> aggregate : set.report().entrySet() ) {
			buf.append(",\n");
			DescriptiveStatistics stat = aggregate.getValue();
			buf.append(aggregate.getKey());
			buf.append(": "+ formatStat( stat ) );

		}
		
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
