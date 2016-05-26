package com.bryanreinero.firehose.metrics;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by brein on 5/21/2016.
 */
public class Format {

    public static String report( SampleSet set ) {
        StringBuffer buf = new StringBuffer("{\n ");
        buf.append("\"ops\": [\n");
        Iterator<Map.Entry<String, DescriptiveStatistics>> it = set.report().entrySet().iterator();
        while( it.hasNext() ) {
            buf.append( "{" );
            Map.Entry<String, DescriptiveStatistics> entry = it.next();
            DescriptiveStatistics stat = entry.getValue();
            buf.append( "\"op\": \""+entry.getKey()+"\",\n");
            buf.append( formatStat( stat ) );
            buf.append( "}" );

            if( it.hasNext() )
                buf.append(",");

        }
        buf.append( "\n\t]\n");
        buf.append("\n}");

        return buf.toString();
    }

    public static String formatStat( DescriptiveStatistics stat ) {

        StringBuffer buf = new StringBuffer();
        buf.append( "\n\t\"stats\": {");
        buf.append("\n\t\t\"mean\": "+stat.getMean()+", \n");
        buf.append("\t\t\"median\": "+stat.getPercentile(50)+", \n");
        buf.append("\t\t\"std\": "+stat.getStandardDeviation()+", \n");
        buf.append("\t\t\"count\": "+stat.getN()+", \n");
        buf.append("\t\t\"total\": "+stat.getSum());
        buf.append("\n\t\t}");
        return buf.toString();
    }

}
