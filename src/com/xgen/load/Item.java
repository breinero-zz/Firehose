package com.xgen.load;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
* Immutable Object
*/
public class Item {
    
    public static final String A_FIELDNAME  = "a";
    public static final String B_FIELDNAME  = "b";
    public static final String C_FIELDNAME  = "c";
    
    private final int a;
    private final String b;
    private final int c;
    
    public Item( int a, String b, int c ) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public DBObject toDBObject () {
        DBObject object = new BasicDBObject();
        
        object.put( A_FIELDNAME, a );
        object.put( B_FIELDNAME, b );
        object.put( C_FIELDNAME, c );

        return object;
    }
}