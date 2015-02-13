package com.bryanreinero.util;

public interface WorkerPoolMBean {
	public int getNumThreads();
	public void start( int count );
	public void stop();
}
