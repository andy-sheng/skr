package com.mi.liveassistant.room.presenter.live;

import android.support.annotation.NonNull;

import com.mi.liveassistant.common.api.ErrorCode;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.mvp.BaseRxPresenter;
import com.mi.liveassistant.data.model.Location;
import com.mi.liveassistant.proto.LiveProto;
import com.mi.liveassistant.room.request.BeginLiveRequest;
import com.mi.liveassistant.room.request.EndLiveRequest;
import com.mi.liveassistant.room.view.ILiveView;

import component.IEventController;
import component.Params;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.mi.liveassistant.room.manager.LiveEventController.MSG_BEGIN_LIVE_FAILED;
import static com.mi.liveassistant.room.manager.LiveEventController.MSG_BEGIN_LIVE_SUCCESS;
import static com.mi.liveassistant.room.manager.LiveEventController.MSG_END_LIVE_FAILED;
import static com.mi.liveassistant.room.manager.LiveEventController.MSG_END_LIVE_SUCCESS;

/**
 * Created by lan on 17/4/20.
 *
 * @description 开启直播，结束直播
 */
public abstract class BaseLivePresenter extends BaseRxPresenter<ILiveView> {
    protected IEventController mEventController;

    protected int mLiveRoomType;

    public BaseLivePresenter(@NonNull IEventController controller, ILiveView view) {
        super(view);
        mEventController = controller;
    }

    public void beginLive(final Location location, final String title, final String coverUrl) {
        MyLog.d(TAG, "beginLive liveRoomType=" + mLiveRoomType);
        Observable.just(0)
                .map(new Func1<Integer, LiveProto.BeginLiveRsp>() {
                    @Override
                    public LiveProto.BeginLiveRsp call(Integer integer) {
                        return new BeginLiveRequest(location, mLiveRoomType, title, coverUrl).syncRsp();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.BeginLiveRsp>() {
                    @Override
                    public void call(LiveProto.BeginLiveRsp rsp) {
                        int errCode = ErrorCode.CODE_ERROR_NORMAL;
                        if (rsp != null && (errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                            mEventController.postEvent(MSG_BEGIN_LIVE_SUCCESS, new Params()
                                    .putItem(rsp.getLiveId())
                                    .putItem(rsp.getNewUpStreamUrlList())
                                    .putItem(rsp.getUdpUpstreamUrl()));
                        } else {
                            mEventController.postEvent(MSG_BEGIN_LIVE_FAILED, new Params()
                                    .putItem(errCode));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                        mEventController.postEvent(MSG_BEGIN_LIVE_FAILED, new Params()
                                .putItem(ErrorCode.CODE_ERROR_NORMAL));
                    }
                });
    }

    public void endLive(final String liveId) {
        MyLog.d(TAG, "endLive liveId=" + liveId);
        Observable
                .create(new Observable.OnSubscribe<LiveProto.EndLiveRsp>() {
                    @Override
                    public void call(Subscriber<? super LiveProto.EndLiveRsp> subscriber) {
                        LiveProto.EndLiveRsp rsp = new EndLiveRequest(liveId).syncRsp();
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
                            mEventController.postEvent(MSG_END_LIVE_SUCCESS);
                        } else {
                            mEventController.postEvent(MSG_END_LIVE_FAILED, new Params()
                                    .putItem(errCode));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                        mEventController.postEvent(MSG_BEGIN_LIVE_FAILED, new Params()
                                .putItem(ErrorCode.CODE_ERROR_NORMAL));
                    }
                });
    }
}
