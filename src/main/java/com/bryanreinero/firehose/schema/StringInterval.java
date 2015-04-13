package com.bryanreinero.firehose.schema;

import java.util.regex.Pattern;

import com.bryanreinero.firehose.schema.type.StringType;

public class StringInterval implements Interval<String> {

	private final Pattern pattern ;
	public final Type type = new StringType();

	public StringInterval( String regex ) {
		pattern = Pattern.compile(regex );
	};

	@Override
	public boolean inRange(String t) {
		return pattern.matcher( t ).matches();
	}

	public String getRegex() { 
		return pattern.pattern();
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
