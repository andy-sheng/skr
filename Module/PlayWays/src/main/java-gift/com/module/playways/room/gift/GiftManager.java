package com.module.playways.room.gift;

import com.alibaba.fastjson.JSON;
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
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GiftManager {
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
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) {
                List<BaseGift> baseGiftList = GiftLocalApi.getAllGift();
                if (baseGiftList == null || baseGiftList.size() == 0) {
                    fetchGift();
                } else {
                    isGiftReady = true;
                    mBaseGiftList.addAll(baseGiftList);
                    EventBus.getDefault().post(new GiftReadyEvent());
                }
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void fetchGift() {
        mGiftServerApi.getGiftList(0, 1000)
                .map(new Function<ApiResult, Object>() {
                    @Override
                    public Object apply(ApiResult result) throws Exception {
                        if (result.getErrno() == 0) {
                            List<GiftServerModel> giftServerModelList = JSON.parseArray(result.getData().getString("list"), GiftServerModel.class);
                            mGiftServerModelList.addAll(giftServerModelList);
                            cacheToDb(giftServerModelList);
                            toLocalGiftModel(mGiftServerModelList);
                            isGiftReady = true;
                            EventBus.getDefault().post(new GiftReadyEvent());
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
//        ApiMethods.subscribe(, new ApiObserver<ApiResult>() {
//            @Override
//            public void process(ApiResult result) {
//
//            }
//        });
    }

    private void cacheToDb(List<GiftServerModel> giftServerModelList) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) {
                GiftLocalApi.insertAll(giftServerModelList);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void toLocalGiftModel(List<GiftServerModel> giftServerModelList) {
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
