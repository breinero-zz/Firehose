package com.bryanreinero.firehose.metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Map;

/**
 * Created by brein on 5/21/2016.
 */
public class Format {

    public static String report( SampleSet set ) {
        StringBuffer buf = new StringBuffer("{\n ");
        buf.append("\"ops\": [\n");
        for( Map.Entry<String, DescriptiveStatistics> aggregate : set.report().entrySet() ) {

            DescriptiveStatistics stat = aggregate.getValue();
            buf.append( "\t{ name: \""+aggregate.getKey()+"\",");
            buf.append( formatStat( stat ) );

        }
        buf.append( "\n\t]\n");
        buf.append("\n}");

        return buf.toString();
    }

    public static String formatStat( DescriptiveStatistics stat ) {

        StringBuffer buf = new StringBuffer();
        buf.append( "\n\t stats: {");
        buf.append("\n\t\tmean: "+stat.getMean()+", \n");
        buf.append("\t\tmedian: "+stat.getPercentile(50)+", \n");
        buf.append("\t\tstd: "+stat.getStandardDeviation()+", \n");
        buf.append("\t\tcount: "+stat.getN()+", \n");
        buf.append("\t\ttotal: "+stat.getSum());
        buf.append("\n\t\t}");
        buf.append("\n\t}");
        return buf.toString();
    }

}
