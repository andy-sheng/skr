package com.common.core.account.event;

/**
 * 登陆错误的文案提示
 */
public class LoginApiErrorEvent {
    int errno;
    String errmsg;

    public LoginApiErrorEvent(int errno, String errmsg) {
        this.errno = errno;
        this.errmsg = errmsg;
    }

    public int getErrno() {
        return errno;
    }

    public String getErrmsg() {
        return errmsg;
    }
}
