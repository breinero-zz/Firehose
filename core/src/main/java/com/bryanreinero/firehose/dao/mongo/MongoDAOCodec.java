package com.bryanreinero.firehose.dao.mongo;

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

        MongoDAO descriptor = new MongoDAO(
                bsonReader.readString("name"),
                bsonReader.readString( "cluster" ),
                bsonReader.readString( "namespace" )
        );

        return descriptor;
    }

    @Override
    public void encode(BsonWriter bsonWriter, MongoDAO mongoOpDescriptor, EncoderContext encoderContext) {
    }

    @Override
    public Class<MongoDAO> getEncoderClass() {
        return null;
    }


}
