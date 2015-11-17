package com.bryanreinero.firehose.test;

import com.bryanreinero.firehose.circuitbreaker.BreakerBox;
import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.markov.Chain;
import com.bryanreinero.firehose.markov.Event;
import com.bryanreinero.firehose.markov.Outcome;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.util.Application;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by bryan on 10/22/15.
 */
public class RandomMongoOperationFactory {

    private static final String appName = "LoadDemo";
    private Application worker = null;
    private final SampleSet samples;
    private final Statistics stats;
    private MongoDAO dao = null;

    // members for Circuit Breaker
    private BreakerBox breakerBox;

    private Random rand = new Random();

    private int maxNumberObjects = 0;
    private Vector<ObjectId> ids = null;

    private Chain operations = new Chain();

    public RandomMongoOperationFactory () {

        samples = new SampleSet();
        stats = new Statistics( samples );

        // create Markov tree for CRUD operation distribution

        List<Event> events = new ArrayList<Event>();

        events.add( new Event("create", 0.25f,
                        new Outcome() {
                            @Override
                            public void execute() {
                                createADocument();
                            }
                        }
                )
        );

        events.add( new Event("read", 0.25f, new Outcome() {
            @Override
            public void execute() {
                readADocument();
            }
        }));

        events.add( new Event("update", 0.25f, new Outcome() {
            @Override
            public void execute() {
                updateADocument();
            }
        }));

        events.add(  new Event("delete", 0.25f, new Outcome() {
            @Override
            public void execute() {
                deleteADocument();
            }
        }));

        operations.setEvent( events );
    }

    private void createADocument() {

        if ( ids.size() >= maxNumberObjects ||
                breakerBox.isTripped("insert") )
            return;

        ObjectId id = new ObjectId();
        DBObject newguy = new BasicDBObject("_id", id);
        newguy.put( "a", rand.nextFloat() );
        newguy.put( "b", rand.nextFloat() );
        newguy.put( "c", rand.nextFloat() );

        try ( Interval t = samples.set("insert") ) {
            //dao.insert( newguy );
        }

        ids.add( id );
    }

    private void readADocument() {
        if( ids.isEmpty()  ||
                breakerBox.isTripped("read") )
            return;

        DBObject query = new BasicDBObject(
                "_id",
                ids.get( rand.nextInt( ids.size() ) )
        );

        try ( Interval t = samples.set("read") ) {
            //dao.read( query );
        }
    }

    private void updateADocument() {
        if( ids.isEmpty()  ||
                breakerBox.isTripped("update") )
            return;

        DBObject query = new BasicDBObject(
                "_id",
                ids.get( rand.nextInt( ids.size() ) )
        );

        DBObject set = new BasicDBObject( "$set", new BasicDBObject( "a", rand.nextFloat() ) );

        try ( Interval t = samples.set("update") ) {
            //dao.update( query, set );
        }
    }

    private void deleteADocument() {
        if( ids.isEmpty()  ||
                breakerBox.isTripped("delete") )
            return;

        int index = rand.nextInt( ids.size() ) ;
        ObjectId id = ids.get( index );
        DBObject query = new BasicDBObject( "_id", id );

        try ( Interval t = samples.set("delete") ) {
            //dao.delete(query);
        }

        ids.remove( index );
    }

    public void execute() {

        try ( Interval t = samples.set("total") ) {
            // get a random CRUD operation to execute
            operations.run( rand.nextFloat() );
        }

    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("threads: "+worker.getNumThreads() );
        buf.append(", \"documents\": "+ ids.size() );
        buf.append(", samples: "+ stats.report() );
        buf.append(" }");
        return buf.toString();
    }
}
