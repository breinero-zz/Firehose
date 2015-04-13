package com.bryanreinero.firehose.schema.serializer;

import java.util.Map;
import java.util.Set;

import com.bryanreinero.firehose.schema.DoubleInterval;
import com.bryanreinero.firehose.schema.Interval;
import com.bryanreinero.firehose.schema.SchemaDescriptor;
import com.bryanreinero.firehose.schema.FieldDescriptor;
import com.bryanreinero.firehose.schema.SchemaDescriptor.Validation;
import com.bryanreinero.firehose.schema.StringInterval;
import com.bryanreinero.firehose.schema.Visitor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;



public class Serializer implements Visitor {

	public final static String version = "0.0.0";
	
	StringBuffer buf = new StringBuffer( "{ 'version': "+version );
	
	public String serialize( SchemaDescriptor descriptor ) {
		
		buf.append(", 'namespace': \""+descriptor.getNamespace() +"\"");
		buf.append(", 'name': \""+descriptor.getName() +"\" ");
		buf.append(", 'fields': { ");
		
		boolean first = true;
		for( FieldDescriptor d : descriptor.getFieldDescriptors() ) {
			if( first ) 
				first = false;
			else 
				buf.append(", " );
			d.accept(this);
		}
		buf.append(" }");
		return buf.toString();
	}

	@Override
	public void visit ( FieldDescriptor d ) {
		
		buf.append( "'name': \""+d.getName()+"\"" );
		buf.append( ", 'required': "+d.isRequired() );

		Set<Interval> is = d.getIntervals();
		if( !is.isEmpty() ) {
			buf.append(", 'ranges': [ ");
			boolean first = true;
			
			for( Interval i : d.getIntervals() ) {
				if( first ) 
					first = false;
				else 
					buf.append(", " );
				i.accept(this);

			}
			
			buf.append(" ]" );
		}
		buf.append(" }");
	}

	@Override
	public void visit( StringInterval i ) {
		buf.append( "{ 'type': \"\"" );
		buf.append( ", 'regex': \""+i.getRegex()+"\"" );
		buf.append(" }");
	}
	
	public static void main ( String[] args ) {
		SchemaDescriptor sd = new SchemaDescriptor( "descriptor", "com.bryanreinero.schema" );
		FieldDescriptor descriptor = new FieldDescriptor( "version", true );
		StringInterval interval = new StringInterval( "\\d+\\.\\d+\\.\\d+" );
		descriptor.setInterval( interval );
		sd.SetDescriptor(descriptor);
		
		DBObject object = new BasicDBObject();
		object.put("version", "0.0.0");
		
		Validation v = sd.validate( (Map<String, Object>)object );
		
		if ( !v.valid )
			System.out.print( "INVALID: " );
		
		System.out.println( v.comment );
		
		Serializer s = new Serializer();
		System.out.println( s.serialize(sd) );
	}

	@Override
	public void visit(DoubleInterval v) {
		buf.append("{ 'min': "+v.getMin()+", max: "+v.getMax()+" }");
	}
}	
