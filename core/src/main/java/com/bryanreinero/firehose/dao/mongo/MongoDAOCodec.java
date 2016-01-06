package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.dao.DataStore;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Created by bryan on 10/27/15.
 */
public class MongoDAOCodec implements Codec<MongoDAO> {

    @Override
    public MongoDAO decode(BsonReader bsonReader, DecoderContext decoderContext) {

        MongoDAO descriptor = null;
        bsonReader.readStartDocument();
        bsonReader.readObjectId();
        String name = bsonReader.readString("name");
        String datastore = bsonReader.readString("datastore" );
        String namespace = bsonReader.readString( "namespace" );
        String typeString = bsonReader.readString( "type" );


        DataStore.Type type = DataStore.Type.getType( typeString );
        if ( type.equals( DataStore.Type.unknown ))
            throw new IllegalArgumentException( "Unregonized data store type: "+typeString  );


        String application = bsonReader.readString( "application" );

        Class<?> c = null;
        String className = null;
        try {
            className = bsonReader.readString("class");
            c = Class.forName( className );
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException( "Unregonized entity: "+className, e  );
        }

        descriptor = new MongoDAO(
                name,
                datastore,
                namespace,
                c
        );

        //TODO: Add decoding call for embedded retry strategy

        bsonReader.readEndDocument();
        return descriptor;
    }

    @Override
    public void encode(BsonWriter bsonWriter, MongoDAO op, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString( "name", op.getName() );
        bsonWriter.writeString( "datastore", op.getClusterName() );
        bsonWriter.writeString( "namespace", op.getDatabaseName()+op.getCollectionName() );
        bsonWriter.writeString( "type", "mongodb" );
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<MongoDAO> getEncoderClass() {
        return MongoDAO.class;
    }
}

