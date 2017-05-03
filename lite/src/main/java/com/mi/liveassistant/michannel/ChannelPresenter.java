package com.mi.liveassistant.michannel;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mi.liveassistant.proto.CommonChannelProto;
import com.mi.liveassistant.proto.HotChannelProto;

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
@Keep
public class ChannelPresenter implements IChannelPresenter {
    public static final String TAG = ChannelPresenter.class.getSimpleName();
    public static final String LOG_FORMAT = "<channel id:%s> %s";

    private ChannelDataStore mDataStore;
    private IChannelView mView;

    private Subscription mSubscription;
    private long mChannelId;

    private Subscription mTimerSubscription;

    public ChannelPresenter(IChannelView view) {
        mDataStore = new ChannelDataStore();
        mView = view;
    }

    @Override
    public void setChannelId(long channelId) {
        mChannelId = channelId;
    }

    @Override
    public void start() {
        Log.d(TAG, formatLog("start"));
        if (mTimerSubscription != null) {
            mTimerSubscription.unsubscribe();
        }
        //  因为开始可能会拉不到数据，用个定时器不停拉
        mTimerSubscription = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(10)
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
        Log.d(TAG, formatLog("getDataFromServer"));
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
                .subscribe(new Observer<List<? extends BaseViewModel>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, formatLog("getChannelObservable onCompleted"));
                        mView.finishRefresh();
                        mTimerSubscription.unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, formatLog("getChannelObservable onError=" + e.getMessage()));
                        mView.finishRefresh();
                        mTimerSubscription.unsubscribe();
                    }

                    @Override
                    public void onNext(List<? extends BaseViewModel> list) {
                        Log.d(TAG, formatLog("getChannelObservable onNext"));
                        if (list != null && list.size() != 0) {
                            mView.updateView(list);
                        }
                    }
                });
    }

    private List<? extends BaseViewModel> processRsp(@NonNull HotChannelProto.GetRecommendListRsp rsp) {
        Log.d(TAG, formatLog("processRsp"));
        List<ChannelViewModel> models = new ArrayList();

        for (CommonChannelProto.ChannelItem protoItem : rsp.getItemsList()) {
            ChannelViewModel viewModel = ChannelModelFactory.getChannelViewModel(protoItem);
            if (viewModel == null) {
                continue;
            }

            int uiType = viewModel.getUiType();
            if (uiType == ChannelUiType.TYPE_TWO_CARD || uiType == ChannelUiType.TYPE_THREE_CARD) {
                models.add(viewModel);
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
