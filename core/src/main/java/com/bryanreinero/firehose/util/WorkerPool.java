package com.bryanreinero.firehose.util;

import java.util.concurrent.*;

public class WorkerPool <T extends Result> {

	private final ExecutorService es;
	private final CompletionService<Result> cs;
	
	public WorkerPool(int t) {
		es = Executors.newFixedThreadPool( t );
		cs = new ExecutorCompletionService<Result>( es );

	}
	
	public Result submitTask(Callable<Result> task ) {
		
		cs.submit(task);

        Result r = null;
		try {
            Future<Result> future = cs.take();
            r = future.get();

		} catch (InterruptedException |  ExecutionException e ) {
			if ( r == null )
				r = new Result( true );
			r.setFailed( e.getMessage() ) ;
		}
        return r;
	}
}