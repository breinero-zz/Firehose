package com.xgen.load;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Item {
    
    //Column (key) names for the STR BOARD
    public static final String STR_NBR = "_nbr";
    public static final String STR_UPC = "upc";
    public static final String STR_LOC_NBR = "loc_nbr";
    public static final String STR_AVAIL_TO_SELL = "avail_to_sell";
    public static final String STR_NOT_AVAIL_THLD_QTY = "not_avail_thld_qty";
    public static final String STR_FIIS_ANS_CODE = "fiis_ans_code";
    public static final String STR_NOT_AVAIL_QTY_SITE = "not_avail_qty_site";
    public static final String STR_NOT_AVAIL_QTY_ESEND = "not_avail_qty_esend";
    public static final String STR_IN_ROUTE_CODE = "in_route_code";
    public static final String STR_PHYS_ON_HAND = "phys_on_hand";
    public static final String STR_CEB_LAST_UPDATED_TS = "ceb_last_updated_ts";
    public static final String STR_SPACE_UPDATED_TS = "space_updated_ts";
    public static final String STR_TXN_ID = "txn_id";
    
    private final int STR_NBR_VAL;
    private final String STR_UPC_VAL;
    private final int STR_LOC_NBR_VAL;
    private final int STR_AVAIL_TO_SELL_VAL;
    private final int STR_NOT_AVAIL_THLD_QTY_VAL;
    private final String STR_FIIS_ANS_CODE_VAL;
    private final int STR_NOT_AVAIL_QTY_SITE_VAL;
    private final int STR_NOT_AVAIL_QTY_ESEND_VAL;
    private final int STR_IN_ROUTE_CODE_VAL;
    private final int STR_PHYS_ON_HAND_VAL;
    private final long STR_CEB_LAST_UPDATED_TS_VAL;
    private final long STR_SPACE_UPDATED_TS_VAL;
    private final int STR_TXN_ID_VAL;
    
    
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
        STR_NBR_VAL = _nbr;
        STR_LOC_NBR_VAL = loc_nbr;
        STR_UPC_VAL = upc;
        STR_AVAIL_TO_SELL_VAL = avail_to_sell;
        STR_NOT_AVAIL_THLD_QTY_VAL = not_avail_thld_qty;
        STR_FIIS_ANS_CODE_VAL = fiis_ans_code;
        STR_NOT_AVAIL_QTY_SITE_VAL = not_avail_qty_site;
        STR_NOT_AVAIL_QTY_ESEND_VAL = not_avail_qty_esend;
        STR_IN_ROUTE_CODE_VAL = in_route_code;
        STR_PHYS_ON_HAND_VAL = phys_on_hand;
        STR_CEB_LAST_UPDATED_TS_VAL = ceb_last_updated_ts;
        STR_SPACE_UPDATED_TS_VAL = space_updated_ts;
        STR_TXN_ID_VAL = txn_id;
    }

    public int getSTR_KEY_VAL() {
        return STR_NBR_VAL;
    }


    public String getSTR_UPC_VAL() {
        return STR_UPC_VAL;
    }


    public int getSTR_LOC_NBR_VAL() {
        return STR_LOC_NBR_VAL;
    }

    public int getSTR_AVAIL_TO_SELL_VAL() {
        return STR_AVAIL_TO_SELL_VAL;
    }


    public int getSTR_NOT_AVAIL_THLD_QTY_VAL() {
        return STR_NOT_AVAIL_THLD_QTY_VAL;
    }


    public String getSTR_FIIS_ANS_CODE_VAL() {
        return STR_FIIS_ANS_CODE_VAL;
    }


    public int getSTR_NOT_AVAIL_QTY_SITE_VAL() {
        return STR_NOT_AVAIL_QTY_SITE_VAL;
    }


    public int getSTR_NOT_AVAIL_QTY_ESEND_VAL() {
        return STR_NOT_AVAIL_QTY_ESEND_VAL;
    }


    public int getSTR_IN_ROUTE_CODE_VAL() {
        return STR_IN_ROUTE_CODE_VAL;
    }


    public long getSTR_PHYS_ON_HAND_VAL() {
        return STR_PHYS_ON_HAND_VAL;
    }


    public long getSTR_CEB_LAST_UPDATED_TS_VAL() {
        return STR_CEB_LAST_UPDATED_TS_VAL;
    }


    public long getSTR_SPACE_UPDATED_TS_VAL() {
        return STR_SPACE_UPDATED_TS_VAL;
    }


    public int getSTR_TXN_ID_VAL() {
        return STR_TXN_ID_VAL;
    }
    
    public DBObject toDBObject () {
        DBObject object = new BasicDBObject();
        
        object.put( "_id", Long.parseLong(STR_LOC_NBR_VAL+STR_UPC_VAL) );
        object.put( STR_NBR, STR_NBR_VAL );
        object.put( STR_UPC, getSTR_UPC_VAL() );
        object.put( STR_LOC_NBR, getSTR_LOC_NBR_VAL() );
        object.put( STR_AVAIL_TO_SELL, getSTR_AVAIL_TO_SELL_VAL() );
        object.put( STR_NOT_AVAIL_THLD_QTY, getSTR_NOT_AVAIL_THLD_QTY_VAL() );
        object.put( STR_FIIS_ANS_CODE, getSTR_FIIS_ANS_CODE_VAL() );
        object.put( STR_NOT_AVAIL_QTY_SITE, getSTR_NOT_AVAIL_QTY_SITE_VAL() );
        object.put( STR_NOT_AVAIL_QTY_ESEND, getSTR_NOT_AVAIL_QTY_ESEND_VAL() );
        object.put( STR_IN_ROUTE_CODE, getSTR_IN_ROUTE_CODE_VAL() );
        object.put( STR_PHYS_ON_HAND, getSTR_PHYS_ON_HAND_VAL() );
        object.put( STR_CEB_LAST_UPDATED_TS, getSTR_CEB_LAST_UPDATED_TS_VAL() );
        object.put( STR_SPACE_UPDATED_TS, getSTR_SPACE_UPDATED_TS_VAL() );
        object.put( STR_TXN_ID, getSTR_TXN_ID_VAL() );

        return object;
    }
}