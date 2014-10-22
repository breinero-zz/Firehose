package com.bryanreinero.firehose.cli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CommandLineInterface {

	private Options options;

	private HelpFormatter formatter = new HelpFormatter();
	private CommandLineParser parser = new GnuParser();
	
	
	private Map<String, CallBack> callbacks = new HashMap<String, CallBack>();
	
	public void addCallBack( String key, CallBack cb ) {
		callbacks.put(key, cb);
	}
	

	public CommandLineInterface( ) throws Exception  {
		
		InputStream is = Options.class.getResourceAsStream("/options.json");
		try {
			options = OptionFactory.parseJSON( OptionFactory.ingest( is ) );
		} catch (IOException e) {
			throw new Exception( "Can't read options configuration", e );
		}
	}
	

	public void printHelp() {
		formatter.printHelp("Firehose", options);
	}

	public void parse(String[] args) throws UnknownHostException,
			FileNotFoundException, org.apache.commons.cli.ParseException, Exception {
		CommandLine line = parser.parse(options, args);

		for (Option option : line.getOptions())
			if (line.hasOption(option.getOpt())) {
				try { 
					callbacks.get(option.getOpt()).handle(option.getValues());
				} catch( Exception e ) {
					throw new Exception("Could not parse opt "+option.getOpt(), e);
				}
			}
	}
}
