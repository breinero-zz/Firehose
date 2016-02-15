package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.util.Operation;
import com.bryanreinero.firehose.util.Result;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

/**
 * Created by breinero on 10/13/15.
 */
public class Read <T> extends Operation  {

    private final MongoDAO descriptor;
    private final Document query;

    public Read( Document query, MongoDAO descriptor ) {
        super( descriptor );
        this.descriptor = descriptor;
        this.query = query;
    }

    @Override
    public Result call() throws Exception {
        Result r = new Result( false );
        incAttempts();

        Iterable<T> it;
        try ( Interval i = samples.set( getName() ) ) {
            MongoCollection<T> c = descriptor.getCollection();
            it = c.find( query );
            r.setResults( it );

        } catch (MongoTimeoutException mte) {
            AttemptRetry();

        } catch ( MongoQueryException mqe ) {
            r.setFailed( "Read failed. "+mqe );
        }
        catch (MongoException me) {
            r.setFailed( "Read failed. "+me );
        }
        return r;
    }
}
