package com.bryanreinero.firehose.dao;

/**
 * Created by brein on 1/3/2016.
 */
public class DataStore {

    public enum Type {
        unknown("unknown"), mongodb ( "mongodb" );

        private final String name;

        Type( String name ) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static Type getType(String s) {
            if( s.equals( mongodb.toString() ))
                return mongodb;

            return unknown;
        }
    }

    private final String name;
    private final String application;
    private final String uri;
    private final Type type;

    public DataStore( String name, String app, String uri, Type type ) {
        this.name = name;
        this.application = app;
        this.uri = uri;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getApplication() {
        return application;
    }

    public String getUri() {
        return uri;
    }

    public Type getType() {
        return type;
    }
}
