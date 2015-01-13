package com.bryanreinero.firehose;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import org.bson.types.ObjectId;

public abstract class Transformer <V extends Object> {

    public abstract V transform( String value );

    private static final Map<String, Transformer> transformers;

    private Transformer(){};
    
    // TODO: Make support all BSON types 
    public static final String TYPE_OBJECT_ID = "objectid";
    public static final String TYPE_STRING = "string";
    public static final String TYPE_INT = "int";
    public static final String TYPE_FLOAT = "float";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_DATE = "date";
    public static final String TYPE_BINARY = "binary";
    
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
                    Integer i;
                    try {
                        i = new Integer( value );
                    } catch (NumberFormatException e) {
                        i = new Integer( 0 );
                    }
                    return i;
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
                    Float f;
                    try {
                        f = new Float( value );
                    } catch (NumberFormatException e) {
                        f = new Float( 0 );
                    }
                    return f;
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

        transformers.put( TYPE_BOOLEAN ,
            new Transformer <Boolean> () {
                @Override
                public Boolean transform( String value ) {
                    return new Boolean( value );
                }
                
                @Override
                public String toString() {
                	return TYPE_BOOLEAN;
                }
            }
        );

        transformers.put( TYPE_DATE ,
            new Transformer <Date> () {
                @Override
                public Date transform( String value ) {
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy.MM.dd");
                    Date date;
                    try {
                        date = fmt.parse(value);
                        //System.out.println(date);
                        //System.out.println(fmt.format(date));
                        return (date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return (new Date(0));
                    }
                }
                
                @Override
                public String toString() {
                	return TYPE_DATE;
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
