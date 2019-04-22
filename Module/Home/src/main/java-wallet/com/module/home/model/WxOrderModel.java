package com.module.home.model;

import java.io.Serializable;

public class WxOrderModel implements Serializable {
    String prepayID;
    String sign;
    String nonceStr;

    public String getPrepayID() {
        return prepayID;
    }

    public void setPrepayID(String prepayID) {
        this.prepayID = prepayID;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }
}
