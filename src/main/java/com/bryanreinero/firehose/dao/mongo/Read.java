package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.firehose.metrics.Interval;
import com.bryanreinero.util.Operation;
import com.mongodb.*;

/**
 * Created by breinero on 10/13/15.
 */
public class Read <T> extends Operation  {

    private final MongoDAO descriptor;
    private final T query;

    public Read( T query, MongoDAO descriptor ) {

        super( descriptor );
        this.descriptor = descriptor;
        this.query = query;
    }

    @Override
    public Iterable<T> call() throws Exception {
        incAttempts();

        Iterable<T> it = null;
        try ( Interval i = samples.set( getName() ) ) {
            it = descriptor.getCollection().find(query.getClass());

        } catch (MongoTimeoutException mte) {
            AttemptRetry();

        } catch ( MongoQueryException mqe ) {
            //TODO: something betta than this
            mqe.printStackTrace();
        }
        catch (MongoException me) {

            //TODO: something betta than this
            me.printStackTrace();
        }
        return it;
    }
}
