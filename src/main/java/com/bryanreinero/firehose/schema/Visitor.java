package com.bryanreinero.firehose.schema;


public interface Visitor {
	public void visit( StringInterval v );
	public void visit( FieldDescriptor v );
	public void visit( DoubleInterval v );
}
