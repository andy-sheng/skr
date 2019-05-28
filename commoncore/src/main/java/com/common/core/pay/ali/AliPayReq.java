package com.common.core.pay.ali;

import com.common.core.pay.EPayPlatform;
import com.common.core.pay.PayBaseReq;

public class AliPayReq extends PayBaseReq {
    String mOrderInfo;

    public AliPayReq(String orderInfo) {
        mOrderInfo = orderInfo;
        mEPayPlatform = EPayPlatform.ALI_PAY;
    }

    public String getOrderInfo() {
        return mOrderInfo;
    }
}
