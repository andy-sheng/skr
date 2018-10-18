package com.wali.live.modulechannel.presenter.channellist;

import android.support.annotation.NonNull;

import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.mvp.PresenterEvent;
import com.common.mvp.RxLifeCyclePresenter;
import com.wali.live.modulechannel.api.ChannelDataStore;
import com.wali.live.modulechannel.model.channellist.ChannelShowModel;
import com.wali.live.proto.LiveShow.ChannelShow;
import com.wali.live.proto.LiveShow.GetChannelsRsp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhujianning on 18-10-17.
 */

public class ChannelListPresenter extends RxLifeCyclePresenter {
    private static final String TAG = "ChannelListPresenter";
    public static final String LOG_FORMAT = "<channel id:%s> %s";

    private ChannelDataStore mDataStore;
    private IChannelListView mView;

    private Disposable mDisposable;
    private Disposable mGetDataDisposable;

    private long mFcId;

    public ChannelListPresenter(IChannelListView view) {
        mDataStore = new ChannelDataStore();
        mView = view;
        addToLifeCycle();
    }

    public void setFcId(long fcId) {
        mFcId = fcId;
    }

    @Override
    public void start() {
        super.start();
        MyLog.d(TAG, formatLog("start"));

        if (mDisposable != null
                && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        //  因为开始可能会拉不到数据，用个定时器不停拉
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(10)
                .compose(this.bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        if(mGetDataDisposable != null && !mGetDataDisposable.isDisposed()) {
                            return;
                        }

                        getDataFromServer();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void getDataFromServer() {

        if(mGetDataDisposable != null
                && !mGetDataDisposable.isDisposed()) {
            return;
        }
        MyLog.d(TAG, formatLog("getDataFromServer"));

        mDataStore.getChannelListObservable(mFcId)
                .subscribeOn(Schedulers.io())
                .map(new io.reactivex.functions.Function<GetChannelsRsp, List<? extends ChannelShowModel>>() {
                    @Override
                    public List<? extends ChannelShowModel> apply(GetChannelsRsp getChannelsRsp) throws Exception {
                        return processRsp(getChannelsRsp);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(new Observer<List<? extends ChannelShowModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mGetDataDisposable = d;
                    }

                    @Override
                    public void onNext(List<? extends ChannelShowModel> list) {
                        MyLog.d(TAG, formatLog("getChannelObservable onNext"));
                        if (list != null && list.size() != 0) {
                            mView.listUpdateView(list);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onComplete() {
                        if(mDisposable != null && !mDisposable.isDisposed()) {
                            mDisposable.dispose();
                        }
                    }
                });
    }

    private List<? extends ChannelShowModel> processRsp(@NonNull GetChannelsRsp rsp) {
        MyLog.d(TAG, formatLog("processRsp"));
        List<ChannelShowModel> channelShows = new ArrayList<>();
        for (ChannelShow show : rsp.getChannelsList()) {
            ChannelShowModel channelShow = ChannelShowModel.parseFromPb(show);
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

    @Override
    public void destroy() {
        super.destroy();
    }
}
