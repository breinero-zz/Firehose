package com.bryanreinero.firehose.dao;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import static com.bryanreinero.firehose.dao.DataStore.*;

/**
 * Created by brein on 1/2/2016.
 */
public class DataStoreCodec implements Codec<DataStore> {
    @Override
    public DataStore decode(BsonReader reader, DecoderContext decoderContext) {

        DataStore store = null;
        reader.readStartDocument();
        String name = reader.readString("_id");
        String app = reader.readString("application");
        String typeS = reader.readString("type");
        Type type = Type.getType(typeS);
        String uri = reader.readString("uri");
        reader.readEndDocument();

        switch (type) {
            case unknown:
                throw new IllegalArgumentException("Unrecognized data store type: " + typeS);

            case mongodb:
                store = new DataStore(name, app, uri, type);
            break;
        }
        return store;
    }

    @Override
    public void encode(BsonWriter writer, DataStore value, EncoderContext encoderContext) {

    }

    @Override
    public Class<DataStore> getEncoderClass() {
        return DataStore.class;
    }
}
