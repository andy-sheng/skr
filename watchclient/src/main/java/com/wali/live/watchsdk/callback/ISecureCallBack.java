package com.wali.live.watchsdk.callback;

/**
 * 权限检查回调
 * <p>
 * Created by wuxiaoshan on 17-2-23.
 */
public interface ISecureCallBack {
    void process(Object... objects);

    void processFailure();
}
