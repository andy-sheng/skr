package com.common.core.pay;

//充值基本类
public abstract class PayBaseReq {
    public EPayPlatform mEPayPlatform;

    public EPayPlatform getEPayPlatform() {
        return mEPayPlatform;
    }

    public void setEPayPlatform(EPayPlatform EPayPlatform) {
        mEPayPlatform = EPayPlatform;
    }
}
