package com.bryanreinero.firehose.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPool {

	private final ExecutorService es;
	private final CompletionService<Result> cs;
	
	public ThreadPool(int t) {

		es = Executors.newFixedThreadPool( t );
		cs = new ExecutorCompletionService<Result>( es );

	}
	
	public Result submitTask( Callable<Result> task ) {
		
		cs.submit(task);

        Result r = null;
		try {
            Future<Result> future = cs.take();
            r = future.get();

		} catch (InterruptedException ie ) {
			// gotta figure out what to do here
			ie.printStackTrace();

		} catch ( ExecutionException e) {
			e.printStackTrace();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
        return r;
	}

    public static void main ( String[] args ) {
        ThreadPool p = new ThreadPool( 1 );

       Result result =  p.submitTask(new Callable<Result>() {
            @Override
            public Result call() throws Exception {
                System.out.println("Executing my work");

                List<String> messages = new ArrayList<String>();
                messages.add("success");
                Result r = new Result( false );
                r.setResults( messages );
                return r;
            }
        });

        Iterable it = result.getResults();
        for ( Object o : it )
            System.out.println( o.toString() );

    }
}