package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.dao.DAOException;
import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.util.Operation;
import com.bryanreinero.util.OperationDescriptor;
import com.bryanreinero.util.Result;
import com.mongodb.DBAddress;
import com.mongodb.DBObject;

import java.util.Map;

/**
 * This class represents an individual, concrete implementation
 * of the an operation
 * Created by breinero on 9/27/15.
 */
public class MongoInsert implements Operation {


	/*
	* The operation descriptor  Object
	* has a timeout from which it should consider it's request failed
	*/

    private final OperationDescriptor descriptor;
    private final DBObject object;
    private int attempts = 0;


    // the operation need
    public MongoInsert( OperationDescriptor d, DBObject o ) {
        this.descriptor = d;
        this.object = o;
    }

    @Override
    public Object call() throws Exception {
        attempts++;
        Result r = null;

        try ( Interval i = samples.set(name) ) {
            r = dao.execute(request);
        }
        return r;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Operation getRetry() {
        return null;
    }

    @Override
    public void complete() {

    }
}
