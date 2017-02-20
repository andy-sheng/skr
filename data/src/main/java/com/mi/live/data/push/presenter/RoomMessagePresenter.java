package com.mi.live.data.push.presenter;

import com.base.activity.BaseActivity;
import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.presenter.Presenter;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.event.BarrageMsgEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.RoomMessageRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.proto.LiveMessageProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by chengsimin on 16/6/13.
 *
 * @module 房间消息
 */
public class RoomMessagePresenter implements Presenter {
    public static final String TAG = "RoomMessagePresenter";
    public static final int PUSH_MODE = 0;
    public static final int PULL_MODE = 1;
    /**
     * 绘制间隔
     */
    public static final int INTERVAL = 200;

    public static final int MAX_DISPLAY_NUMBER_INTERVAL = 50;

    private final RoomBaseDataModel mMyRoomData;
    private final RoomMessageRepository mRoomMessageRepository;

    private long mLastSyncImportantTs = 0;
    private long mLastSyncNormalTs = 0;
    private long mSyncInterval = 5000;
    /**
     * 上一次拉取间隔
     */
    private long mLastPullTs = 0;

    private RxActivity mRxActivity;

    public RoomMessagePresenter(RoomBaseDataModel roomBaseDataModel,
                                RoomMessageRepository roomMessageRepository, RxActivity rxActivity) {
        this.mMyRoomData = roomBaseDataModel;
        this.mRoomMessageRepository = roomMessageRepository;
        this.mRxActivity = rxActivity;
    }

    private boolean mCanRunning = false;

    /**
     * 开始拉取
     */
    public void startWork() {
        mLastSyncImportantTs = 0;
        mLastSyncNormalTs = 0;
        mCanRunning = true;
        startWorkInternal();
    }

    static int i = 0;

    private static List<BarrageMsg> loadFromPB(List<LiveMessageProto.Message> list) {
        List<BarrageMsg> resultList = new ArrayList<>();
        if (list != null) {
            for (LiveMessageProto.Message m : list) {
                resultList.add(BarrageMsg.toBarrageMsg(m));
            }
//            if (!resultList.isEmpty() && Constants.isDebugOrTestBuild) {
//                //TEST  测试代码
//                BarrageMsg ex = resultList.get(0);
//                int j = i + 20000;
//                while (i < j) {
//                    BarrageMsg msg = new BarrageMsg();
//                    msg.setSenderMsgId(i);
//                    msg.setMsgType(303);
//                    msg.setRoomId(ex.getRoomId());
//                    msg.setBody("当前处理的i" + i++);
//                    msg.setSender(10001);
//                    msg.setSenderName("程思敏测试");
//                    resultList.add(msg);
//                }
//                MyLog.d(TAG, "弹幕序号i已经到" + i);
//            }
        }
        return resultList;
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
        if (singleThread != null) {
            singleThread.shutdown();
        }
    }

    static class A {
        public List<BarrageMsg> normalList;
        public List<BarrageMsg> importList;
    }

    Subscription mPullRoomMessageSubscription;

