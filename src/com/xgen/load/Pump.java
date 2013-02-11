package com.xgen.load;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Pump<T> {

    private Integer inputThreads = new Integer(0);
    private Integer outputThreads = new Integer(0);
    
    private Source<T> source;
    private Sink<T> sink;
    private ArrayBlockingQueue <T> queue;
    
    private Boolean intakeRunning = Boolean.FALSE;
    private Boolean outputRunning = Boolean.FALSE;
    
    private List<Thread> outputs = new ArrayList<Thread>();
    private List<Thread> inputs = new ArrayList<Thread>();
    
    public interface Source<T> {
        public T produce();
    }

    public interface Sink<T> {
        public void consume(T object);
    }

    class Intake implements Runnable {
        private final BlockingQueue<T> queue;
        private Source<T> source;

        Intake(BlockingQueue<T> q, Source<T> source) {
            queue = q;
            this.source = source;
        }

        public void run() {
            try {
                while (isIntakeRunning()) {
                    T item = source.produce();
                    if(item != null )
                        queue.put(item);
                    else
                        throw new InterruptedException("source produced null item");
                }
            } catch (InterruptedException ex) {
                System.out.println("Output thread interrupted. "+ ex.getMessage() );
            }        
        }
    }

    class Output implements Runnable {
        private final BlockingQueue<T> queue;
        private Sink<T> sink;

        Output(BlockingQueue<T> q, Sink<T> sink) {
            queue = q;
            this.sink = sink;
        }

        public void run() {
            try {
                while (isOutputRunning()) {
                    try {
                    sink.consume(queue.take());
                    } catch ( Exception e) {
                        throw new InterruptedException();
                    }
                }
            } catch (InterruptedException ex) {
                System.out.println("Input thread interrupted. "+ ex.getMessage() );
            }
        }
    }
    
    public  boolean isIntakeRunning() { 
        synchronized ( intakeRunning ) {
            return intakeRunning.booleanValue();
        }
    }
    
    public  boolean isOutputRunning() { 
        synchronized ( outputRunning ) {
            return outputRunning.booleanValue();
        }
    }
    
    public void stopIntake() {
        synchronized ( intakeRunning ) {
            intakeRunning = Boolean.FALSE;
        }
        for (int i = 0; i < inputs.size(); i++)
            try {
                Thread input = inputs.get(i);
                if( input.isAlive() ) input.interrupt();
                (input).join(1000000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
    
    public void startIntake() {
        
        synchronized ( intakeRunning ) {
            intakeRunning = Boolean.TRUE;
        }
        
        for( int i = 0; i < inputThreads; i++) {
           Thread thread = new Thread( new Intake( queue, source ) );
           thread.start();
           inputs.add(thread);
        }
    }
    
    public void stopOutput () {
        synchronized ( outputRunning ) {
            outputRunning = Boolean.TRUE;
        }
        
        for (int i = 0; i < outputs.size(); i++)
            try {
                Thread output = outputs.get(i);
                if( output.isDaemon() ) output.interrupt();
                (output).join(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }
    
    public void startOutput() {
        
        synchronized ( outputRunning ) {
            outputRunning = Boolean.TRUE;
        }
        
        for( int i = 0; i < outputThreads; i++) {
            Thread thread = new Thread( new Output( queue, sink ) );
            thread.start();
            outputs.add(thread);
         }
    }
    
    public int getQueueSize() {
        return ( queue.size() - queue.remainingCapacity() );
    }

    public Pump( Source<T> source, Sink<T> sink, int numIntakeThreads, int numOuttakeThreads, int queueSize ) {
        inputThreads = numIntakeThreads;
        outputThreads = numOuttakeThreads;
        this.source = source;
        this.sink = sink;
        queue = new ArrayBlockingQueue<T>(queueSize);
    }
}
