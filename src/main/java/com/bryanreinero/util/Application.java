package com.bryanreinero.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.cli.MissingOptionException;

import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.cli.CommandLineInterface;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.util.WorkerPool.Executor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.BasicDBObject;
import java.net.UnknownHostException;

public class Application {
	
	private final static String appName = "ApplicationFramework";
	private final WorkerPool workers;
	private final CommandLineInterface cli;
	private Printer printer = new Printer( DEFAULT_PRINT_INTERVAL );
	private final SampleSet samples;
	
	public static final int DEFAULT_PRINT_INTERVAL = 1;
	public static final long DEFAULT_REPORTING_INTERVAL = 5;
	
	private int numThreads = 1; 
	private List<ServerAddress> addresses = null;
	private String writeConcern = null;
	private boolean journal = false;
	private boolean fsync = false;
	
	
	public static class ApplicationFactory {
		public static Application getApplication( String name, Executor executor, String[] args, Map<String, CallBack> cbs ) throws Exception {
			try {
				Application w = new Application(executor);
				
				w.cli.addOptions(name);

				//add custom callbacks
				for ( Entry<String, CallBack> e : cbs.entrySet() )
					w.cli.addCallBack(e.getKey(), e.getValue());
				
				try { 
					// the CLI is ready to parse the command line
					w.cli.parse(args);
				} catch ( MissingOptionException e) {
					w.cli.printHelp();
                    // dmf: this needs work; need to throw more pertinent except
                    //   unrecognized arg
                    //   missing arg
                    //   incorect value
                    //   etc
					throw new Exception( "bad options", e );
				}
				
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

	private Application(Executor executor) throws Exception {
		samples = new SampleSet();
		samples.setTimeToLive(DEFAULT_REPORTING_INTERVAL);

		// prep the CLI with a set of 
		// standard CL option handlers
		cli = new CommandLineInterface();

		// Add Application options
		cli.addOptions(appName);

        // Threads
		cli.addCallBack("t", new CallBack() {

			@Override
			public void handle(String[] values) {
				numThreads = Integer.parseInt(values[0]);
			}
		});

        // Report Interval
		cli.addCallBack("ri", new CallBack() {

			@Override
			public void handle(String[] values) {
				samples.setTimeToLive(Long.parseLong(values[0]));
			}
		});

        // Print Interval
		cli.addCallBack("pi", new CallBack() {

			@Override
			public void handle(String[] values) {
				printer = new Printer(Integer.parseInt(values[0]));
			}
		});

        // No pretty print (carriage return lines output)
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
				addresses = DAO.getServerAddresses(values);

			}

		});

        // WriteConcern
		cli.addCallBack("wc", new CallBack() {
			@Override
			public void handle(String[] values) {
				writeConcern = values[0];
			}

		});

        // Write Journal
		cli.addCallBack("wj", new CallBack() {

			@Override
			public void handle(String[] values) {
				journal = Boolean.parseBoolean(values[0]);
			}

		});

        // Write sync
		cli.addCallBack("ws", new CallBack() {

			@Override
			public void handle(String[] values) {
				fsync = Boolean.parseBoolean(values[0]);
			}

		});

		workers = new WorkerPool(executor);
	}

	public void start() {
		workers.start(this.numThreads);
		printer.start();
	}
	
	public void stop() {
		workers.stop();
		printer.stop();
		samples.stop();
	}

	public int getNumThreads() {
		return this.numThreads;
	}

	public DAO getDAO(String dbName, String collectionName) {
        MongoClient client;
        DAO dao = null;

        try {
            if( this.addresses == null || this.addresses.isEmpty() ) 
                client = new MongoClient();
            else
                client = new MongoClient(this.addresses);
        } catch (UnknownHostException e) {
            System.out.println("Application framework caught excpetion: "
                               +e.getMessage());
            e.printStackTrace();
            return null;
        }
				
        dao = new DAO(client.getDB(dbName).getCollection(collectionName));

        if (this.writeConcern != null)
            dao.setConcern(this.writeConcern);
        if (this.journal)
            dao.setJournal(this.journal);
        if (this.fsync)
            dao.setFSync(this.fsync);

		return dao;
	}

	public void addPrintable(Object o) {
		printer.addPrintable(o);
	}

}

