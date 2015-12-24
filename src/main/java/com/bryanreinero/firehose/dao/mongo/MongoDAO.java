package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.dao.DataAccessHub;

import com.bryanreinero.util.Operation;
import com.bryanreinero.util.OperationDescriptor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by bryan on 10/24/15.
 */
    public class MongoDAO<T, K extends Operation > extends OperationDescriptor {

    private MongoDatabase database;
    private MongoCollection<T> collection;
    private final String cluster;
    private final String dbName;
    private final String collectionName;
    private Constructor<K> opCtor = null;
    private DataAccessHub hub;

    public MongoDAO(String name, String cluster, String namespace) {
        super( name );
        this.cluster = cluster;
        String[] nameSpaceArray = namespace.split("\\.");
        dbName = nameSpaceArray[0];
        collectionName = nameSpaceArray[1];
    }

    public MongoCollection<T> getCollection(){ return collection; }

    public void setCollection( MongoCollection c ) { collection = c; }

    public void setOperationCtor(Constructor<K> ctor) {
        this.opCtor = ctor;
    }

    public Operation getOperation(Object... o) {
        Operation operation = null;
        try {
            operation = opCtor.newInstance( o, this);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return operation;
    }

    public String getDatabaseName() { return dbName; }
    public void setDatabase( MongoDatabase db ){ this.database = db; }

    public String getCollectionName() { return collectionName; }


    public static void main ( String[] args ) {

        DataAccessHub hub = new DataAccessHub();
        MongoClient client = new MongoClient();
        hub.addCluster( "blimpyacht", new MongoClient());


        MongoDAO descriptor = new MongoDAO("testRead", "blimpyacht", "firehose.test" );

        MongoDatabase db = client.getDatabase( descriptor.getDatabaseName() );
        MongoCollection coll = db.getCollection(descriptor.getCollectionName());

        descriptor.setDatabase( db );
        descriptor.setCollection( coll );


    }

    public String getClusterName() { return cluster; }
}
