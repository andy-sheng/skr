package com.common.core.crash;

/**
 * RX 可以忽略的错误
 * 不要轻易用，如果确实可以忽略可以用
 */
public class IgnoreException extends Exception {
    public IgnoreException(String desc) {
        super(desc);
    }
}
