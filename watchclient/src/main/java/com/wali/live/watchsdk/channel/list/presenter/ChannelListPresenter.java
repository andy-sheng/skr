package com.wali.live.watchsdk.channel.list.presenter;

import android.support.annotation.NonNull;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.LiveShowProto;
import com.wali.live.watchsdk.channel.data.ChannelDataStore;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;

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
public class ChannelListPresenter {
    public static final String TAG = ChannelListPresenter.class.getSimpleName();
    public static final String LOG_FORMAT = "<channel id:%s> %s";

    private ChannelDataStore mDataStore;
    private IChannelListView mView;

    private Subscription mSubscription;
    private long mFcId;

    private RxActivity mRxActivity;
    private Subscription mTimerSubscription;

    public ChannelListPresenter(RxActivity rxActivity, IChannelListView view) {
        mDataStore = new ChannelDataStore();
        mRxActivity = rxActivity;
        mView = view;
    }

    public void setFcId(long fcId) {
        mFcId = fcId;
    }

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
        mSubscription = mDataStore.getChannelListObservable(mFcId)
                .subscribeOn(Schedulers.io())
                .map(new Func1<LiveShowProto.GetChannelsRsp, List<? extends ChannelShow>>() {
                    @Override
                    public List<? extends ChannelShow> call(LiveShowProto.GetChannelsRsp rsp) {
                        if (rsp != null) {
                            return processRsp(rsp);
                        }
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mRxActivity.<List<? extends ChannelShow>>bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<List<? extends ChannelShow>>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, formatLog("getChannelObservable onCompleted"));
                        mTimerSubscription.unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, formatLog("getChannelObservable onError=" + e.getMessage()));
                        mTimerSubscription.unsubscribe();
                    }

                    @Override
                    public void onNext(List<? extends ChannelShow> list) {
                        MyLog.d(TAG, formatLog("getChannelObservable onNext"));
                        if (list != null && list.size() != 0) {
                            mView.listUpdateView(list);
                        }
                    }
                });
    }

    private List<? extends ChannelShow> processRsp(@NonNull LiveShowProto.GetChannelsRsp rsp) {
        MyLog.d(TAG, formatLog("processRsp"));
        List<ChannelShow> channelShows = new ArrayList<>();
        for (LiveShowProto.ChannelShow show : rsp.getChannelsList()) {
            ChannelShow channelShow = ChannelShow.parseFromPb(show);
            if (!UserAccountManager.getInstance().hasAccount()) {
                if ("关注".equals(channelShow.getChannelName())) {
                    continue;
                }
            }
            channelShows.add(channelShow);
        }
        return channelShows;
    }

    private String formatLog(String method) {
        return String.format(LOG_FORMAT, mFcId, method);
    }

    public void stop() {
        if (mTimerSubscription != null && !mTimerSubscription.isUnsubscribed()) {
            mTimerSubscription.unsubscribe();
        }
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }
}
