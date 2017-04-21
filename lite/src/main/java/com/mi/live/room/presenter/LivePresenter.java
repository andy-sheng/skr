package com.mi.live.room.presenter;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.location.Location;
import com.mi.live.room.mvp.BaseRxPresenter;
import com.mi.live.room.request.LitBeginLiveRequest;
import com.mi.live.room.request.LitEndLiveRequest;
import com.mi.live.room.view.ILiveView;
import com.wali.live.proto.LiveProto;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/4/20.
 */
public class LivePresenter extends BaseRxPresenter<ILiveView> {
    public LivePresenter(ILiveView view) {
        super(view);
    }

    public void beginNormalLive(Location location, String title, String coverUrl) {
        innerBeginLive(location, LiveManager.TYPE_LIVE_PUBLIC, title, coverUrl);
    }

    public void beginGameLive(Location location, String title, String coverUrl) {
        innerBeginLive(location, LiveManager.TYPE_LIVE_GAME, title, coverUrl);
    }

    private void innerBeginLive(final Location location, final int type, final String title, final String coverUrl) {
        MyLog.d(TAG, "inner beginLive type=" + type);
        Observable.just(0)
                .map(new Func1<Integer, LiveProto.BeginLiveRsp>() {
                    @Override
                    public LiveProto.BeginLiveRsp call(Integer integer) {
                        return new LitBeginLiveRequest(location, type, title, coverUrl).syncRsp();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.BeginLiveRsp>() {
                    @Override
                    public void call(LiveProto.BeginLiveRsp rsp) {
                        int errCode = ErrorCode.CODE_ERROR_NORMAL;
                        if (rsp != null && (errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                            mView.notifyBeginLiveSuccess(rsp.getLiveId(), rsp.getNewUpStreamUrlList(), rsp.getUdpUpstreamUrl());
                        } else {
                            mView.notifyBeginLiveFail(errCode);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
    }

    public void endLive(final String liveId) {
        MyLog.d(TAG, "endLive liveId=" + liveId);
        Observable
                .create(new Observable.OnSubscribe<LiveProto.EndLiveRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.EndLiveRsp> subscriber) {
                        LiveProto.EndLiveRsp rsp = new LitEndLiveRequest(liveId).syncRsp();
                        subscriber.onNext(rsp);
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.EndLiveRsp>() {
                    @Override
                    public void call(LiveProto.EndLiveRsp rsp) {
                        int errCode = ErrorCode.CODE_ERROR_NORMAL;
                        if (rsp != null && (errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                            mView.notifyEndLiveSuccess();
                        } else {
                            mView.notifyEndLiveFail(errCode);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
    }
}
