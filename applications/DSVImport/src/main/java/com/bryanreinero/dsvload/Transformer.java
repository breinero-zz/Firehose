package com.bryanreinero.dsvload;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

public abstract class Transformer <V extends Object> {

    public abstract V transform( String value );

    private static final Map<String, Transformer> transformers;

    private Transformer(){};
    
    // TODO: Make support all BSON types 
    public static final String Type_Array = "array";
    public static final String TYPE_OBJECT_ID = "objectid";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_INT = "int";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_BINARY = "binary";
    public static final String TYPE_Object = "object";
    public static final String TYPE_Date = "date";
    
    public static enum Type {
    	ArrayType( Type_Array ),
    	Object_Id(TYPE_OBJECT_ID), 
    	StringType(TYPE_STRING), 
    	IntType(TYPE_INT), 
    	FloatType(TYPE_FLOAT),
    	DoubleType(TYPE_DOUBLE),
    	BinaryType(TYPE_BINARY),
    	ObjectType(TYPE_Object),
        DateType(TYPE_Date);
    	
    	private final String name; 
    	private Type ( String name ) { this.name = name; }  
    	public String getName() { return name; }
    	
    	public static Type getType( String type ) {
    		if( type.compareTo(TYPE_OBJECT_ID) == 0 )
    			return Object_Id;
    		if( type.compareTo(TYPE_STRING) == 0 )
    			return StringType;
    		if( type.compareTo(TYPE_INT) == 0 )
    			return IntType;
    		if( type.compareTo(TYPE_FLOAT) == 0 )
        			return FloatType;
    		if( type.compareTo(TYPE_DOUBLE) == 0 )
    			return DoubleType;
    		if( type.compareTo(TYPE_BINARY) == 0 )
    			return BinaryType;
    		if( type.compareTo(TYPE_Object) == 0 )
    			return BinaryType;
            if( type.compareTo(TYPE_Date) == 0 )
                return DateType;
    		return null;
    	}
    }
    
    static {
        transformers = new HashMap<String, Transformer>();

        transformers.put( TYPE_OBJECT_ID,
            new Transformer <ObjectId> () {
        	
        		private final Pattern oIdRegex = Pattern.compile("^ObjectId\\(\"([^\"]+)\"\\)");
        		
                @Override
                public ObjectId transform( String input ) {
                	String value;
                	Matcher matcher = oIdRegex.matcher(input);
                	if ( matcher.matches() ) 
                		value = matcher.group(1);
                	else
                		value = input;
                	
                    return new ObjectId( value.replaceAll("\"", "") );
                }
                
                @Override
                public String toString() {
                	return TYPE_OBJECT_ID;
                }
            }
        );
        
        transformers.put( TYPE_STRING ,
            new Transformer <String> () {
                @Override
                public String transform( String value ) {
                    return value ;
                }
                
                @Override
                public String toString() {
                	return TYPE_STRING;
                }
            }
        );
        
        transformers.put( TYPE_INT ,
            new Transformer <Integer> () {
                @Override
                public Integer transform( String value ) {
                    return new Integer( value );
                }

                @Override
                public String toString() {
                	return TYPE_INT;
                }
            }
        );
        
        transformers.put(  TYPE_FLOAT ,
            new Transformer <Float> () {
                @Override
                public Float transform( String value ) {
                    return new Float( value );
                }
                
                @Override
                public String toString() {
                	return TYPE_FLOAT;
                }
            }
        );
        
        transformers.put( TYPE_DOUBLE ,
            new Transformer <Double> () {
                @Override
                public Double transform( String value ) {
                    return new Double( value );
                }
                
                @Override
                public String toString() {
                	return TYPE_DOUBLE;
                }
            }
        );

        transformers.put( TYPE_Date ,
                new Transformer <Date> () {
                    @Override
                    public Date transform( String value ) {
                        return new Date( Long.parseLong( value )  * 1000 );
                    }

                    @Override
                    public String toString() {
                        return TYPE_Date;
                    }
                }
        );
    }

    public static Transformer getTransformer( String type ) {
        if( ! transformers.containsKey( type ) )
            throw new IllegalArgumentException( "Unsupported type: "+type);

        return transformers.get( type );
    }
    
    public static void main( String[] args ) {
    	String objectStr = "ObjectId(\"53ffbf464e8adce448d620ac\")";
    	
    	Pattern oIdRegex = Pattern.compile("^ObjectId\\(\"([^\"]+)\"\\)");
    	Matcher matcher = oIdRegex.matcher(objectStr);
    	if ( matcher.matches() ) 
    		System.out.println( matcher.group(1) );
    		
    	Object obj = Transformer.getTransformer(TYPE_OBJECT_ID).transform( objectStr );
    	
    	System.out.println( obj );
    }
}