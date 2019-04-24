package com.module.playways.room.gift.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.module.playways.room.gift.GiftServerApi;
import com.module.playways.room.gift.inter.IContinueSendView;
import com.module.playways.room.gift.model.BaseGift;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class BuyGiftPresenter extends RxLifeCyclePresenter {
    public final static String TAG = "BuyGiftPresenter";
    GiftServerApi mGiftServerApi;
    IContinueSendView mIContinueSendView;

    ExecutorService mBuyGiftExecutor = Executors.newSingleThreadExecutor();

    public BuyGiftPresenter(IContinueSendView iContinueSendView) {
        mGiftServerApi = ApiManager.getInstance().createService(GiftServerApi.class);
        mIContinueSendView = iContinueSendView;
        addToLifeCycle();
    }

    public void buyGift(BaseGift baseGift, int continueCount, long roomId, long userID, long continueId) {
        MyLog.d(TAG, "buyGift gift id is " + baseGift.getGiftID() + ", continue Count is " + continueCount);
        long ts = System.currentTimeMillis() / 1000;
        HashMap<String, Object> map = new HashMap<>();
        map.put("giftID", baseGift.getGiftID());
        map.put("continueCnt", continueCount);
        map.put("continueID", continueId);
        map.put("count", 1);
        map.put("receiveUserID", userID);
        map.put("roomID", roomId);
        map.put("timestamp", ts);

        HashMap<String, Object> signMap = new HashMap<>(map);
        signMap.put("appSecret", "64c5b47f618489dece9b2f95afb56654");
        map.put("sign", U.getMD5Utils().signReq(signMap));

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(getBuyGiftObservable(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.d(TAG, "buyGift process" + " result=" + result);
                if (result.getErrno() == 0) {
                    mIContinueSendView.buySuccess(baseGift, continueCount);
                } else {
                    ToastUtils.showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                ToastUtils.showShort("购买礼物失败");
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                ToastUtils.showShort("网络超时");
            }
        }, this);
    }

    private io.reactivex.Observable getBuyGiftObservable(RequestBody body) {
        return mGiftServerApi.buyGift(body)
                .subscribeOn(Schedulers.from(mBuyGiftExecutor))
                .observeOn(AndroidSchedulers.mainThread());

    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
