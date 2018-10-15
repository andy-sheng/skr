package com.wali.live.common.gift.exception;

/**
 * Created by chengsimin on 16/7/28.
 */
public class GiftException extends Exception{
    public int errCode;

    public GiftException(int errCode,String msg) {
        super(msg);
        this.errCode = errCode;
    }
    public GiftException(String msg) {
        super(msg);
        this.errCode = 0;
    }
}
