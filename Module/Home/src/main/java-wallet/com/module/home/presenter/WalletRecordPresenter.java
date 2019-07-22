package com.module.home.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.home.inter.IWalletView;
import com.module.home.WalletServerApi;
import com.module.home.model.WalletRecordModel;
import com.module.home.model.WithDrawInfoModel;

import java.util.List;

public class WalletRecordPresenter extends RxLifeCyclePresenter {
    public final String TAG = "WalletRecordPresenter";

    WalletServerApi mWalletServerApi;
    IWalletView mIWalletView;
    WithDrawInfoModel mWithDrawInfoModel;

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

    public void getWithDrawInfo(int deep) {
        if (deep < 10) {
            ApiMethods.subscribe(mWalletServerApi.getWithdrawInfo(), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        mWithDrawInfoModel = JSON.parseObject(result.getData().toString(), WithDrawInfoModel.class);
                        mIWalletView.showWithDrawInfo(mWithDrawInfoModel);
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

    /**
     * 获取全部流水
     *
     * @param offset
     * @param limit
     */
    public void getAllWalletRecords(int offset, int limit) {
        ApiMethods.subscribe(mWalletServerApi.getWalletRecord(offset, limit, 0), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<WalletRecordModel> walletRecordModels = JSON.parseArray(result.getData().getString("records"), WalletRecordModel.class);
                    int newOffset = result.getData().getIntValue("offset");
                    if (mIWalletView != null) {
                        mIWalletView.onGetAllRecords(newOffset, walletRecordModels);
                    }
                }
            }
        }, this);
    }

    /**
     * 获取收入流水
     *
     * @param offset
     * @param limit
     */
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

    /**
     * 获取提现流水
     *
     * @param offset
     * @param limit
     */
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
