package com.xgen.load;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.xgen.load.Pump.Sink;
import com.xgen.load.Pump.Source;

public class LoadClient implements Source<DBObject>, Sink<DBObject> {

    private MongoClient mongo;

    private String filename = null;
    private Integer readerThreads = 4;
    private Integer writerThreads = 4;
    private String[] locations = null;
    private WriteConcern durability = WriteConcern.NORMAL;
    private int bufferSize = Integer.valueOf(50000);
    private String namespace = "macystest.strSpace";
    
    private BufferedReader br = null;

    private Integer itemsProcessed = new Integer(0);
    private Integer linesRead = new Integer(0);
    
    private Pump<DBObject> pump = null;
    private DBCollection items = null;
    
    // create a List of all the nodes in this replica set
    List<ServerAddress> addrs = new ArrayList<ServerAddress>();

    public LoadClient ( String filename ) throws UnknownHostException, FileNotFoundException {

        if(filename == null || filename.length() == 0 )
            throw new IllegalArgumentException("No filename");
        this.filename = filename;
    }
    
    @Override
    public String toString() {
        
        DBObject clientObj = new BasicDBObject();
        
        clientObj.put("mongos", mongo.getAllAddress().toString() );
        clientObj.put("durability", durability.toString() );
        clientObj.put("buffer", bufferSize );
        clientObj.put("file", filename );
        clientObj.put("location", locations );
        clientObj.put("namespace", namespace );
        clientObj.put("readers", readerThreads );
        clientObj.put("writers", writerThreads );
        return clientObj.toString();
    }
    
    public void initialize () throws FileNotFoundException {
        br = new BufferedReader(new FileReader(filename));
        
        // Build mongo options 
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.writeConcern(durability); // Set durability
        builder.readPreference(ReadPreference.primary()); // Set consistency
        MongoClientOptions options = builder.build();
        
        mongo = new MongoClient(addrs, options);
        
        String[] comp = namespace.split("\\.");
        items =  mongo.getDB( comp[0] ).getCollection( comp[1] );
        
        pump = new Pump<DBObject>( this, this, readerThreads, writerThreads, bufferSize );
    }
    
    public void start() {
        pump.start();
    }
    
    private void stop() {
        pump.stop();
    }

    public int getItemsProcessed() {
        synchronized ( itemsProcessed ) {  return itemsProcessed.intValue(); }
    }

    public int getLinesRead() {
        synchronized ( linesRead ) {  return linesRead.intValue(); }
    }
    
    public void report ()  {
        System.out.println("lines read: "+linesRead+" buffer size:"+    pump.getQueueSize()+" insertions: "+itemsProcessed);
    }
    
    @Override
    public void consume( DBObject object) {
        if ( object == null ) {
            pump.stop();
            return;
        }
        try {
            items.insert(object);
        } catch ( MongoException e ) {
            System.out.println("Stopping output: "+e.getMessage());
            pump.stop();
        }
        synchronized ( itemsProcessed ) {  itemsProcessed++; }
    }

    @Override
    public DBObject produce() {
        String currentLine = null;
        
        try { 
            currentLine = br.readLine();
            
            if ( currentLine == null ) {
                System.out.println("Reached end of file. Stopping intake");
                pump.stop();
                return null;
            }
            else {
                synchronized ( linesRead ) {  linesRead++; }
                
                String[] fields = currentLine.split(",");
                Item item = new Item(
                        Integer.valueOf(fields[0]).intValue(),
                        Integer.valueOf(fields[1]).intValue(), 
                        fields[2], 
                        Integer.valueOf(fields[3]).intValue(),
                        Integer.valueOf(fields[4]).intValue(), 
                        fields[5], 
                        Integer.valueOf(fields[6]).intValue(),
                        Integer.valueOf(fields[7]).intValue(), 
                        Integer.valueOf(fields[8]).intValue(), 
                        Integer.valueOf(fields[9]).intValue(),
                        Long.valueOf(fields[10]).longValue(), 
                        Long.valueOf(fields[11]).longValue(), 
                        Integer.valueOf(fields[12]).intValue()
                        );

                // load specific locations is specified otherwise load all
                if( locations != null && locations.length > 0 ) {
                    for ( String location : locations ) {
                        if ( Integer.valueOf(location ) == item.getNBR() )
                            return item.toDBObject();
                    }
                } 
                else {
                    return item.toDBObject();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            
            try {
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static void main ( String[] args ) {

        LoadClient client = null;
        CommandLineInterface cli = new CommandLineInterface();
        
        try {
            client = cli.parse(args);
        } catch ( Exception e) {
            e.printStackTrace();
        } 

        if ( client != null ) {
            try {
                client.initialize();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            
            System.out.println(client);
            client.start();
            Scanner scanner = new Scanner (System.in);
            while( true ) {
                System.out.println("report stats or quit? s/q");  
                String command = scanner.next();
                
                if( command.equalsIgnoreCase("q")) {
                    client.stop();
                    break;
                }
                if ( command.equalsIgnoreCase("s") ) client.report();
            }
            
            System.out.print("Stopping.... ");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.report();
        }
    }

    public void setMongos(String[] hosts) throws NumberFormatException, UnknownHostException  {
        for ( String host : hosts ) {
            String[] comp = host.split(":");
            addrs.add(new ServerAddress(comp[0], Integer.valueOf( comp[1] ) ) );
        }
    }

    public void setReaderThreads(Integer readerThreads) {
        this.readerThreads = readerThreads;
    }

    public void setWriterThreads(Integer writerThreads) {
        this.writerThreads = writerThreads;
    }

    public void setLocations(String[] locations) {
        this.locations = locations;
    }

    public void setDurability(String durability) {
        
        if( durability.equalsIgnoreCase("ACKNOWLEDGED"))
            this.durability = WriteConcern.ACKNOWLEDGED;
        
        else if( durability.equalsIgnoreCase("ERRORS_IGNORED"))
            this.durability = WriteConcern.ERRORS_IGNORED;
        
        else if( durability.equalsIgnoreCase("JOURNAL_SAFE"))
            this.durability = WriteConcern.JOURNAL_SAFE;
        
        else if( durability.equalsIgnoreCase("JOURNALED"))
            this.durability = WriteConcern.JOURNALED;
        
        else if( durability.equalsIgnoreCase("MAJORITY"))
            this.durability = WriteConcern.MAJORITY;
        
        else if( durability.equalsIgnoreCase("REPLICA_ACKNOWLEDGED"))
            this.durability = WriteConcern.REPLICA_ACKNOWLEDGED;
        
        else  if( durability.equalsIgnoreCase("REPLICAS_SAFE"))
            this.durability = WriteConcern.REPLICAS_SAFE;
        
        else  if( durability.equalsIgnoreCase("NORMAL"))
            this.durability = WriteConcern.NORMAL;
        
        else throw new IllegalArgumentException("Unsupported durability: "+durability);
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
