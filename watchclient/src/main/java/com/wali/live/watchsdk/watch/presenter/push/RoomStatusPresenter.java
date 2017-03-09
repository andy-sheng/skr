package com.wali.live.watchsdk.watch.presenter.push;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.watch.event.LiveEndEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 需要mInfoTips，mPlayerView
 * Created by chengsimin on 16/7/5.
 */
public class RoomStatusPresenter implements IPushMsgProcessor {

    private static final String TAG = "RoomStatusPresenter";

    private static final int MSG_ANCHOR_LEAVE = 106; // 主播离开房间

    private static final int MSG_ANCHOR_JOIN = 107;  // 主播回到房间

    Handler mUIHandler;

    LiveRoomChatMsgManager mRoomChatMsgManager;

    public void setInfoTips(RelativeLayout mInfoTips) {
        this.mInfoTips = mInfoTips;
    }

    RelativeLayout mInfoTips;

    public RoomStatusPresenter(LiveRoomChatMsgManager mRoomChatMsgManager) {
        this.mRoomChatMsgManager = mRoomChatMsgManager;
    }

    @Override
    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIVE_END) {
            // 直播结束
            roomBaseDataModel.setLiveEnd(true);
            BarrageMsg.LiveEndMsgExt ext = (BarrageMsg.LiveEndMsgExt) msg.getMsgExt();
            roomBaseDataModel.setViewerCnt(ext.viewerCount);
            ensureHandlerNotNull();

            MyLog.w(TAG + " B_MSG_TYPE_LIVE_END");
            // 直播结束
            EventBus.getDefault().post(new LiveEndEvent());
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TOP_GET) {
            //TODO 于锐
//            LiveRoomCharactorManager.getInstance().setTopRank(roomBaseDataModel.getUid(), UserAccountManager.getInstance().getUuidAsLong());
            MyLog.w(TAG + " B_MSG_TYPE_TOP_GET");
            mRoomChatMsgManager.addChatMsg(msg, true);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TOP_LOSE) {
            //TODO 于锐
//            LiveRoomCharactorManager.getInstance().removeTopRank(roomBaseDataModel.getUid(), UserAccountManager.getInstance().getUuidAsLong());
            MyLog.w(TAG + " B_MSG_TYPE_TOP_LOSE");
            mRoomChatMsgManager.addChatMsg(msg, true);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE) {
            MyLog.w(TAG, "B_MSG_TYPE_ANCHOR_LEAVE");
            ensureHandlerNotNull();
            mUIHandler.removeMessages(MSG_ANCHOR_LEAVE); // 发送MSG_ANCHOR_LEAVE之间，删除之前的MSG_ANCHOR_LEAVE和MSG_ANCHOR_JOIN
            mUIHandler.removeMessages(MSG_ANCHOR_JOIN);
            Message m = mUIHandler.obtainMessage(MSG_ANCHOR_LEAVE);
            m.obj = roomBaseDataModel;
            mUIHandler.sendMessage(m);
            mRoomChatMsgManager.addChatMsg(msg, true);
        } else if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN) {
            MyLog.w(TAG, "B_MSG_TYPE_ANCHOR_JOIN");
            ensureHandlerNotNull();
            mUIHandler.removeMessages(MSG_ANCHOR_LEAVE); // 发送MSG_ANCHOR_JOIN之间，删除之前的MSG_ANCHOR_LEAVE和MSG_ANCHOR_JOIN
            mUIHandler.removeMessages(MSG_ANCHOR_JOIN);
            Message m = mUIHandler.obtainMessage(MSG_ANCHOR_JOIN);
            m.obj = roomBaseDataModel;
            mUIHandler.sendMessage(m);
            mRoomChatMsgManager.addChatMsg(msg, true);
        }
    }

    @Override
    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_LIVE_END,
                BarrageMsgType.B_MSG_TYPE_TOP_GET,
                BarrageMsgType.B_MSG_TYPE_TOP_LOSE,
                BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE,
                BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN
        };
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
        if (mUIHandler != null) {
            mUIHandler.removeCallbacksAndMessages(null);
        }
    }

    private void ensureHandlerNotNull() {
        if (mUIHandler == null) {
            mUIHandler = new Handler(Looper.getMainLooper()) {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_ANCHOR_LEAVE: {
                            MyLog.w(TAG, "MSG_ANCHOR_LEAVE");
                            showInfoTips(GlobalData.app().getString(R.string.pause_tip));
                            RoomBaseDataModel d = ((RoomBaseDataModel) msg.obj);
                            if (d != null) {
                                d.setAnchorLeave(true);
                            }
                            if (!hasMessages(MSG_ANCHOR_JOIN)) { // Note: 此处可能有多线程并发问题，
                                Message m = obtainMessage(MSG_ANCHOR_JOIN); // 主播暂时离开的提示最多显示两分钟
                                m.obj = d;
                                sendMessageDelayed(m, 120 * 1000);
                            }
                        }
                        break;
                        case MSG_ANCHOR_JOIN: {
                            MyLog.w(TAG, "MSG_ANCHOR_JOIN");
                            RoomBaseDataModel d = ((RoomBaseDataModel) msg.obj);
                            if (d != null) {
                                d.setAnchorLeave(false);
                            }
                            hideInfoTips(GlobalData.app().getString(R.string.pause_tip));
                        }
                        break;
                    }
                }
            };
        }
    }

    // 动态加view
    private void showInfoTips(String tips) {
//        ((TextView) mInfoTips.findViewById(R.id.tips_tv)).setText(tips);
        if (mInfoTips != null) {
            mInfoTips.setVisibility(View.VISIBLE);
        }

    }


    private void hideInfoTips(String tips) {
//        if (tips.equals(((TextView) mInfoTips.findViewById(R.id.tips_tv)).getText().toString())) {
        if (mInfoTips != null) {
            mInfoTips.setVisibility(View.GONE);
        }
//        }
    }

}
