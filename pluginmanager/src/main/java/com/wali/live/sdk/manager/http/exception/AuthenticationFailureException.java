package com.wali.live.sdk.manager.http.exception;

/**
 * Created by chengsimin on 2016/12/12.
 */
public class AuthenticationFailureException extends Exception {
    private String wwwAuthenticateHeader;

    public AuthenticationFailureException(String s) {
        super(s);
    }

    public void setWwwAuthenticateHeader(String wwwAuthenticateHeader) {
        this.wwwAuthenticateHeader = wwwAuthenticateHeader;
    }
}
