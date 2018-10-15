package com.wali.live.watchsdk.ipc.api;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.repository.RoomMessageRepository;
import com.mi.live.data.repository.datasource.RoomMessageStore;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.watchsdk.ipc.service.BarrageInfo;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class BarragePullManager {

    public static final String TAG = "BarragePullManager";

    private final RoomMessageRepository mRoomMessageRepository;

    private long mLastSyncImportantTs = 0;
    private long mLastSyncNormalTs = 0;
    private long mSyncInterval = 5000;
    private String mRoomId;
    private HashSet<Integer> mMsgTypeSet = new HashSet<>();
    private boolean mCanRunning = false;
    private int mChannelId;

    private static class BarragePullManagerHolder {
        private static final BarragePullManager INSTANCE = new BarragePullManager();
    }

    public static final BarragePullManager getInstance() {
        return BarragePullManagerHolder.INSTANCE;
    }

    private BarragePullManager() {
        this.mRoomMessageRepository = new RoomMessageRepository(new RoomMessageStore());
    }

    /**
     * 开始拉取
     * 如何保护避免进入死循环
     */
    public void startWork(int channelId, String roomId, int[] msgType) {
        mChannelId = channelId;
        mLastSyncImportantTs = 0;
        mLastSyncNormalTs = 0;
        mCanRunning = true;
        mRoomId = roomId;
        mMsgTypeSet.clear();
        for (int type : msgType) {
            mMsgTypeSet.add(type);
        }
        startWorkInternal();
    }

    Subscription mPullRoomMessageSubscription;

    private void startWorkInternal() {
        MyLog.d(TAG, "startWorkInternal");
        if (mChannelId != HostChannelManager.getInstance().getChannelId()) {
            return;
        }
        if (!mCanRunning) {
            return;
        }
        if (mPullRoomMessageSubscription != null && !mPullRoomMessageSubscription.isUnsubscribed()) {
            MyLog.d(TAG, "startWorkInternal unsubscribe");
            return;
        }
        mPullRoomMessageSubscription = mRoomMessageRepository.pullRoomMessage(UserAccountManager.getInstance().getUuidAsLong(), mRoomId, mLastSyncImportantTs, mLastSyncNormalTs)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<LiveMessageProto.SyncRoomMessageResponse, Observable<List<BarrageInfo>>>() {
                    @Override
                    public Observable<List<BarrageInfo>> call(LiveMessageProto.SyncRoomMessageResponse rsp) {
                        mLastSyncImportantTs = rsp.getCurrentSyncImportantTs();
                        mLastSyncNormalTs = rsp.getCurrentSyncNormalTs();
                        mSyncInterval = rsp.getSyncInterval() * 1000;
                        List<BarrageInfo> result = new ArrayList<>();
                        for (LiveMessageProto.Message m : rsp.getImportantRoomMsgList()) {
                            if (mMsgTypeSet.contains(m.getMsgType())) {
                                result.add(toBarrageInfo(m));
                            }
                        }
                        for (LiveMessageProto.Message m : rsp.getNormalRoomMsgList()) {
                            if (mMsgTypeSet.contains(m.getMsgType())) {
                                result.add(toBarrageInfo(m));
                            }
                        }
                        return Observable.just(result);
                    }
                })
                .subscribe(new Subscriber<List<BarrageInfo>>() {
                    @Override
                    public void onCompleted() {
                        if (mSyncInterval < 5000) {
                            mSyncInterval = 5000;
                        }
                        delayPull(mSyncInterval);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "startWorkInternal:" + e);
                        delayPull(mSyncInterval);
                    }

                    @Override
                    public void onNext(List<BarrageInfo> temp) {
                        if (!temp.isEmpty()) {
                            // 进队列
                            MiLiveSdkBinder.getInstance().onEventRecvBarrage(mChannelId, temp);
                        }
                    }
                });
    }

    private BarrageInfo toBarrageInfo(LiveMessageProto.Message m) {
        BarrageInfo barrageInfo = new BarrageInfo();
        barrageInfo.setSender(m.getFromUser());
        barrageInfo.setRoomId(m.getRoomId());
        barrageInfo.setBody(m.getMsgBody());
        barrageInfo.setSenderName(m.getFromUserNickName());
        barrageInfo.setAnchorId(m.getToUser());
        barrageInfo.setMsgType(m.getMsgType());
        String ext = "";
        barrageInfo.setExt(ext);
        return barrageInfo;
    }

    /**
     * 停止拉取
     */
    public void stopWork(String roomid) {
        if (TextUtils.isEmpty(mRoomId) && mRoomId.equals(roomid)) {
            MyLog.d(TAG, "stopWork");
            if (mPullRoomMessageSubscription != null) {
                mPullRoomMessageSubscription.unsubscribe();
            }
            if (mDelayPullSubscriber != null) {
                mDelayPullSubscriber.unsubscribe();
            }
            mCanRunning = false;
        }
    }

    private Subscription mDelayPullSubscriber;

    private void delayPull(long delay) {
        if (!mCanRunning) {
            return;
        }
        MyLog.d(TAG, "delayPull delay:" + delay);
        if (mDelayPullSubscriber != null && !mDelayPullSubscriber.isUnsubscribed()) {
            MyLog.d(TAG, "delayPull delay already lauch timer");
            return;
        }
        mDelayPullSubscriber = Observable.timer(delay, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        startWorkInternal();
                    }
                });
    }

}
