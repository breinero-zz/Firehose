package com.bryanreinero.util;


import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BetterWorkPool {
	
	private int NTHREADS = 1;
	private final ExecutorService es;
	private final CompletionService<Operation> cs;

	/*
	Should this class have a DelayQueue for retries?
	 */
	
	public BetterWorkPool() {
		es = Executors.newFixedThreadPool(NTHREADS);
		cs = new ExecutorCompletionService<Operation>( es );
	}
	
	public void submitTask( Operation task ) {
		
		es.submit( task );

		try {
			Future<Operation> future = cs.take();
			Object o = future.get();

			
		} catch (InterruptedException ie ) {

			if( !task.isCancelled() ) {

				Operation retry = null;
				if ( ( retry = task.getRetry() ) != null )
					submitTask(retry);
			}

		} catch ( ExecutionException e) {
			if ( future != null )
				e.printStackTrace();
		} finally {
			task.complete();
		}
	}
}

/*
 * OperationImplementation has a retry strategy
 * RetryStrategy is of 
 */