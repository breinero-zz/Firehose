package com.bryanreinero.loadgen;

import com.bryanreinero.firehose.circuitbreaker.BreakerBox;
import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.dao.DataAccessHub;
import com.bryanreinero.firehose.dao.DataStore;
import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.dao.mongo.Read;
import com.bryanreinero.firehose.dao.mongo.Write;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.firehose.util.Application;
import com.bryanreinero.firehose.util.OperationDescriptor;
import com.bryanreinero.firehose.util.Result;
import com.bryanreinero.firehose.util.ThreadPool;
import com.bryanreinero.markov.Chain;
import com.bryanreinero.markov.Event;
import com.bryanreinero.platypus.generator.DocumentGenerator;
import com.bryanreinero.platypus.schema.DocumentDescriptor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

public class LoadDemo {

	private static final String appName = "LoadDemo";
	private Application app = null;
    private ThreadPool pool = new ThreadPool( 1 );

    // Control database
    private String controlDBURI = null;
    private Read<DataStore> findDataStores;
    private Read<MongoDAO> findOperations;

    private MongoDAO<DataStore, Read> dataStoreDAO
            = new MongoDAO( "getDataStores", "control", "Firehose.datastores", DataStore.class );

    private MongoDAO<MongoDAO, Read> operationDAO
            = new MongoDAO( "getDescitptors", "control", "Firehose.operations", MongoDAO.class );

    private MongoDAO<MongoDAO, Read> docGeneratorDAO
            =  new MongoDAO( "getDocDescriptor", "control", "Firehose.documents", DocumentDescriptor.class );


	private final Statistics stats;
	
	// members for Circuit Breaker
	private BreakerBox breakerBox;
	
	private Random rand = new Random();
	
	private int maxNumberObjects = 0;
    private int numThreads = 1;
	private Vector<ObjectId> ids = null;

	private interface Operation {
		void execute();
	}
	
	private Chain<Operation> operations = new Chain<Operation>();

	public LoadDemo( String[] args ) throws Exception {
		// create Markov tree for CRUD operation distribution
		Set<Event<Operation>> events = new HashSet<Event<Operation>>();

        events.add(
                new Event<Operation>( 0.25f,
                        new Operation() {
                            @Override
                            public void execute() {
                                MongoDAO dao = (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "insert" );

                                pool.submitTask(
                                        new Write<Document>(
                                                dao.getGenerator().getDocument(),
                                                (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "insert" )
                                        )
                                );
                            }
						}
                )
        );

		events.add(
                new Event<Operation>( 0.25f, new Operation() {
			@Override
			public void execute() {
                MongoDAO dao = (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "read" );

                pool.submitTask(
                        new Read<Document>(
                                dao.getGenerator().getDocument(),
                                (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "read" )
                        )
                );
			}

		}));

		events.add( new Event<Operation>( 0.25f, new Operation() {
			@Override
			public void execute() {
                MongoDAO dao = (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "update" );

                pool.submitTask(
                        new Write<Document>(
                                dao.getGenerator().getDocument(),
                                (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "update" )
                        )
                );
			}

		}));

		events.add( new Event<Operation>( 0.25f, new Operation() {
			@Override
			public void execute() {
                MongoDAO dao = (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "delete" );

                pool.submitTask(
                        new Write<Document>(
                                dao.getGenerator().getDocument(),
                                (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "delete" )
                        )
                );
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

        // Tell the DataAccesHub where to find the Control DB
		DataAccessHub.INSTANCE.setDataStore(
				new DataStore( "control", appName, controlDBURI, DataStore.Type.mongodb )
		);

        // Initialize the Data HUB with the Data Access Objects
        DataAccessHub.INSTANCE.setDao( dataStoreDAO );
        DataAccessHub.INSTANCE.setDao( operationDAO );
        DataAccessHub.INSTANCE.setDao( docGeneratorDAO );

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

			// Get the DocumentDescriptors so we can create DocumentGenerators
            Read<DocumentDescriptor> read  = new Read<DocumentDescriptor>(
					new Document( "_id", ((OperationDescriptor)descriptor).getName()  ),
					docGeneratorDAO
			);


            r = pool.submitTask( read );
            if ( r.hasFailed() )
                throw new Exception( "Could not retrieve document descriptor. "+r.getMessage() );

            Object docDesc = r.getResults();
            DocumentGenerator generator = new DocumentGenerator(  (DocumentDescriptor)docDesc );
            ((MongoDAO)descriptor).setGenerator( generator );

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
                operations.run();
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
