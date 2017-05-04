package com.mi.liveassistant.barrage.processer;

import android.util.SparseArray;

import com.mi.liveassistant.barrage.callback.MessageCallBack;
import com.mi.liveassistant.barrage.converter.GiftMsgConverter;
import com.mi.liveassistant.barrage.converter.IConverter;
import com.mi.liveassistant.barrage.converter.RoomManageMsgConverter;
import com.mi.liveassistant.barrage.converter.RoomStatusMsgConverter;
import com.mi.liveassistant.barrage.converter.RoomSystemMsgConverter;
import com.mi.liveassistant.barrage.converter.RoomTextMsgConverter;
import com.mi.liveassistant.barrage.converter.RoomViewerChangeMsgConverter;
import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.common.log.MyLog;
import com.trello.rxlifecycle.ActivityEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
 * Created by wuxiaoshan on 17-5-2.
 */
public class BarrageMainProcesser {

    private static final String TAG = BarrageMainProcesser.class.getSimpleName();

    private static BarrageMainProcesser mInstance = new BarrageMainProcesser();

    private List<MessageCallBack> mCallBackList = new ArrayList<>();

    protected SparseArray<HashSet<IConverter>> mMsgConverterMap = new SparseArray<>();

    private ExecutorService singleThread = Executors.newSingleThreadExecutor();

    /**
     * 需要绘制的队列
     */
    private LinkedList<Message> mRenderQueue = new LinkedList<>();

    /**
     * 绘制间隔
     */
    public static final int INTERVAL = 200;

    public static final int MAX_DISPLAY_NUMBER_INTERVAL = 50;

    private static final int SYNC_INTERVAL = 5000;


    private Subscription mNotifyRenderSubscriber;

    private long mLastPullTs;

    private BarrageMainProcesser() {
        initConverter();
    }

    public static BarrageMainProcesser getInstance() {
        return mInstance;
    }

    public void registCallBack(final MessageCallBack callBack) {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                mCallBackList.add(callBack);
            }
        });
    }

    public void unregistCallBack(final MessageCallBack callBack) {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                mCallBackList.remove(callBack);
            }
        });
    }

    public void enterRenderQueue(final List<BarrageMsg> importantList, final List<BarrageMsg> normalList) {
        if (mCallBackList == null || mCallBackList.size() == 0) {
            return;
        }
        mLastPullTs = System.currentTimeMillis();
        Observable.just(new ArrayList<Message>(mRenderQueue))
                .onBackpressureBuffer()
                .observeOn(Schedulers.from(singleThread))
                .map(new Func1<ArrayList<Message>, ArrayList<Message>>() {
                    @Override
                    public ArrayList<Message> call(ArrayList<Message> sortList) {
                        for (BarrageMsg barrageMsg : importantList) {
                            sortList.add(Message.loadFromBarrage(barrageMsg));
                        }
                        for (BarrageMsg barrageMsg : normalList) {
                            sortList.add(Message.loadFromBarrage(barrageMsg));
                        }
                        Collections.sort(sortList, new Comparator<Message>() {
                            @Override
                            public int compare(Message lhs, Message rhs) {
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
        if (mCallBackList == null || mCallBackList.size() == 0) {
            return;
        }
        mLastPullTs = System.currentTimeMillis();
        Observable.just(new ArrayList<Message>(mRenderQueue))
                .onBackpressureBuffer()
                .observeOn(Schedulers.from(singleThread))
                .map(new Func1<ArrayList<Message>, ArrayList<Message>>() {
                    @Override
                    public ArrayList<Message> call(ArrayList<Message> sortList) {
                        for (BarrageMsg barrageMsg : barrageMsgList) {
                            sortList.add(Message.loadFromBarrage(barrageMsg));
                        }
                        Collections.sort(sortList, new Comparator<Message>() {
                            @Override
                            public int compare(Message lhs, Message rhs) {
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
        if (mCallBackList == null || mCallBackList.size() == 0) {
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
                            List<Message> temp = new ArrayList();
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
                            List<Message> temp = new ArrayList();
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
        if (mCallBackList == null || mCallBackList.size() == 0) {
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
    private void initConverter() {
        addConverter(new GiftMsgConverter());
        addConverter(new RoomManageMsgConverter());
        addConverter(new RoomStatusMsgConverter());
        addConverter(new RoomSystemMsgConverter());
        addConverter(new RoomTextMsgConverter());
        addConverter(new RoomViewerChangeMsgConverter());

    }

    private void addConverter(IConverter converter) {
        if (converter == null) {
            return;
        }
        for (int msgType : converter.getAcceptMsgType()) {
            HashSet<IConverter> set = mMsgConverterMap.get(msgType);
            if (set != null) {
                set.add(converter);
            } else {
                set = new HashSet<>();
                set.add(converter);
                mMsgConverterMap.put(msgType, set);
            }
        }
    }

    private List barrageConvert(BarrageMsg barrageMsg) {
        List list = new ArrayList();
        HashSet<IConverter> convertSet = mMsgConverterMap.get(barrageMsg.getMsgType());
        for (IConverter converter : convertSet) {
            list.addAll(converter.barrageConvert(barrageMsg));
        }
        return list;
    }

    private void renderRoomMsg(List<Message> messageList) {
        if (mCallBackList == null || mCallBackList.size() == 0) {
            return;
        }
        for (MessageCallBack callBack : mCallBackList) {
            callBack.handleMessage(messageList);
        }
    }


}
