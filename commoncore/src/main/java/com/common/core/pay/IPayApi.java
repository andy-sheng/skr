package com.common.core.pay;

public interface IPayApi {
    void pay(PayBaseReq t);

    void release();
}
