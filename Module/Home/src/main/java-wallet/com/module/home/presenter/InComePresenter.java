package com.module.home.presenter;

import android.app.Activity;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.pay.IPayCallBack;
import com.common.core.pay.PayApi;
import com.common.core.pay.ali.AliPayReq;
import com.common.core.pay.wx.WxPayReq;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.home.WalletServerApi;
import com.module.home.inter.IBallanceView;
import com.module.home.inter.IInComeView;
import com.module.home.model.ExChangeInfoModel;
import com.module.home.model.RechargeItemModel;
import com.module.home.model.WithDrawInfoModel;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

//同时只能有一个订单
public class InComePresenter extends RxLifeCyclePresenter {
    public final static String TAG = "BallencePresenter";

    IInComeView mIInComeView;

    WalletServerApi mWalletServerApi;

    ExChangeInfoModel mExChangeInfoModel;

    public InComePresenter(IInComeView iInComeView) {
        mIInComeView = iInComeView;
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
    }

    public void getWithDrawInfo(int deep) {
        if (deep < 10) {
            ApiMethods.subscribe(mWalletServerApi.getWithdrawInfo(), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        WithDrawInfoModel mWithDrawInfoModel = JSON.parseObject(result.getData().toString(), WithDrawInfoModel.class);
                        mIInComeView.showWithDrawInfo(mWithDrawInfoModel);
                    } else {
                        U.getToastUtil().showShort(result.getErrmsg());
                    }
                }

                @Override
                public void onError(Throwable e) {
                    getWithDrawInfo(deep+1);
                }

                @Override
                public void onNetworkError(ErrorType errorType) {
                    getWithDrawInfo(deep+1);
                }
            }, this);
        } else {
            MyLog.e(TAG, "10次都没拉到数据");
            U.getToastUtil().showShort("您网络异常，请退出重进");
        }
    }

    public void getRule() {
        if (mExChangeInfoModel != null) {
            mIInComeView.showRule(mExChangeInfoModel);
            return;
        }

        ApiMethods.subscribe(mWalletServerApi.getExChangeInfo(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mExChangeInfoModel = JSON.parseObject(obj.getData().toString(), ExChangeInfoModel.class);
                    mIInComeView.showRule(mExChangeInfoModel);
                }
            }
        });
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
