package com.wali.live.livesdk.live.presenter;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.location.Location;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.livesdk.live.api.BeginLiveRequest;
import com.wali.live.livesdk.live.api.EndLiveRequest;
import com.wali.live.livesdk.live.api.GetRoomIdRequest;
import com.wali.live.livesdk.live.task.IActionCallBack;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.proto.AccountProto;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chenyong on 2017/2/10.
 */

public class LiveRoomPresenter extends RxLifeCyclePresenter {

    private static final String TAG = "LiveRoomPresenter";

    private IActionCallBack mCallback;

    public LiveRoomPresenter(@Nullable IActionCallBack callBack) {
        mCallback = callBack;
    }

    public void getRoomIdByAppInfo(AccountProto.AppInfo appInfo) {
        Observable.just(appInfo).map(new Func1<AccountProto.AppInfo, LiveProto.GetRoomIdRsp>() {
            @Override
            public LiveProto.GetRoomIdRsp call(AccountProto.AppInfo appInfo) {
                LiveProto.GetRoomIdRsp rsp = null;
                if (appInfo != null) {
                    rsp = new GetRoomIdRequest(appInfo).syncRsp();
                } else {
                    rsp = new GetRoomIdRequest().syncRsp();
                }
                if (rsp == null) {
                    MyLog.w(TAG, "getRoomId, but rsp is null");
                    return null;
                }
                MyLog.w(TAG, "getRoomId rsp.toString()=" + rsp.toString());
                return rsp;
            }
        }).subscribeOn(Schedulers.io())
                .compose(this.<LiveProto.GetRoomIdRsp>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                               @Override
                               public void call(Object o) {
                                   LiveProto.GetRoomIdRsp rsp = (LiveProto.GetRoomIdRsp) o;
                                   int errCode = ErrorCode.CODE_ERROR_NORMAL;
                                   if (rsp == null) {
                                       mCallback.processAction(MiLinkCommand.COMMAND_LIVE_GET_ROOM_ID, errCode);
                                       return;
                                   }
                                   if ((errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                                       List<LiveCommonProto.UpStreamUrl> upStreamUrlList = rsp.getNewUpStreamUrlList();
                                       if ((upStreamUrlList == null || upStreamUrlList.isEmpty()) && !TextUtils.isEmpty(rsp.getUpStreamUrl())) {
                                           upStreamUrlList = new ArrayList<>();
                                           upStreamUrlList.add(LiveCommonProto.UpStreamUrl.newBuilder().setUrl(rsp.getUpStreamUrl()).setWeight(100).build());
                                       }
                                       mCallback.processAction(MiLinkCommand.COMMAND_LIVE_GET_ROOM_ID, errCode,
                                               rsp.getLiveId(), rsp.getShareUrl(), upStreamUrlList, rsp.getUdpUpstreamUrl());
                                   } else if (errCode == ErrorCode.CODE_NOT_MEET_BEGIN_LIVE_LEVEL) {
                                       mCallback.processAction(MiLinkCommand.COMMAND_LIVE_GET_ROOM_ID, errCode, rsp.getBeginLevel());
                                   }
                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {
                                   MyLog.e(throwable);
                               }
                           }

                );
    }

    public void beginLiveByAppInfo(final Location location, final int liveType, final List<Long> inviteeList,
                                   final boolean addHistory, final String liveTitle, final String coverUrl, final String preLiveId, final AccountProto.AppInfo appInfo,
                                   final int playUi, final RoomTag roomTag, final int appType, final boolean supportMagicFaceFlag) {
        Observable.just(0).map(new Func1<Integer, LiveProto.BeginLiveRsp>() {
            @Override
            public LiveProto.BeginLiveRsp call(Integer integer) {
                return new BeginLiveRequest(location, liveType, inviteeList, addHistory, liveTitle,
                        coverUrl, preLiveId, appInfo, playUi, appType, roomTag, supportMagicFaceFlag).syncRsp();

            }
        }).subscribeOn(Schedulers.io())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        LiveProto.BeginLiveRsp rsp = (LiveProto.BeginLiveRsp) o;
                        int errCode = ErrorCode.CODE_ERROR_NORMAL;
                        if (rsp == null) {
                            mCallback.processAction(MiLinkCommand.COMMAND_LIVE_GET_ROOM_ID, errCode);
                            return;
                        }
                        MyLog.w(TAG, "beginLive rsp.toString()=" + rsp.toString());
                        if ((errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                            mCallback.processAction(MiLinkCommand.COMMAND_LIVE_BEGIN, errCode,
                                    rsp.getLiveId(), rsp.getCreateTime(), rsp.getShareUrl(), rsp.getNewUpStreamUrlList(), rsp.getUdpUpstreamUrl(), rsp.getUpStreamUrl()); // 带上冗余的upStreamUrl，供第三方开播使用
                        } else {
                            mCallback.processAction(MiLinkCommand.COMMAND_LIVE_BEGIN, errCode);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
    }

    /*第三方app 结束直播*/
    public void endLiveByAppInfo(final String liveId, final AccountProto.AppInfo appInfo) {
        Observable.just(0)
                .map(new Func1<Integer, LiveProto.EndLiveRsp>() {
                    @Override
                    public LiveProto.EndLiveRsp call(Integer integer) {
                        LiveProto.EndLiveRsp rsp = null;
                        if (appInfo != null) {
                            rsp = new EndLiveRequest(liveId, appInfo).syncRsp();
                        } else {
                            rsp = new EndLiveRequest(liveId).syncRsp();
                        }
                        return rsp;
                    }
                }).subscribeOn(Schedulers.io())
                .compose(bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        LiveProto.EndLiveRsp rsp = (LiveProto.EndLiveRsp) o;
                        int errCode = ErrorCode.CODE_ERROR_NORMAL;
                        if (rsp == null) {
                            mCallback.processAction(MiLinkCommand.COMMAND_LIVE_END, errCode);
                        }
                        MyLog.w(TAG, "endLive rsp.toString()=" + rsp.toString());
                        if ((errCode = rsp.getRetCode()) == ErrorCode.CODE_SUCCESS) {
                            mCallback.processAction(MiLinkCommand.COMMAND_LIVE_END, errCode,
                                    rsp.getHisViewerCnt(), rsp.getGenerateHistorySucc(), rsp.getGenerateHistoryMsg(), rsp.getTicketBuyerCount());
                        } else {
                            mCallback.processAction(MiLinkCommand.COMMAND_LIVE_END, errCode);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });

    }

    @Override
    public void start() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {

    }
}
