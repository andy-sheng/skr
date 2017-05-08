package com.mi.liveassistant.barrage.manager;

import android.os.Message;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.account.MyUserInfoManager;
import com.mi.liveassistant.account.UserAccountManager;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;
import com.mi.liveassistant.barrage.processor.BarrageMainProcessor;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.milink.callback.MiLinkPacketDispatcher;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.milink.constant.MiLinkConstant;
import com.mi.liveassistant.proto.LiveMessageProto;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.base.CustomHandlerThread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @module com.wali.live.message
 * <p>
 * Created by MK on 16/2/23.
 * 直播的弹幕消息管理
 */
public class BarragePushMessageManager implements MiLinkPacketDispatcher.PacketDataHandler {

    private static final String TAG = BarragePushMessageManager.class.getSimpleName();

    private static BarragePushMessageManager sInstance = new BarragePushMessageManager();

    public static BarragePushMessageManager getInstance() {
        return sInstance;
    }

    public static ConcurrentHashMap<Long, BarrageMsg> mSendingMsgCache = new ConcurrentHashMap<Long, BarrageMsg>();//cache主那些还没有发送成功的弹幕,可用来重发
    private static final int MAX_RETRY_SEND_TIMS = 2; //弹幕重发次数最多为两次
    private static final long MIN_RESPONSE_TIME_OUT_CHECK_TIME = 10 * 1000; //弹幕超时时间

    public static final int MESSAGE_BARRAGE_MSG_TIME_OUT_CHECK = 2;//友好互助下

    private BarragePushMessageManager() {
    }

    CustomHandlerThread mCustomHandlerThread = new CustomHandlerThread(TAG) {
        @Override
        protected void processMessage(Message message) {
            switch (message.what) {
                case MESSAGE_BARRAGE_MSG_TIME_OUT_CHECK: {
                    removeMessage(MESSAGE_BARRAGE_MSG_TIME_OUT_CHECK);
                    BarragePushMessageManager.checkBarrageMsgStatus();
                }
                break;
            }
        }
    };

