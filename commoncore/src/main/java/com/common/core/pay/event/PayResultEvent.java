package com.common.core.pay.event;

import com.common.core.pay.EPayPlatform;

public class PayResultEvent {
    String errorMsg;

    EPayPlatform mEPayPlatform;
    Object extra;

    // 0  成功
    // -1 错误，支付失败
    // -2 取消支付
    int errorCode;

    public PayResultEvent(EPayPlatform EPayPlatform, String errorMsg, int errorCode) {
        this.errorMsg = errorMsg;
        mEPayPlatform = EPayPlatform;
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public EPayPlatform getEPayPlatform() {
        return mEPayPlatform;
    }

    public void setEPayPlatform(EPayPlatform EPayPlatform) {
        mEPayPlatform = EPayPlatform;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }
}
