package com.wali.live.watchsdk.watch.presenter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.activity.RxActivity;
import com.base.activity.assist.IBindActivityLIfeCycle;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.data.LiveShow;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.push.SendBarrageManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.push.presenter.RoomMessagePresenter;
import com.mi.live.data.query.LiveRoomQuery;
import com.mi.live.data.query.mapper.RoomDataMapper;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.mi.live.data.repository.RoomMessageRepository;
import com.mi.live.data.repository.datasource.RoomMessageStore;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.event.LiveEndEvent;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by lan on 16/11/28.
 */
public class LiveTaskPresenter implements ILiveTaskPresenter, IBindActivityLIfeCycle {
    public static final String TAG = LiveTaskPresenter.class.getSimpleName();

    private IWatchView mView;

    private RxActivity mRxActivity;
    private RoomBaseDataModel mMyRoomData;

    private RoomMessagePresenter mPullRoomMessagePresenter;

    //这里限制进入房间只会被调用一次
    private boolean mHasEnter = false;
    private Subscription mEnterRoomSubscription;

    public LiveTaskPresenter(@NonNull RxActivity rxActivity, IWatchView view, @NonNull RoomBaseDataModel myRoomData) {
        mRxActivity = rxActivity;
        mView = view;
        mMyRoomData = myRoomData;
    }

    public void reset() {
        mHasEnter = false;
        if (mEnterRoomSubscription != null && !mEnterRoomSubscription.isUnsubscribed()) {
            mEnterRoomSubscription.unsubscribe();
        }
        if (mPullRoomMessagePresenter != null) {
            mPullRoomMessagePresenter.stopWork();
        }
    }

    private void pullRoomMessage() {
        // 对外的不收push了，统一走拉取模式
        if (mPullRoomMessagePresenter == null) {
            mPullRoomMessagePresenter = new RoomMessagePresenter(mMyRoomData, new RoomMessageRepository(new RoomMessageStore()), mRxActivity);
        }
        mPullRoomMessagePresenter.startWork();
    }


    public void queryLiveShowById(final long uuid, final String liveId) {
        if (uuid <= 0 || TextUtils.isEmpty(liveId)) {
            MyLog.d(TAG, "queryLiveShowById uuid=" + uuid + ", liveId=" + liveId);
            // 直播结束
            EventBus.getDefault().post(new LiveEndEvent());
            return;
        }
        Observable.just(uuid)
                .map(new Func1<Long, LiveShow>() {
                    @Override
                    public LiveShow call(Long userId) {
                        LiveShow liveShow = UserInfoManager.getLiveShowByUserId(userId);
                        return liveShow;
                    }
                }).subscribeOn(Schedulers.io())
                .compose(mRxActivity.<LiveShow>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveShow>() {
                    @Override
                    public void call(LiveShow liveShow) {
                        if (liveShow == null || liveShow.getUid() != uuid || !liveId.equals(liveShow.getLiveId())) {
                            MyLog.d(TAG, "queryLiveShowById uuid=" + uuid + " liveId=" + liveId +
                                    ", liveShow uid=" + liveShow.getUid() + " liveId=" + liveShow.getLiveId());
                            // 直播结束
                            EventBus.getDefault().post(new LiveEndEvent());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.w(TAG, throwable);
                    }
                });
    }

    @Override
    public void enterLive() {
        if (mHasEnter) {
            return;
        }
        MyLog.w(TAG, "enterLive roomId=" + mMyRoomData.getRoomId());
        if (TextUtils.isEmpty(mMyRoomData.getRoomId())) {
            return;
        }
        // 加上这句，否则会触发离开房间逻辑
        RoomInfoGlobalCache.getsInstance().enterCurrentRoom(mMyRoomData.getRoomId());
        if (mEnterRoomSubscription != null && !mEnterRoomSubscription.isUnsubscribed()) {
            return;
        }
        mEnterRoomSubscription = LiveRoomQuery.enterRoom(mMyRoomData.getUid(), mMyRoomData.getRoomId(), "")
                .compose(mRxActivity.<EnterRoomInfo>bindUntilEvent())
                .retryWhen(new RxRetryAssist())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<EnterRoomInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(EnterRoomInfo enterRoomInfo) {
                        MyLog.w(TAG, "enterRoomInfo code: " + enterRoomInfo.getRetCode());
                        RoomDataMapper.fillRoomDataModelByEnterRoomInfo(mMyRoomData, enterRoomInfo);
                        pullRoomMessage();
//                        if (mMyRoomData.getGetMessageMode() == RoomMessagePresenter.PULL_MODE) {
//                            if (mPullRoomMessagePresenter == null) {
//                                mPullRoomMessagePresenter = new RoomMessagePresenter(mMyRoomData, new RoomMessageRepository(new RoomMessageStore()), mRxActivity);
//                            }
//                            mPullRoomMessagePresenter.startWork();
//                        } else {
//                            if (mPullRoomMessagePresenter != null) {
//                                mPullRoomMessagePresenter.stopWork();
//                            }
//                        }
                        if (mView != null && enterRoomInfo.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            mView.enterLive(enterRoomInfo);
                        } else if (mView != null && enterRoomInfo.getRetCode() == ErrorCode.CODE_SERVER_RESPONSE_ERROR_CODE_NO_PERMISSION_TO_ENTER_ROOM) {
                            MyLog.w(TAG, "CODE_SERVER_RESPONSE_ERROR_CODE_NO_PERMISSION_TO_ENTER_ROOM");
                            EventBus.getDefault().post(new EventClass.KickEvent());
                        } else {
                            BarrageMsg.LiveEndMsgExt ext = new BarrageMsg.LiveEndMsgExt();
                            ext.viewerCount = 0;
                            if (enterRoomInfo.getRetCode() == ErrorCode.CODE_PARAM_ERROR) {
                                ToastUtils.showToast(R.string.token_live_error_toast_room_not_exist);
                            }
                            BarrageMsg msg = SendBarrageManager.createBarrage(BarrageMsgType.B_MSG_TYPE_LIVE_END, GlobalData.app().getString(R.string.live_finish), mMyRoomData.getRoomId(), mMyRoomData.getUid(), System.currentTimeMillis(), ext);
                            SendBarrageManager.pretendPushBarrage(msg);
                        }
                        mHasEnter = true;
                    }
                });
    }

    @Override
    public void leaveLive() {
        RoomInfoGlobalCache.getsInstance().leaveCurrentRoom(mMyRoomData.getRoomId());
        LiveRoomQuery.leaveRoom(mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getGetMessageMode())
//                .compose(mRxActivity.<Boolean>bindUntilEvent())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        MyLog.d(TAG, "leaveLive onNext");
                    }
                });
    }


    @Override
    public void onActivityDestroy() {
        if (mPullRoomMessagePresenter != null) {
            mPullRoomMessagePresenter.stopWork();
            mPullRoomMessagePresenter.destroy();
        }
    }

    @Override
    public void onActivityCreate() {

    }
}
