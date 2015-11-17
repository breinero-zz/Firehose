package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.dao.DataAccessHub;
import com.bryanreinero.util.Operation;
import com.bryanreinero.util.OperationDescriptor;
import com.bryanreinero.util.retry.BasicRetry;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.util.ThreadPool;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;


public class LoadGenerator  {

    private static final String appName = "LoadGenerator";

    private final SampleSet samples;
    private final Statistics stats;

    private final ThreadPool pool = new ThreadPool(1);

    private final MongoClient mongoClient = new MongoClient();
    private final MongoCollection<Document> collection;

    private final DataAccessHub hub  = new DataAccessHub();

    public LoadGenerator() throws Exception {

        samples = new SampleSet();
        stats = new Statistics( samples );

        MongoDatabase database = mongoClient.getDatabase("test");
        collection =  database.getCollection("test");

        MongoDAO dao = new MongoDAO<Document, Write>("ExampleWrite", "mongodb://localhost:27017/", "firehose.test");
        dao.setOperationCtor( Write.class.getConstructor() );
        hub.addDAO( dao );

        dao = new MongoDAO<Document, Read>("ExampleRead", "mongodb://localhost:27017/", "firehose.test");
        dao.setOperationCtor( Read.class.getConstructor() );
        hub.addDAO( dao );

    }

    public void runOne ( )  {
        Operation op = hub.submit("ExampleWrite", new Document());
        pool.submitTask( op );

        op = hub.submit("ExampleRead", new Document() );
        pool.submitTask( op );
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
