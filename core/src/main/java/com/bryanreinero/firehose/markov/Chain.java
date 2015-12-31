package com.bryanreinero.firehose.markov;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;


public class Chain {

	private Set<Event> events = new HashSet<Event>();

	public void setEvent( List<Event>  newEvents  ) {
		
		Map<String, Float> newProbs = new HashMap<String, Float>();
		for( Event e : newEvents ) {
			events.add(e);
			newProbs.put(e.getId(), e.getProbability());
		}
		
		setProbabilities( newProbs );
	}
	
	public void setProbabilities( Map<String, Float> candidates ) {
		
		float totalUnchangedProbablilities = 0;
		float totalDesiredProbability = 0f;	
		float firstDesiredProbability = 0;
		float S0 = 0;
		String firstEvent = null;
		
		for ( Entry <String, Float> entry : candidates.entrySet() ) {
			if( firstDesiredProbability == 0 ) {
				firstDesiredProbability = entry.getValue();
				firstEvent = entry.getKey();
			} else
				totalDesiredProbability += entry.getValue();
		}
		
		for ( Event e : events )
			if( candidates.containsKey( e.getId() ) )
				continue;
			else
				totalUnchangedProbablilities += e.getProbability();
		
		float totalScore = 0;
		Map<String, Float>scores = new HashMap<String, Float>();
		
		
		
		
		for ( Event e : events ) { 
			if( e.getId().equals(firstEvent) ) {
				
				if( totalUnchangedProbablilities == 0 ) {
					// the sum of candidate probabilities are 1
					// need to avoid dividing by 0
					S0 = candidates.get( firstEvent );
				} 
				else {
					S0 = totalUnchangedProbablilities
							/ (((1 - totalDesiredProbability) / firstDesiredProbability) - 1);
				}
				totalScore += S0;
				scores.put( e.getId(), S0);
			}
		}
		
		for ( Event e : events ) {
			if( e.getId().equals( firstEvent )  )
				continue;
			
			float score = e.getProbability();
			if( candidates.containsKey( e.getId() ) ) 
				score = ( S0 * candidates.get( e.getId() ).floatValue() ) / firstDesiredProbability ;
			
			scores.put(e.getId(), score );
			totalScore += score;
		}
		
		// normalize the scores to get actual probabilities
		float totalProb = 0;
		for ( Event e : events ) {
			float prob =  scores.get( e.getId() ) / totalScore;
			totalProb += prob;
			e.setProbability( prob );
		}
	}

	@Override
	public String toString() {
		float cumulativeProbability = 0;
		StringBuffer buf = new StringBuffer("{\n[\n");
		
		boolean first = true;
		for ( Event event : events )  {
			if( first )
				first = false;
			else
				buf.append(",\n");
			
			cumulativeProbability+= event.getProbability();
			buf.append("{ name: "+event.getId()+", probablity: "+event.getProbability()+" }" );
		}
		buf.append("\n],\n");
		buf.append("total: "+cumulativeProbability+"\n}");
		
		return buf.toString();
	}
	
	public void run( float point ) {
		float cumulativeProbability = 0;
		
		for( Event e : events ) {
			cumulativeProbability += e.getProbability();
			if( point <=  cumulativeProbability  ) {
				e.getOutcome();
				break;
			}
		}
	}
	
	public static void main( String[] args ) {	
		Chain s = new Chain();
		
		Random r = new Random();
		
//		for ( int i = 0; i < 10; i++ ) {
//			float desired =  r.nextFloat();
//			System.out.println( "Desired probabilty = "+desired);
//			s.setProbability( "Bryan", desired);
//			System.out.println(s);
//		}
//		
//		s.setProbability( "Bryan", 0f);
//		System.out.println(s);
		
		Map<String, Float> request = new HashMap<String, Float>();
		request.put("Bryan", 0.8F);
		request.put("Lyla", 0.1F);
		
		s.setProbabilities( request );
		System.out.println(s);
		
		request = new HashMap<String, Float>();
		request.put("Bryan", 0.5F);
		request.put("Lyla", 0.2F);
		request.put("Nicoletta", 0.3F);
		
		s.setProbabilities( request );
		System.out.println(s);
		
		request = new HashMap<String, Float>();
		request.put("Bryan", 0.0F);
		request.put("Lyla", 0.3F);
		
		s.setProbabilities( request );
		System.out.println(s);
		
		request = new HashMap<String, Float>();
		request.put("Bryan", 0.43F);
		
		s.setProbabilities( request );
		System.out.println(s);
		
		
		for( int i = 0; i < 10; i++ )
			s.run(r.nextFloat());
		
	}
}
