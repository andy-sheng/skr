package com.wali.live.watchsdk.channel.presenter;

import android.support.annotation.NonNull;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.HotChannelProto;
import com.wali.live.watchsdk.channel.data.ChannelDataStore;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelModelFactory;
import com.wali.live.watchsdk.channel.viewmodel.ChannelUiType;
import com.wali.live.watchsdk.channel.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description HotChannelView的Presenter，提供数据的加载
 */
public class ChannelPresenter implements IChannelPresenter {
    public static final String TAG = ChannelPresenter.class.getSimpleName();
    public static final String LOG_FORMAT = "<channel id:%s> %s";

    private ChannelDataStore mDataStore;
    private IChannelView mView;

    private Subscription mSubscription;
    private long mChannelId;

    private RxActivity mRxActivity;
    private Subscription mTimerSubscription;

    public ChannelPresenter(RxActivity rxActivity, IChannelView view) {
        mDataStore = new ChannelDataStore();
        mRxActivity = rxActivity;
        mView = view;
    }

    public void setChannelId(long channelId) {
        mChannelId = channelId;
    }

    @Override
    public void start() {
        MyLog.d(TAG, formatLog("start"));
        if (mTimerSubscription != null) {
            mTimerSubscription.unsubscribe();
        }
        //  因为开始可能会拉不到数据，用个定时器不停拉
        mTimerSubscription = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(10)
                .compose(mRxActivity.<Long>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
                            return;
                        }
                        getDataFromServer();
                    }
                });
    }

    private void getDataFromServer() {
        MyLog.d(TAG, formatLog("getDataFromServer"));
        mSubscription = mDataStore.getHotChannelObservable(mChannelId)
                .subscribeOn(Schedulers.io())
                .map(new Func1<HotChannelProto.GetRecommendListRsp, List<? extends BaseViewModel>>() {
                    @Override
                    public List<? extends BaseViewModel> call(HotChannelProto.GetRecommendListRsp getRecommendListRsp) {
                        if (getRecommendListRsp != null) {
                            return processRsp(getRecommendListRsp);
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mRxActivity.<List<? extends BaseViewModel>>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<List<? extends BaseViewModel>>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, formatLog("getChannelObservable onCompleted"));
                        mView.finishRefresh();
                        mTimerSubscription.unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, formatLog("getChannelObservable onError=" + e.getMessage()));
                        mView.finishRefresh();
                        mTimerSubscription.unsubscribe();
                    }

                    @Override
                    public void onNext(List<? extends BaseViewModel> list) {
                        MyLog.d(TAG, formatLog("getChannelObservable onNext"));
                        if (list != null) {
                            mView.updateView(list, mChannelId);
                        }
                    }
                });
    }

    private List<? extends BaseViewModel> processRsp(@NonNull HotChannelProto.GetRecommendListRsp rsp) {
        MyLog.d(TAG, formatLog("processRsp"));
        List<ChannelViewModel> models = new ArrayList();
        boolean splitFirst = true;
        boolean splitDuplicate = false;
        for (CommonChannelProto.ChannelItem protoItem : rsp.getItemsList()) {
            ChannelViewModel viewModel = ChannelModelFactory.getChannelViewModel(protoItem);
            if (viewModel == null || viewModel != null && viewModel.isNeedRemove()) {
                MyLog.i(TAG, "viewModel need remove ");
                continue;
            }

            viewModel.setChannelId(mChannelId);

            int uiType = viewModel.getUiType();
            if (ChannelUiType.ALL_CHANNEL_UI_TYPE.contains(uiType)) {
                if (viewModel.getUiType() == ChannelUiType.TYPE_SPLIT_LINE) {
                    if (!splitFirst && !splitDuplicate) {
                        models.add(viewModel);
                        splitDuplicate = true;
                    }
                } else {
                    models.add(viewModel);
                    splitFirst = false;
                    splitDuplicate = false;
                }
            }
        }
        return models;
    }

    private String formatLog(String method) {
        return String.format(LOG_FORMAT, mChannelId, method);
    }

    @Override
    public void stop() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }
}
