package com.bryanreinero.firehose.metrics;

import com.bryanreinero.firehose.util.Result;
import com.bryanreinero.firehose.util.WorkerPool;
import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * Created by brein on 6/15/2016.
 */
public class WorkerPoolTest {

    WorkerPool p = new WorkerPool( 1 );

    @Test
    public void testPool () {

        final String resultS = "success";
        final String message = "a test message";

        Result<String> result =  p.submitTask( new Callable<Result<String>>() {
            @Override
            public Result<String> call() throws Exception {

                Result r = new Result( false );
                r.setMessage( message );
                r.setResults( resultS );
                return r;
            }
        });

        assert( !result.hasFailed() );
        assert( result.getResults().equals( resultS ) );
        assert( result.getMessage().equals( message ) );
    }

    @Test
    public void PolyMorphicResult () {

        final String resultS = "success";
        final Integer integer = 1;

        Result<String> result =  p.submitTask(
                new Callable<Result<String>>() {
                    @Override
                    public Result<String> call() throws Exception {

                        Result r = new Result( false );
                        r.setMessage( "String type" );
                        r.setResults( resultS );
                        return r;
                    }
                }
        );

        assert( !result.hasFailed() );
        assert( result.getResults().equals( resultS ) );
        assert( result.getMessage().equals( "String type" ) );

        Result<Integer> resultInt =  p.submitTask(
                new Callable<Result<Integer>>() {
                    @Override
                    public Result<Integer> call() throws Exception {

                        Result r = new Result( false );
                        r.setMessage( "Integer type" );
                        r.setResults( integer );
                        return r;
                    }
                }
        );

        assert( !resultInt.hasFailed() );
        assert( resultInt.getResults().equals( integer ) );
        assert( resultInt.getMessage().equals( "Integer type" ) );
    }
}
