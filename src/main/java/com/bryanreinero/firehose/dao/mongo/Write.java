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
 * Created by breinero on 10/11/15.
 */
public class Write extends Operation {

    private final MongoCollection<Document> collection;
    private final SampleSet samples;
    private final Document document;

    public Write( String name, Document d, MongoCollection<Document> c, SampleSet s ) {
        super( name );
        document = d;
        collection = c;
        samples = s;
    }

    @Override
    public Result call() throws Exception {
        incAttempts();

        Result r = new Result();

        try ( Interval i = samples.set( getName() ) ) {
            collection.insertOne( document );

        }
        catch ( MongoWriteException mwe ) {
            WriteError error = mwe.getError();
            r.setFailed( error.getMessage() );

            if( error.getCategory().equals( ErrorCategory.DUPLICATE_KEY ) )
                // failed, not to be attempted again
                r.setNextAttempt( null );

            if (error.getCategory().equals(ErrorCategory.EXECUTION_TIMEOUT)) {
                // May be eligible for a retry

                RetryPolicy policy = getRetryPolicy();

                if ( policy != null && getAttempts() < policy.getMaxRetries() )
                    r.setNextAttempt( policy.getRetry( this ) );
            }

            if (error.getCategory().equals( ErrorCategory.UNCATEGORIZED ) )
                r.setNextAttempt( null );


        } catch (MongoWriteConcernException mwce) {
            com.mongodb.bulk.WriteConcernError error;
            error = mwce.getWriteConcernError();
            BsonDocument details = error.getDetails();

        } catch ( MongoTimeoutException mte ) {
            r.setFailed();
            RetryPolicy policy = getRetryPolicy();

            if ( policy != null ) {
                RetryRequest retry =  policy.getRetry( this );
                if ( retry != null )
                    retry.setCallable( this );

                r.setNextAttempt( retry );
            }

        } catch (MongoException me) {

            //TODO: something betta than this
            me.printStackTrace();
        }
        return r;
    }

}
