package com.bryanreinero.util;


import com.bryanreinero.util.retry.RetryQueue;

import java.util.concurrent.*;

public class ThreadPool {

	private final ExecutorService es;
	private final CompletionService<Result> cs;
    private final RetryQueue queue;
	
	public ThreadPool(int t) {

		es = Executors.newFixedThreadPool( t );
		cs = new ExecutorCompletionService<Result>( es );

        queue = new RetryQueue( this );
        queue.run();
	}
	
	public void submitTask( Callable<Result> task ) {
		
		cs.submit(task);

		Future<Result> future = null;
		try {
			future = cs.take();
			Result r = future.get();

			if( r.hasFailed() ) {

                if ( r.getNextAttempt() != null )
                    queue.put( r.getNextAttempt() );

                else
                    System.out.println("Operation " + r.toString() + " Failed. " + r.getMessage());
            }
			
		} catch (InterruptedException ie ) {
			// gotta figure out what to do here
			ie.printStackTrace();

		} catch ( ExecutionException e) {
			e.printStackTrace();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		finally {
            //TODO something for real


            System.out.println( "task.complete()" ) ;
		}
	}
}