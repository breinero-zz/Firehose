package com.bryanreinero.firehose.dao.mongo;

/**
 * Created by brein on 12/31/2015.
 */
public abstract class MongoOperation {

    private String namespace;
    private
    enum TYPE {
        insert, update, query, delete, command
    }

    public class Insert {

    }
}
