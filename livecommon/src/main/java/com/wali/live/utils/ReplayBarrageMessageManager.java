package com.wali.live.utils;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.push.event.BarrageMsgEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveMessageProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by anping on 16-4-11.
 * 　不使用BarrageMessageManager 是因为 replay的逻辑更加的独立和特殊,比如
 * <p>
 * 　拿弹幕有两种方式 1. 发现拿的弹幕没有了，快去拿, 这个是被acticity check 的时候做
 * 2. 发现服务器给的最后一条弹幕还没有我（ˇˍˇ）　想～播放的地方大，我再去拿，知道大或者没有了.这个在最后的时候做
 *
 * @Module 回放弹幕管理
 */

public class ReplayBarrageMessageManager implements MiLinkPacketDispatcher.PacketDataHandler {

    private static final String TAG = "BarrageMessageManager";
    private static final int DEFAULT_LITMIT = 60;
    private static final int ASSOCIATION_FAILED_TIMES = 10;
    private static final int MAX_RE_FETCH_SIZE = 500;
    private static final int PRE_FETCH_TIME = 20 * 1000; //预拉取时间,每次预拉20秒的数据

    public String token;

    private boolean mActive = false; //默认是未激活状态，未激活则不接收弹幕

    private boolean mIsFetchToEnd = false; // 存储是否拉到了最后

    private CopyOnWriteArrayList<BarrageMsg> mBarrageMsg = new CopyOnWriteArrayList<>();//历史弹幕存储容器，待回放页每每秒来捞取响应的数据

    private long mLastRelayTime = 0; //存储最后播放的时间，当发现拉到的数据比最后播放的时间还小，赶快塞进去

    private long[] mNowFetchRelayInterval; //现在正在拉取的回放区间,基本是 第一次拉取　和　第一次拉取+ １分钟

    private String pageId = ""; // 最后一次response 给的pageId,服务器用来做分页

    private long mFailedTimes = 0; //　连续拉取失败的次数,当超过5次的时候，之后设置 mIsFetchToEnd，避免压死服务器

    private long mLoadButEmptyTime = 0;

    private ReplayBarrageMessageManager() {

    }

    public static ReplayBarrageMessageManager sInstance = new ReplayBarrageMessageManager();

    public static ReplayBarrageMessageManager getInstance() {
        return sInstance;
    }

