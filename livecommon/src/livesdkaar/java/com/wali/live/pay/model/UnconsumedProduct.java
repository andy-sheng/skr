package com.wali.live.pay.model;

/**
 * Created by rongzhisheng on 16-11-12.
 */
public class UnconsumedProduct {
    private String purchaseData;
    private String signature;

    public UnconsumedProduct(String purchaseData, String signature) {
        this.purchaseData = purchaseData;
        this.signature = signature;
    }

    public String getPurchaseData() {
        return purchaseData;
    }

    public String getSignature() {
        return signature;
    }

}
