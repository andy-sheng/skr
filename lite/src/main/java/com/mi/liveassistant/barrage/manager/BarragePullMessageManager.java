package com.mi.liveassistant.barrage.manager;

import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.processor.BarrageMainProcessor;
import com.mi.liveassistant.barrage.request.PullRoomMsgRequest;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.proto.LiveMessageProto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 拉取弹幕
 *
 * Created by wuxiaoshan on 17-4-28.
 */
public class BarragePullMessageManager {

    private static final String TAG = BarragePullMessageManager.class.getSimpleName();

    private String mRoomId;

    private ExecutorService mSingleThread;

    //弹幕是push模式
    public static final int PUSH_MODE = 0;
    //弹幕是pull模式
    public static final int PULL_MODE = 1;
    //是否运行
    private boolean isRunning;

    private long mLastSyncImportantTs;
    private long mLastSyncNormalTs;
     //拉取的时间间隔
    private long mSyncInterval;
     //上一次拉取间隔
    private long mLastPullTs;

    private BarrageMainProcessor mBarrageProcesser;

    public BarragePullMessageManager(String roomId){
        mRoomId = roomId;
        mSyncInterval = 5000;
        mSingleThread = Executors.newSingleThreadExecutor();
    }


    private Subscription mPullRoomMessageSubscription;

    private Subscription mDelayPullSubscriber;

    public void start(){
        MyLog.w(TAG,"start");
        isRunning = true;
        mBarrageProcesser = BarrageMainProcessor.getInstance();
        pullMsg();
    }

    public void stop(){
        MyLog.w(TAG,"stop");
        isRunning = false;
        if(mPullRoomMessageSubscription != null && mPullRoomMessageSubscription.isUnsubscribed()){
            mPullRoomMessageSubscription.unsubscribe();
            mPullRoomMessageSubscription = null;
        }
        if(mDelayPullSubscriber != null && mDelayPullSubscriber.isUnsubscribed()){
            mDelayPullSubscriber.unsubscribe();
            mDelayPullSubscriber = null;
        }
        mSingleThread.execute(new Runnable() {
            @Override
            public void run() {
                mBarrageProcesser = null;
            }
        });
        mSingleThread.shutdown();
    }

    public boolean isRunning(){
        return isRunning;
    }

    private void pullMsg(){
        if(!isRunning){
            return;
        }
        MyLog.d(TAG,"pull msg");
        if(mPullRoomMessageSubscription != null && mPullRoomMessageSubscription.isUnsubscribed()){
            mPullRoomMessageSubscription.unsubscribe();
            mPullRoomMessageSubscription = null;
        }
        mPullRoomMessageSubscription = Observable.create(new Observable.OnSubscribe<LiveMessageProto.SyncRoomMessageResponse>() {
            @Override
            public void call(Subscriber<? super LiveMessageProto.SyncRoomMessageResponse> subscriber) {
                try {
                    PullRoomMsgRequest request = new PullRoomMsgRequest(UserAccountManager.getInstance().getUuidAsLong(), mRoomId, mLastSyncImportantTs, mLastSyncNormalTs);
                    LiveMessageProto.SyncRoomMessageResponse response = request.syncRsp();
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                }catch (Exception e){
                    MyLog.e(TAG,e);
                    subscriber.onError(e);
                }
            }
        })
                .flatMap(new Func1<LiveMessageProto.SyncRoomMessageResponse, Observable<?>>() {
                    @Override
                    public Observable<?> call(LiveMessageProto.SyncRoomMessageResponse response) {
                        mLastSyncImportantTs = response.getCurrentSyncImportantTs();
                        mLastSyncNormalTs = response.getCurrentSyncNormalTs();
                        mSyncInterval = response.getSyncInterval() * 1000;
                        mLastPullTs = System.currentTimeMillis();
                        A temp = new A();
                        temp.importList = message2Barrage(response.getImportantRoomMsgList());
                        temp.normalList = message2Barrage(response.getNormalRoomMsgList());
                        return Observable.just(temp);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.from(mSingleThread))
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        delayPull(mSyncInterval);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "startWorkInternal:" + e);
                        delayPull(mSyncInterval);
                    }

                    @Override
                    public void onNext(Object temp) {
                        A a = (A) temp;
                        if (a != null) {
                            List<BarrageMsg> l1 = a.importList;
                            List<BarrageMsg> l2 = a.normalList;
                            MyLog.d(TAG, "startWorkInternal result list size:" + (l1.size() + l2.size()));
                            // 进队列
                            if(mBarrageProcesser != null){
                                mBarrageProcesser.enterRenderQueue(l1,l2);
                            }
                        }
                    }
                });

    }

    private void delayPull(long delay) {
        if (!isRunning) {
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
                        pullMsg();
                    }
                });
    }

    static class A {
        public List<BarrageMsg> normalList;
        public List<BarrageMsg> importList;
    }

    private static List<BarrageMsg> message2Barrage(List<LiveMessageProto.Message> list) {
        List<BarrageMsg> resultList = new ArrayList<>();
        if (list != null) {
            for (LiveMessageProto.Message m : list) {
                BarrageMsg barrage = BarrageMsg.toBarrageMsg(m);
                if(barrage != null) {
                    resultList.add(BarrageMsg.toBarrageMsg(m));
                }
            }
        }
        return resultList;
    }




}
