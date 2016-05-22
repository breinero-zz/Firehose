package com.bryanreinero.loadgen;

import com.bryanreinero.firehose.circuitbreaker.BreakerBox;
import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.firehose.util.Application;
import com.bryanreinero.firehose.util.ThreadPool;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Random;
import java.util.Vector;

public class LoadDemo {

	private static final String appName = "LoadDemo";
	private Application app = null;
    private ThreadPool pool = new ThreadPool( 1 );

    // Control database
    private String controlDBURI = null;

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

	public LoadDemo( String[] args ) throws Exception {

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
	
	private void updateADocument() {
		if( ids.isEmpty()  ||
				breakerBox.isTripped("update") )
			return;

		Document query = new Document(
				"_id",
				 ids.get( rand.nextInt( ids.size() ) )
		);
		
		Document set = new Document( "$set", new Document( "a", rand.nextFloat() ) );

		// TODO: replace following line with better implementation
        //pool.submitTask( new Write<Document>( query, (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "update" ) ) );
	}
	
	private void deleteADocument() {
		if( ids.isEmpty() )
			return;
		
		int index = rand.nextInt( ids.size() ) ;
		ObjectId id = ids.get( index );
		Document query = new Document( "_id", id );

		// TODO: replace following line with better implementation
        //pool.submitTask( new Write<Document>( query, (MongoDAO) DataAccessHub.INSTANCE.getDescriptor( "delete" ) ) );
		
		ids.remove( index );
	}

	public void execute() {
       while ( true ) {
            try (Interval t = app.getSampleSet().set("total")) {
                // get a random CRUD operation to execute

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
