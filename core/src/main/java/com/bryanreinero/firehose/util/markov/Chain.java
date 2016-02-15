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
	
	public static void main( String[] args ) {	
		Chain s = new Chain();
		
		Random r = new Random();

		
		Set<Event> request = new HashSet<Event>();
		request.add(
				new Event(0.8F, new Outcome<String>() {
                    private final String name = "Bryan";
					@Override
					public Object execute() {
                        return name;
					}

					@Override
					public String getName() {
						return name;
					}
				})
		);

        request.add(
                new Event(0.1F, new Outcome<String>() {
                    private final String name = "Lyla";
                    @Override
                    public String execute() {
                        return name;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );
		
		s.setProbabilities( request );
		System.out.println(s);
		
		request = new HashSet<Event>();

        request.add(
                new Event(0.5F, new Outcome<String>() {
                    private final String name = "Bryan";
                    @Override
                    public String execute() {
                        return name;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );

        request.add(
                new Event(0.2F, new Outcome<String>() {
                    private final String name = "Lyla";
                    @Override
                    public Object execute() {
                        return name;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );

        request.add(
                new Event(0.3F, new Outcome<String>() {
                    private final String name = "Nicoletta";
                    @Override
                    public Object execute() {
                        return name;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );
		
		s.setProbabilities( request );
		System.out.println(s);
		
		request = new HashSet<Event>();

        request.add(
                new Event(0.0F, new Outcome<String>() {

                    private final String name = "Bryan";
                    @Override
                    public String execute() {
                        return name;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );

        request.add(
                new Event(0.3F, new Outcome<String>() {

                    private final String name = "Lyla";
                    @Override
                    public String execute() {
                        return name;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );

		s.setProbabilities( request );
		System.out.println(s);
		
		request = new HashSet<Event>();

        request.add(
                new Event(0.43F, new Outcome() {
                    private final String name = "Bryan";
                    @Override
                    public Object execute() {
                        return name;
                    }

                    @Override
                    public String getName() {
                        return name;
                    }
                })
        );
		
		s.setProbabilities( request );
		System.out.println(s);
		
		
		for( int i = 0; i < 10; i++ )
			s.run();
	}
}
