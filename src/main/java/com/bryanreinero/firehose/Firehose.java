package com.bryanreinero.firehose;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.cli.ParseException;

import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.util.WorkerPool.Executor;
import com.bryanreinero.util.DAO;
import com.bryanreinero.util.Printer;
import com.bryanreinero.util.WorkerPool;
import com.mongodb.DBObject;

public class Firehose implements Executor {
	
	private final WorkerPool workers;
	private int numThreads = 1;
	private AtomicInteger linesRead = new AtomicInteger(0);
	private Converter converter;
	private final SampleSet samples;
	private BufferedReader br = null;
	private DAO dao = null;
	private Printer printer = null;
	
	private Boolean verbose = false;
	private String filename = null;
	

    @Override
    public void execute() {
        String currentLine = null;
        Interval total = samples.set("total");
        try {
        	
        	// read the next line from source file
        	Interval readLine = samples.set("readline");
        	synchronized ( br ) {
            	currentLine = br.readLine();
        	}
            readLine.mark();
            
            if ( currentLine == null ) {
            	//total.mark();
            	//System.out.println("Reached end of file. Stopping intake");
                stop();
                
            }
            else {
                linesRead.incrementAndGet();
                
                // Create the DBObject for insertion
                Interval build = samples.set("build");
                DBObject object = converter.convert( currentLine );
                build.mark();
                
                // Insert the DBObject
                Interval insert = samples.set("insert");
                dao.insert( object );
                insert.mark();
                
                total.mark();
   
            }
        } catch (IOException e) {
            e.printStackTrace();
            
            try {
            	synchronized ( br ) {
                	if (br != null)br.close();
            	}
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void setConverter ( Converter converter ) {
    	this.converter = converter;
    }
    
    public void setInput( String filename ) throws FileNotFoundException {
    	this.filename  = filename;
    	br = new BufferedReader(new FileReader(filename));
    }
    
    public void setDao( DAO dao ) {
    	this.dao = dao;
    }
    
	public void setThreadCount( int count ) {
		numThreads = count;
	}
	
	public void setVerbose( Boolean v ) {
		this.verbose = v;
	}
	
	public void setPrinter( Printer printer ) {
		this.printer = printer;
		this.printer.addPrintable(this);
	}
	
	public void setTTL( Long ttl ) {
		samples.setTimeToLive( ttl );
	}
	
	public void setConsoleMode( boolean bool ) {
		printer.setConsole(bool );
	}
	
	public void start() {
		printer.start();
		workers.start( numThreads );
	}
	
	public void stop() {
		workers.stop();
		printer.stop();
		samples.stop();
	}
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer("{ ");
		buf.append("threads: "+workers.getNumThreads() );
		buf.append(", \"lines read\": "+ this.linesRead );
		buf.append(", samples: "+ samples );
		
		if( verbose ) {
			buf.append(", converter: "+converter);
			buf.append(", dao: "+dao);
			buf.append(", source: "+filename);
		}
		buf.append(" }");
		return buf.toString();
	}
    
    public Firehose() {
    	workers = new WorkerPool(this);
    	samples = new SampleSet();
    }
    
    public static void main( String[] args ) {
    	Firehose hoser = new Firehose();
    	CommandLineInterface cli = new CommandLineInterface(hoser);
    	try {
			cli.parse(args);
			hoser.start();
		} catch ( ParseException e1 ) {
			System.exit(-1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
    }
}
