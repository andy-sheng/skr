package com.module.home.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.home.WalletServerApi;
import com.module.home.inter.IExchangeDiamomdView;
import com.module.home.model.ExChangeInfoModel;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class ExChangeDiamondPresenter extends RxLifeCyclePresenter {
    WalletServerApi mWalletServerApi;

    IExchangeDiamomdView mIExchangeDiamomdView;

    public ExChangeDiamondPresenter(IExchangeDiamomdView iExchangeDiamomdView) {
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
        mIExchangeDiamomdView = iExchangeDiamomdView;
    }

    public void getDQBalance() {
        ApiMethods.subscribe(mWalletServerApi.getExChangeInfo(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    ExChangeInfoModel exChangeInfoModel = JSON.parseObject(obj.getData().toString(), ExChangeInfoModel.class);
//                    exChangeInfoModel.getDqBalance().setTotalAmount(396);
                    mIExchangeDiamomdView.showDQ(exChangeInfoModel);
                }
            }
        });
    }

    public void exChange(float diamondNum) {
        if (diamondNum == 0) {
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", diamondNum * 1000);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mWalletServerApi.exChangeDiamond(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    mIExchangeDiamomdView.exChangeSuccess();
                } else {
                    mIExchangeDiamomdView.exChangeFailed(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                mIExchangeDiamomdView.exChangeFailed("兑换错误");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                mIExchangeDiamomdView.exChangeFailed("网络超时，请重试");
            }
        }, this);
    }
}