    private void startWorkInternal() {
        MyLog.d(TAG, "startWorkInternal");
        if (!mCanRunning) {
            return;
        }
        if (mPullRoomMessageSubscription != null && !mPullRoomMessageSubscription.isUnsubscribed()) {
            mPullRoomMessageSubscription.unsubscribe();
            MyLog.d(TAG, "startWorkInternal unsubscribe");
        }
        mPullRoomMessageSubscription = mRoomMessageRepository.pullRoomMessage(UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getRoomId(), mLastSyncImportantTs, mLastSyncNormalTs)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<LiveMessageProto.SyncRoomMessageResponse, Observable<A>>() {
                    @Override
                    public Observable<A> call(LiveMessageProto.SyncRoomMessageResponse rsp) {
                        mLastSyncImportantTs = rsp.getCurrentSyncImportantTs();
                        mLastSyncNormalTs = rsp.getCurrentSyncNormalTs();
                        mSyncInterval = rsp.getSyncInterval() * 1000;
                        mLastPullTs = System.currentTimeMillis();
                        A temp = new A();
                        temp.importList = loadFromPB(rsp.getImportantRoomMsgList());
                        temp.normalList = loadFromPB(rsp.getNormalRoomMsgList());
                        return Observable.just(temp);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mRxActivity.bindUntilEvent(ActivityEvent.DESTROY))
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
                            enterRenderQueue(l1, l2);
                        }
                    }
                });
    }

    /**
     * 停止拉取
     */
    public void stopWork() {
        MyLog.d(TAG, "stopWork");
        if (mPullRoomMessageSubscription != null) {
            mPullRoomMessageSubscription.unsubscribe();
        }
        if (mDelayPullSubscriber != null) {
            mDelayPullSubscriber.unsubscribe();
        }
        if (mNotifyRenderSubscriber != null) {
            mNotifyRenderSubscriber.unsubscribe();
        }
        if (mEnterRenderQueueSubscription != null) {
            mEnterRenderQueueSubscription.unsubscribe();
        }
        if (mDelayNotifyRenderSubscription != null) {
            mDelayNotifyRenderSubscription.unsubscribe();
        }
        mCanRunning = false;
        mRenderQueue.clear();
    }

    private ExecutorService singleThread = Executors.newSingleThreadExecutor(); // 送礼的线程池

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
                .compose(mRxActivity.bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        startWorkInternal();
                    }
                });
    }

    /**
     * 需要绘制的队列
     */
    private LinkedList<BarrageMsg> mRenderQueue = new LinkedList<>();

    private Subscription mEnterRenderQueueSubscription;

    /*ui线程*/
    private void enterRenderQueue(final List<BarrageMsg> importantList, final List<BarrageMsg> normalList) {
        if (!mCanRunning) {
            return;
        }
        mEnterRenderQueueSubscription = Observable.just(new ArrayList<BarrageMsg>(mRenderQueue))
                .onBackpressureBuffer()
                .observeOn(Schedulers.from(singleThread))
                .map(new Func1<ArrayList<BarrageMsg>, ArrayList<BarrageMsg>>() {
                    @Override
                    public ArrayList<BarrageMsg> call(ArrayList<BarrageMsg> sortList) {
                        sortList.addAll(importantList);
                        sortList.addAll(normalList);
                        Collections.sort(sortList, new Comparator<BarrageMsg>() {
                            @Override
                            public int compare(BarrageMsg lhs, BarrageMsg rhs) {
                                return (int) (lhs.getSentTime() - rhs.getSentTime());
                            }
                        });
                        mRenderQueue.clear();
                        mRenderQueue.addAll(sortList);
                        return sortList;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(mRxActivity.bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        notifyRender();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "enterRenderQueue:" + e);
                        notifyRender();
                    }

                    @Override
                    public void onNext(Object roomMsgs) {

                    }
                });

    }

    private Subscription mNotifyRenderSubscriber;

    /*ui线程*/
    private void notifyRender() {
        if (!mCanRunning) {
            return;
        }
        MyLog.d(TAG, "notifyRender");
        if (mRenderQueue.isEmpty()) {
            return;
        }
        //以200ms为单位，算当前这次应该取多少，取得数量 = 管道数量/()
        if (mNotifyRenderSubscriber != null && !mNotifyRenderSubscriber.isUnsubscribed()) {
            return;
        }
        mNotifyRenderSubscriber = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                subscriber.onNext(mRenderQueue);
                subscriber.onCompleted();
            }
        })
                .observeOn(Schedulers.from(singleThread))
                .map(new Func1<Object, Object>() {
                    @Override
                    public Object call(Object o) {
                        long now = System.currentTimeMillis();
                        long leftTime = (mLastPullTs + mSyncInterval) - now;

                        if (leftTime <= 0) {
                            //已经超时了，还有没展示完的，全部展示
                            List<BarrageMsg> temp = new ArrayList();
                            if (mRenderQueue.size() > MAX_DISPLAY_NUMBER_INTERVAL) {
                                temp.addAll(mRenderQueue.subList(0, MAX_DISPLAY_NUMBER_INTERVAL));
                            } else {
                                temp.addAll(mRenderQueue);
                            }
                            mRenderQueue.clear();
                            renderRoomMsg(temp);
                        } else {
                            int size = mRenderQueue.size();
                            int times = (int) (leftTime / INTERVAL + 1);
                            int number = size / times;
                            if (number <= 0) {
                                number = 1;
                            }
                            List<BarrageMsg> temp = new ArrayList();
                            int i = 0;
                            while (!mRenderQueue.isEmpty() && i < number && i < MAX_DISPLAY_NUMBER_INTERVAL) {
                                temp.add(mRenderQueue.pollFirst());
                                i++;
                            }
                            renderRoomMsg(temp);
                        }
                        return leftTime;
                    }
                })
                .compose(mRxActivity.bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        delayNotifyRender(INTERVAL);
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "notifyRender:" + e);
                        delayNotifyRender(INTERVAL);
                    }

                    @Override
                    public void onNext(Object r) {

                    }
                });

    }

    private Subscription mDelayNotifyRenderSubscription;

    private void delayNotifyRender(long interval) {
        if (!mCanRunning) {
            return;
        }
        mDelayNotifyRenderSubscription = Observable.timer(interval, TimeUnit.MILLISECONDS)
                .compose(mRxActivity.bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        notifyRender();
                    }
                });
    }

    private void renderRoomMsg(List<BarrageMsg> list) {
        if (!mCanRunning) {
            return;
        }
        if (list != null) {
            MyLog.d(TAG, "renderRoomMsg l.size:" + list.size());
            EventBus.getDefault().post(new BarrageMsgEvent.ReceivedBarrageMsgEvent(list, "renderRoomMsg"));
        }
    }
}
