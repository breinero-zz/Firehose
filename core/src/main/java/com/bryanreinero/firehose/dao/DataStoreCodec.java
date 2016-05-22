package com.bryanreinero.firehose.dao;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by brein on 1/2/2016.
 */
public class DataStoreCodec implements Codec<DataStore> {
    @Override
    public DataStore decode(BsonReader reader, DecoderContext decoderContext) {

        DataStore store = null;
        reader.readStartDocument();
        String name = reader.readString("_id");
        String typeS = reader.readString("type");
        String uri = reader.readString("uri");
        String app = reader.readString("application");
        reader.readEndDocument();

        try {
            return new DataStore(name, app, new URI( uri ) );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException( "bad uri ", e );
        }
    }

    @Override
    public void encode(BsonWriter writer, DataStore value, EncoderContext encoderContext) {

    }

    @Override
    public Class<DataStore> getEncoderClass() {
        return DataStore.class;
    }
}
