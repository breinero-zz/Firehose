package com.bryanreinero.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WorkerPool {
	
	public interface Executor {
		public void execute();
	}

	private class Worker extends Thread {

		public void run() {
            while (running.get())
                executor.execute();
		}
	}

	public void stop() {
		synchronized (running) {
			running.set(false);
			// dmf: let 'em finish; they'll figure it out
			//for (Worker worker : workers) {
			//	worker.interrupt();
			//}
		}
	}

	public void start(int numThreads ) {
		synchronized (running) {
			running.set(true);

			for (int i = 0; i < numThreads; i++) {
				Worker intake = new Worker();
				intake.start();
				workers.add(intake);
			}
		}
	}
	
	public int getNumThreads() {
		return workers.size();
	}

	private final Executor executor;
	private List<Worker> workers = new ArrayList<Worker>();
	private AtomicBoolean running = new AtomicBoolean(false);
	
	public WorkerPool ( Executor executor ) { 
		this.executor = executor;
	}
}
