package com.bryanreinero.util;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import com.mongodb.Cursor;

public class DAO {

	private final DBCollection collection;

	public void createIndex ( DBObject object ) {
		collection.createIndex(object);
	}

	public WriteResult insert ( DBObject object ) {
		return collection.insert(object);
	}

    public WriteResult update( DBObject query , DBObject update , boolean upsert , boolean multi , WriteConcern concern ) {
        return collection.update(query,update,upsert,multi,concern);
    }

    public WriteResult update( DBObject query , DBObject update , boolean upsert , boolean multi ) {
        return collection.update(query,update,upsert,multi);
    }

    public Cursor find ( DBObject query , DBObject projection, int numToSkip, int batchSize, int options ){
		return collection.find(query, projection, numToSkip, batchSize, options);
	}

    public Cursor find ( DBObject query , DBObject projection, int numToSkip, int batchSize ){
        int options = 0;
		return find(query, projection, numToSkip, batchSize, options);
	}

    public Cursor find ( DBObject query , DBObject projection ){
        int numToSkip = 0;
        int batchSize = 20;
        int options = 0;
		return find(query, projection, numToSkip, batchSize, options);
	}

	public DAO ( DBCollection collection ) {
		this.collection = collection;
	}

	@Override 
	public String toString() {
		
		WriteConcern concern = collection.getWriteConcern();
		StringBuffer buf = new StringBuffer( " { namespace: \""+collection.getFullName()+"\"" );
		buf.append( ", writeConcern: { ");
		buf.append( " w: "+concern.getWString() );
		buf.append( ", j: "+concern.getJ() );
		buf.append( ", timeout: "+concern.getWtimeout() );
		buf.append( ", fsync: "+concern.getFsync() );
		buf.append(" }");
		buf.append(" }");
		return buf.toString();
	}
	
	public void setW( String concern ) {
		WriteConcern wc = collection.getWriteConcern();
		
		WriteConcern newguy = new WriteConcern( 
				concern,
				wc.getWtimeout(),
				wc.getFsync(),
				wc.getJ(),
				wc.getContinueOnError()
				);
		collection.setWriteConcern(newguy);
	}
	

	public void setTimeOut( int timeout ) {
		WriteConcern wc = collection.getWriteConcern();
		
		WriteConcern newguy = new WriteConcern( 
				wc.getWString(),
				timeout,
				wc.getFsync(),
				wc.getJ(),
				wc.getContinueOnError()
				);
		
		collection.setWriteConcern(newguy);
	}
	
	public void setFSync( boolean s ) {
		WriteConcern wc = collection.getWriteConcern();
		
		WriteConcern newguy = new WriteConcern( 
				wc.getWString(),
				wc.getWtimeout(),
				s,
				wc.getJ(),
				wc.getContinueOnError()
				);
		
		collection.setWriteConcern(newguy);
	}
	
	public void setJournal( boolean j ) {
		WriteConcern wc = collection.getWriteConcern();
		
		WriteConcern newguy = new WriteConcern( 
				wc.getWString(),
				wc.getWtimeout(),
				wc.getFsync(),
				j,
				wc.getContinueOnError()
				);
		
		collection.setWriteConcern(newguy);
	}
	
	public void setContinueOnError( boolean cont ) {
		WriteConcern wc = collection.getWriteConcern();
		
		WriteConcern newguy = new WriteConcern( 
				wc.getWString(),
				wc.getWtimeout(),
				wc.getFsync(),
				wc.getJ(),
				cont
				);
		
		collection.setWriteConcern(newguy);
	}
	
	public void setConcern( String s ) {
		WriteConcern wc = collection.getWriteConcern();
		
		WriteConcern newguy = new WriteConcern( 
				s,
				wc.getWtimeout(),
				wc.getFsync(),
				wc.getJ(),
				wc.getContinueOnError()
				);
	
		collection.setWriteConcern(newguy);
	}
	
	public static List<ServerAddress> getServerAddresses(String[] hosts) {
		List<ServerAddress> addresses = new ArrayList<ServerAddress>();

		for (String uri : hosts) {
			String[] s = uri.split(":");
			try {
				ServerAddress address = null;
				if (s.length > 1)
					addresses.add( new ServerAddress(s[0], Integer.parseInt(s[1]) ) );
				else
					addresses.add( new ServerAddress(s[0]) );
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		return addresses;
	}
	
	public static void main( String[] args ) {
		try {
			MongoClient client = new MongoClient();
			DBCollection collection = client.getDB("test").getCollection("insert");
			DAO dao = new DAO( collection ) ;
			
			System.out.println( dao );
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setDurability(String key, String s) {
		if( key.compareTo("journal") == 0 )
			this.setJournal( Boolean.parseBoolean(s));
		else if ( key.compareTo("fsync") == 0 )
			this.setFSync( Boolean.parseBoolean(s));
		else if ( key.compareTo("continueOnErr") == 0 )
			this.setContinueOnError( Boolean.parseBoolean(s));
		else if ( key.compareTo("timeout") == 0 )
			this.setTimeOut( Integer.parseInt(s));
		else if ( key.compareTo("w") == 0 )
			this.setW(s);
		else if ( key.compareTo("concerns") == 0 )
			this.setConcern(s);
	}

}
