package com.bryanreinero.firehose;

import java.util.AbstractMap.SimpleEntry;
import java.util.Deque;
import java.util.List;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

public class Converter {
	
	private String delimiter;
	
	private static final CharSequence namspace_delimeter = ".";

    private final List<SimpleEntry<String, Transformer>> transforms
        = new ArrayList<SimpleEntry<String, Transformer>>();

    public Converter ( Map<String, String> header, String delimiter ) {
        
        this.delimiter = delimiter;

        if( header == null || header.isEmpty() )
            throw new IllegalArgumentException("Bad header");
        
        for( Map.Entry<String, String> entry : header.entrySet() )
            this.addField( 
                entry.getKey(), 
                Transformer.getTransformer( entry.getValue() )
            );
    }
    
    /**
     * 
     * @param name
     * @param transformer
     */
    public void addField( String name, Transformer transformer ) {
        transforms.add(
            new SimpleEntry( name, transformer )
        );
    }

    public DBObject convert( String line ) {
    	
    	String[] values = line.split( delimiter );
    	DBObject document = new BasicDBObject();
    	
        if ( values.length != transforms.size() )
            throw new IllegalArgumentException ( 
                        "Number of input fields != "+transforms.size() 
                    );

        for( int i = 0; i < transforms.size(); i++ ) {
            SimpleEntry transformKV = transforms.get(i);
            	
            String fieldName = ((String)transformKV.getKey());
            Object value = ((Transformer)transformKV.getValue()).transform( values[i] );
            
            document.put( fieldName, value );
        }
        return document;
    }
    
    @Override
    public String toString() {
    	StringBuffer buf =  new StringBuffer( "{ fields: [");
    	
    	boolean first = true;
    	
    	for ( Entry entry : transforms ) {
    		if( !first )
    			buf.append(",");
    		else
    			first = false;
    		buf.append(" { field: \""+entry.getKey()+"\", type: "+entry.getValue().toString()+" }" );
    	}
    	buf.append(" ] }");
		return buf.toString();
    }
}