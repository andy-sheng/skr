package com.module.common;

public interface ICallback {
    void onSucess(Object obj);

    void onFailed(Object obj, int errcode, String message);
}
