package com.wali.live.watchsdk.base;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.SparseArray;

import com.base.activity.BaseRotateSdkActivity;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.FragmentListener;
import com.base.log.MyLog;
import com.mi.live.data.account.event.AccountEventController;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.collection.InsertSortLinkedList;
import com.mi.live.data.push.event.BarrageMsgEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.common.gift.view.GiftRoomEffectView;
import com.wali.live.event.EventClass;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.active.KeepActiveProcessor;
import com.wali.live.watchsdk.login.LoginPresenter;
import com.wali.live.watchsdk.watch.event.WatchOrReplayActivityCreated;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.watch.presenter.ExpLevelPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by linjinbin on 16/5/1.
 *
 * @module 基础模块
 */
public abstract class BaseComponentSdkActivity extends BaseRotateSdkActivity {
    public static final String EXTRA_ROOM_INFO = "extra_room_info";

    /**
     * 控制是否活跃，活跃的标准为LiveActivity,WatchActivity,ReplayActivity其中之一没有destroy
     * 控制是否活跃的目的为：当渠道A活跃时其他渠道不能够调用登录接口
     */
    private static AtomicInteger activeNum = new AtomicInteger(0);

    private MyAlertDialog mLogOffDialog;
    private ExpLevelPresenter mExpLevelPresenter;

    private boolean hasKicked;

    public static boolean isActive() {
        return activeNum.get() != 0;
    }

    public static void decrementActive() {
        int num = activeNum.decrementAndGet();
        if (num < 0) {
            activeNum.set(0);
        }
    }

    /**
     * 子类除了长经验值外的分享行为添加在子类的onEventShare
     *
     * @param event
     */
    protected void onEventShare(EventClass.ShareEvent event) {
    }

    /**
     * 本房间相关信息
     */
    protected RoomBaseDataModel mMyRoomData = new RoomBaseDataModel("sdk_Myroominfo");
    /**
     * 房间弹幕管理
     */
    protected LiveRoomChatMsgManager mRoomChatMsgManager = new LiveRoomChatMsgManager(InsertSortLinkedList.DEFAULT_MAX_SIZE);

    protected SparseArray<HashSet<IPushMsgProcessor>> mIPushMsgProcessorMap = new SparseArray<>();

    protected GiftRoomEffectView mGiftRoomEffectView;

    /**
     * bean类放在这里
     */
    protected RoomInfo mRoomInfo = null;

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

    private LoginPresenter mLoginPresenter;

    protected final boolean fragmentBackPressed(Fragment fragment) {
        if (fragment != null && fragment instanceof FragmentListener) {
            if (((FragmentListener) fragment).onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(R.color.color_black);
        // 通知别的观看的activity关闭
        EventBus.getDefault().post(new WatchOrReplayActivityCreated());
        activeNum.incrementAndGet();
        KeepActiveProcessor.stopActive();
        super.onCreate(savedInstanceState);
    }

    @Subscribe
    public void onEvent(WatchOrReplayActivityCreated event) {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMyRoomData.setIsForeground(mIsForeground);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMyRoomData.setIsForeground(mIsForeground);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        decrementActive();
        if (mGiftRoomEffectView != null) {
            mGiftRoomEffectView.onActivityDestroy();
        }

        if (sPushThreadPool != null) {
            sPushThreadPool.destroy();
        }
    }

    // 专门弄个线程池来处理push
    private CustomHandlerThread sPushThreadPool = new CustomHandlerThread("sPushThreadPool") {
        @Override
        protected void processMessage(Message message) {

        }
    };

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(final BarrageMsgEvent.ReceivedBarrageMsgEvent event) {
        sPushThreadPool.post(new Runnable() {
            @Override
            public void run() {
                if (event == null || isFinishing()) {
                    return;
                }
                if (mMyRoomData == null) {
                    return;
                }
                String roomId = mMyRoomData.getRoomId();
                MyLog.d(TAG, "mMyRoomData.getRoomId():" + roomId);
                if (TextUtils.isEmpty(roomId)) {
                    return;
                }
                processPushMsgList(event.getMsgList());
            }
        });

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
            String roomId = mMyRoomData.getRoomId();
            if (roomId == null || !roomId.equals(msg.getRoomId())) {
                if (msg.getMsgType() != BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG && msg.getMsgType() != BarrageMsgType.B_MSG_TYPE_LEVEL_UPGRADE_SYS_MSG) {
                    MyLog.w(TAG, "not this room msg, my_room_id:" + roomId + ", msg_room_id:" + msg.getRoomId());
                    continue;
                } else {
                }
            }
            // 处理自己房间的push消息
            MyLog.d(TAG, "processPushMsgList msg=" + msg.getBody() + ",msgType" + msg.getMsgType());
            processPushMessage(msg, mMyRoomData);
            MyLog.d(TAG, "processPushMsgList msg=" + msg.getBody() + ",msgType" + msg.getMsgType() + " over");
        }
    }

    //返回true代表已经处理过了，子类无需再处理。3个Activity处理push都相同的push逻辑可 以放在这，以后只要在这修改就行了。
    protected void processPushMessage(BarrageMsg msg, RoomBaseDataModel roomData) {
        HashSet<IPushMsgProcessor> set = mIPushMsgProcessorMap.get(msg.getMsgType());
        if (set != null) {
            for (IPushMsgProcessor msgProcessor : set) {
                MyLog.d(TAG, "recv msg,msgType" + msg.getMsgType());
                msgProcessor.process(msg, roomData);
            }
        } else {
            MyLog.w(TAG, "recv this msg but no processor,check the code!!!msg:" + msg);
        }
    }

    protected void initGiftRoomEffectView() {
        mGiftRoomEffectView = (GiftRoomEffectView) findViewById(R.id.gift_room_effect_view);
        mGiftRoomEffectView.onActivityCreate();
    }

    protected abstract void trySendDataWithServerOnce();

    protected abstract void tryClearData();

    // milink链接成功了,在主线程保证时序
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MiLinkEvent.StatusLogined event) {
        // 登录成功了
        if (event != null) {
            MyLog.w(TAG, "StatusLogined");
            trySendDataWithServerOnce();
        }
    }

