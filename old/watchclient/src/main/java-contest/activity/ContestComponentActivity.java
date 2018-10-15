package activity;

import android.os.Bundle;
import android.os.Message;
import android.support.annotation.WorkerThread;
import android.util.SparseArray;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.event.BarrageMsgEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.milink.sdk.base.CustomHandlerThread;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.List;

/**
 * Created by zyh on 2018/1/12.
 */
public class ContestComponentActivity extends BaseSdkActivity {
    protected RoomBaseDataModel mMyRoomData;

    protected long mLastProcessPushTime = 0;
    protected boolean mPushProcessing = false;

    protected SparseArray<HashSet<IPushMsgProcessor>> mIPushMsgProcessorMap = new SparseArray<>();
    // 专门弄个线程池来处理push
    private CustomHandlerThread sPushThreadPool = new CustomHandlerThread("sPushThreadPool") {
        @Override
        protected void processMessage(Message message) {

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMyRoomData = new RoomBaseDataModel(TAG);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sPushThreadPool != null) {
            sPushThreadPool.destroy();
        }
    }

    public void addPushProcessor(IPushMsgProcessor processor) {
        if (processor == null) {
            return;
        }
        for (int msgType : processor.getAcceptMsgType()) {
            HashSet<IPushMsgProcessor> set = mIPushMsgProcessorMap.get(msgType);

            if (set != null) {
                set.add(processor);
            } else {
                set = new HashSet<>();
                set.add(processor);
                mIPushMsgProcessorMap.put(msgType, set);
            }
        }
        //加入activity生命周期管理
        addPresent(processor);
    }


    public void removePushProcessor(IPushMsgProcessor processor) {
        if (processor == null) {
            return;
        }
        for (int msgType : processor.getAcceptMsgType()) {
            HashSet<IPushMsgProcessor> set = mIPushMsgProcessorMap.get(msgType);
            if (set != null) {
                set.remove(processor);
            }
        }
        //取消activity生命周期管理
        removePresent(processor);
    }

    protected void clearPushMsgList() {
        sPushThreadPool.getHandler().removeCallbacksAndMessages(null);
    }

    //返回true代表已经处理过了，子类无需再处理。3个Activity处理push都相同的push逻辑可 以放在这，以后只要在这修改就行了。
    @WorkerThread
    protected void processPushMessage(BarrageMsg msg, RoomBaseDataModel roomData) {
        HashSet<IPushMsgProcessor> set = mIPushMsgProcessorMap.get(msg.getMsgType());
        if (set != null) {
            for (IPushMsgProcessor msgProcessor : set) {
                msgProcessor.process(msg, roomData);
            }
        } else {
            MyLog.w(TAG, "recv this msg but no processor,check the code!!!msg:" + msg);
        }
    }

    protected void processPushMsgList(List<BarrageMsg> pushMsgList) {
        if (pushMsgList == null) {
            return;
        }
        for (BarrageMsg msg : pushMsgList) {
            if (msg == null || null == mMyRoomData) {
                continue;
            }
            // 不是本房间且不是系统消息一律不处理
            // 如果不是本房间的消息（没明确指定是本房间的消息），指定白名单，在白名单里的消息类型不会被丢弃
            if (!mMyRoomData.getRoomId().equals(msg.getRoomId())) {
                if (!BarrageMsgType.doNotCareRoomId(msg.getMsgType())) {
                    MyLog.w(TAG, "not this room msg ,my_room_id:" + mMyRoomData.getRoomId()
                            + ", msg_room_id:" + msg.getRoomId() + ", drop it");
                    continue;
                } else {

                }
            }
            // 处理自己房间的push消息
            MyLog.d(TAG, "processPushMsgList msg=" + msg.getBody() + ",msgType" + msg.getMsgType());
            processPushMessage(msg, mMyRoomData);
            MyLog.d(TAG, "processPushMsgList msg=" + msg.getBody() + ",msgType" + msg.getMsgType() + " over");
        }//end-for
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(final BarrageMsgEvent.ReceivedBarrageMsgEvent event) {
//        MyLog.d(TAG, "onEvent(BarrageMsgEvent.ReceivedBarrageMsgEvent event from:" + event.from);
//        MyLog.d(TAG, "isAlive:" + sPushThreadPool.getHandlerThread().isAlive() + ",isInterrupted:" + sPushThreadPool.getHandlerThread().isInterrupted());
//        checkPushThreadPoolIsDead();
        sPushThreadPool.post(new Runnable() {
            @Override
            public void run() {
                mPushProcessing = true;
                mLastProcessPushTime = System.currentTimeMillis();
                if (event == null || isFinishing()) {
                    return;
                }
                if (mMyRoomData == null || null == mMyRoomData.getRoomId()) {
                    return;
                }
                processPushMsgList(event.getMsgList());
                mPushProcessing = false;
            }
        });
    }
}
