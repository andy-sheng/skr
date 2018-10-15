package com.base.utils.rx;

/**
 * Created by chengsimin on 16/2/27.
 */
public class MaxRetryException extends Exception {
    public MaxRetryException() {
    }

    public MaxRetryException(String detailMessage) {
        super(detailMessage);
    }

    public MaxRetryException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MaxRetryException(Throwable throwable) {
        super(throwable);
    }
}
