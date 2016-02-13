package com.bryanreinero.firehose.dao;

import java.util.HashMap;
import java.util.Map;

import com.bryanreinero.firehose.dao.mongo.MongoDAO;
import com.bryanreinero.firehose.dao.mongo.MongoDAOCodec;
import com.bryanreinero.util.OperationDescriptor;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import static com.bryanreinero.firehose.dao.DataStore.*;
import static com.bryanreinero.firehose.dao.DataStore.Type.mongodb;

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

    public void setDao(MongoDAO dao) {
        String name = dao.getName();
        String cluster = dao.getClusterName();

        MongoClient client;
        if ((client = clusters.get(cluster)) == null)
            throw new IllegalArgumentException("No cluster exists for " + cluster);

        MongoDatabase db = client.getDatabase(dao.getDatabaseName());
        dao.setDatabase(db);

        dao.setCollection(db.getCollection(dao.getCollectionName(), dao.getEntityClass() ));

        descriptors.put(name, dao);
    }

    public OperationDescriptor getDescriptor(String name) {
        return descriptors.get(name);
    }

    public void setDataStore ( DataStore store ) {

        switch ( store.getType() ) {
            case mongodb: {
                Codec<Document> defaultDocumentCodec = MongoClient.getDefaultCodecRegistry().get(
                        Document.class);

                MongoDAOCodec daoCodec = new MongoDAOCodec();
                DataStoreCodec dsCodec = new DataStoreCodec();

                CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                        MongoClient.getDefaultCodecRegistry(),
                        CodecRegistries.fromCodecs( daoCodec, dsCodec )
                );

                MongoClientOptions options
                        = MongoClientOptions.builder().codecRegistry(codecRegistry).build();

                clusters.put(store.getName(), new MongoClient(store.getUri(), options ) );
                break;
            }
            default:
                throw new IllegalArgumentException( "Unrecognized data store type: "+store.getType() );
        }
    }
}