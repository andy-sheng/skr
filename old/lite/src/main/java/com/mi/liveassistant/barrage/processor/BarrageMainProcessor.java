package com.mi.liveassistant.barrage.processor;

import android.util.SparseArray;

import com.mi.liveassistant.barrage.callback.IChatMsgListener;
import com.mi.liveassistant.barrage.callback.ISysMsgListener;
import com.mi.liveassistant.barrage.callback.InternalMsgListener;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.common.log.MyLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
 * Created by wuxiaoshan on 17-5-2.
 */
public class BarrageMainProcessor implements IMsgDispenser {

    private static final String TAG = BarrageMainProcessor.class.getSimpleName();

    private static BarrageMainProcessor mInstance = new BarrageMainProcessor();

    private IChatMsgListener mChatMsgListener;

    private ISysMsgListener mSysMsgListener;

    private InternalMsgListener mInternalMsgListener;

    private String mRoomId;

    protected SparseArray<HashSet<MsgProcessor>> mMsgProcessorMap = new SparseArray<>();

    private ExecutorService singleThread = Executors.newSingleThreadExecutor();

    /**
     * 需要绘制的队列
     */
    private LinkedList<BarrageMsg> mRenderQueue = new LinkedList<>();

    /**
     * 绘制间隔
     */
    public static final int INTERVAL = 200;

    public static final int MAX_DISPLAY_NUMBER_INTERVAL = 50;

    private static final int SYNC_INTERVAL = 5000;

    private long mSyncInterval = SYNC_INTERVAL;

    private Subscription mNotifyRenderSubscriber;

    private long mLastPullTs;

    private BarrageMainProcessor() {
        initProcessor();
    }

    public static BarrageMainProcessor getInstance() {
        return mInstance;
    }

    public void init(final String roomId, final IChatMsgListener chatMsgListener, final ISysMsgListener sysMsgListener) {
        mRoomId = roomId;
        mChatMsgListener = chatMsgListener;
        mSysMsgListener = sysMsgListener;
    }

    public void destroy() {
        mRoomId = null;
        mChatMsgListener = null;
        mSysMsgListener = null;
    }

    public String getRoomId() {
        return mRoomId;
    }

    public void registerInternalMsgListener(final InternalMsgListener internalMsgListener) {
        mInternalMsgListener = internalMsgListener;
    }

    public void unregisterInternalMsgListener() {
        mInternalMsgListener = null;
    }

    public void enterRenderQueue(final List<BarrageMsg> importantList, final List<BarrageMsg> normalList, long lastPullTs, long syncInterval) {
        if (mChatMsgListener == null && mSysMsgListener == null && mInternalMsgListener == null) {
            return;
        }
        mLastPullTs = lastPullTs;
        mSyncInterval = syncInterval;
        Observable.just(new ArrayList<BarrageMsg>(mRenderQueue))
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

    public void enterRenderQueue(final List<BarrageMsg> barrageMsgList) {
        if (mChatMsgListener == null && mSysMsgListener == null && mInternalMsgListener == null) {
            return;
        }
//        mLastPullTs = System.currentTimeMillis();
        Observable.just(0)
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .map(new Func1<Integer, List<BarrageMsg>>() {
                    @Override
                    public List<BarrageMsg> call(Integer i) {
                        Collections.sort(barrageMsgList, new Comparator<BarrageMsg>() {
                            @Override
                            public int compare(BarrageMsg lhs, BarrageMsg rhs) {
                                return (int) (lhs.getSentTime() - rhs.getSentTime());
                            }
                        });
                        return barrageMsgList;
                    }
                })
                .observeOn(Schedulers.from(singleThread))
                .subscribe(new Subscriber<List<BarrageMsg>>() {
                    @Override
                    public void onCompleted() {
                        MyLog.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "enterRenderQueue:" + e);
                    }

                    @Override
                    public void onNext(List<BarrageMsg> roomMsgs) {
                        renderRoomMsg(roomMsgs);
                    }
                });
    }

    /*ui线程*/
    private void notifyRender() {
        if (mChatMsgListener == null && mSysMsgListener == null && mInternalMsgListener == null) {
            if (mNotifyRenderSubscriber != null && !mNotifyRenderSubscriber.isUnsubscribed()) {
                mNotifyRenderSubscriber.unsubscribe();
                mNotifyRenderSubscriber = null;
            }
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

    private void delayNotifyRender(long interval) {
        if (mChatMsgListener == null && mSysMsgListener == null && mInternalMsgListener == null) {
            return;
        }
        Observable.timer(interval, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        notifyRender();
                    }
                });
    }

    /**
     * 初始化消息转换类的列表
     */
    private void initProcessor() {
        addProcessor(new GiftMsgProcessor(this));
        addProcessor(new RoomManageMsgProcessor(this));
        addProcessor(new RoomStatusMsgProcessor(this));
        addProcessor(new RoomSystemMsgProcessor(this));
        addProcessor(new RoomTextMsgProcessor(this));
        addProcessor(new RoomViewerChangeMsgProcessor(this));

    }

    private void addProcessor(MsgProcessor msgProcessor) {
        if (msgProcessor == null) {
            return;
        }
        for (int msgType : msgProcessor.getAcceptMsgType()) {
            HashSet<MsgProcessor> set = mMsgProcessorMap.get(msgType);
            if (set != null) {
                set.add(msgProcessor);
            } else {
                set = new HashSet<>();
                set.add(msgProcessor);
                mMsgProcessorMap.put(msgType, set);
            }
        }
    }

    private void renderRoomMsg(List<BarrageMsg> messageList) {
        if (mChatMsgListener == null && mSysMsgListener == null && mInternalMsgListener == null) {
            return;
        }
        for (BarrageMsg barrageMsg : messageList) {
            MyLog.d(TAG, "barrage msg type:" + barrageMsg.getMsgType());
            Set<MsgProcessor> processors = mMsgProcessorMap.get(barrageMsg.getMsgType());
            if (processors != null) {
                for (MsgProcessor processor : processors) {
                    processor.process(barrageMsg, mRoomId);
                }
            } else {
                MyLog.d(TAG, "message type:" + barrageMsg.getMsgType() + "\tnot have processor");
            }
        }
    }

    @Override
    public void addChatMsg(List<Message> messageList) {
        if (mChatMsgListener != null) {
            mChatMsgListener.handleMessage(messageList);
        }
    }

    @Override
    public void addChatMsg(Message message) {
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        if (mChatMsgListener != null) {
            mChatMsgListener.handleMessage(messages);
        }
    }

    @Override
    public void addSysMsg(List<Message> messageList) {
        if (mSysMsgListener != null) {
            mSysMsgListener.handleMessage(messageList);
        }
    }

    @Override
    public void addInternalMsgListener(List<Message> messageList) {
        if (mInternalMsgListener != null) {
            mInternalMsgListener.handleMessage(messageList);
        }
    }
}