    // milink链接成功了,在主线程保证时序
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AccountEventController.LogOffEvent event) {
        if (event != null && event.getEventType() == AccountEventController.LogOffEvent.EVENT_TYPE_NORMAL_LOGOFF) {
            MyLog.d(TAG, "log off channelId=" + event.getChannelId());
            tryClearData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.ShareEvent event) {
        if (event != null) {
            onEventShare(event);
            if (event.state == EventClass.ShareEvent.TYPE_SUCCESS) {
                //update experience
                if (mExpLevelPresenter == null) {
                    mExpLevelPresenter = new ExpLevelPresenter();
                    addPresent(mExpLevelPresenter);
                }
                mExpLevelPresenter.updateExperience(ExpLevelPresenter.SHARE_TYPE, event.getSnsType());
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoom_out);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(AccountEventController.LogOffEvent event) {
        MyLog.w(TAG, "KickEvent,eventType:" + event.getEventType());
        if (event == null) {
            return;
        }
        switch (event.getEventType()) {
            case AccountEventController.LogOffEvent.EVENT_TYPE_KICK:
            case AccountEventController.LogOffEvent.EVENT_TYPE_ACCOUNT_FORBIDDEN:
                hasKicked = true;
                onKickEvent("EVENT_TYPE_ACCOUNT_FORBIDDEN");
                showLogoffDialog(event.getEventType(), event.getUuid());
                break;
            case AccountEventController.LogOffEvent.EVENT_TYPE_NORMAL_LOGOFF:
                if (!hasKicked) {
                    finish();
                }
                break;
        }
    }

    public abstract void onKickEvent(String msg);

    private void showLogoffDialog(int exitType, long uuid) {
        MyLog.w(TAG, " showExitDialog KickEvent");
        if (mLogOffDialog == null) {
            MyAlertDialog.Builder builder = new MyAlertDialog.Builder(this);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            mLogOffDialog = builder.create();
            mLogOffDialog.setCanceledOnTouchOutside(false);
            mLogOffDialog.setCancelable(false);
            Date date = new Date();
            DateFormat format = new SimpleDateFormat("HH:mm");
            String time = format.format(date);
            if (exitType == AccountEventController.LogOffEvent.EVENT_TYPE_KICK) {
                mLogOffDialog.setMessage(getString(R.string.service_token_expired, time));
                mLogOffDialog.show();
            } else if (exitType == AccountEventController.LogOffEvent.EVENT_TYPE_ACCOUNT_FORBIDDEN) {
                mLogOffDialog.setMessage(getString(R.string.forbbiden_message, String.valueOf(uuid)));
                mLogOffDialog.show();
            }
        }
    }
}
