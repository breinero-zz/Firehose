package com.bryanreinero.firehose.metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Map;

/**
 * Created by brein on 5/21/2016.
 */
public class Format {

    public static String report( SampleSet set ) {
        StringBuffer buf = new StringBuffer("{\n ");

        for( Map.Entry<String, DescriptiveStatistics> aggregate : set.report().entrySet() ) {
            buf.append(",\n");
            DescriptiveStatistics stat = aggregate.getValue();
            buf.append(aggregate.getKey());
            buf.append(": "+ formatStat( stat ) );

        }
        buf.append("\n}");

        return buf.toString();
    }

    public static String formatStat( DescriptiveStatistics stat ) {

        StringBuffer buf = new StringBuffer();
        buf.append("{\n\tmean: "+stat.getMean()+", \n");
        buf.append("\tmedian: "+stat.getPercentile(50)+", \n");
        buf.append("\tstd: "+stat.getStandardDeviation()+", \n");
        buf.append("\tcount: "+stat.getN()+", \n");
        buf.append("\ttotal: "+stat.getSum());
        buf.append("\n}");
        return buf.toString();
    }

}