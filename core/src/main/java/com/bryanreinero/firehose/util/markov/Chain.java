package com.bryanreinero.firehose.util.markov;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class Chain<T> {

    private static final Random rand = new Random();
	private Set<Event<T>> events = new HashSet<Event<T>>();
	
	public void setProbabilities( Set<Event<T>> candidates ) {

		float totalDesiredProbability = 0;


        Set<Event<T>> newEvents = new HashSet<>();
		
		for ( Event e : candidates ) {
			totalDesiredProbability += e.getProbability();
            if( e.getProbability() > 0 )
                newEvents.add( e );
        }
		
		for ( Event e : events )
			if( candidates.contains( e ) )
                 continue;
            else {
                e.setProbability((1 - totalDesiredProbability) * e.getProbability());
                if( e.getProbability() > 0 )
                    newEvents.add( e );
            }

        float newTotalProb = 0;
        for ( Event e : newEvents )
            newTotalProb += e.getProbability();

        if (  newTotalProb < 1 && newTotalProb > 0 )
            for( Event e : newEvents )
                e.setProbability( e.getProbability() / newTotalProb  );

        events = newEvents;
	}

	@Override
	public String toString() {
		float cumulativeProbability = 0;
		StringBuffer buf = new StringBuffer("{\nevents: [\n");
		
		boolean first = true;
		for ( Event event : events )  {
			if( first )
				first = false;
			else
				buf.append(",\n");
			
			cumulativeProbability+= event.getProbability();
			buf.append( event );
		}
		buf.append("\n],\n");
		buf.append("total: "+cumulativeProbability+"\n}");
		
		return buf.toString();
	}
	
	public T run() {
		float cumulativeProbability = 0;

		for( Event<T> e : events ) {
			cumulativeProbability += e.getProbability();
			if( rand.nextFloat() <=  cumulativeProbability  )
				return e.getOutcome();
		}
        return null;
    }
}
