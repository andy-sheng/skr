package com.common.core.pay.event;

import com.common.core.pay.EPayPlatform;

public class PayResultEvent {
    String errorMsg;

    EPayPlatform mEPayPlatform;

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
}
