package com.wali.live.recharge.constants;

/**
 * Created by rongzhisheng on 16-12-23.
 */

public interface RechargeConstants {
    interface RechargeListType {
        int UNKNOWN = 0;
        int CHINA = 1;// 微信、支付宝、小米钱包共用一套充值代码
        int GOOGLE_WALLET = 2;
        int PAYPAL = 3;
        int CODA_IDR = 4;
        int CODA_ATM = 5;
        int MIBI = 6;
    }
    int GOOGLE_PLAY_PURCHASE_REQUEST_CODE = 1001;
    int PAYPAL_PAY_REQUEST_CODE = 1002;
}
