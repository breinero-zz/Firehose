package com.bryanreinero.firehose;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.dao.DataAccessHub;
import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.dao.mongo.Write;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.util.*;
import com.mongodb.MongoClient;
import org.bson.Document;

public class Firehose {
	
	private static final String appName = "Firehose";
	private final Application app;
	private final ThreadPool threadPool;
	private final SampleSet samples;
	private final Statistics stats;
	private AtomicInteger linesRead = new AtomicInteger(0);
	private Converter converter = new Converter();
	private BufferedReader br = null;

	private final DataAccessHub dataHub;
	
	private Boolean verbose = false;
	private String filename = null;

	private AtomicBoolean running = new AtomicBoolean( true );

	private void unitOfWork() {

		String currentLine = null;

		try ( Interval total = samples.set("total") ) {

					// read the next line from source file
					try (Interval readLine = samples.set("readline")) {
						synchronized (br) {
							currentLine = br.readLine();
						}
					} catch (IOException e1) {
                        running.set(false);
                        e1.printStackTrace();
                    }

                    if (currentLine == null)
						running.set(false);

					else {
                        linesRead.incrementAndGet();

                        Document object = null;

                        // Create the DBObject for insertion
                        try (Interval build = samples.set("build")) {
                            object = converter.convert(currentLine);
                        }

                        // Insert the new Document
                        threadPool.submitTask(
                                new Write<Document>(object, (MongoDAO) dataHub.getDescriptor("insert"))
                        );
                    }
        }
	}



	public Firehose ( String[] args ) throws Exception {
		
		// First step, set up the command line interface
		Map<String, CallBack> myCallBacks = new HashMap<String, CallBack>();
		
		// custom command line callback for csv conversion
		myCallBacks.put("h", new CallBack() {
			@Override
			public void handle(String[] values) {
				for (String column : values) {
					String[] s = column.split(":");
					converter.addField( s[0], Transformer.getTransformer( s[1] ) );
				}
			}
		});
		
		// custom command line callback for delimiter
		myCallBacks.put("d", new CallBack() {
			@Override
			public void handle(String[] values) {
				converter.setDelimiter( values[0] );
			}
		});

		// custom command line callback for delimiter
		myCallBacks.put("f", new CallBack() {
			@Override
			public void handle(String[] values) {
				filename  = values[0];
				try { 
					br = new BufferedReader(new FileReader(filename));
				}catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		});

		// Second step, set up the application logic, including the app queue
		app = Application.ApplicationFactory.getApplication( appName, args, myCallBacks);
		threadPool = app.getThreadPool();
		samples = app.getSampleSet();
		stats = new Statistics( samples );

        // Initialize the connection to the DB
		dataHub = new DataAccessHub();
		dataHub.addCluster( "test", new MongoClient() );
        MongoDAO d  = new MongoDAO<Document, Write>( "insert", "test", "firehose.csv" );
        d.setSamples( app.getSampleSet() );
		dataHub.addDAO( d );

		app.addPrinable(this);

	}

    public void execute() {
        while ( running.get()  )
            unitOfWork() ;

        synchronized (br) {
            if (br != null) try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer("{ ");
		buf.append("threads: " + app.getNumThreads());
		buf.append(", \"lines read\": "+ this.linesRead );
		buf.append(", samples: "+ stats.report() );
		
		if( verbose ) {
			buf.append(", converter: "+converter);
			buf.append(", source: "+filename);
		}
		buf.append(" }");
		return buf.toString();
	}
    
    public static void main( String[] args ) {
    	
    	try {
    		Firehose f = new Firehose( args );
			f.execute();
		} 
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
    }
}
