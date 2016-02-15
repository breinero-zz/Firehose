package com.bryanreinero.loadgen;

import java.util.*;

import com.bryanreinero.firehose.dao.DataAccessHub;
import com.bryanreinero.firehose.dao.DataStore;
import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.dao.mongo.Write;
import com.bryanreinero.firehose.dao.mongo.Read;

import com.bryanreinero.firehose.util.Result;
import com.bryanreinero.firehose.util.ThreadPool;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bryanreinero.firehose.circuitbreaker.BreakerBox;
import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.Statistics;

import com.bryanreinero.firehose.util.markov.Chain;
import com.bryanreinero.firehose.util.markov.Event;

import com.bryanreinero.firehose.util.Application;

public class LoadDemo {

	private static final String appName = "LoadDemo";
	private Application app = null;
    private ThreadPool pool = new ThreadPool( 1 );

    // Control database
    private String controlDBURI = null;
    private Read<DataStore> findDataStores;
    private Read<MongoDAO> findOperations;
    private MongoDAO<DataStore, Read> dataStoreDAO
            = new MongoDAO( "getDataStores", "control", "control.datastores", DataStore.class );

    private MongoDAO<MongoDAO, Read> operationDAO
            = new MongoDAO( "getDescitptors", "control", "control.descriptors", MongoDAO.class );

	private final Statistics stats;
	
	// members for Circuit Breaker
	private BreakerBox breakerBox;
	
	private Random rand = new Random();
	
	private int maxNumberObjects = 0;
	private Vector<ObjectId> ids = null;

	private interface Operation {
		void execute();
	}
	
	private Chain<Operation> operations = new Chain<Operation>();

	public class numThreadsCB implements CallBack {
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
	}

	public LoadDemo( String[] args ) throws Exception {
		// create Markov tree for CRUD operation distribution
		Set<Event<Operation>> events = new HashSet<Event<Operation>>();

        events.add(
                new Event<Operation>( 0.25f,
                        new Operation() {
                            @Override
                            public void execute() {
                                createADocument();
                            }
						}
                )
        );

		events.add( new Event<Operation>( 0.25f, new Operation() {
			@Override
			public void execute() {
				readADocument();
			}

		}));

		events.add( new Event<Operation>( 0.25f, new Operation() {
			@Override
			public void execute() {
				updateADocument();
			}

		}));

		events.add(new Event<Operation>( 0.25f, new Operation() {
			@Override
			public void execute() {
				deleteADocument();
			}
		}));
				
		operations.setProbabilities( events );

		app = new Application( appName );
        // First step, set up the command line interface
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

        app.setCommandLineInterfaceCallback( "db",
                new  CallBack() {
                    @Override
                    public void handle(String[] values) {
                        controlDBURI = values[0];
                    }
                }
        );

		try {
			app.parseCommandLineArgs( args );
		} catch (Exception e) {
            throw new Exception( "Application failed to initialize", e );
		}


        initializeDataStores();

        // prepare the instrumentation
		stats = new Statistics( app.getSampleSet() );
		
		// Next, set up the breaker box
		breakerBox = new BreakerBox( app.getSampleSet() );
		breakerBox.setBreaker("insert", "latency", 200000D );
		breakerBox.start();

        // prepare the output format
		app.addPrinable(this);
	}

	private void initializeDataStores() throws Exception {

		DataAccessHub.INSTANCE.setDataStore(
				new DataStore( "control", appName, controlDBURI, DataStore.Type.mongodb )
		);

        DataAccessHub.INSTANCE.setDao( dataStoreDAO );
        DataAccessHub.INSTANCE.setDao( operationDAO );

		findDataStores = new Read<DataStore>(
				new Document( "application", appName ),
				dataStoreDAO
		);

        // Ask the control database for the datastore objects
        findDataStores.setSamples( app.getSampleSet() );
        Result r = pool.submitTask( findDataStores );

        if ( r.hasFailed() )
            throw new Exception( "Could not initialize datastores "+r.getMessage() );

		for ( Object store : r.getResults() )
			DataAccessHub.INSTANCE.setDataStore( (DataStore) store );

        findOperations = new Read<MongoDAO>(
                new Document( "application", appName ),
                operationDAO
        );

        findOperations.setSamples( app.getSampleSet() );

        // Ask the control database for the operation objects
        r = pool.submitTask( findOperations );

        if ( r.hasFailed() )
            throw new Exception( "Could not initialize operations "+r.getMessage() );

        for ( Object descriptor : r.getResults() ) {
            ((MongoDAO)descriptor).setSamples( app.getSampleSet() );
            DataAccessHub.INSTANCE.setDao((MongoDAO) descriptor);
        }
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

        Write<Document> op =  new Write<Document>(
                newguy,
                (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "insert" ) );
        op.setSamples( app.getSampleSet() );
        pool.submitTask( op );
		
		ids.add( id );
	}
	
	private void readADocument() {
		if( ids.isEmpty() ) return;
		Document query = new Document(
				"_id",
				 ids.get( rand.nextInt( ids.size() ) )
		);

        pool.submitTask(
                new Read<Document>(
                        query,
                        (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "read" ) )
        );
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

        pool.submitTask( new Write<Document>( query, (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "update" ) ) );
	}
	
	private void deleteADocument() {
		if( ids.isEmpty() )
			return;
		
		int index = rand.nextInt( ids.size() ) ;
		ObjectId id = ids.get( index );
		Document query = new Document( "_id", id );

        pool.submitTask( new Write<Document>( query, (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "delete" ) ) );
		
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
