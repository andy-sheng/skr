package com.wali.live.recharge.presenter;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.wali.live.pay.constant.PayWay;
import com.wali.live.pay.model.Diamond;

/**
 * Created by rongzhisheng on 16-12-23.
 */

public interface IRechargePresenter {

    @MainThread
    void loadDataAndUpdateView();

    @MainThread
    void recharge(Diamond goods, @NonNull PayWay payWay);

    void checkOrder(@NonNull PayWay payWay, String orderId, String payId, String receipt, String transactionId, boolean showTip);

    void getBalance();

    void showPopup();
}
