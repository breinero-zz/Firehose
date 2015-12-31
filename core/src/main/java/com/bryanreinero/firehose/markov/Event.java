package com.bryanreinero.firehose.markov;

public class Event implements Comparable<Event> {

	private final String id;
	private final Outcome outcome;
	private float probability;

	public Event(String id, float p, Outcome o) {
		if (id == null)
			throw new IllegalArgumentException("Outcome id can't be null");
		if (p < 0 || p > 1)
			throw new IllegalArgumentException(
					"Probability must be a value > 0 and < 1");
		if (o == null)
			throw new IllegalArgumentException("Outcome callback can't be null");
		this.id = id;
		this.probability = p;
		this.outcome = o;
	}

	public String getId() {
		return id;
	}

	public float getProbability() {
		return probability;
	}

	public void setProbability(float probability) {
		this.probability = probability;
	}

	public void getOutcome() {
		outcome.execute();
	}

	@Override
	public int compareTo(Event rival) {

		if (rival.getProbability() > this.probability)
			return -1;

		if (rival.getProbability() < this.probability)
			return 1;

		return 0;
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Event && ((Event) o).getId().equals(this.id));
	}
	
	@Override
	public String toString () {
		return "{ id: "+id+", probability: "+probability+" outcome: "+outcome+" }";
	}
}
