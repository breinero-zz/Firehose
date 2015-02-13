package com.bryanreinero.firehose.metrics;

public class Statistics implements StatisticsMBean {

	private SampleSet set;
	
	public Statistics( SampleSet samples ) {
		this.set = samples;
	}
	
	@Override
	public String report() {
		return set.toString();
	}

	@Override
	public String report(String metric) {
		StringBuffer buf = new StringBuffer("{ \""+metric+"\": ");
		buf.append(set.formatStat( set.report(metric) )+"\n}" );
		return buf.toString();
	}
}
