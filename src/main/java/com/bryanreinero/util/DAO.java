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

public class DAO {

	private final DBCollection collection;

	public void insert ( DBObject object ) {
		WriteResult result = collection.insert(object);
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

}
