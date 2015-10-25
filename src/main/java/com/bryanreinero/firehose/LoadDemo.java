package com.bryanreinero.firehose;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.bryanreinero.firehose.circuitbreaker.BreakerBox;
import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.dao.MongoDAO;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.metrics.Statistics;

import com.bryanreinero.firehose.markov.Chain;
import com.bryanreinero.firehose.markov.Event;
import com.bryanreinero.firehose.markov.Outcome;

import com.bryanreinero.util.Application;
import com.bryanreinero.util.WorkerPool.Executor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class LoadDemo implements Executor {
	
	
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
	
	public LoadDemo( String[] args ) {

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
		
		// First step, set up the command line interface
		Map<String, CallBack> myCallBacks = new HashMap<String, CallBack>();
		// custom command line callback for delimiter
				myCallBacks.put("n", new CallBack() {
					@Override
					public void handle(String[] values) {	
						try { 
							maxNumberObjects  = Integer.parseInt( values[0] );
							ids = new Vector( maxNumberObjects );
						}catch (Exception e) {
							e.printStackTrace();
							System.exit(-1);
						}
					}
				});
				
		// Second step, set up the application logic, including the worker queue
		try {
			worker = Application.ApplicationFactory.getApplication( appName, args, myCallBacks);
			dao = worker.getDAO();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// prepare the instrumentation
		samples = worker.getSampleSet();
		stats = new Statistics(samples);
		
		// Next, set up the breaker box
		breakerBox = new BreakerBox( samples );
		breakerBox.setBreaker("insert", "latency", 200000D );
		breakerBox.start();
		
		// start the work queue
		dao = worker.getDAO();
		worker.addPrinable(this);
	}
	
	public static void main ( String[] args ) {
		LoadDemo demo = new LoadDemo( args );
	}
	
	private void createADocument() {
		
		if ( ids.size() >= maxNumberObjects ||
				breakerBox.isTripped("insert") )
			return;
		
		ObjectId id = new ObjectId();
		Document newguy = new Document("_id", id);
		newguy.put( "a", rand.nextFloat() );
		newguy.put( "b", rand.nextFloat() );
		newguy.put( "c", rand.nextFloat() );
		
		try ( Interval t = samples.set("insert") ) {
		    dao.getNewInsert( newguy );
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
	    	dao.read( query );
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
			dao.update( query, set );
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
			dao.delete(query);
		}
		
		ids.remove( index );
	}

	@Override
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
