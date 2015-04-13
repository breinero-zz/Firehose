package com.bryanreinero.firehose.schema;

import java.util.HashSet;
import java.util.Set;

public class FieldDescriptor implements Visitable {
	
	private final String name;
	private final boolean required;
	private Set <Interval> intervals = new HashSet <Interval> ();
	
	public FieldDescriptor ( String name, boolean required ) {
		this.name = name;
		this.required = required;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isRequired(){ return required;  };
	
	public void setInterval( Interval e ){
		intervals.add( e );
	};
	
	public Set<Interval> getIntervals() {
		Set<Interval> is = new HashSet<Interval>();
		for( Interval i : intervals )
			is.add(i);
		
		return is;
	}
	
	public boolean validate( Object o ){
		for( Interval i : intervals )
			if( !i.inRange(o) )
				return false;
		return true;
	}

	@Override
	public void accept(Visitor v) {
		v.visit(this);
	}
}
