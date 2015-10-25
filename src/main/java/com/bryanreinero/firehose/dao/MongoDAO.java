package com.bryanreinero.firehose.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bryanreinero.firehose.dao.mongo.Write;

import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.util.retry.BasicRetry;
import com.bryanreinero.util.Operation;

import com.mongodb.MongoClient;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;

public class MongoDAO implements DataAccessObject {

	protected final MongoClient client;
	private final MongoCollection<Document> collection;
    private final Map<String, Operation> operations = new HashMap<String, Operation>();

	protected WriteConcern wc = null;
	protected ReadPreference rp;

    private SampleSet samples = null;

	public MongoDAO( MongoClient client, String namespace) {
		this.client = client;
		String[] nameSpaceArray = namespace.split("\\.");

		MongoDatabase db = client.getDatabase(nameSpaceArray[0]);
		collection = db.getCollection( nameSpaceArray[1] );

        CodecRegistry registry = collection.getCodecRegistry();
		
		wc = new WriteConcern();
		rp = ReadPreference.primary();
	}

    public Write getNewInsert(Document object) {
        Write write = new Write( "WriteSample", object, collection, samples );
        write.setRetryPolicy( new BasicRetry( 3, 5000, 50000000, write ) );
        return  write ;
    }

    public void setSampleSet( SampleSet s ) { samples = s; }

    public void setOperation( Document descriptor ){

        Operation op = null;

		Document retryDescriptor = descriptor.get( "retryPolicy" );

        //Map<String, String> retryPolicy = descriptor.get( "retryPolicy" );

        //RetryPolicy policy = new BasicRetry( 3, 5000, 50000000, );
        operations.put(descriptor.getString("name"), op);
    }

    public void submitOperation( String name, Document doc ) {
        Write write = new Write( name, doc, collection, samples );
    }

    @Override
	public String toString() {

		WriteConcern concern = collection.getWriteConcern();
		StringBuffer buf = new StringBuffer(" { namespace: \""
				+ collection.getNamespace() + "\"");
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
