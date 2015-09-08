package com.bryanreinero.firehose.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class MongoDAO implements DataAccessObject {

	protected final DBCollection collection;
	protected final DB db;
	protected final MongoClient client;

	protected WriteConcern wc = null;
	protected ReadPreference rp;

	public WriteResult insert(DBObject object) {
		return collection.insert(object);
	}
	
	public DBObject read( DBObject query ) {
		return collection.findOne( query );
	}
	
	public WriteResult update( DBObject query, DBObject update ) {
		return collection.update(query, update);
	}
	
	public WriteResult delete( DBObject query ) {
		return collection.remove(query);
	}

	public MongoDAO( MongoClient client, String namespace) {
		this.client = client;
		String[] nameSpaceArray = namespace.split("\\.");
		db = client.getDB(nameSpaceArray[0]);
		collection = db.getCollection(nameSpaceArray[1]);
		
		wc = new WriteConcern();
		rp = ReadPreference.primary();
	}

	@Override
	public String toString() {

		WriteConcern concern = collection.getWriteConcern();
		StringBuffer buf = new StringBuffer(" { namespace: \""
				+ collection.getFullName() + "\"");
		buf.append(", writeConcern: { ");
		buf.append(" w: " + concern.getWString());
		buf.append(", j: " + concern.getJ());
		buf.append(", timeout: " + concern.getWtimeout());
		buf.append(", fsync: " + concern.getFsync());
		buf.append(" }");
		buf.append(" }");
		return buf.toString();
	}

	public void setW(String concern) {
		wc = new WriteConcern(concern, wc.getWtimeout(), wc.getFsync(),
				wc.getJ() );
	}

	public void setTimeOut(int timeout) {
		wc = new WriteConcern(wc.getWString(), timeout, wc.getFsync(),
				wc.getJ());
	}

	public void setFSync(boolean s) {
		wc = new WriteConcern(wc.getWString(), wc.getWtimeout(), s, wc.getJ() );
	}

	public void setJournal(boolean j) {

		wc = new WriteConcern(wc.getWString(), wc.getWtimeout(), wc.getFsync(), j );
	}

	public void setContinueOnError(boolean cont) {
		wc = new WriteConcern(wc.getWString(), wc.getWtimeout(), wc.getFsync(),
				wc.getJ() );
	}

	public void setConcern(String s) {
		wc = new WriteConcern(s, 
			wc.getWtimeout(), 
			wc.getFsync(), 
			wc.getJ()
		);
	}

	/**
	 * Converts a String array into a list of ServerAdress objects which a
	 * MongoClient can connect to
	 * 
	 * @param hosts
	 *            The array of hostname:port strings
	 * @return a List of ServerAddress
	 */
	public static List<ServerAddress> getServerAddresses(String[] hosts) {
		List<ServerAddress> addresses = new ArrayList<ServerAddress>();

		for (String uri : hosts) {
			String[] s = uri.split(":");
			try {
				ServerAddress address = null;
				if (s.length > 1)
					addresses.add(new ServerAddress(s[0], Integer
							.parseInt(s[1])));
				else
					addresses.add(new ServerAddress(s[0]));
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			} catch ( Exception e) {
				e.printStackTrace();
			}
		}
		return addresses;
	}
	
	public void setReadPreference( ReadPreference rp ) {
		this.rp = rp;
	}

	public void setDurability(String key, String s) {
		if (key.compareTo("journal") == 0)
			this.setJournal(Boolean.parseBoolean(s));
		else if (key.compareTo("fsync") == 0)
			this.setFSync(Boolean.parseBoolean(s));
		else if (key.compareTo("continueOnErr") == 0)
			this.setContinueOnError(Boolean.parseBoolean(s));
		else if (key.compareTo("timeout") == 0)
			this.setTimeOut(Integer.parseInt(s));
		else if (key.compareTo("w") == 0)
			this.setW(s);
		else if (key.compareTo("concerns") == 0)
			this.setConcern(s);
	}

	@Override
	public Object execute(Map<String, Object> request) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}
}
