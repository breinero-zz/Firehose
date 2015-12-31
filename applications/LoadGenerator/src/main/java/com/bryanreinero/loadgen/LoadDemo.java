package com.bryanreinero.loadgen;

import java.util.Random;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import com.bryanreinero.firehose.dao.DataAccessHub;
import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.dao.mongo.Write;
import com.bryanreinero.firehose.dao.mongo.Read;

import com.bryanreinero.util.ThreadPool;
import com.mongodb.MongoClient;
import org.bson.Document;
import org.bson.types.ObjectId;

import com.bryanreinero.firehose.circuitbreaker.BreakerBox;
import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.Statistics;

import com.bryanreinero.firehose.markov.Chain;
import com.bryanreinero.firehose.markov.Event;
import com.bryanreinero.firehose.markov.Outcome;

import com.bryanreinero.util.Application;

public class LoadDemo {

	private static final String appName = "LoadDemo";
	private Application app = null;
    private ThreadPool pool = new ThreadPool( 1 );

	private final Statistics stats;
	private DataAccessHub hub = null;
	
	// members for Circuit Breaker
	private BreakerBox breakerBox;
	
	private Random rand = new Random();
	
	private int maxNumberObjects = 0;
	private Vector<ObjectId> ids = null;
	
	private Chain operations = new Chain();
	
	public LoadDemo( String[] args ) throws Exception {

		// create Markov tree for CRUD operation distribution
		List<Event> events = new ArrayList<Event>();

        events.add(
                new Event("create", 0.25f,
                        new Outcome() {
                            @Override
                            public void execute() {
                                createADocument();
                            }
                        }
                )
        );

		events.add(new Event("read", 0.25f, new Outcome() {
			@Override
			public void execute() {
				readADocument();
			}
		}));

		events.add(new Event("update", 0.25f, new Outcome() {
			@Override
			public void execute() {
				updateADocument();
			}
		}));

		events.add(new Event("delete", 0.25f, new Outcome() {
			@Override
			public void execute() {
				deleteADocument();
			}
		}));
				
		operations.setEvent( events );
		
		// First step, set up the command line interface
				

		app = new Application( appName );
		app.setCommandLineInterfaceCallback( "n", new CallBack() {
			@Override
			public void handle(String[] values) throws Exception {
				try {
					maxNumberObjects  = Integer.parseInt( values[0] );
					ids = new Vector( maxNumberObjects );
				}catch (Exception e) {
					throw new Exception(
							"CLI Callback to set max number of objects failed ", e );
				}
			}
		});

		try {
			app.parseCommandLineArgs( args );
		} catch (Exception e) {
            throw new Exception( "Application failed to initialize", e );
		}

		// Second step, set up the application logic, including the app queue

        // operation descriptors
        hub = new DataAccessHub();
        hub.addCluster( "test", new MongoClient() );

        MongoDAO<Document, Write> d = new MongoDAO<Document, Write>( "insert", "test", "firehose.data" );
        d.setSamples( app.getSampleSet() );
        hub.addDAO( d );

        d =  new MongoDAO<Document, Write>( "update", "test", "firehose.data" ) ;
        d.setSamples( app.getSampleSet() );
        hub.addDAO( d );

        d =  new MongoDAO<Document, Write>( "delete", "test", "firehose.data" ) ;
        d.setSamples( app.getSampleSet() );
        hub.addDAO( d );

        MongoDAO<Document, Read> r =  new MongoDAO<Document, Read>( "query", "test", "firehose.data" ) ;
        r.setSamples( app.getSampleSet() );
        hub.addDAO( r );

        // prepare the instrumentation
		stats = new Statistics( app.getSampleSet() );
		
		// Next, set up the breaker box
		breakerBox = new BreakerBox( app.getSampleSet() );
		breakerBox.setBreaker("insert", "latency", 200000D );
		breakerBox.start();

        // prepare the output format
		app.addPrinable(this);
	}
	
	public static void main ( String[] args ) {
        try {
            LoadDemo demo = new LoadDemo(args);
            demo.execute();
        } catch ( Exception e ) {
            System.out.println( "Load Demo Failed \n" );
			e.printStackTrace();
        }
	}

	private void createADocument() {
		
		if ( ids.size() >= maxNumberObjects ) return;
		
		ObjectId id = new ObjectId();
		Document newguy = new Document("_id", id);
		newguy.put( "a", rand.nextFloat() );
		newguy.put( "b", rand.nextFloat() );
		newguy.put( "c", rand.nextFloat() );
        System.out.println( "Submitting document "+newguy );

        pool.submitTask( new Write<Document>( newguy, (MongoDAO) hub.getDescriptor( "insert" ) ) );
		
		ids.add( id );
	}
	
	private void readADocument() {
		if( ids.isEmpty() ) return;
		Document query = new Document(
				"_id",
				 ids.get( rand.nextInt( ids.size() ) )
		);

        pool.submitTask(  new Read<Document>( query, (MongoDAO) hub.getDescriptor( "read" ) ) );
	}
	
	private void updateADocument() {
		if( ids.isEmpty()  ||
				breakerBox.isTripped("update") )
			return;

		Document query = new Document(
				"_id",
				 ids.get( rand.nextInt( ids.size() ) )
		);
		
		Document set = new Document( "$set", new Document( "a", rand.nextFloat() ) );

        pool.submitTask( new Write<Document>( query, (MongoDAO) hub.getDescriptor( "update" ) ) );
	}
	
	private void deleteADocument() {
		if( ids.isEmpty() )
			return;
		
		int index = rand.nextInt( ids.size() ) ;
		ObjectId id = ids.get( index );
		Document query = new Document( "_id", id );

        pool.submitTask( new Write<Document>( query, (MongoDAO) hub.getDescriptor( "delete" ) ) );
		
		ids.remove( index );
	}

	public void execute() {
       while ( true ) {
            try (Interval t = app.getSampleSet().set("total")) {
                // get a random CRUD operation to execute
                //operations.run(rand.nextFloat());

				createADocument();
				System.out.println( this );
            }
       }
	}
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("threads: " + app.getNumThreads());
		buf.append(", \"documents\": "+ ids.size() );
		buf.append(", samples: "+ stats.report() );
		buf.append(" }");
		return buf.toString();
	}

}
