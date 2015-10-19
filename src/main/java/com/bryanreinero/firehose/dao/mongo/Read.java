package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.metrics.SampleSet;
import com.bryanreinero.util.Operation;
import com.bryanreinero.util.Result;
import com.bryanreinero.util.RetryPolicy;
import com.bryanreinero.util.RetryRequest;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.Document;

/**
 * Created by breinero on 10/13/15.
 */
public class Read extends Operation {

    private final Document query ;
    private final MongoCollection<Document>  collection;
    private final Document query;
    private final SampleSet samples;

    public Read( String name, Document query, MongoCollection<Document> c, SampleSet s ) {
        super(name);
        this.query = query;
        collection = c;
        samples = s;
    }

    @Override
    public Result call() throws Exception {
        incAttempts();

        Result r = new Result();

        try (Interval i = samples.set(getName())) {
            collection.find( query );

        } catch (MongoTimeoutException mte) {
            r.setFailed();
            RetryPolicy policy = getRetryPolicy();

            if (policy != null) {
                RetryRequest retry = policy.getRetry(this);
                if (retry != null)
                    retry.setCallable(this);

                r.setNextAttempt(retry);
            }

        } catch ( MongoQueryException mqe ) {

        };
        catch (MongoException me) {

            //TODO: something betta than this
            me.printStackTrace();
        }
        return r;
    }

}
