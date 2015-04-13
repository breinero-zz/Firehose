package com.bryanreinero.firehose.schema;

public interface Interval <T> extends Visitable {
	public boolean inRange( T t);
}
