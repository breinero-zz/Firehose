package com.bryanreinero.firehose;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

public class Converter {
	
	private String delimiter;
	
	private static final String fieldNameSeparator = "\\.";
	private static final Pattern arrayElemPosition = Pattern.compile( "^\\$\\d+$" );

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
            
            nest( document, fieldName.split( fieldNameSeparator ), 0, value );
        }
        return document;
    }
    
    private void nest( Object object, String[] prefix, int i, Object value ) {
    	String name = prefix[i];
    	Matcher m;
    	
    	if ( i < prefix.length - 1  ) {

    		// casting hell
    		DBObject parent = (DBObject)object;
    		Object obj = parent.get( name );
    		
    		if ( obj == null ) {
    			// look ahead to see if this is an array
    			m = arrayElemPosition.matcher( prefix[ i + 1 ] );
    			if( m.matches() ) 
    				obj = new ArrayList();

    			else
    				obj = new BasicDBObject();

    			parent.put( name, obj );
    		}
    		
    		nest( obj, prefix, ++i, value );
    	} else {
    		// check if this is an array element
    		m = arrayElemPosition.matcher( name );
    		if ( object instanceof ArrayList  && m.matches() ) {
    			// value is an array element 
    			((ArrayList)object).add(value);
    		}
    		else {
    			((DBObject)object).put( name, value);
    		}
    	}
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
    
    public static void main( String[] args ) {

    	Map<String, String> header = new LinkedHashMap<String, String>();
    	
    	header.put("root.scores.$0", Transformer.TYPE_INT );
    	header.put("root.scores.$1", Transformer.TYPE_INT );
    	header.put("root.user.name", Transformer.TYPE_STRING );
    	header.put("root.user.address", Transformer.TYPE_STRING );
    	header.put("root.user.id", Transformer.TYPE_INT );
    	Converter c = new Converter(header, ",");
    	DBObject obj =  c.convert("98,42,bryan,Magrethea,5676") ;
    	System.out.println( obj );
    }
}