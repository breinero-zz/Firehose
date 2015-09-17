package com.bryanreinero.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.bryanreinero.firehose.dao.Operation;

public class BetterWorkPool {
	
	private int NTHREADS = 1;
	private final ExecutorService es;
	private final CompletionService<Operation> cs;
	
	public BetterWorkPool() {
		es = Executors.newFixedThreadPool(NTHREADS);
		cs = new ExecutorCompletionService<Operation>( es );
	}
	
	public void submitRunnable( Operation task ) { 
		
		es.submit(task);
		Future<Operation> future = null;
		try {
			 future = cs.take();
			Object o = future.get();
			
			// the type of exception thrown by the future
			// depends on the implementation 
			
		} catch (InterruptedException | ExecutionException e) {
			if( e instanceof InterruptedException )
				Thread.currentThread().interrupt();
			else {
				if ( future != null ) 
				e.printStackTrace();
			}
		}
	}
}

/*
 * Operation has a retry strategy
 * RetryStrategy is of 
 */