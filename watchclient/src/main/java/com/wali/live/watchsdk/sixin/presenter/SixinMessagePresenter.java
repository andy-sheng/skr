package com.wali.live.watchsdk.sixin.presenter;

import com.base.log.MyLog;
import com.base.mvp.BaseRxPresenter;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.sixin.data.SixinMessageLocalStore;
import com.wali.live.watchsdk.sixin.message.SixinMessageModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 2017/10/29.
 */
public class SixinMessagePresenter extends BaseRxPresenter<ISixinMessageView> {
    public final static int PAGE_MESSAGE_COUNT = 10; //分页加载十个消息

    public SixinMessagePresenter(ISixinMessageView view) {
        super(view);
    }

    public void firstLoadDataFromDB(final long uuid, final int targetType) {
        Observable
                .create(new Observable.OnSubscribe<List<SixinMessageModel>>() {
                    @Override
                    public void call(Subscriber<? super List<SixinMessageModel>> subscriber) {
                        List<SixinMessage> messageList = SixinMessageLocalStore.getSixinMessagesByUUid(uuid, PAGE_MESSAGE_COUNT, Long.MAX_VALUE, true, targetType);
                        if (messageList == null) {
                            subscriber.onError(new Exception("messageList is null"));
                        }
                        MyLog.d(TAG, "mesageList size=" + messageList);
                        List<SixinMessageModel> messageModelList = new ArrayList<>();
                        for (SixinMessage sixinMessage : messageList) {
                            SixinMessageModel item = new SixinMessageModel(sixinMessage);
                            messageModelList.add(item);
                        }
                        Collections.sort(messageModelList);
                        subscriber.onNext(messageModelList);
                        subscriber.onCompleted();

                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mView.<List<SixinMessageModel>>bindLifecycle())
                .subscribe(new Action1<List<SixinMessageModel>>() {
                    @Override
                    public void call(List<SixinMessageModel> messageList) {
                        if (messageList != null) {
                            mView.loadDataSuccess(messageList);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, throwable);
                    }
                });
    }
}
