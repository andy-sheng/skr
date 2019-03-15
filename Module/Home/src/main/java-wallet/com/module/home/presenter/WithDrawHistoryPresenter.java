package com.module.home.presenter;

import com.alibaba.fastjson.JSON;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.module.home.WalletServerApi;
import com.module.home.inter.IWithDrawHistoryView;
import com.module.home.model.WithDrawHistoryModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class WithDrawHistoryPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "WalletRecordPresenter";

    WalletServerApi mWalletServerApi;
    IWithDrawHistoryView mIWalletView;
    int mLimit = 20;
    int mOffset = 0;

    Disposable mDisposable;

    List<WithDrawHistoryModel> mWithDrawHistoryModels = new ArrayList<>();

    public WithDrawHistoryPresenter(IWithDrawHistoryView walletView) {
        this.mIWalletView = walletView;
        mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);
    }

    public void getMoreWithDrawHistory() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }

        mDisposable = ApiMethods.subscribeWith(mWalletServerApi.getListWithdraw(mOffset, mLimit), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<WithDrawHistoryModel> withDrawHistoryModelList = JSON.parseArray(result.getData().getString("withdraws")
                            , WithDrawHistoryModel.class);

                    //没有数据
                    if(withDrawHistoryModelList == null){
                        mIWalletView.hasMore(false);
                        return;
                    }

                    if (mOffset == 0) {
                        mWithDrawHistoryModels.clear();
                    }


                    mWithDrawHistoryModels.addAll(withDrawHistoryModelList);
                    mOffset = result.getData().getInteger("offset");
                    mIWalletView.update(mWithDrawHistoryModels);
                    mIWalletView.hasMore(true);
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                    mIWalletView.update(mWithDrawHistoryModels);
                    mIWalletView.hasMore(true);
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络延迟");
                mIWalletView.update(mWithDrawHistoryModels);
                mIWalletView.hasMore(true);
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort("网络错误");
                mIWalletView.update(mWithDrawHistoryModels);
                mIWalletView.hasMore(true);
            }
        }, this);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
