package com.bryanreinero.firehose.util.retry;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bryan on 10/23/15.
 */
public class RetryPolicyCodec implements Codec<RetryPolicy> {

    private static final Map<RetryPolicyType, Class<? extends RetryPolicy> > map
            = new HashMap<RetryPolicyType, Class<? extends RetryPolicy>>();

    @Override
    public RetryPolicy decode(BsonReader bsonReader, DecoderContext decoderContext) {
        int value = bsonReader.readInt32( "value" );
        int delay = bsonReader.readInt32( "delay" );
        int maxRetries = bsonReader.readInt32( "retries" );
        int maxDuration = bsonReader.readInt32( "duration" );

        RetryPolicy policy = null;

        Class<? extends RetryPolicy> clazz = map.get( value );
        Constructor<? extends RetryPolicy> ctor = null;
        try {
            ctor = clazz.getConstructor( int.class, long.class, long.class );
            policy = ctor.newInstance(maxRetries, delay, maxDuration);
        } catch (Exception e) {
            throw new IllegalArgumentException( "Can not instantiate RetryPolicy type: "+value, e );
        }
        return policy;
    }

    @Override
    public void encode(BsonWriter bsonWriter, RetryPolicy retryPolicy, EncoderContext encoderContext) {

    }

    @Override
    public Class<RetryPolicy> getEncoderClass() {
        return null;
    }
}
