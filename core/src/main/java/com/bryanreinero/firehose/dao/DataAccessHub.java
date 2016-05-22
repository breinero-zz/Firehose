package com.bryanreinero.firehose.dao;

import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.util.OperationDescriptor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.HashMap;
import java.util.Map;

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
public enum DataAccessHub {
    INSTANCE;

    private final Map<String, MongoClient> clusters = new HashMap<String, MongoClient>();
    private final Map<String, OperationDescriptor> descriptors = new HashMap<String, OperationDescriptor>();

    /**
     * @param key
     * @param c
     */
    public void addCluster(String key, MongoClient c) {
        clusters.put(key, c);
    }

    public void addOperationDescriptor( MongoDAO dao) {
        String name = dao.getName();
        String cluster = dao.getClusterName();

        MongoClient client;
        if ( (client = clusters.get(cluster) ) == null)
            clusters.put( cluster, new MongoClient( dao.getClusterName() ) );

        MongoDatabase db = client.getDatabase(dao.getDatabaseName());
        dao.setDatabase(db);

        dao.setCollection( db.getCollection(dao.getCollectionName()) );

        descriptors.put(name, dao);
    }

    public OperationDescriptor getDescriptor(String name) {
        return descriptors.get(name);
    }

    public void setDataStore ( DataStore store ) {

        switch ( store.getType() ) {
            case unknown:
                break;
            case mongodb: {
                clusters.put( store.getName(), new MongoClient(store.getUri() ) );
                break;
            }
            default:
                throw new IllegalArgumentException( "Unrecognized data store type: "+store.getType() );
        }
    }
}