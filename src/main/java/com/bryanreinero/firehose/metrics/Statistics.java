package com.bryanreinero.firehose.metrics;

public class Statistics implements StatisticsMBean {

	private SampleSet samples;
	
	public Statistics( SampleSet samples ) {
		this.samples = samples;
	}
	
	@Override
	public String report() {
		return samples.toString();
	}

	@Override
	public String report(String metric) {
		StringBuffer buf = new StringBuffer("{ name: \""+metric+"\", ");
		Aggregate agg = samples.report(metric);
		
		if( agg == null )
			buf.append("count: \"N/A\", average: \"N/A\" }");
					
		
		else
			buf.append("count: "+agg.getCount()+", "
				+"average: "+agg.average()+" }"
			);
		return buf.toString();
	}
}
