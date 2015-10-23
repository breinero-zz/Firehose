package com.bryanreinero.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.MissingOptionException;

import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.cli.CommandLineInterface;
import com.bryanreinero.firehose.dao.MongoDAO;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class Application {
	
	private final static String appName = "ApplicationFramework";
	private final ThreadPool workers;
	private final CommandLineInterface cli;
	private Printer printer = new Printer( DEFAULT_PRINT_INTERVAL );
	private final SampleSet samples;
	private MongoDAO dao = null;
	
	public static final int DEFAULT_PRINT_INTERVAL = 1;
	public static final long DEFAULT_REPORTING_INTERVAL = 5;
	
	private int numThreads = 1; 
	private List<ServerAddress> adresses = null;
	private String collectionName = null;
	private String dbname = null;
	private String writeConcern = null;
	private boolean journal = false;
	private boolean fsync = false;
	
	
	public static class ApplicationFactory {
		public static Application getApplication( String name, String[] args, Map<String, CallBack> cbs ) throws Exception {
			try {
				Application w = new Application();
				
				w.cli.addOptions(name);
				//add custom callbacks
				if( cbs != null && ! cbs.isEmpty() ) 
					for ( Entry<String, CallBack> e : cbs.entrySet() )
						w.cli.addCallBack(e.getKey(), e.getValue());
				
				try { 
					// the CLI is ready to parse the command line
					w.cli.parse(args);
				} catch ( MissingOptionException e) {
					w.cli.printHelp();
					throw new Exception( "bad options", e );
				}
				
				// Sanity checking
				if(  w.collectionName == null )
					throw new IllegalStateException( "Target collection name can't be null");
				if(  w.dbname == null )
					throw new IllegalStateException( "Target database name can't be null");
				
				MongoClient client;
				if( w.adresses == null || w.adresses.isEmpty() ) 
					client = new MongoClient();
				else
					client = new MongoClient(w.adresses);
				
				w.dao = new MongoDAO( client, w.dbname+"."+w.collectionName );

				if(  w.writeConcern != null )
					w.dao.setConcern(w.writeConcern);
				if(w.journal) w.dao.setJournal(w.journal);
				if(w.fsync ) w.dao.setFSync(w.fsync);

				return w;
			} catch ( Exception e )  {
				throw new Exception( "Can't initialize Worker", e );
			}
		}
	}
	
	public SampleSet getSampleSet() {
		return samples;
	}

	public CommandLineInterface getCli() {
		return cli;
	}

	public void addCommandLineCallback(String key, CallBack cb ) {
		cli.addCallBack(key, cb);
	}

	private Application() throws Exception {
		samples = new SampleSet();
		samples.setTimeToLive(DEFAULT_REPORTING_INTERVAL);

		// prep the CLI with a set of 
		// standard CL option handlers
		cli = new CommandLineInterface();
		cli.addOptions(appName);
		cli.addCallBack("t", new CallBack() {

			@Override
			public void handle(String[] values) {
				numThreads = Integer.parseInt(values[0]);
			}
		});

		cli.addCallBack("ri", new CallBack() {

			@Override
			public void handle(String[] values) {
				samples.setTimeToLive(Long.parseLong(values[0]));
			}
		});

		cli.addCallBack("pi", new CallBack() {

			@Override
			public void handle(String[] values) {
				printer = new Printer(Integer.parseInt(values[0]));
			}
		});

		cli.addCallBack("cr", new CallBack() {

			@Override
			public void handle(String[] values) {
				printer.setConsole(false);
			}

		});

		// Mongos'es
		cli.addCallBack("m", new CallBack() {

			@Override
			public void handle(String[] values) {
				adresses = MongoDAO.getServerAddresses(values);

			}

		});

		// target namespace
		cli.addCallBack("ns", new CallBack() {

			@Override
			public void handle(String[] values) {
				collectionName = values[1];
				dbname = values[0];
			}

		});

		cli.addCallBack("wc", new CallBack() {
			@Override
			public void handle(String[] values) {
				writeConcern = values[0];
			}

		});

		cli.addCallBack("j", new CallBack() {

			@Override
			public void handle(String[] values) {
				journal = Boolean.parseBoolean(values[0]);
			}

		});

		cli.addCallBack("fs", new CallBack() {

			@Override
			public void handle(String[] values) {
				fsync = Boolean.parseBoolean(values[0]);
			}

		});

		workers = new ThreadPool( numThreads );
	}

	public int getNumThreads() {
		return this.numThreads;
	}

	public MongoDAO getDAO() {
		return dao;
	}

	public void addPrinable(Object o) {
		printer.addPrintable(o);
	}

	public ThreadPool getThreadPool() { return workers; }

}

