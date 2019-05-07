package com.module.playways.room.gift;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.room.gift.event.GiftReadyEvent;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.model.GiftServerModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GiftManager {
    public final static String TAG = "GiftManager";

    private static class GiftManagerHolder {
        private static final GiftManager INSTANCE = new GiftManager();
    }

    List<GiftServerModel> mGiftServerModelList = new ArrayList<>();

    List<BaseGift> mBaseGiftList = new ArrayList<>();

    GiftServerApi mGiftServerApi;

    boolean isGiftReady = false;

    private GiftManager() {
        mGiftServerApi = ApiManager.getInstance().createService(GiftServerApi.class);
    }

    public static final GiftManager getInstance() {
        return GiftManagerHolder.INSTANCE;
    }

    public boolean isGiftReady() {
        return isGiftReady;
    }

    public List<BaseGift> getBaseGiftList() {
        return mBaseGiftList;
    }

    public void loadGift() {
        Observable.create(emitter -> {
            List<BaseGift> baseGiftList = GiftLocalApi.getAllGift();
            if (baseGiftList != null && baseGiftList.size() > 0) {
                isGiftReady = true;
                mBaseGiftList.addAll(baseGiftList);
                EventBus.getDefault().post(new GiftReadyEvent(true));
            }

            fetchGift();
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void fetchGift() {
        ApiMethods.subscribe(mGiftServerApi.getGiftList(0, 1000), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                MyLog.w(TAG, "fetchGift process" + " obj=" + result);
                if (result.getErrno() == 0) {
                    List<GiftServerModel> giftServerModelList = JSON.parseArray(result.getData().getString("list"), GiftServerModel.class);
                    mGiftServerModelList.addAll(giftServerModelList);
                    cacheToDb(giftServerModelList);
                    toLocalGiftModel(mGiftServerModelList);
                    isGiftReady = true;
                    EventBus.getDefault().post(new GiftReadyEvent(true));
                } else {
                    //礼物加载失败
                    MyLog.e(TAG, "礼物加载失败" + result.toString());
                    EventBus.getDefault().post(new GiftReadyEvent(false));
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                MyLog.e(TAG, "礼物加载失败，网络延迟");
                EventBus.getDefault().post(new GiftReadyEvent(false));
            }

            @Override
            public void onError(Throwable e) {
                EventBus.getDefault().post(new GiftReadyEvent(false));
            }
        });
    }

    private void cacheToDb(List<GiftServerModel> giftServerModelList) {
        Observable.create(emitter -> {
            GiftLocalApi.deleteAll();
            GiftLocalApi.insertAll(giftServerModelList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void toLocalGiftModel(List<GiftServerModel> giftServerModelList) {
        mBaseGiftList.clear();
        mBaseGiftList.addAll(BaseGift.parse(giftServerModelList));
    }

    public BaseGift getGiftById(int giftId) {
        for (BaseGift baseGift : mBaseGiftList) {
            if (giftId == baseGift.getGiftID()) {
                return baseGift;
            }
        }

        return null;
    }
}
