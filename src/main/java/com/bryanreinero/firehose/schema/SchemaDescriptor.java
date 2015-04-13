package com.bryanreinero.firehose.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SchemaDescriptor {
	
	private final String namespace;
	private final String name;
	private final Map <String, FieldDescriptor> descriptors = new HashMap<String, FieldDescriptor>();
	
	public class Validation {
		
		public final boolean valid;
		public final String comment;
		
		private Validation ( boolean passFail, String comment ) {
			this.valid = passFail;
			this.comment = comment;
		}
	};
	
	public SchemaDescriptor( String name, String namespace ) {
		this.name = name;
		this.namespace = namespace;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void SetDescriptor( FieldDescriptor descriptor ) {
		String key = descriptor.getName();
		descriptors.put(key, descriptor);
	}
	
	public Set<FieldDescriptor> getFieldDescriptors() {
		Set<FieldDescriptor> ds = new HashSet<FieldDescriptor>();
		for( FieldDescriptor d : descriptors.values() )
			ds.add(d);
		
		return ds;
	}
	
	public Validation validate( Map<String, Object> o ){
		Map <String, Boolean> required = new HashMap<String, Boolean>();
		
		// set up checks for required fields
		for( Entry<String, FieldDescriptor> entry : descriptors.entrySet() )
			if( entry.getValue().isRequired() )
				required.put(entry.getKey(), Boolean.FALSE );
		
		for( Entry<String, Object> entry : o.entrySet() ) {
			
			String key = entry.getKey();
			if ( descriptors.containsKey(key) )
				if( !descriptors.get(key).validate( entry.getValue() ) )
					return new Validation(false, "field \""+key+"\" "+entry.getValue()+" not in range");			
			
			// mark required field as satisfied
			if( required.containsKey( key ) )
				required.put(key, Boolean.TRUE );
				
		}
		
		// check for required fields
		for( Entry<String, Boolean> entry : required.entrySet() )
			if( ! entry.getValue() )
				return new Validation(false, "field"+entry.getKey()+" required");
		
		return new Validation(true, "Valid "+namespace+" document");
	};
	
	@Override 
	public String toString() {
		StringBuffer buf = new StringBuffer();
		return buf.toString();
	}
	
	public static void main ( String[] args ) {
		SchemaDescriptor sd = new SchemaDescriptor( "test", "com.bryanreinero.schema" );
		FieldDescriptor descriptor = new FieldDescriptor( "a", true );
		StringInterval interval = new StringInterval( "^test" );
		descriptor.setInterval( interval );
		sd.SetDescriptor(descriptor);
		
		DBObject object = new BasicDBObject();
		object.put("a", "test");
		
		Validation v = sd.validate( (Map<String, Object>)object );
		
		if ( !v.valid )
			System.out.print( "INVALID: " );
		
		System.out.println( v.comment );
	}

}
