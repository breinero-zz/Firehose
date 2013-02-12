package com.xgen.load;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
* Immutable Object
*/
public class Item {
    
    //Column (key) names for the STR BOARD
    public static final String NBR_FIELDNAME  = "_nbr";
    public static final String UPC_FIELDNAME  = "upc";
    public static final String LOC_NBR_FIELDNAME  = "loc_nbr";
    public static final String AVAIL_TO_SELL_FIELDNAME  = "avail_to_sell";
    public static final String NOT_AVAIL_THLD_QTY_FIELDNAME  = "not_avail_thld_qty";
    public static final String FIIS_ANS_CODE_FIELDNAME  = "fiis_ans_code";
    public static final String NOT_AVAIL_QTY_SITE_FIELDNAME  = "not_avail_qty_site";
    public static final String NOT_AVAIL_QTY_ESEND_FIELDNAME  = "not_avail_qty_esend";
    public static final String IN_ROUTE_CODE_FIELDNAME  = "in_route_code";
    public static final String PHYS_ON_HAND_FIELDNAME  = "phys_on_hand";
    public static final String CEB_LAST_UPDATED_TS_FIELDNAME  = "ceb_last_updated_ts";
    public static final String SPACE_UPDATED_TS_FIELDNAME  = "space_updated_ts";
    public static final String TXN_ID_FIELDNAME = "txn_id";
    
    private final int NBR;
    private final String UPC;
    private final int LOC_NBR;
    private final int AVAIL_TO_SELL;
    private final int NOT_AVAIL_THLD_QTY;
    private final String FIIS_ANS_CODE;
    private final int NOT_AVAIL_QTY_SITE;
    private final int NOT_AVAIL_QTY_ESEND;
    private final int IN_ROUTE_CODE;
    private final int PHYS_ON_HAND;
    private final long CEB_LAST_UPDATED_TS;
    private final long SPACE_UPDATED_TS;
    private final int TXN_ID;
    
    public int getNBR() { return NBR; }
    
    public Item(
            int _nbr, 
            int loc_nbr,
            String upc,
            int avail_to_sell, 
            int not_avail_thld_qty, 
            String fiis_ans_code,
            int not_avail_qty_site, 
            int not_avail_qty_esend, 
            int in_route_code,
            int phys_on_hand, 
            long ceb_last_updated_ts, 
            long space_updated_ts,
            int txn_id) {
        NBR = _nbr;
        LOC_NBR = loc_nbr;
        UPC = upc;
        AVAIL_TO_SELL = avail_to_sell;
        NOT_AVAIL_THLD_QTY = not_avail_thld_qty;
        FIIS_ANS_CODE = fiis_ans_code;
        NOT_AVAIL_QTY_SITE = not_avail_qty_site;
        NOT_AVAIL_QTY_ESEND = not_avail_qty_esend;
        IN_ROUTE_CODE = in_route_code;
        PHYS_ON_HAND = phys_on_hand;
        CEB_LAST_UPDATED_TS = ceb_last_updated_ts;
        SPACE_UPDATED_TS = space_updated_ts;
        TXN_ID = txn_id;
    }

    public DBObject toDBObject () {
        DBObject object = new BasicDBObject();
        
        object.put( "_id", Long.parseLong(LOC_NBR+UPC) );
        object.put( NBR_FIELDNAME, NBR );
        object.put( UPC_FIELDNAME, UPC );
        object.put( LOC_NBR_FIELDNAME, LOC_NBR );
        object.put( AVAIL_TO_SELL_FIELDNAME, AVAIL_TO_SELL );
        object.put( NOT_AVAIL_THLD_QTY_FIELDNAME, NOT_AVAIL_THLD_QTY );
        object.put( FIIS_ANS_CODE_FIELDNAME, FIIS_ANS_CODE );
        object.put( NOT_AVAIL_QTY_SITE_FIELDNAME, NOT_AVAIL_QTY_SITE );
        object.put( NOT_AVAIL_QTY_ESEND_FIELDNAME, NOT_AVAIL_QTY_ESEND );
        object.put( IN_ROUTE_CODE_FIELDNAME, IN_ROUTE_CODE );
        object.put( PHYS_ON_HAND_FIELDNAME, PHYS_ON_HAND );
        object.put( CEB_LAST_UPDATED_TS_FIELDNAME, CEB_LAST_UPDATED_TS );
        object.put( SPACE_UPDATED_TS_FIELDNAME, SPACE_UPDATED_TS );
        object.put( TXN_ID_FIELDNAME, TXN_ID );

        return object;
    }
}