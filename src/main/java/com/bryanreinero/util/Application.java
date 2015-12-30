package com.bryanreinero.util;

import com.bryanreinero.firehose.cli.CallBack;
import com.bryanreinero.firehose.cli.CommandLineInterface;
import com.bryanreinero.firehose.metrics.SampleSet;

import org.apache.commons.cli.Option;

public class Application {

    private final String name;
	private ThreadPool workers;
	private final CommandLineInterface cli;
	private Printer printer = new Printer( DEFAULT_PRINT_INTERVAL );
	private final SampleSet samples;
	
	public static final int DEFAULT_PRINT_INTERVAL = 1;
	public static final long DEFAULT_REPORTING_INTERVAL = 5;
	
	private int numThreads = 1;

	public SampleSet getSampleSet() {
		return samples;
	}

	public void setCommandLineInterfaceCallback(String key, CallBack cb ) {
		cli.addCallBack(key, cb);
	}

    public void parseCommandLineArgs ( String[] args ) {

        try {
            cli.addOptions( name );
        } catch (Exception e) {
            throw new IllegalStateException ( "Failed to initialize application command line configuration. ", e );
        }

        try {
            cli.addOptions( name );
            cli.parse( args );
            workers = new ThreadPool( numThreads );
        } catch (Exception e) {
            throw new IllegalStateException ( "Failed to process command line arguments. ", e );
        }
    }

	public Application(String name) {
        this.name = name;
		samples = new SampleSet();
		samples.setTimeToLive(DEFAULT_REPORTING_INTERVAL);

		// prep the CLI with a set of 
		// standard CL option handlers
		cli = new CommandLineInterface();

		Option o = new Option( "t", null ) ;
		o.setLongOpt("threads");
		o.setArgName( "number" );
		o.setDescription( "number of worker threads. Default 1" );
        o.setArgs( 1 );
        cli.addOption( o );
		cli.addCallBack("t", new CallBack() {

			@Override
			public void handle(String[] values) {
				numThreads = Integer.parseInt(values[0]);
			}
		});

        o = new Option( "ri", null ) ;
        o.setLongOpt("reportingInterval");
        o.setArgName( "interval" );
        o.setDescription(  "average stats over an time interval of i milleseconds" );
        o.setArgs( 1 );
        cli.addOption( o );
		cli.addCallBack("ri", new CallBack() {

			@Override
			public void handle(String[] values) {
				samples.setTimeToLive(Long.parseLong(values[0]));
			}
		});

        o = new Option( "pi", null ) ;
        o.setLongOpt("printInterval");
        o.setArgName( "interval" );
        o.setDescription( "print output every n seconds" );
        o.setArgs( 1 );
        cli.addOption( o );
        cli.addCallBack("pi", new CallBack() {

			@Override
			public void handle(String[] values) {
				printer = new Printer(Integer.parseInt(values[0]));
			}
		});

        o = new Option( "cr", null ) ;
        o.setLongOpt("noPretty");
        o.setArgName( "format" );
        o.setDescription( "print out in CR-delimited lines. Default is console mode pretty printing, when possible" );
        cli.addOption( o );
		cli.addCallBack("cr", new CallBack() {

			@Override
			public void handle(String[] values) {
				printer.setConsole(false);
			}

		});
    }

    public int getNumThreads() {
		return this.numThreads;
	}

	public void addPrinable(Object o) {
		printer.addPrintable(o);
	}

	public ThreadPool getThreadPool() { return workers; }

}

