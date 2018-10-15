package com.common.core.login.execption;

/**
 * 案例1，没有设置Exception的msg 导致 postcarad tag为null，拦截失败，
 */
public class UnloginException extends Exception {
    public UnloginException() {
        super("unlogin，can not go this page");
    }

    public UnloginException(String message) {
        super(message);
    }
}
