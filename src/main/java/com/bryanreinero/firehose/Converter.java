package com.bryanreinero.firehose;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.bryanreinero.firehose.Transformer.Type;
import com.mongodb.DBObject;
import com.mongodb.BasicDBObject;

public class Converter {
	
	private String delimiter = ",";
	
	private static final String fieldNameSeparator = "\\.";
	private static final Pattern arrayElemPosition = Pattern.compile( "^\\$(\\d+)$" );

    private final List<SimpleEntry<String, Transformer>> transforms
        = new ArrayList<SimpleEntry<String, Transformer>>();
    
    public Converter(){};

    public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

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
    	
    	String[] values = line.split( String.valueOf( delimiter ) );
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
    
    public static void convert( Map<String, Object> document, String name, Type type, String value ) {	
    	Object v = Transformer.getTransformer(type.getName()).transform(value);
        nest( document, name.split( fieldNameSeparator ), 0, v );
    }
    
    private static void nest( Object object, String[] prefix, int i, Object value ) {
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
    			
    			ArrayList array = ((ArrayList)object);
    		
    			// frontfill if we are getting array elements out of order
    			Integer index = Integer.parseInt(m.group(1));
    			while ( index >= array.size() )
    				array.add(null);
    		
    			// value is an array element 
    			array.set(index, value);
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
    	
    	String testString = "37.5,-122.2,Slartibartfast,\"Magrethea, Center of\",5676";

    	Map<String, String> header = new LinkedHashMap<String, String>();
    	
    	header.put("geometry.coordinates.$1", Transformer.TYPE_DOUBLE );
    	header.put("geometry.coordinates.$0", Transformer.TYPE_DOUBLE );
    	header.put("user.name", Transformer.TYPE_STRING );
    	header.put("user.address", Transformer.TYPE_STRING );
    	header.put("_id", Transformer.TYPE_INT );
    	Converter c = new Converter(header, "(?!\\B\"[^\"]*),(?![^\"]*\"\\B)" );
    	DBObject obj =  c.convert( testString ) ;
    	
    	
    	System.out.println( obj );
    	
    	
    	Map<String, Object> someDoc = new BasicDBObject();
    	Converter.convert(someDoc, "geometry.coordinates.$1", Type.getType("double"), "37.5" );
    	Converter.convert(someDoc, "geometry.coordinates.$0", Type.getType("double"), "-122.2" );
    	Converter.convert(someDoc, "user.name", Type.getType("string"), "Slartibartfast" );
    	Converter.convert(someDoc, "user.address", Type.getType("string"), "\"Magrethea, Center of\"" );
    	Converter.convert(someDoc, "_id", Type.getType("int"), "5676" );
    	System.out.println(someDoc);
    }
}