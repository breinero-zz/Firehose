package com.xgen.load;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineInterface {
    
    private Option helpOp;
    private Option filenameOp;
    private Option mongosOp;
    private Option readerThreadsOp;
    private Option writerThreadsOp;
    private Option bufferSizeOp;
    private Option locationsOp;
    private Option durabilityOp;
    private Option namespaceOp;
    
    private Options options = new Options();
    
    private HelpFormatter formatter = new HelpFormatter();

    public CommandLineInterface() {
        
        helpOp = OptionBuilder.withArgName( "help" )
                .withType(String.class)
                .withDescription( "print help info" )
                .create( "h" );
        
        mongosOp = OptionBuilder.withArgName( "mongos" )
                .withType(String[].class)
                .withValueSeparator(',')
                .hasArgs() 
                .withDescription( "',' delimited list of mongos'es. Default localhost:27017," )
                .create( "m" );
        
        filenameOp = OptionBuilder.withArgName( "file" )
                .withType(String.class)
                .withDescription( "filename to import. REQUIRED" )
                .isRequired(true)
                .hasArg()
                .create( "f");;
        
        readerThreadsOp = OptionBuilder.withArgName( "readers" )
        .withType(Integer.class)
        .withDescription( "num reader threads. Defaults to 4" )
        .hasArg()
        .create( "r");
        
        writerThreadsOp = OptionBuilder.withArgName( "writers" )
                .withType(Integer.class)
                .withDescription( "num reader threads. Defaults to 4" )
                .hasArg()
                .create( "w");
        
        bufferSizeOp  = OptionBuilder.withArgName( "buffer-size" )
                .withDescription( "buffer size. Default = 50000 items" )
                .hasArg()
                .create( "b");
        
        locationsOp = OptionBuilder.withArgName( "location" )
                .withType(String[].class)
                .withValueSeparator(',')
                .hasArgs()
                .withDescription( "',' delimited list of store locations to be handled by this client")
                .create( "l" );
        
        durabilityOp = OptionBuilder.withArgName( "durability" )
                .withType(String.class)
                .withDescription( "write concern. Default = NORMAL")
                .hasArg()
                .create( "d" );
        
        namespaceOp = OptionBuilder.withArgName( "namespace" )
                .withType(String.class)
                .withDescription( "target namespace. Default = macystest.strSpace")
                .hasArg()
                .create( "n" );
        
        options.addOption(helpOp);
        //options.addOption(filenameOp);
        
        options.addOption("f", true, "filename to import. REQUIRED" );
        
        options.addOption(mongosOp);
        options.addOption(readerThreadsOp);
        options.addOption(writerThreadsOp);
        options.addOption(bufferSizeOp);
        options.addOption(locationsOp);
        options.addOption(durabilityOp);
        options.addOption(namespaceOp);
        
        HelpFormatter formatter = new HelpFormatter();
    }
        
    public void printHelp() {
        formatter.printHelp( "LoadClient", options );
    }
    
    public LoadClient parse ( String[] args ) throws ParseException, UnknownHostException, FileNotFoundException {
        CommandLineParser parser = new GnuParser();

        LoadClient client = null;
        CommandLine line = null;
        
        try {
            line = parser.parse( options, args );
            
            if ( line.hasOption( "h" ) ) {
                formatter.printHelp( "LoadClient", options );
                return null;
            }
            
            client = new LoadClient( line.getOptionValue( "f" ) );
            
            if ( line.hasOption( "b" ) ) 
                client.setBufferSize( new Integer( line.getOptionValue("b") ));
            
            if ( line.hasOption("d" ) )
                client.setDurability(line.getOptionValue("d"));
            
            if ( line.hasOption("l") )
                client.setLocations(line.getOptionValue("l").split(","));
            
            if ( line.hasOption( "m" ) )
                client.setMongos(line.getOptionValue("m").split(","));
            
            if ( line.hasOption("n") )
                client.setNamespace( line.getOptionValue("n") );
            
            if ( line.hasOption("r") )
                client.setReaderThreads( new Integer( line.getOptionValue("r")));
            
            if ( line.hasOption( "w" ) )
                client.setWriterThreads( new Integer( line.getOptionValue("w")));
            
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            throw e;
        }
        
        return client;
    }

    public static void main ( String[] args ) {
        CommandLineInterface  cli = new CommandLineInterface();
        try {
            cli.parse(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
