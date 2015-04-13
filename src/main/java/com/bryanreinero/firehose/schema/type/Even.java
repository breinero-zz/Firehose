package com.bryanreinero.firehose.schema.type;

public class Even implements Number {
	
	public static Number.Type type = Number.Type.Odd;
	
	@Override
	public boolean validate(Double d) {
		return ( d % 2 == 0 );
	}

}
