package com.bryanreinero.firehose;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.bryanreinero.util.DAO;
import com.bryanreinero.util.OptionFactory;
import com.bryanreinero.util.Printer;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;

public class CommandLineInterface {

	private static final String DefaultDelimiter = ",";
	private final Firehose client;

	private Options options;

	private HelpFormatter formatter = new HelpFormatter();
	private CommandLineParser parser = new GnuParser();
	

	public CommandLineInterface(Firehose app) throws Exception  {

		this.client = app;
		
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

	public void parse(String[] args) throws ParseException,
			UnknownHostException, FileNotFoundException {
		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("verbose"))
				client.setVerbose(true);

			String[] columns = line.getOptionValues("headers");
			Map<String, String> fieldDefs = new LinkedHashMap<String, String>();
			for (String column : columns) {
				String[] s = column.split(":");
				fieldDefs.put(s[0], s[1]);
			}

			String delim = DefaultDelimiter;
			if (line.hasOption("delimiter"))
				delim = line.getOptionValue("delimiter");

			client.setConverter(new Converter(fieldDefs, delim));

			DAO dao;
			MongoClient mongo;
			if (line.hasOption("mongos")) {

				List<ServerAddress> adresses = DAO.getServerAddresses(line
						.getOptionValues("mongos"));
				mongo = new MongoClient(adresses);

			} else {
				mongo = new MongoClient();
			}

			String val = line.getOptionValue("namespace");
			String[] ns = val.split("\\.");
			DBCollection collection = mongo.getDB(ns[0]).getCollection(ns[1]);
			client.setDao(new DAO(collection));

			if (line.hasOption("threads"))
				client.setThreadCount(
						Integer.parseInt(
								line.getOptionValue("threads", "1")
							)
						);

			client.setInput( line.getOptionValue("filepath") );

			if (line.hasOption("report"))
				client.setPrinter(new Printer(Integer.parseInt(line
						.getOptionValue("report"))));
			else
				client.setPrinter(new Printer(1));

			if (line.hasOption("interval"))
				client.setTTL(Long.parseLong(line.getOptionValue("interval")));
			
			if (line.hasOption("format"))
				client.setConsoleMode( false );
			
			if (line.hasOption("durability"))
				client.setDurability( line.getOptionValues("durability") );
			
		} catch (ParseException e) {
			System.out.println( e.getMessage() );
			printHelp();
			throw e;
		}

	}

	public static void main(String[] args) {

		Firehose client = new Firehose();

		try {
			CommandLineInterface cli = new CommandLineInterface(client);
			cli.parse(args);
			//System.out.println(client);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
