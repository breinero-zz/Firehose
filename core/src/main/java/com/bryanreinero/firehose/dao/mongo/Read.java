package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.firehose.util.Operation;
import com.bryanreinero.firehose.util.Result;
import com.mongodb.MongoException;
import com.mongodb.MongoQueryException;
import com.mongodb.MongoTimeoutException;
import org.bson.Document;

/**
 * Created by breinero on 10/13/15.
 */
public class Read extends Operation  {

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

        try ( Interval i = samples.set( getName() ) ) {
            r.setResults( descriptor.getCollection().find( query ) );

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
