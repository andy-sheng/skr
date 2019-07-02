package com.module.playways.grab.room.model;

import java.io.Serializable;

public class NumericDetailModel implements Serializable {

    public final static int RNT_SUCCESS_STAND = 1;
    public final static int RNT_GET_BLIGHT = 2;
    public final static int RNT_GIFT_FLOWER = 3;
    public final static int RNT_MEILI = 4;
    public final static int RNT_COIN = 5;
    public final static int RNT_HONGZHUAN = 6;

    /**
     * numericType : 1
     * numericVal : 100
     * needShow : true
     */

    private int numericType;
    private int numericVal;
    private boolean needShow;

    public int getNumericType() {
        return numericType;
    }

    public void setNumericType(int numericType) {
        this.numericType = numericType;
    }

    public int getNumericVal() {
        return numericVal;
    }

    public void setNumericVal(int numericVal) {
        this.numericVal = numericVal;
    }

    public boolean isNeedShow() {
        return needShow;
    }

    public void setNeedShow(boolean needShow) {
        this.needShow = needShow;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
