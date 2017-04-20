package com.wali.live.livesdk.live.room.presenter;

import com.base.log.MyLog;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.location.Location;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.livesdk.live.room.mvp.BaseRxPresenter;
import com.wali.live.livesdk.live.room.request.LitBeginLiveRequest;
import com.wali.live.livesdk.live.room.view.IRoomView;
import com.wali.live.proto.LiveProto;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 17/4/20.
 */
public class RoomPresenter extends BaseRxPresenter<IRoomView> {
    public RoomPresenter(IRoomView view) {
        super(view);
    }

    public void beginNormalLive(Location location, String title, String coverUrl) {
        innerBeginLive(location, LiveManager.TYPE_LIVE_PUBLIC, title, coverUrl);
    }

    public void beginGameLive(Location location, String title, String coverUrl) {
        innerBeginLive(location, LiveManager.TYPE_LIVE_GAME, title, coverUrl);
    }

    private void innerBeginLive(final Location location, final int type, final String title, final String coverUrl) {
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
                            mView.processAction(MiLinkCommand.COMMAND_LIVE_BEGIN, errCode,
                                    rsp.getLiveId(), rsp.getNewUpStreamUrlList(), rsp.getUdpUpstreamUrl());
                        } else {
                            mView.processAction(MiLinkCommand.COMMAND_LIVE_BEGIN, errCode);
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
