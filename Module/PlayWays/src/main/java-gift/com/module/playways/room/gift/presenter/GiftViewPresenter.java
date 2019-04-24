package com.module.playways.room.gift.presenter;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.mvp.PresenterEvent;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.module.playways.room.gift.GiftServerApi;
import com.module.playways.room.gift.inter.IGiftView;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.model.GiftServerModel;
import com.umeng.socialize.media.Base;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class GiftViewPresenter extends RxLifeCyclePresenter {
    IGiftView mIGiftView;

    Disposable mGiftDisposable;

    GiftServerApi mGiftServerApi;

    public GiftViewPresenter(IGiftView iGiftView) {
        mIGiftView = iGiftView;
        mGiftServerApi = ApiManager.getInstance().createService(GiftServerApi.class);
        addToLifeCycle();
//        EventBus.getDefault().register(this);
    }

    public void loadData() {
        //先从数据库拉数据
        mGiftDisposable = ApiMethods.subscribe(mGiftServerApi.getGiftList(0, 1000), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<GiftServerModel> giftServerModelList = JSON.parseArray(result.getData().getString("list"), GiftServerModel.class);
                    formatGiftData(giftServerModelList);
                }
            }
        }, this);
    }

    public void formatGiftData(List<GiftServerModel> giftServerModelList) {
        ArrayList<List<BaseGift>> arrayList = new ArrayList<>();
        Observable.just(giftServerModelList).flatMap(new Function<List<GiftServerModel>, ObservableSource<BaseGift>>() {
            @Override
            public ObservableSource<BaseGift> apply(List<GiftServerModel> giftServerModelList) throws Exception {
                return Observable.fromIterable(BaseGift.parse(giftServerModelList));
            }
        }).buffer(8)
                .subscribeOn(Schedulers.io())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<BaseGift>>() {
                    @Override
                    public void accept(List<BaseGift> baseGifts) throws Exception {
                        arrayList.add(baseGifts);
                    }
                }, throwable -> {

                }, () -> {
                    HashMap<Integer, List<BaseGift>> giftHashMap = new HashMap<>();
                    int index = 0;
                    for (List<BaseGift> baseGiftList : arrayList) {
                        giftHashMap.put(index++, baseGiftList);
                    }

                    mIGiftView.showGift(giftHashMap);
                });
    }


    @Override
    public void destroy() {
        super.destroy();
//        EventBus.getDefault().unregister(this);
    }
}
