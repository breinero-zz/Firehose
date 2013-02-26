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
    
    private List<Output> outputs = new ArrayList<Output>();
    private List<Intake> intakes = new ArrayList<Intake>();
    
    private Boolean running = Boolean.FALSE;
    
    public interface Source<T> {
        public T produce();
    }

    public interface Sink<T> {
        public void consume(T object);
    }

    class Intake extends Thread {
        private final BlockingQueue<T> queue;
        private Source<T> source;

        Intake(BlockingQueue<T> q, Source<T> source) {
            queue = q;
            this.source = source;
        }

        public void run() {
            try {
                while ( true ) {
                    synchronized ( running ) { 
                        if ( !running )
                            throw new InterruptedException();
                    }
                    T item = source.produce();
                    if(item != null )
                        queue.put(item);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } 
        }
    }

    class Output extends Thread {
        private final BlockingQueue<T> queue;
        private Sink<T> sink;

        Output(BlockingQueue<T> q, Sink<T> sink) {
            queue = q;
            this.sink = sink;
        }

        public void run() {
            try {
                while ( true ) {
                    synchronized ( running ) {
                        if( !running && queue.size() == 0 )
                            break;
                    }

                    sink.consume(queue.take());
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stop () {
        synchronized ( running ) {
            running = Boolean.FALSE;
            for ( Intake intake : intakes )
                intake.interrupt();
        }
    }
    
    public void start() {
        synchronized ( running ) {
            running = Boolean.TRUE;
            
            for( int i = 0; i < outputThreads; i++) {
                Output output = new Output( queue, sink );
                outputs.add(output);
                output.start();
            }
            
            for( int i = 0; i < inputThreads; i++) {
                Intake intake = new Intake( queue, source );
                intake.start();
                intakes.add(intake);
             }
        }
    }
    
    public int getQueueSize() {
        return queue.size() ;
    }

    public Pump( Source<T> source, Sink<T> sink, int numIntakeThreads, int numOuttakeThreads, int queueSize ) {
        inputThreads = numIntakeThreads;
        outputThreads = numOuttakeThreads;
        this.source = source;
        this.sink = sink;
        queue = new ArrayBlockingQueue<T>(queueSize);
    }
}