    @Override
    public boolean processPacketData(PacketData data) {
        if (data != null && mActive) {
            MyLog.v(TAG + "ReplayBarrageMessageManager processPacketData cmd=" + data.getCommand());
            switch (data.getCommand()) { //JDK7之后支持字符串
                case MiLinkCommand.COMMAND_REPLAY_BARRAGE:
                    processReplayBarrage(data);
                    break;
            }
        }
        return false;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                MiLinkCommand.COMMAND_REPLAY_BARRAGE
        };
    }

    public void init(String token) {
        mBarrageMsg.clear();
        mLastRelayTime = 0;
        pageId = "";
        mIsFetchToEnd = false;
        mFailedTimes = 0;
        mActive = true;
        this.token = token;
        mNowFetchRelayInterval = null;
        mLoadButEmptyTime = 0;
    }

    public void destory() {
        mActive = false;
        mBarrageMsg.clear();
        mLastRelayTime = 0;
        pageId = "";
        mIsFetchToEnd = false;
        mFailedTimes = 0;
        token = "";
        mNowFetchRelayInterval = null;
        mLoadButEmptyTime = 0;
    }

    private void processReplayBarrage(PacketData data) {
        if (data != null) {//处理数据之后并主动排序,时间短的在前面
            try {
                LiveMessageProto.ReplayMessageResponse response = LiveMessageProto.ReplayMessageResponse.parseFrom(data.getData());
                if (response != null) {
                    if (response.getRet() == ErrorCode.CODE_SUCCESS) {
//                        pageId = response.getPageId();
                        List<LiveMessageProto.Message> messageList = response.getReplayMessageList();
                        if (messageList != null && messageList.size() > 0) {

                            List<BarrageMsg> barrageMsgs = new ArrayList<>();
                            LiveMessageProto.Message lastMessage = messageList.get(messageList.size() - 1);
                            long mLastMessageTime = 0;
                            if (lastMessage != null) {
                                mLastMessageTime = lastMessage.getTimestamp();
                            }
                            if (mBarrageMsg.size() < MAX_RE_FETCH_SIZE && mNowFetchRelayInterval != null && mLastMessageTime > mNowFetchRelayInterval[0] && mLastMessageTime < mNowFetchRelayInterval[1]) {
                                String roomId = messageList.get(messageList.size() - 1).getRoomId();
                                MyLog.v(TAG + " getbarrage barrage msg  is not enough ,then goto fetch");
                                sendReplayBarrageRequest(roomId, mLastMessageTime);
                            }

                            for (LiveMessageProto.Message message : messageList) {

                                if (message != null) {
                                    if (mNowFetchRelayInterval != null && (message.getTimestamp() < mNowFetchRelayInterval[0] - 1 * 1000 || message.getTimestamp() > mNowFetchRelayInterval[1] + 2 * 1000)) {
                                        continue;// 如果相差的范围太大　干脆丢掉
                                    }

                                    BarrageMsg msg = BarrageMsg.toBarrageMsg(message);
                                    if (!mBarrageMsg.contains(msg)) {
                                        if (msg.getSentTime() <= mLastRelayTime) {
                                            barrageMsgs.add(msg);
                                        } else {
                                            mBarrageMsg.add(msg);
                                        }
                                        MyLog.v(TAG + "barrage time " + msg.getSentTime());
                                    }
                                }
                            }

                            if (mBarrageMsg != null && mBarrageMsg.size() > 0) {
                                List<BarrageMsg> temp = new ArrayList<>(mBarrageMsg.size());
                                temp.addAll(mBarrageMsg);
                                Collections.sort(temp);
                                synchronized (mBarrageMsg) {
                                    mBarrageMsg.clear();
                                    mBarrageMsg.addAll(temp);
                                }
                            }
                            if (!barrageMsgs.isEmpty()) {
                                MyLog.v(TAG + " in interval barrage count " + barrageMsgs.size());
                                EventBus.getDefault().post(new BarrageMsgEvent.ReceivedBarrageMsgEvent(barrageMsgs));
                            }
                        } else {
//                            mIsFetchToEnd = true;
                        }
                        mFailedTimes = 0;
                    } else {
                        mFailedTimes++;
                        if (mFailedTimes >= ASSOCIATION_FAILED_TIMES) {
//                            mIsFetchToEnd = true;
                        }
                        MyLog.e(TAG + " replay message response is error and code is " + response.getRet() + "msg is:" + response.getErrorMsg());
                    }
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    private void sendReplayBarrageRequest(String roomId, long timestamp) {//需要判断是否已经拉到了最后,避免多余的拉取
        if (!mIsFetchToEnd) {
            long uuid = UserAccountManager.getInstance().getUuidAsLong();
            if (uuid == 0 || TextUtils.isEmpty(roomId)) {
                return;
            }
            LiveMessageProto.ReplayMessageRequest replayMessageRequest = LiveMessageProto.ReplayMessageRequest.newBuilder()
                    .setFromUser(uuid).setRoomId(roomId).setPageId(pageId).setLimit(DEFAULT_LITMIT).setCid(System.currentTimeMillis())
                    .setTimestamp(timestamp * 1000).build();
            PacketData packetData = new PacketData();
            packetData.setCommand(MiLinkCommand.COMMAND_REPLAY_BARRAGE);
            packetData.setData(replayMessageRequest.toByteArray());
            MiLinkClientAdapter.getsInstance().sendAsync(packetData);

            MyLog.v("testDataaaa:"+com.base.utils.CommonUtils.printPBDataLog(replayMessageRequest));
        }
    }

    /**
     * @param roomId
     * @param timestamp
     * @param isManualDrive 是否是手动的拖动
     */
    public void getBarrageMessageByReplayTime(String roomId, long timestamp, boolean isManualDrive) {
        MyLog.v(TAG + " getBarrageMessage  mLastRelayTime=" + mLastRelayTime + ",timestamp=" + timestamp + ",isManualDrive=" + isManualDrive);
        if (this.mLastRelayTime == timestamp) {
            return;
        }
        // 每次都判断时间戳的差值，解决跳动不显示弹幕问题，没必须要判断是不是手动。
        isManualDrive = true;
        if (isManualDrive) {
            if (mLastRelayTime != 0 && timestamp - mLastRelayTime < -60 * 1000) {//需要拉取的数据还小１秒,说明是在回退,则全清数据
                MyLog.v(TAG + " clean data  " + timestamp + "   " + mLastRelayTime);
                EventBus.getDefault().post(new BarrageMsgEvent.CleanBarrageMsgEvent());
                mBarrageMsg.clear();
                pageId = "";
                mNowFetchRelayInterval = null;
                mLoadButEmptyTime = 0;
                mIsFetchToEnd = false;
            } else if (mLastRelayTime != 0 && timestamp - mLastRelayTime > 60 * 1000) { //这是在快进，也全清下数据
                MyLog.v(TAG + " clean data  " + timestamp + "   " + mLastRelayTime);
                EventBus.getDefault().post(new BarrageMsgEvent.CleanBarrageMsgEvent());
                mBarrageMsg.clear();
                pageId = "";
                mIsFetchToEnd = false;
                mNowFetchRelayInterval = null;
                mLoadButEmptyTime = 0;
            }
        }

        this.mLastRelayTime = timestamp;

        if (mBarrageMsg.size() > 0) {
            List<BarrageMsg> needShowBarrageMsg = new ArrayList<>();

            for (Iterator<BarrageMsg> iterator = mBarrageMsg.iterator(); iterator.hasNext(); ) {
                BarrageMsg barrageMsg = iterator.next();
                if (barrageMsg != null) {
                    long sentTime = barrageMsg.getSentTime();
                    MyLog.v(TAG, "barrageMsg.sentTime:" + sentTime);
                    if (sentTime <= timestamp) {
                        needShowBarrageMsg.add(barrageMsg);
                        mBarrageMsg.remove(barrageMsg);
                    } else if (sentTime > timestamp) {
                        break;
                    }
                }
            }
            if (!needShowBarrageMsg.isEmpty()) {
                EventBus.getDefault().post(new BarrageMsgEvent.ReceivedBarrageMsgEvent(needShowBarrageMsg));
            }

            if (mBarrageMsg.isEmpty()) {
                MyLog.v(TAG + " getbarrage barrage msg  id empty ,then goto fetch");
                mNowFetchRelayInterval = new long[]{timestamp, timestamp + PRE_FETCH_TIME};
                sendReplayBarrageRequest(roomId, timestamp);
            }
        } else {

            if (mNowFetchRelayInterval != null && timestamp >= mNowFetchRelayInterval[0] && timestamp <= mNowFetchRelayInterval[1]) {
                //donothing ,在拉取的范围，不需要再请求拉取
                if (timestamp - mLoadButEmptyTime > 3 * 1000) {
                    sendReplayBarrageRequest(roomId, timestamp);
                    mLoadButEmptyTime = timestamp;
                }
            } else {
                MyLog.v(TAG + " getbarrage message time " + timestamp);
                mNowFetchRelayInterval = new long[]{timestamp, timestamp + PRE_FETCH_TIME};
                sendReplayBarrageRequest(roomId, timestamp);
            }
        }
    }

}
