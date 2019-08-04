package com.common.rxretrofit;

public class RequestControl {
    String mKey; // key 用于标识 唯一的请求行为
    ControlType mControlType; // 控制类型

    public RequestControl(String key, ControlType controlType) {
        mKey = key;
        mControlType = controlType;
    }
}
