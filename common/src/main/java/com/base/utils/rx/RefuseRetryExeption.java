package com.base.utils.rx;

/**
 * Created by chengsimin on 16/2/27.
 */
public class RefuseRetryExeption extends Exception {
    public RefuseRetryExeption() {
    }

    public RefuseRetryExeption(String detailMessage) {
        super(detailMessage);
    }

    public RefuseRetryExeption(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RefuseRetryExeption(Throwable throwable) {
        super(throwable);
    }
}
