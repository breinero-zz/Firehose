package com.bryanreinero.firehose;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.bryanreinero.util.DAO;
import com.bryanreinero.util.Printer;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class CommandLineInterface {

	private static final String DefaultDelimiter = ",";
	private final Firehose client;

	private Options options = new Options();

	private HelpFormatter formatter = new HelpFormatter();
	private CommandLineParser parser = new GnuParser();

	public CommandLineInterface(Firehose app) {

		this.client = app;
		
		options.addOption(OptionBuilder
				.withArgName("[host:port]")
				.withType(String[].class)
				.withValueSeparator(',')
				.hasArgs()
				.withDescription(
						"',' delimited list of mongos'es. Default localhost:27017,")
				.create("m"));

		options.addOption(OptionBuilder.withArgName("filepath")
				.withType(String.class)
				.withDescription("filename to import. REQUIRED")
				.isRequired(true).hasArg().create("f"));

		options.addOption(OptionBuilder.withArgName("num")
				.withType(Integer.class)
				.withDescription("number of threads. Default 1.").hasArg()
				.create("t"));

		options.addOption(OptionBuilder.withArgName("durability")
				.withType(String.class)
				.withDescription("write concern. Default = NORMAL").hasArg()
				.create("dur"));

		options.addOption(OptionBuilder.withArgName("dbname.collection")
				.withType(String.class)
				.withDescription("target namespace. REQUIRED")
				.isRequired(true)
				.hasArg().create("n"));

		options.addOption(OptionBuilder.withArgName("name:type")
				.withType(String[].class).withValueSeparator(',').hasArgs()
				.isRequired()
				.withDescription("',' delimited list of columns [name:type]")
				.create("cols"));

		options.addOption(OptionBuilder
				.withArgName("delimeter")
				.withType(String[].class)
				.withDescription(
						"the value separator used to parse columns. Default ','")
				.create("delim"));

		options.addOption(OptionBuilder
				.withType(Boolean.class)
				.withDescription(
						"Enable verbose output")
				.create("v"));
		
		options.addOption(OptionBuilder
				.withArgName("num")
				.withType(Integer.class)
				.hasArg()
				.withDescription(
						"print progress every 'n' seconds")
				.create("p"));
		
		options.addOption(OptionBuilder
				.withArgName("num")
				.withType(Integer.class)
				.hasArg()
				.withDescription(
						"sample interval over which to report stats, (milleseconds)")
				.create("i"));
		
		options.addOption(OptionBuilder
				.withDescription(
						"print output in console mode, when possible")
				.create("c"));
	}

	public void printHelp() {
		formatter.printHelp("Firehose", options);
	}

	public void parse(String[] args) throws ParseException,
			UnknownHostException, FileNotFoundException {
		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("v"))
				client.setVerbose(true);

			String[] columns = line.getOptionValues("cols");
			Map<String, String> fieldDefs = new LinkedHashMap<String, String>();
			for (String column : columns) {
				String[] s = column.split(":");
				fieldDefs.put(s[0], s[1]);
			}

			String delim = DefaultDelimiter;
			if (line.hasOption("delim"))
				delim = line.getOptionValue("delim");

			client.setConverter(new Converter(fieldDefs, delim));

			DAO dao;
			MongoClient mongo;
			if (line.hasOption("m")) {

				List<ServerAddress> adresses = DAO.getServerAddresses(line
						.getOptionValues("m"));
				mongo = new MongoClient(adresses);

			} else {
				mongo = new MongoClient();
			}

			String val = line.getOptionValue("n");
			String[] ns = val.split("\\.");
			DBCollection collection = mongo.getDB(ns[0]).getCollection(ns[1]);
			client.setDao(new DAO(collection));

			if (line.hasOption("t"))
				client.setThreadCount(Integer.parseInt(line.getOptionValue("t")));
			else
				client.setThreadCount(1);

			client.setInput(line.getOptionValue("f"));

			if (line.hasOption("p"))
				client.setPrinter(new Printer(Integer.parseInt(line
						.getOptionValue("p"))));
			else
				client.setPrinter(new Printer(1));

			if (line.hasOption("i"))
				client.setTTL(Long.parseLong(line.getOptionValue("i")));

		} catch (ParseException e) {
			System.out.println( e.getMessage() );
			printHelp();
			throw e;
		}

	}

	public static void main(String[] args) {

		Firehose client = new Firehose();
		CommandLineInterface cli = new CommandLineInterface(client);

		try {
			cli.parse(args);
			System.out.println(client);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
