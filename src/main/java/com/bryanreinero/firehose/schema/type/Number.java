package com.bryanreinero.firehose.schema.type;

public interface Number {
	
	public static enum Type {
		Counting, Odd, Even, Natural;
	};
	
	public boolean validate( Double d );
}
