package com.common.core.account.event;

/**
 * 验证码验证出错事件
 */
public class VerifyCodeErrorEvent {
    int errno;
    String errmsg;

    public VerifyCodeErrorEvent(int errno, String errmsg) {
        this.errno = errno;
        this.errmsg = errmsg;
    }
}
