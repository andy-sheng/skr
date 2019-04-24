package com.common.core.pay;

import com.common.core.pay.event.PayResultEvent;

public interface IPayCallBack {
    void onFaild(PayResultEvent event);

    void onSuccess();

    void payStart(PayBaseReq payBaseResp);
}
