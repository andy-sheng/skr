package com.common.rxretrofit;

public class RequestControl {
    String key; // key 用于标识 唯一的请求行为
    ControlType controlType; // 控制类型

    public RequestControl(String key, ControlType controlType) {
        this.key = key;
        this.controlType = controlType;
    }
}
