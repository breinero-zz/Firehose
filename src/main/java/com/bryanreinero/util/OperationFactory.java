package com.bryanreinero.util;

import com.bryanreinero.firehose.dao.mongo.MongoInsert;
import com.mongodb.DBObject;

import java.util.HashMap;

/**
 * Created by breinero on 9/26/15.
 */
public class OperationFactory {

    private Map<String, OperaitonDescriptor> descriptors;

    public OperationFactory() {
        descriptors = new HashMap <String, OperationDesrciptor> ();
    }

    public void addDesrciptor( OperationDescriptor descriptor ) {
        descriptors.put( descriptor.getName(), descriptor );
    }

    public Operation getOperation( String name,  DBObject... objs ) {
        for ( DBObject o : objs ) {
            return new MongoInsert();
        }
    }

}
