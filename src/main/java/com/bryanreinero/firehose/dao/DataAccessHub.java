package com.bryanreinero.firehose.dao;

import java.util.HashMap;
import java.util.Map;

import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.util.Operation;
import com.bryanreinero.util.OperationDescriptor;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * DataAccessHub is a map for Database
 * connections such as the MongoClient Class. The
 * Hub allows an application to connect to multiple database
 * types.
 *
 * The Firehose application sends database request 
 * to an Instance of the DataAccessHub (should therefore
 * be a singleton), which is helpful for data
 * migrations or polyglot persistence architectures.
 */
public class DataAccessHub {
	
	private final Map<String, MongoClient> clusters = new HashMap<String, MongoClient>();
	private final Map<String, OperationDescriptor> descriptors = new HashMap<String, OperationDescriptor>();


	private final Map<String, Class<? extends Operation>> operations = new HashMap<String, Class<? extends Operation>>();
	
	/**
	 *
	 * @param key
	 * @param c
	 */
	public void addCluster( String key, MongoClient c) {
		clusters.put(key, c);
	}

    public void addDAO(MongoDAO dao) {
        String name = dao.getName();
        String cluster = dao.getClusterName();

        MongoClient client = null;
        if( ( client = clusters.get(cluster) ) == null )
            throw new IllegalArgumentException( "No cluster exists for "+cluster );

        MongoDatabase db = client.getDatabase(dao.getDatabaseName());
        dao.setDatabase(db);
        dao.setCollection(db.getCollection(dao.getCollectionName()));

        descriptors.put(name, dao);
    }

	public Operation submit( String name, Object... o ) {
        return descriptors.get( name ).getOperation();
    }
}
