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
import com.bryanreinero.firehose.dao.MongoDAO;
import com.bryanreinero.firehose.dao.mongo.Write;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.firehose.metrics.Statistics;
import com.bryanreinero.util.Application;
import com.bryanreinero.util.ThreadPool;
import com.bryanreinero.util.Result;
import com.mongodb.DBObject;

public class Firehose {
	
	private static final String appName = "Firehose";
	private final Application app;
	private final ThreadPool threadPool;
	private final SampleSet samples;
	private final Statistics stats;
	private AtomicInteger linesRead = new AtomicInteger(0);
	private Converter converter = new Converter();
	private BufferedReader br = null;
	private MongoDAO dao = null;
	
	private Boolean verbose = false;
	private String filename = null;

	private AtomicBoolean running = new AtomicBoolean( true );

	private class UnitOfWork implements Callable<Result> {
		@Override
		public Result call() throws Exception {
			String currentLine = null;

			try {

				try (Interval total = samples.set("total")) {

					// read the next line from source file
					try (Interval readLine = samples.set("readline")) {
						synchronized (br) {
							currentLine = br.readLine();
						}
					}

					if (currentLine == null)
						running.set(false);

					else {
						linesRead.incrementAndGet();

						DBObject object = null;

						// Create the DBObject for insertion
						try (Interval build = samples.set("build")) {
							object = converter.convert(currentLine);
						}

						// Insert the DBObject
						try (Interval insert = samples.set("insert")) {
							dao.insert(object);
						}

					}
				}
			} catch (IOException e) {
				running.set(false);
				e.printStackTrace();
			} finally {
				synchronized (br) {
					if (br != null) br.close();
				}

			}
			return null;
		};
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
		
		// start the work queue
		dao = app.getDAO();
		app.addPrinable(this);

	}

    public void execute() {
        while ( running.get()  ) threadPool.submitTask(new UnitOfWork());
    }
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer("{ ");
		buf.append("threads: " + app.getNumThreads());
		buf.append(", \"lines read\": "+ this.linesRead );
		buf.append(", samples: "+ stats.report() );
		
		if( verbose ) {
			buf.append(", converter: "+converter);
			buf.append(", dao: "+dao);
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
