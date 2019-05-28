package com.common.core.pay;

import com.common.base.BaseActivity;
import com.common.core.pay.ali.AliPayApi;
import com.common.core.pay.event.PayResultEvent;
import com.common.core.pay.wx.WxPayApi;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PayApi implements IPayApi {
    public static final int PAY_SUCCESS = 0;
    public static final int PAY_FAILD = -1;
    public static final int PAY_CANCEL = -2;
    IPayApi mSelectedPayApi;

    AliPayApi mAliPayApi;

    WxPayApi mWxPayApi;

    IPayCallBack mIPayCallBack;

    BaseActivity mBaseActivity;

    public PayApi(BaseActivity activity, IPayCallBack iPayCallBack) {
        mBaseActivity = activity;
        mIPayCallBack = iPayCallBack;
        EventBus.getDefault().register(this);
    }

    @Override
    public void pay(PayBaseReq payBaseResp) {
        if (mIPayCallBack != null) {
            mIPayCallBack.payStart(payBaseResp);
        }

        if (EPayPlatform.ALI_PAY == payBaseResp.getEPayPlatform()) {
            selectAliPay();
            mSelectedPayApi.pay(payBaseResp);
        } else if (EPayPlatform.WX_PAY == payBaseResp.getEPayPlatform()) {
            selectWxPay();
            mSelectedPayApi.pay(payBaseResp);
        }
    }

    private void selectAliPay() {
        if (mAliPayApi == null) {
            mAliPayApi = new AliPayApi(mBaseActivity);
        }

        mSelectedPayApi = mAliPayApi;
    }

    private void selectWxPay() {
        if (mWxPayApi == null) {
            mWxPayApi = new WxPayApi();
        }

        mSelectedPayApi = mWxPayApi;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PayResultEvent event) {
        if (event.getErrorCode() == 0) {
            mIPayCallBack.onSuccess();
        } else {
            mIPayCallBack.onFaild(event);
        }
    }

    @Override
    public void release() {
        EventBus.getDefault().unregister(this);
        if (mAliPayApi != null) {
            mAliPayApi.release();
        }

        if (mWxPayApi != null) {
            mWxPayApi.release();
        }
    }
}
