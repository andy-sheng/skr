package com.module.home.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.home.WalletServerApi;
import com.module.home.inter.IExChangeCashView;
import com.module.home.model.ExChangeInfoModel;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ExChangeCashPresenter extends RxLifeCyclePresenter {
    IExChangeCashView mIExChangeCashView;
    WalletServerApi mWalletServerApi;

    public ExChangeCashPresenter(IExChangeCashView IExChangeCashView) {
        mIExChangeCashView = IExChangeCashView;
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
    }

    public void getDQBalance() {
        ApiMethods.subscribe(mWalletServerApi.getExChangeInfo(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    ExChangeInfoModel exChangeInfoModel = JSON.parseObject(obj.getData().toString(), ExChangeInfoModel.class);
                    mIExChangeCashView.showExChangeInfo(exChangeInfoModel);
                }
            }
        });
    }

    public void exChange(long cashNum) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", cashNum);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mWalletServerApi.exChangeCash(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    mIExChangeCashView.exChangeSuccess();
                } else {
                    mIExChangeCashView.exChangeFailed(obj.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                mIExChangeCashView.exChangeFailed("兑换失败，请重试");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                mIExChangeCashView.exChangeFailed("网络异常，请重试");
            }
        });
    }
}
