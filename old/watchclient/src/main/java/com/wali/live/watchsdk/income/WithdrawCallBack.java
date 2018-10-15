package com.wali.live.watchsdk.income;

/**
 * Created by qianyuan on 16/3/3.
 */
public interface WithdrawCallBack {

    void commitError(int errCode);

    void commitSuccess();

    void process(Object... params);

}
