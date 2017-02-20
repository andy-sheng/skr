package com.wali.live.common.pay.constant;

/**
 * 充值相关的常量
 * Created by rongzhisheng on 16-7-7.
 */
public interface PayConstant {
    /** 充值相关配置持久化 */
    String SP_FILENAME_RECHARGE_CONFIG = "recharge.config";
    /** 用户上次选择的payWay */
    String SP_KEY_LAST_PAY_WAY = "last.pay.way";
    /** 用户是否是首次充值，充值成功一次才算 */
    String SP_KEY_IS_FIRST_RECHARGE = "is.first.recharge";

    interface GooglePlay {
        int IN_APP_BILLING_VERSION = 3;
        String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
        String INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
        String INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN";
        String DETAILS_LIST = "DETAILS_LIST";
        String ITEM_ID_LIST = "ITEM_ID_LIST";
        String PRODUCT_TYPE_INAPP = "inapp";
        String RESPONSE_CODE = "RESPONSE_CODE";
        String INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA";
        String INAPP_DATA_SIGNATURE = "INAPP_DATA_SIGNATURE";
        String PURCHASE_TOKEN = "purchaseToken";
        String DEVELOPER_PAYLOAD = "developerPayload";
        String BUY_INTENT = "BUY_INTENT";
        String BIND_IN_APP_BILLING_SERVICE_INTENT_ACTION = "com.android.vending.billing.InAppBillingService.BIND";
        String COM_ANDROID_VENDING = "com.android.vending";
    }
    
    interface PayPal {
        String PAYPAL_CLIENT_ID_SANDBOX = "AUW4-zwY_fae3gpGtax34pNaGM6cPOlHej2oExldPhSio5tLsyOH3YmPR1dHvJ9ier5RbGfb3iYIL4hs";
        String PAYPAL_CLIENT_ID_PRODUCTION = "AV6iL5RVB-FofJrsevqNaPbk1AI85hYjWsBDN88emKZ3o5dT3yk5g7Xb-pma8LGOtRWu0Y7eRGDLr1Xx";
    }

    int SINGLE_DEAL_QUOTA_WEIXIN = 3000_00;//分
    int SINGLE_DEAL_QUOTA_MIWALLET = 3000_00;//分
    int ONE_DAY_RECHARGE_QUOTA_WEIXIN = 1_0000_00;//分
    int ONE_DAY_RECHARGE_QUOTA_MIWALLET = 5000_00;//分

    //当前支付方式对应的充值列表类型，减少拉取充值列表次数
    int RECHARGE_LIST_TYPE_NATIVE = 1;// 微信、支付宝、小米钱包共用一套充值代码
    int RECHARGE_LIST_TYPE_GOOGLE_WALLET = 2;
    int RECHARGE_LIST_TYPE_PAYPAL = 3;

    // 当用户首次充值时，区分界面
    int RECHARGE_STEP_FIRST = 1;
    int RECHARGE_STEP_SECOND = 2;
    /**用户点击充值按钮的间隔，单位：秒*/
    int RECHARGE_CLICK_INTERVAL = 2;

}
