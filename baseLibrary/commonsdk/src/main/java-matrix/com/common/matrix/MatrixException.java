package com.common.matrix;

public class MatrixException extends Throwable{
    public MatrixException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}
