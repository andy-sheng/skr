package com.common.core.pay.ali;

import com.common.core.pay.EPayPlatform;
import com.common.core.pay.PayBaseReq;

public class AliPayReq extends PayBaseReq {
    String mOrderInfo;
    String mOrderID;

    public AliPayReq(String orderInfo, String orderID) {
        mOrderInfo = orderInfo;
        mOrderID = orderID;
        mEPayPlatform = EPayPlatform.ALI_PAY;
    }

    public String getOrderInfo() {
        return mOrderInfo;
    }

    public String getOrderID() {
        return mOrderID;
    }
}
