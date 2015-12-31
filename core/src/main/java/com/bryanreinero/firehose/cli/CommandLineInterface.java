package com.bryanreinero.firehose.cli;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.cli.*;

public class CommandLineInterface {

	private Options options = new Options();

	private HelpFormatter formatter = new HelpFormatter();
	private CommandLineParser parser = new GnuParser();
	
	
	private Map<String, CallBack> callbacks = new HashMap<String, CallBack>();
	
	public void addCallBack( String key, CallBack cb ) {
		callbacks.put(key, cb);
	}
	
	
	public void addOptions( String appName ) throws Exception  {
		
		InputStream is = CommandLineInterface.class.getClassLoader().getResourceAsStream(appName+".json");
		
		try {
			
			Options newOptions = OptionFactory.parseJSON( OptionFactory.ingest( is ) );
			
			Iterator<Option> it = newOptions.getOptions().iterator();
			while( it.hasNext() )
				options.addOption( it.next() );
			
		} catch (IOException e) {
			throw new Exception( "Can't read options configuration", e );
		}
	}

	public void printHelp() {
		formatter.printHelp("Firehose", options);
	}

	public void parse(String[] args) throws ParseException {
		CommandLine line = parser.parse(options, args);

		for (Option option : line.getOptions())
			if ( line.hasOption( option.getOpt() ) ) {
				try {
					callbacks.get( option.getOpt() ).handle(option.getValues());
				} catch (Exception e) {
					throw new IllegalArgumentException("Could not parse opt "+option.getOpt(), e);
				}
			}
	}

	public void addOption( Option o ) {
		options.addOption( o );
	}
}
