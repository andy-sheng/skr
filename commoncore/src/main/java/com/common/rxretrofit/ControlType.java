package com.common.rxretrofit;

public enum ControlType {
    CancelLast,// 同一个api请求不可以重复，有重复会取消上一次
    CancelThis// 同一个api请求不可以重复，有重复会取消这一次
    // 如果不想有控制 RequestControl 传入 null
}
