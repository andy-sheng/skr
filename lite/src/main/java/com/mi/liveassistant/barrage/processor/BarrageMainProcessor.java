package com.mi.liveassistant.barrage.processor;

import android.util.SparseArray;

import com.mi.liveassistant.barrage.callback.ChatMsgCallBack;
import com.mi.liveassistant.barrage.callback.InternalMsgCallBack;
import com.mi.liveassistant.barrage.callback.SysMsgCallBack;
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

    private ChatMsgCallBack mChatMsgCallBack;

    private SysMsgCallBack mSysMsgCallBack;

    private InternalMsgCallBack mInternalMsgCallBack;

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

    private Subscription mNotifyRenderSubscriber;

    private long mLastPullTs;

    private BarrageMainProcessor() {
        initProcessor();
    }

    public static BarrageMainProcessor getInstance() {
        return mInstance;
    }

    public void init(final String roomId, final ChatMsgCallBack chatMsgCallBack, final SysMsgCallBack sysMsgCallBack) {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                mRoomId = roomId;
                mChatMsgCallBack = chatMsgCallBack;
                mSysMsgCallBack = sysMsgCallBack;
            }
        });
    }

    public void destroy() {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                mRoomId = null;
                mChatMsgCallBack = null;
                mSysMsgCallBack = null;
            }
        });
    }

    public void registInternalMsgCallBack(final InternalMsgCallBack internalMsgCallBack) {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                mInternalMsgCallBack = internalMsgCallBack;
            }
        });
    }

    public void unregistInternalMsgCallBack() {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                mInternalMsgCallBack = null;
            }
        });
    }

    public void enterRenderQueue(final List<BarrageMsg> importantList, final List<BarrageMsg> normalList) {
        if (mChatMsgCallBack == null && mSysMsgCallBack == null && mInternalMsgCallBack == null) {
            return;
        }
        mLastPullTs = System.currentTimeMillis();
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
        if (mChatMsgCallBack == null && mSysMsgCallBack == null && mInternalMsgCallBack == null) {
            return;
        }
        mLastPullTs = System.currentTimeMillis();
        Observable.just(new ArrayList<BarrageMsg>(mRenderQueue))
                .onBackpressureBuffer()
                .observeOn(Schedulers.from(singleThread))
                .map(new Func1<ArrayList<BarrageMsg>, ArrayList<BarrageMsg>>() {
                    @Override
                    public ArrayList<BarrageMsg> call(ArrayList<BarrageMsg> sortList) {
                        sortList.addAll(barrageMsgList);
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

    /*ui线程*/
    private void notifyRender() {
        if (mChatMsgCallBack == null && mSysMsgCallBack == null && mInternalMsgCallBack == null) {
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
                        long leftTime = (mLastPullTs + SYNC_INTERVAL) - now;

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
        if (mChatMsgCallBack == null && mSysMsgCallBack == null && mInternalMsgCallBack == null) {
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
        if (mChatMsgCallBack == null && mSysMsgCallBack == null && mInternalMsgCallBack == null) {
            return;
        }
        for (BarrageMsg barrageMsg : messageList) {
            Set<MsgProcessor> processors = mMsgProcessorMap.get(barrageMsg.getMsgType());
            if(processors != null){
                for (MsgProcessor processor : processors) {
                    processor.process(barrageMsg, mRoomId);
                }
            }else{
                MyLog.w(TAG,"message type:"+barrageMsg.getMsgType()+"\tnot have processor");
            }
        }
    }

    public void addChatMsgRightNow(final BarrageMsg barrageMsg){
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                addChatMsg(Message.loadFromBarrage(barrageMsg));
            }
        });
    }

    @Override
    public void addChatMsg(List<Message> messageList) {
        if (mChatMsgCallBack != null) {
            mChatMsgCallBack.handleMessage(messageList);
        }
    }

    @Override
    public void addChatMsg(Message message) {
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        if (mChatMsgCallBack != null) {
            mChatMsgCallBack.handleMessage(messages);
        }
    }

    @Override
    public void addSysMsg(List<Message> messageList) {
        if (mSysMsgCallBack != null) {
            mSysMsgCallBack.handleMessage(messageList);
        }
    }

    @Override
    public void addInternalMsgCallBack(List<Message> messageList) {
        if (mInternalMsgCallBack != null) {
            mInternalMsgCallBack.handleMessage(messageList);
        }
    }
}
