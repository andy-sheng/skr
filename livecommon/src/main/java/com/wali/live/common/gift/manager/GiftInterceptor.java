package com.wali.live.common.gift.manager;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.repository.GiftRepository;
import com.wali.live.dao.Gift;

import java.util.Iterator;
import java.util.Vector;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by xzy on 17-3-2.
 * 大礼物拦截器
 */

public class GiftInterceptor {
    private static final String TAG = GiftInterceptor.class.getSimpleName();
    //最大数量
    private static final int maxSize = 10;
    private OnAddGift mOnAddGift;
    private Vector<GiftRecvModel> models = new Vector<>();

    public GiftInterceptor(@NonNull OnAddGift addGift) {
        mOnAddGift = addGift;
    }

    public void add(final GiftRecvModel model) {
        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Gift gift
                        = GiftRepository.checkExistedGiftRes(model.getGiftId());
                boolean isExist = !(null == gift);

                if (!isExist) {
                    GiftRepository.fillGiftEntityById(model);
                }

                subscriber.onNext(isExist);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "add onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.w(TAG, "add onError:" + e);
                    }

                    @Override
                    public void onNext(Boolean isExist) {
                        MyLog.w(TAG, "add onNext:" + isExist);
                        if (isExist && mOnAddGift != null) {
                            mOnAddGift.add(model);
                        } else {
                            addModel(model);
                        }
                    }
                });

    }

    public void clear() {
        models.clear();
    }

    private void addModel(GiftRecvModel model) {
        MyLog.w(TAG, "addModel:" + model);
        /**
         * 最多存maxSize礼物
         */
        if (models.size() > maxSize) {
            models.remove(0);
        }
        models.add(model);
    }

    public void findModel(Gift gift) {
        MyLog.d(TAG, "findModel:" + gift);
        Iterator<GiftRecvModel> iter = models.iterator();
        while (iter.hasNext()) {
            GiftRecvModel model = iter.next();
            if (gift.getGiftId() == model.getGiftId()) {
                MyLog.w(TAG, "findModel:" + gift);
                models.remove(model);
                mOnAddGift.add(model);
            }
        }
    }

    public interface OnAddGift {
        void add(GiftRecvModel model);
    }
}
