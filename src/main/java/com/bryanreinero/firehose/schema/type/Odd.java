package com.bryanreinero.firehose.schema.type;

public class Odd implements Number {
	
	public static Number.Type type = Number.Type.Odd;
	
	@Override
	public boolean validate( Double d ) {
		return ( ( d - 1D ) % 2 == 0 );
	}

}
