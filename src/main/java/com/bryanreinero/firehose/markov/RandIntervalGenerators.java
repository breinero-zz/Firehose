package com.bryanreinero.firehose.markov;

import java.util.Map;
import java.util.Random;

/**
 * Created by bryan on 10/22/15.
 */
public class RandIntervalGenerators {

    private static final Random rand = new Random();
    static final String key = "key";
    static final String min = "min";
    static final String max = "max";
    static final String regex = "regex";

    private static enum Type {
        intType("int"),
        longType("long"),
        floatType("float"),
        doubleType("double"),
        stringType( "string" ),
        boolType( "boolean" ),
        unknown("");

        private final String type;

        Type( String type ) {
            this.type = type;
        }


        static Type getType( String id ) {

            for ( Type type : Type.values() ) {
                if( type.type.equals( id ) )
                    return type;
            }
            return unknown;
        }

    }

    private static RandomInterval getRandomIntervalGenerator ( final Map<String, String> spec ) {

        switch ( Type.getType( spec.get( key ) ) ) {
            case intType:
                return new RandomInterval<Integer>() {
                    private final int lowerBound = Integer.parseInt( spec.get( min ) );
                    private final int upperBound = Integer.parseInt( spec.get( max ) );

                    public Integer getNext(){
                        int i;
                        do {
                            i = rand.nextInt(upperBound);
                        } while ( i < lowerBound );
                        return new Integer( i );
                    }
                };

            case longType:
                return new RandomInterval<Long>() {
                    private final long lowerBound = Long.parseLong(spec.get(min));
                    private final long upperBound = Long.parseLong(spec.get(max));

                    public Long getNext(){
                        Long i;
                        do {
                            i = new Long( rand.nextLong() );
                        } while ( i < lowerBound );
                        return new Long( i );
                    }
                };

            case floatType:
                return new RandomInterval<Float>() {
                    private final float lowerBound = Float.parseFloat(spec.get(min));
                    private final float upperBound = Float.parseFloat(spec.get(max));

                    public Float getNext(){
                        Float i;
                        do {
                            i = rand.nextFloat();
                        } while ( i < lowerBound );
                        return new Float( i );
                    }
                };

            case doubleType:
                return new RandomInterval<Double>() {


                    private final Double lowerBound = Double.parseDouble(spec.get(min));
                    private final Double upperBound = Double.parseDouble(spec.get(max));

                    @Override
                    public Double getNext(){
                        Double i;
                        do {
                            i = rand.nextDouble();
                        } while ( i < lowerBound );
                        return i;
                    }
                };

            case stringType:
                return new RandomInterval<String>() {
                    private final String expression = spec.get(regex);

                    public String getNext(){
                        // Random String should be based on regex
                        // http://code.google.com/p/xeger

                        String s =  expression;
                        return s;
                    }
                };
            case boolType:
                break;
            case unknown:
                throw new IllegalArgumentException( "Unknown type: "+spec.get( key ) );

        }

        return null;
    }


    /*
    return new RandGenerator<type>() {
                    private final type min = Float.parsetype(spec.get(min));
                    private final type max = Float.parsetype(spec.get(max));

                    public type getNext(){
                        int i;
                        do {
                            i = rand.nexttype(max);
                        } while ( i < min );
                        return new type( i );
                    }
                };
     */

}
