package com.bryanreinero.firehose.schema;

public interface Visitable {
	public void accept( Visitor v );
}
