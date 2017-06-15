package com.wali.live.watchsdk.watch.presenter;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.wali.live.proto.LiveProto;
import com.wali.live.watchsdk.watch.view.IWatchVideoView;

import rx.Observable;
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

    public VideoShowPresenter(IWatchVideoView view) {
        mView = view;
    }

    public void getVideoUrlByRoomId(final long uuid, final String roomId) {
        MyLog.w(TAG, "getVideoUrlByRoomId uuid=" + uuid + " roomId=" + roomId);
        Observable.just("")
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        LiveProto.RoomInfoRsp rsp = LiveManager.roomInfoRsp(uuid, roomId);
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
                }).subscribeOn(Schedulers.io())
                .compose(this.<String>bindUntilEvent(PresenterEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String liveUrl) {
                        if (!TextUtils.isEmpty(liveUrl)
                                && mView != null) {
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
}
