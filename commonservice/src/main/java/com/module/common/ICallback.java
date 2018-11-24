package com.module.common;

public interface ICallback {
    void onSucess();
    void onFailed(int errcode,String message);
}
