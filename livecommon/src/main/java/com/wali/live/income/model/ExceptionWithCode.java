package com.wali.live.income.model;

/**
 * Created by rongzhisheng on 16-12-16.
 */

public class ExceptionWithCode extends Exception {
    private int code;

    public ExceptionWithCode(int code) {
        super();
        this.code = code;
    }

    public ExceptionWithCode(String detailMessage) {
        super(detailMessage);
    }

    public ExceptionWithCode(String detailMessage, int code) {
        super(detailMessage);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExceptionWithCode{");
        sb.append("code=").append(code);
        sb.append(", msg=").append(getMessage());
        sb.append('}');
        return sb.toString();
    }
}
