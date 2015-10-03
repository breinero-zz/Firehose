package com.bryanreinero.firehose.dao.mongo;

import com.bryanreinero.util.Operation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

/**
 * Created by breinero on 9/28/15.
 */
public class DataAccess {

    public DataAccess( ExecutorService es ) {
        cs = new ExecutorCompletionService<Operation>( es );
    }

    public void execute( Operation op ) {

        es.submit( op );

        try {
            Future<Operation> future = cs.take();
            Object o = future.get();


        } catch (InterruptedException ie ) {

            if( !op.isCancelled() ) {

                Operation retry = null;
                if ( ( retry = op.getRetry() ) != null )
                    submitTask(retry);
            }

        } catch ( ExecutionException e) {
            if ( future != null )
                e.printStackTrace();
        } finally {
            op.complete();
        }
    }
}
}
