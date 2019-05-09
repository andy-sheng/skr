package com.module.home.presenter;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.pay.IPayCallBack;
import com.common.core.pay.PayApi;
import com.common.core.pay.ali.AliPayReq;
import com.common.core.pay.wx.WxPayReq;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.home.WalletServerApi;
import com.module.home.inter.IBallanceView;
import com.module.home.inter.IInComeView;
import com.module.home.model.RechargeItemModel;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

//同时只能有一个订单
public class InComePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "BallencePresenter";

    IInComeView mIInComeView;

    WalletServerApi mWalletServerApi;

    public InComePresenter(IInComeView iInComeView) {
        mIInComeView = iInComeView;
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
    }

    public void getBalance() {
        ApiMethods.subscribe(mWalletServerApi.getBalance(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    String available = result.getData().getString("available");
                    if (mIInComeView != null) {
                        mIInComeView.showCash(available);
                    }
                }
            }
        }, this);
    }

    public void getDqBalance() {
        ApiMethods.subscribe(mWalletServerApi.getDQBalance(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    String dq = result.getData().getString("totalAmountStr");
                    mIInComeView.showDq(dq);
                }
            }
        }, this);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
