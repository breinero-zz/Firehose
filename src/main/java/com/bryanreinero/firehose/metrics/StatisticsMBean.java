package com.bryanreinero.firehose.metrics;

public interface StatisticsMBean {
	public String report();
	public String report( String metric );
	public void setReportingInterval( Long milliseconds );
	public Long getReportingInterval();
}
