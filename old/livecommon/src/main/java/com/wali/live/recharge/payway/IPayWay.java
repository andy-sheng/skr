package com.wali.live.recharge.payway;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.AnyThread;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.wali.live.pay.constant.PayWay;
import com.wali.live.pay.model.Diamond;
import com.wali.live.proto.PayProto;

import java.util.List;

/**
 * Created by rongzhisheng on 16-12-23.
 */

public interface IPayWay {
    /**
     * self-use
     * @return
     */
    PayWay getPayWay();

    /**
     * 对服务器返回的数据进行转换，后台线程
     *
     * @param rsp
     * @return
     */
    @WorkerThread
    List<Diamond> parseGemPriceResponse(@NonNull PayProto.GetGemPriceResponse rsp);

    /**
     * 每次进入充值页面都调用
     *
     * @param activity
     */
    void init(@NonNull Activity activity);

    @AnyThread
    void consumeHoldProduct();

    /**
     * 后处理
     *
     * @param receipt
     * @return 不需要后处理返回false
     */
    @MainThread
    boolean postHandleAfterCheckOrder(String receipt);

    void pay(@NonNull Activity activity, String orderId, Diamond diamond, String userInfo);

    void handlePayResult(int resultCode, Intent intent);

    /**
     * 当每次退出充值时要做的清理工作
     */
    @MainThread
    void onExitRecharge(@NonNull Activity activity);

}
