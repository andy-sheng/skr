package com.wali.live.watchsdk.fans.pay.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.dao.Gift;
import com.wali.live.watchsdk.fans.pay.model.FansPayModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/11/21.
 */
public class FansPayPresenter extends BaseRxPresenter<IFansPayView> {
    public FansPayPresenter(IFansPayView view) {
        super(view);
    }

    public void getPayList() {
        Observable
                .create(new Observable.OnSubscribe<List<FansPayModel>>() {
                    @Override
                    public void call(Subscriber<? super List<FansPayModel>> subscriber) {
                        List<Gift> gifts = new ArrayList<>(GiftRepository.getGiftListCache());
                        List<FansPayModel> result = new ArrayList<>();
                        if (gifts != null) {
                            for (Gift gift : gifts) {
                                if (gift.getCatagory() == GiftRepository.GIFT_CATEGORY_VFANS_PRIVILEGE) {
                                    result.add(new FansPayModel(gift));
                                }
                            }
                        }
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(mView.<List<FansPayModel>>bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<FansPayModel>>() {
                    @Override
                    public void call(List<FansPayModel> list) {
                        mView.setPayList(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}
