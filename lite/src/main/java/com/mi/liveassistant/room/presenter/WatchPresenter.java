package com.mi.liveassistant.room.presenter;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.mi.liveassistant.common.mvp.BaseRxPresenter;
import com.mi.liveassistant.room.request.EnterLiveRequest;
import com.mi.liveassistant.room.view.IWatchView;
import com.wali.live.proto.LiveProto;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/4/20.
 */
public class WatchPresenter extends BaseRxPresenter<IWatchView> {
    public WatchPresenter(IWatchView view) {
        super(view);
    }

    public void enterLive(final long playerId, final String liveId) {
        MyLog.d(TAG, "enterLive ");
        Observable.just(0)
                .map(new Func1<Integer, LiveProto.EnterLiveRsp>() {
                    @Override
                    public LiveProto.EnterLiveRsp call(Integer integer) {
                        return new EnterLiveRequest(playerId, liveId).syncRsp();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.EnterLiveRsp>() {
                    @Override
                    public void call(LiveProto.EnterLiveRsp rsp) {
                        int errCode = ErrorCode.CODE_ERROR_NORMAL;
                        if (rsp != null && (errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                            mView.notifyEnterLiveSuccess(rsp.getDownStreamUrl());
                        } else {
                            mView.notifyEnterLiveFail(errCode);
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
