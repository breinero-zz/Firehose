package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.dao.BasicRetry;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.util.BetterWorkPool;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


public class LoadGenerator  {

    private static final String appName = "LoadGenerator";

    private final SampleSet samples;
    private final Statistics stats;

    private final BetterWorkPool pool = new BetterWorkPool();

    private final MongoClient mongoClient = new MongoClient();
    private final MongoCollection<Document> collection;

    public LoadGenerator() throws Exception {

        samples = new SampleSet();
        stats = new Statistics( samples );

        MongoDatabase database = mongoClient.getDatabase("test");
        collection =  database.getCollection("test");
    }

    public void runOne ( )  {

        Document query = new Document();
        Read read = new Read( "ReadLink", query, collection, samples );
        read.setRetryPolicy( new BasicRetry( 3, 5000, 50000000, read ) );
        pool.submitTask( read );

        Document sample = new Document();

        Write write = new Write( "WriteSample", sample, collection, samples );
        write.setRetryPolicy( new BasicRetry( 3, 5000, 50000000, write ) );
        pool.submitTask( write );
    }

    public static void main( String[] args )  {
        LoadGenerator lg = null;
        try {
            lg = new LoadGenerator();
            lg.runOne();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
