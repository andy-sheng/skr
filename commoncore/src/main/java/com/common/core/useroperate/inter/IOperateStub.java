package com.common.core.useroperate.inter;

public interface IOperateStub<T> {
    String getTitle();

    int getViewLayout();

    IOperateHolder<T> getHolder();
}
