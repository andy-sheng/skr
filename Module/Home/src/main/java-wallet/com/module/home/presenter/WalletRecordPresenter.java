package com.module.home.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.home.IWalletView;
import com.module.home.WalletServerApi;
import com.module.home.model.WalletRecordModel;

import java.util.List;

public class WalletRecordPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "WalletRecordPresenter";

    WalletServerApi mWalletServerApi;
    IWalletView mIWalletView;

    public WalletRecordPresenter(IWalletView walletView) {
        this.mIWalletView = walletView;
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
    }

    public void getBalance() {
        ApiMethods.subscribe(mWalletServerApi.getBalance(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    String available = result.getData().getString("available");
                    String locked = result.getData().getString("locked");
                    if (mIWalletView != null) {
                        mIWalletView.onGetBalanceSucess(available, locked);
                    }
                }
            }
        }, this);
    }

    public void getWalletIncrRecords(int offset, int limit) {
        ApiMethods.subscribe(mWalletServerApi.getWalletRecord(offset, limit, 1), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<WalletRecordModel> walletRecordModels = JSON.parseArray(result.getData().getString("records"), WalletRecordModel.class);
                    int newOffset = result.getData().getIntValue("offset");
                    if (mIWalletView != null) {
                        mIWalletView.onGetIncrRecords(newOffset, walletRecordModels);
                    }
                }
            }
        }, this);
    }

    public void getWalletDecrRecords(int offset, int limit) {
        ApiMethods.subscribe(mWalletServerApi.getWalletRecord(offset, limit, 2), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<WalletRecordModel> walletRecordModels = JSON.parseArray(result.getData().getString("records"), WalletRecordModel.class);
                }
            }
        }, this);
    }

}
