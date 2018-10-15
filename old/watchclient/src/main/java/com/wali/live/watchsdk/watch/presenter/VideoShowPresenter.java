package com.wali.live.watchsdk.watch.presenter;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.request.RoomInfoRequest;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.manager.UserInfoManager;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.watch.view.IWatchVideoView;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zyh on 2017/6/15.
 */

public class VideoShowPresenter extends RxLifeCyclePresenter {
    final private String TAG = "VideoShowPresenter";
    private IWatchVideoView mView;

    private Subscription mRoomSubscription;
    private Subscription mLiveSubscription;

    public VideoShowPresenter(IWatchVideoView view) {
        mView = view;
    }

    /**
     * 目前主要用来切换房间时，重置内部状态
     */
    public void reset() {
        if (mRoomSubscription != null && !mRoomSubscription.isUnsubscribed()) {
            mRoomSubscription.unsubscribe();
        }
        if (mLiveSubscription != null && !mLiveSubscription.isUnsubscribed()) {
            mLiveSubscription.unsubscribe();
        }
    }

    /**
     * @notice 直播使用
     */
    public void getVideoUrlByRoomId(long uuid, String roomId) {
        MyLog.w(TAG, "getVideoUrlByRoomId uuid=" + uuid + " roomId=" + roomId);
        // 如果房间号为空，拉取对应的直播信息；如果不为空，拉取对应的直播地址
        if (TextUtils.isEmpty(roomId)) {
            getLiveShowInternal(uuid);
            return;
        }
        getVideoUrlInternal(uuid, roomId);
    }

    /**
     * @notice 回放使用
     */
    public void getVideoUrlOnly(long uuid, String roomId) {
        MyLog.w(TAG, "getVideoUrlOnly uuid=" + uuid + " roomId=" + roomId);
        getVideoUrlInternal(uuid, roomId);
    }

    private void getVideoUrlInternal(final long uuid, final String roomId) {
        MyLog.w(TAG, "getVideoUrlInternal uuid=" + uuid);
        mRoomSubscription = Observable.just("")
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        LiveProto.RoomInfoRsp rsp = new RoomInfoRequest(uuid, roomId).syncRsp();
                        if (rsp != null) {
                            switch (rsp.getRetCode()) {
                                case ErrorCode.CODE_SUCCESS:
                                    return rsp.getDownStreamUrl();
                                case ErrorCode.CODE_ROOM_NOT_EXIST:
                                    if (rsp.hasPlaybackUrl()) {
                                        return rsp.getPlaybackUrl();
                                    }
                                default:
                                    break;
                            }
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<String>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String liveUrl) {
                        if (!TextUtils.isEmpty(liveUrl) && mView != null) {
                            mView.updateVideoUrl(liveUrl);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getVideoUrlByRoomId failed =" + throwable);
                    }
                });
    }

    private void getLiveShowInternal(final long uuid) {
        MyLog.w(TAG, "getLiveShowInternal uuid=" + uuid);
        mLiveSubscription = Observable.just("")
                .map(new Func1<String, LiveShow>() {
                    @Override
                    public LiveShow call(String s) {
                        return UserInfoManager.getLiveShowByUserId(uuid);
                    }
                })
                .subscribeOn(Schedulers.io())
                .compose(this.<LiveShow>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveShow>() {
                    @Override
                    public void call(LiveShow liveShow) {
                        if (mView == null) {
                            return;
                        }
                        if (liveShow == null || TextUtils.isEmpty(liveShow.getLiveId())) {
                            mView.notifyLiveEnd();
                            return;
                        }
                        mView.updateRoomInfo(liveShow.getLiveId(), liveShow.getUrl());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "getVideoUrlByRoomId failed =" + throwable);
                    }
                });
    }
}
