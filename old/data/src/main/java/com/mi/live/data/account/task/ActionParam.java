package com.mi.live.data.account.task;

/**
 * Created by lan on 16/11/24.
 */
public class ActionParam {
    private String mAction;
    private int mErrCode;
    private Object[] params;

    public String getAction() {
        return mAction;
    }

    public void setAction(String action) {
        mAction = action;
    }

    public int getErrCode() {
        return mErrCode;
    }

    public void setErrCode(int errCode) {
        mErrCode = errCode;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