    @Override
    public boolean processPacketData(PacketData data) {
        if (data == null) {
            return false;
        }
        MyLog.v(TAG, "processPacketData cmd=" + data.getCommand());
        // 这里有坑，有的消息虽然会带着房间号，但不是只给当前房间的，而且会触发离开房间的逻辑
        switch (data.getCommand()) { //JDK7之后支持字符串
            case MiLinkCommand.COMMAND_PUSH_BARRAGE: {
                processPushBarrage(data, true);
            }
            break;
            case MiLinkCommand.COMMAND_SEND_BARRAGE: {
                processSendBarrageResponse(data);
            }
            break;
            case MiLinkCommand.COMMAND_SYNC_SYSMSG: {
                processSystemMessage(data);
            }
            break;
            case MiLinkCommand.COMMAND_PUSH_SYSMSG: {
                processPushBarrage(data, false);
            }
            break;
            case MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG: {
                processPushBarrage(data, false);
            }
            break;
        }
        return false;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                MiLinkCommand.COMMAND_PUSH_BARRAGE,
                MiLinkCommand.COMMAND_SEND_BARRAGE,
                MiLinkCommand.COMMAND_SYNC_SYSMSG,
                MiLinkCommand.COMMAND_PUSH_SYSMSG,
                MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG
        };
    }

    private void processSendBarrageResponse(PacketData data) {
        try {
            LiveMessageProto.RoomMessageResponse response = LiveMessageProto.RoomMessageResponse.parseFrom(data.getData());
            if (response != null && (response.getRet() == MiLinkConstant.ERROR_CODE_SUCCESS || response.getRet() == MiLinkConstant.ERROR_CODE_BAN_SPEAKER)) {
            } else if (response != null && response.getRet() == MiLinkConstant.ERROR_CODE_MSG_TOO_LARGE) {
//                ToastUtils.showToast(GlobalData.app(), GlobalData.app().getResources().getString(R.string.barrage_message_too_large));
            }
            if (response != null) {
                MyLog.v(TAG, "BarrageMsg recv:" + response.getCid() + " result:" + response.getRet());
//                StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_BARRAGE_CUSTOM_SEND_SUCCESS, StatisticUtils.SUCCESS);
//                EventBus.getDefault().post(new BarrageMsgEvent.SendBarrageResponseEvent(response.getCid(), response.getTimestamp() == 0 ? System.currentTimeMillis() : response.getTimestamp()));
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
    }

    /**
     * @param data
     * @param careLeaveRoom 是否关心要不要匹配房间号，房间号不匹配就发送离开消息来源房间的消息
     */
    private void processPushBarrage(PacketData data, boolean careLeaveRoom) {
        try {
            LiveMessageProto.PushMessage pushMsg = LiveMessageProto.PushMessage.parseFrom(data.getData());
            if (pushMsg != null) {
                if (pushMsg.getMessageList() != null && !pushMsg.getMessageList().isEmpty()) {
                    ArrayList<BarrageMsg> barrageMsgList = new ArrayList<BarrageMsg>();
                    for (LiveMessageProto.Message msg : pushMsg.getMessageList()) {
                        if (msg != null) {
                            BarrageMsg barrageMsg = BarrageMsg.toBarrageMsg(msg);
                            barrageMsgList.add(barrageMsg);
                            if (msg.getFromUser() == MyUserInfoManager.getInstance().getUser().getUid() && msg.getFromUserLevel() > 0) {
                                MyUserInfoManager.getInstance().setLevel(msg.getFromUserLevel());
                                // 如果是进入房间消息，尝试矫正一下nickname ，这里谨防变成系统消息，这里暂时不考虑英文版
                                if (TextUtils.isEmpty(MyUserInfoManager.getInstance().getUser().getNickname())
                                        && msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN
                                        && !TextUtils.isEmpty(msg.getFromUserNickName())
                                        && !msg.getFromUserNickName().equals("系统消息")) {
                                    MyUserInfoManager.getInstance().setNickname(msg.getFromUserNickName());
                                }
                            }

                            MyLog.v(TAG, "BarrageMsg msgType:" + msg.getMsgType() + ", roomid:" + msg.getRoomId() + ",body:" + msg.getMsgBody());
                            if (careLeaveRoom) {
//                                RoomInfoGlobalCache.getsInstance().sendLeaveRoomIfNeed(barrageMsg.getAnchorId(), barrageMsg.getRoomId());
                            }
                            // 如果是大金龙消息
                            if (barrageMsg.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) {
                                //TODO 大金龙待优化
                            } else if (barrageMsg.getMsgType() == BarrageMsgType.B_MSG_TYPE_RED_NAME_STATUES) {
                                MyUserInfoManager.getInstance().syncSelfDetailInfo();
                            }
                        }
                    }
                    sendRecvEvent(barrageMsgList);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }
    }

    private void sendRecvEvent(List<BarrageMsg> barrageMsgList) {
        if (barrageMsgList != null) {
            MyLog.v(TAG, "sendRecvEvent list.size:" + barrageMsgList.size());
            BarrageMainProcessor.getInstance().enterRenderQueue(barrageMsgList);

        }
    }


    /**
     * 伪装成pushmessage发出去
     *
     * @param msg
     */
    public void pretendPushBarrage(BarrageMsg msg) {
        ArrayList<BarrageMsg> barrageMsgList = new ArrayList<BarrageMsg>(1);
        if (msg != null) {
            barrageMsgList.add(msg);
        }
        sendRecvEvent(barrageMsgList);

    }

    //处理系统弹幕
    private void processSystemMessage(PacketData packetData) {
        if (packetData != null) {
            try {
                LiveMessageProto.SyncSysMsgResponse response = LiveMessageProto.SyncSysMsgResponse.parseFrom(packetData.getData());
                if (response != null && response.getRet() == MiLinkConstant.ERROR_CODE_SUCCESS) {
                    List<LiveMessageProto.Message> messages = response.getMessageList();
                    long cid = response.getCid();
                    if (messages != null && messages.size() > 0) {
                        ArrayList<BarrageMsg> barrageMsgList = new ArrayList<BarrageMsg>();
                        for (LiveMessageProto.Message msg : messages) {
                            if (msg != null) {
                                BarrageMsg barrageMsg = BarrageMsg.toBarrageMsg(msg);
                                barrageMsg.setSenderMsgId(cid);
                                barrageMsgList.add(barrageMsg);
                                MyLog.v(TAG, "BarrageMsg recv systemMessage:" + msg.getCid());
                            }
                        }
                        if (!barrageMsgList.isEmpty()) {
                            BarrageMainProcessor.getInstance().enterRenderQueue(barrageMsgList);
                        }
                    }
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
    }

    //发送弹幕消息，异步接口
    public void sendBarrageMessageAsync(BarrageMsg msg, boolean needReSend) {
        if (msg != null) {
            if (needReSend) {
                mSendingMsgCache.put(msg.getSenderMsgId(), msg);
                sendCheckBarrageMsgStatusMsgToHandle();
            }
            PacketData packetData = new PacketData();
            packetData.setCommand(MiLinkCommand.COMMAND_SEND_BARRAGE);
            LiveMessageProto.RoomMessageRequest.Builder builder = LiveMessageProto.RoomMessageRequest.newBuilder();
            builder.setFromUser(msg.getSender());
            if (!TextUtils.isEmpty(msg.getRoomId())) {
                builder.setRoomId(msg.getRoomId());
            }
            builder.setCid(msg.getSenderMsgId());
            builder.setMsgType(msg.getMsgType());
            builder.setMsgBody(msg.getBody());
            ByteString ext = msg.getMsgExt() != null ? msg.getMsgExt().toByteString() : null;
            if (ext != null) {
                builder.setMsgExt(ext);
            }
            builder.setAnchorId(msg.getAnchorId());
//            builder.setSupportTxt("");
            builder.setRoomType(msg.getRoomType());
            if (!TextUtils.isEmpty(msg.getOpponentRoomId())) {

                LiveMessageProto.PKRoomInfo.Builder pkRoomInfoBuilder = LiveMessageProto.PKRoomInfo.newBuilder();
                pkRoomInfoBuilder.setPkRoomId(msg.getOpponentRoomId())
                        .setPkZuid(msg.getOpponentAnchorId());
                builder.setPkRoomInfo(pkRoomInfoBuilder.build());
            }
            packetData.setData(builder.build().toByteArray());
            MyLog.v(TAG, "BarrageMsg send:" + msg.getSenderMsgId() + "body :" + msg.getBody());
            MiLinkClientAdapter.getsInstance().sendAsync(packetData);
        }
    }

    //发送弹幕消息，同步接口
    public boolean sendBarrageMessageSync(BarrageMsg msg, int timeout) {
        if (msg != null) {
            PacketData packetData = new PacketData();
            packetData.setCommand(MiLinkCommand.COMMAND_SEND_BARRAGE);
            LiveMessageProto.RoomMessageRequest.Builder builder = LiveMessageProto.RoomMessageRequest.newBuilder();
            builder.setFromUser(msg.getSender());
            if (!TextUtils.isEmpty(msg.getRoomId())) {
                builder.setRoomId(msg.getRoomId());
            }
            builder.setAnchorId(msg.getAnchorId());
            builder.setCid(msg.getSenderMsgId());
            builder.setMsgType(msg.getMsgType());
            builder.setMsgBody(msg.getBody());
            ByteString ext = msg.getMsgExt() != null ? msg.getMsgExt().toByteString() : null;
            if (ext != null) {
                builder.setMsgExt(ext);
            }
            packetData.setData(builder.build().toByteArray());
            PacketData result = MiLinkClientAdapter.getsInstance().sendSync(packetData, timeout);
            if (result != null) {
                try {
                    LiveMessageProto.RoomMessageResponse response = LiveMessageProto.RoomMessageResponse.parseFrom(result.getData());
                    if (response != null) {
                        if (response.getRet() == MiLinkConstant.ERROR_CODE_SUCCESS) {
                            msg.setSentTime(response.getTimestamp());
                            return true;
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    MyLog.e(e);
                }
            }
        }
        return false;
    }


    public void sendSyncSystemMessage(LiveMessageProto.SyncSysMsgRequest syncSystemMessageRequest) {
        if (syncSystemMessageRequest != null) {
            if (syncSystemMessageRequest.getFromUser() <= 0 || TextUtils.isEmpty(syncSystemMessageRequest.getRoomId()) || syncSystemMessageRequest.getCid() <= 0) {
                MyLog.w(TAG, "send chaMessageReadRequest from or to or cid is null,so cancel");
                return;
            }
            PacketData packetData = new PacketData();
            packetData.setCommand(MiLinkCommand.COMMAND_SYNC_SYSMSG);
            packetData.setData(syncSystemMessageRequest.toByteArray());
            MiLinkClientAdapter.getsInstance().sendAsync(packetData);
        }
    }

    public static void sendCheckBarrageMsgStatusMsgToHandle() {//借用一下 SixInMessageManager的 thread,不（ˇˍˇ）　想浪费～多开线程
        Message message = Message.obtain();
        message.what = MESSAGE_BARRAGE_MSG_TIME_OUT_CHECK;
        BarragePushMessageManager.getInstance().mCustomHandlerThread.sendMessage(message);
    }

    public static void checkBarrageMsgStatus() {
        if (mSendingMsgCache != null && mSendingMsgCache.size() > 0) {
            Map<Long, BarrageMsg> msgMap = new HashMap<>(mSendingMsgCache);
            Set<Long> keys = msgMap.keySet();
            for (Long key : keys) {
                BarrageMsg barrageMsg = msgMap.get(key);
                if (barrageMsg != null && System.currentTimeMillis() - barrageMsg.getSenderMsgId() > MIN_RESPONSE_TIME_OUT_CHECK_TIME && barrageMsg.getResendTimes() < MAX_RETRY_SEND_TIMS && MiLinkClientAdapter.getsInstance().isMiLinkLogined()) {
                    barrageMsg.setSentTime(System.currentTimeMillis());
                    barrageMsg.setResendTimes(barrageMsg.getResendTimes() + 1);
                    BarragePushMessageManager.getInstance().sendBarrageMessageAsync(barrageMsg, false);
                    MyLog.w(TAG, "resend barrage Msg " + barrageMsg.getSenderMsgId());
                } else if (barrageMsg != null && barrageMsg.getResendTimes() >= MAX_RETRY_SEND_TIMS) {
                    mSendingMsgCache.remove(key);
                }
            }
            if (mSendingMsgCache != null && mSendingMsgCache.size() > 0) {
                sendCheckBarrageMsgStatusMsgToHandle();
            }
        }
    }

    /**
     * @param msgBody
     * @param roomid
     * @param anchorId
     */
    public void sendBarrageMessageAsync(String msgBody, int msgType, String roomid, long anchorId, BarrageMsg.MsgExt ext) {
        if (MyUserInfoManager.getInstance().getUser().getLevel() == 0) {
            MyUserInfoManager.getInstance().syncSelfDetailInfo();
        }
        if (!TextUtils.isEmpty(msgBody) && !TextUtils.isEmpty(roomid)) {
            BarrageMsg msg = new BarrageMsg();
            msg.setMsgType(msgType);
            msg.setSender(UserAccountManager.getInstance().getUuidAsLong());
            String nickname = MyUserInfoManager.getInstance().getUser().getNickname();
            if (nickname == null) {
                nickname = String.valueOf(UserAccountManager.getInstance().getUuidAsLong());
            }
            msg.setSenderName(nickname);
            msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
            msg.setRoomId(roomid);
            msg.setBody(msgBody);
            msg.setAnchorId(anchorId);
            msg.setSentTime(System.currentTimeMillis());
            if (MyUserInfoManager.getInstance().getUser() != null) {
                msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
            }
            if (ext != null) {
                msg.setMsgExt(ext);
            }
            msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
            sendBarrageMessageAsync(msg, true);
            //假装是个push过去
            BarrageMainProcessor.getInstance().addChatMsgRightNow(msg);
        }
    }
}
