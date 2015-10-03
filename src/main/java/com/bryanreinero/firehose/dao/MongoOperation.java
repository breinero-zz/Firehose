package com.bryanreinero.firehose.dao;

import com.bryanreinero.util.Operation;
import com.bryanreinero.util.Result;

/**
 * A description of a type of specific
 * operation. A type
 * Created by breinero on 9/25/15.
 */
public class MongoOperation {



	/*
	* An OperationImplementation Object
	* has a timeout from which it should consider it's request failed
	*/

    private final String name;
    private final SampleSet samples;
    private final DataAccessObject dao;

    private final String name;
    String namespace;

    public MongoOperation( String name ) {
        this.name = name;
    }

    @Override
    public Operation getRetry(){
        return null;
    }

    @Override
    public Result call() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

    /*
    complete notify the requestor
    that the operation is finished.
    Finished could be be cause it was
    interrupted, failed or successful.
     */
    @Override
    public void complete() {};
}
