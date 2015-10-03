package com.bryanreinero.util;

import com.bryanreinero.firehose.dao.DataAccessObject;
import com.bryanreinero.firehose.metrics.SampleSet;

/**
 * Created by breinero on 9/26/15.
 */
public class OperationDescriptor {
    private final String name;
    private final SampleSet samples;
    private final DataAccessObject dao;

    /**
     *
     * @param n the name of the operation
     * @param s the SampleSet to record performance metrics
     * @param dao the DataAccesssObject interfacing with the datastore
     */
    public OperationDescriptor( String n,  SampleSet s, DataAccessObject dao ) {
        this.name = n;
        this.samples = s;
        this.dao = dao;
    }


    public String getName() {
        return name;
    }
}


