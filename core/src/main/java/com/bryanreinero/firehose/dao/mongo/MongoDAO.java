package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.util.Operation;
import com.bryanreinero.firehose.util.OperationDescriptor;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bryan on 10/24/15.
 */
public class MongoDAO<T, K extends Operation > extends OperationDescriptor {

    public static Map operationTypes = new HashMap();

    public enum Type {
        insert, update, query, delete, command
    }

    private MongoDatabase database;
    private MongoCollection<T> collection;
    private final String cluster;
    private final String dbName;
    private final String collectionName;
    private final Class<T> clazz;

    public MongoDAO(String name, String cluster, String namespace, Class<T> clazz ) {
        super( name );
        this.cluster = cluster;
        String[] nameSpaceArray = namespace.split("\\.");
        dbName = nameSpaceArray[0];
        collectionName = nameSpaceArray[1];
        this.clazz = clazz;
    }

    public MongoCollection<T> getCollection(){ return collection; };

    public String getCollectionName() { return collectionName; }
    public void setCollection( MongoCollection c ) { collection = c; }

    public Class<T> getEntityClass() { return clazz; }

    public String getDatabaseName() { return dbName; }
    public void setDatabase( MongoDatabase db ){ this.database = db; }

    public String getClusterName() { return cluster; }
}
