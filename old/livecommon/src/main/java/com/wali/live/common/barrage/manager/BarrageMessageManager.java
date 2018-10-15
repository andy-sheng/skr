package com.wali.live.common.barrage.manager;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.live.module.common.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.event.TurnTableEvent;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.push.event.BarrageMsgEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgExt;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.query.model.SystemMsgModel;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.statistics.StatisticUtils;
import com.wali.live.statistics.StatisticsKey;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.mi.live.data.milink.command.MiLinkCommand.COMMAND_RADIO_SIGNAL_PUSH;

/**
 * @module com.wali.live.message
 * <p>
 * Created by MK on 16/2/23.
 * 直播的弹幕消息管理
 */
public class BarrageMessageManager implements MiLinkPacketDispatcher.PacketDataHandler {

    private static final String TAG = "BarrageMessageManager";

    private static BarrageMessageManager sInstance = new BarrageMessageManager();

    public static BarrageMessageManager getInstance() {
        return sInstance;
    }

    public static ConcurrentMap<Long, BarrageMsg> mSendingMsgCache = new ConcurrentHashMap<Long, BarrageMsg>();//cache主那些还没有发送成功的弹幕,可用来重发
    private static final int MAX_RETRY_SEND_TIMS = 2; //弹幕重发次数最多为两次
    private static final long MIN_RESPONSE_TIME_OUT_CHECK_TIME = 10 * 1000; //弹幕超时时间

    private BarrageMessageManager() {
    }

    @Override
    public boolean processPacketData(PacketData data) {
        if (data == null) {
            return false;
        }
        MyLog.w(TAG, "processPacketData cmd=" + data.getCommand());
        // 这里有坑，有的消息虽然会带着房间号，但不是只给当前房间的，而且会触发离开房间的逻辑
        switch (data.getCommand()) { //JDK7之后支持字符串
            case MiLinkCommand.COMMAND_PUSH_BARRAGE: {
                processPushBarrage(data, true, MiLinkCommand.COMMAND_PUSH_BARRAGE);
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
                processPushBarrage(data, false, MiLinkCommand.COMMAND_PUSH_SYSMSG);
            }
            break;
            case MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG: {
                processPushBarrage(data, false, MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG);
            }
            break;
            case MiLinkCommand.COMMAND_RADIO_SIGNAL_PUSH: {
                processPushBarrage(data, false, COMMAND_RADIO_SIGNAL_PUSH);
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
                MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG,
                MiLinkCommand.COMMAND_RADIO_SIGNAL_PUSH
        };
    }

    private void processSendBarrageResponse(PacketData data) {
        try {
            LiveMessageProto.RoomMessageResponse response = LiveMessageProto.RoomMessageResponse.parseFrom(data.getData());
            if (response != null && (response.getRet() == MiLinkConstant.ERROR_CODE_SUCCESS || response.getRet() == MiLinkConstant.ERROR_CODE_BAN_SPEAKER)) {
            } else if (response != null && response.getRet() == MiLinkConstant.ERROR_CODE_MSG_TOO_LARGE) {
                ToastUtils.showToast(GlobalData.app(), GlobalData.app().getResources().getString(R.string.barrage_message_too_large));
            }
            if (response != null) {
                MyLog.w(TAG, "BarrageMsg recv:" + response.getCid() + " result:" + response.getRet());
                StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_BARRAGE_CUSTOM_SEND_SUCCESS, StatisticUtils.SUCCESS);

                if (response.hasVipBrCnt()) {
                    EventBus.getDefault().post(new BarrageMsgEvent.SendBarrageResponseEvent(response.getCid(), response.getTimestamp() == 0 ? System.currentTimeMillis() : response.getTimestamp(), response.getFltbrCnt(), response.getAdminBrCnt(), response.getVipBrCnt(), response.getGuardBrCnt()));
                } else if (response.hasGuardBrCnt()) {
                    EventBus.getDefault().post(new BarrageMsgEvent.SendBarrageResponseEvent(response.getCid(), response.getTimestamp() == 0 ? System.currentTimeMillis() : response.getTimestamp(), response.getFltbrCnt(), response.getAdminBrCnt(), Integer.MAX_VALUE, response.getGuardBrCnt()));
                } else {
                    EventBus.getDefault().post(new BarrageMsgEvent.SendBarrageResponseEvent(response.getCid(), response.getTimestamp() == 0 ? System.currentTimeMillis() : response.getTimestamp(), response.getFltbrCnt(), response.getAdminBrCnt(), Integer.MAX_VALUE, Integer.MAX_VALUE));
                }
            }
        } catch (InvalidProtocolBufferException e) {
            MyLog.e(e);
        }
    }

    /**
     * @param data
     * @param careLeaveRoom 是否关心要不要匹配房间号，房间号不匹配就发送离开消息来源房间的消息
     */
    private void processPushBarrage(PacketData data, boolean careLeaveRoom, String command) {
        try {
            LiveMessageProto.PushMessage pushMsg = LiveMessageProto.PushMessage.parseFrom(data.getData());
            if (pushMsg == null) {
                return;
            }
            if (pushMsg.getMessageList() == null || pushMsg.getMessageList().isEmpty()) {
                return;
            }
            ArrayList<BarrageMsg> barrageMsgList = new ArrayList<BarrageMsg>();
            for (LiveMessageProto.Message msg : pushMsg.getMessageList()) {
                if (msg == null) {
                    continue;
                }

                BarrageMsg barrageMsg = BarrageMsg.toBarrageMsg(msg);
                if (command.equals(MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG)) {
                    if (barrageMsg.getMsgType() != BarrageMsgType.B_MSG_TYPE_TEXT) {
                        barrageMsgList.add(barrageMsg);
                    }
                } else {
                    barrageMsgList.add(barrageMsg);
                }
                MyLog.d(TAG, "barrageMsgList.size():" + barrageMsgList.size());
                if (msg.getFromUser() == MyUserInfoManager.getInstance().getUuid()) {
                    updateUserInfo(msg);
                }

                if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_OUTDOOR_FOUCES || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_CANCLE_FOUCES) {
//                    TODO 房间外关注消息
//                    EventBus.getDefault().post(new ConversationLocalStore.SomeOneFoucsOrCancelYouFoucsEvent(msg.getFromUser(), msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_OUTDOOR_FOUCES));
                }
                MyLog.d(TAG, "BarrageMsg msgType:" + msg.getMsgType() + ", roomid:" + msg.getRoomId() + ",body:" + msg.getMsgBody() + " cid=" + msg.getCid() + " fromUser=" + msg.getFromUser());
                if (careLeaveRoom) {
                    RoomInfoGlobalCache.getsInstance().sendLeaveRoomIfNeed(barrageMsg.getAnchorId(), barrageMsg.getRoomId());
                }

                if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_OPEN_TURN_TABLE) {
                    EventBus.getDefault().post(new TurnTableEvent(((BarrageMsgExt.TurnTableMessageExt) barrageMsg.getMsgExt()).getTurnTableConfigModel()));
                }
                // 如果是大金龙消息
                if (command.equals(MiLinkCommand.COMMAND_PUSH_GLOBAL_MSG)) {
                    if (barrageMsg.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) {
//                        TODO 打开注释
//                        GiftManager.popupGlobalGiftMsgWindow(barrageMsg, (BarrageMsg.GiftMsgExt) barrageMsg.getMsgExt(), RoomInfoGlobalCache.getsInstance().getCurrentRoomId());
                    } else if (barrageMsg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TEXT) {
                        MyLog.d(TAG, "BarrageMsg msgType:" + msg.getMsgType() + ", roomid:" + msg.getRoomId() + ",body:" + msg.getMsgBody() + " cid=" + msg.getCid() + " fromUser=" + msg.getFromUser());
//                        TODO 打开注释
//                        GiftManager.popupGlobalGiftMsgWindow(barrageMsg, (BarrageMsg.RoomTxtMessageExt) barrageMsg.getMsgExt(), RoomInfoGlobalCache.getsInstance().getCurrentRoomId());
                    }
                }

                if (barrageMsg.getMsgType() == BarrageMsgType.B_MSG_TYPE_RED_NAME_STATUES) {//红名信息状态变化
                    Observable.just(0)
                            .observeOn(Schedulers.io())
                            .subscribe(new Action1<Integer>() {
                                @Override
                                public void call(Integer integer) {
                                    // 会发出UserInfoEvent
//                        TODO 打开注释
//                                    LiveSyncManager.getInstance().syncOwnUserInfo();
                                }
                            });
                }
            }// end-for
            sendRecvEvent(barrageMsgList);

        } catch (InvalidProtocolBufferException e) {
            MyLog.e(TAG, e);
        }
    }

    private void updateUserInfo(LiveMessageProto.Message msg) {
        if (msg.getFromUserLevel() != MyUserInfoManager.getInstance().getLevel()) {
            MyUserInfoManager.getInstance().setLevel(msg.getFromUserLevel());
            MyLog.w(TAG + " updateUserInfo and level is " + msg.getFromUserLevel());
        }
        if (msg.getNobleLevel() != MyUserInfoManager.getInstance().getNobleLevel()) {
            MyUserInfoManager.getInstance().setNobelLevel(msg.getNobleLevel());
        }
        if (msg.getVipLevel() > MyUserInfoManager.getInstance().getVipLevel()
                || msg.getVipDisable() != MyUserInfoManager.getInstance().isVipFrozen()
                || msg.getVipHidden() != MyUserInfoManager.getInstance().isVipHide()) {
            MyUserInfoManager.getInstance().setVipInfo(msg.getVipLevel(), msg.getVipDisable(),
                    msg.getVipHidden());
        }
        // 如果是进入房间消息，尝试矫正一下nickname ，这里谨防变成系统消息，这里暂时不考虑英文版
        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_JOIN
                && TextUtils.isEmpty(MyUserInfoManager.getInstance().getNickname())
                && !TextUtils.isEmpty(msg.getFromUserNickName())
                && !"系统消息".equals(msg.getFromUserNickName())) {
            MyUserInfoManager.getInstance().setNickname(msg.getFromUserNickName());
        }
    }

    private void sendRecvEvent(List<BarrageMsg> barrageMsgList) {
        if (barrageMsgList != null && !barrageMsgList.isEmpty()) {
            MyLog.v(TAG, "sendRecvEvent list.size:" + barrageMsgList.size());
            EventBus.getDefault().post(new BarrageMsgEvent.ReceivedBarrageMsgEvent(barrageMsgList, "sendRecvEvent"));
        }
    }


    public void pretendPushRoomSystemBarrage(String text, long anchorId, String roomId) {
        BarrageMsg msg1 = new BarrageMsg();
        msg1.setAnchorId(anchorId);
        msg1.setRoomId(roomId);
//        msg1.setBody("欢迎" + MyUserInfoManager.getInstance().getNickname() + " 来到主播" + roomBaseDataModel.getNickName() + "的甜蜜派对");
        msg1.setMsgType(BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG);
        msg1.setSenderName("senderName");
        BarrageMsg.RoomMessageExt roomMessageExt = new BarrageMsg.RoomMessageExt();
        List<SystemMsgModel> temp = new ArrayList<>();

        SystemMsgModel systemMsg = new SystemMsgModel();
        systemMsg.setFromUser(0);
        systemMsg.setContent(text);
        systemMsg.setStartTime(System.currentTimeMillis());
        systemMsg.setEndTime(System.currentTimeMillis() + 60000);

        temp.add(systemMsg);
        roomMessageExt.roomSystemMessageList = temp;
        msg1.setMsgExt(roomMessageExt);

        BarrageMessageManager.getInstance().pretendPushBarrage(msg1);
    }

    /**
     * 伪装成pushmessage发出去
     *
     * @param msg
     */
    public void pretendPushBarrage(BarrageMsg msg) {
        // VIP相关
        msg.setVipLevel(MyUserInfoManager.getInstance().getVipLevel());
        msg.setVipFrozen(MyUserInfoManager.getInstance().isVipFrozen());
        msg.setVipHide(MyUserInfoManager.getInstance().isVipHide());
        msg.setNobleLevel(MyUserInfoManager.getInstance().getNobleLevel());
        MyLog.d(TAG, "pretendPushBarrage nobelType=" + MyUserInfoManager.getInstance().getNobleLevel());

        ArrayList<BarrageMsg> barrageMsgList = new ArrayList<BarrageMsg>(1);
        if (msg != null) {
            barrageMsgList.add(addMedalInfo(msg));
        }
        sendRecvEvent(barrageMsgList);
    }

    /**
     * 勋章信息自己的从个人信息页中读取
     *
     * @return
     */
    private BarrageMsg addMedalInfo(BarrageMsg msg) {
        //设置用户勋章-
        if ((MyUserInfoManager.getInstance().getBeforeNickNameMedalList() != null && !MyUserInfoManager.getInstance().getBeforeNickNameMedalList().isEmpty()) ||
                (MyUserInfoManager.getInstance().getAfterNickNameMedalList() != null && !MyUserInfoManager.getInstance().getAfterNickNameMedalList().isEmpty())) {
            ArrayList<BarrageMsgExt.InnerMedalConfig> beforeNickNameConfigList = new ArrayList<>();
            ArrayList<BarrageMsgExt.InnerMedalConfig> afterNickNameConfigList = new ArrayList<>();
            BarrageMsgExt.MedalConfigMessage medalConfigMessage = new BarrageMsgExt.MedalConfigMessage();

            List<String> beforeNickNameMedalList = MyUserInfoManager.getInstance().getBeforeNickNameMedalList();
            List<String> afterNickNameMedalList = MyUserInfoManager.getInstance().getAfterNickNameMedalList();
            if (beforeNickNameMedalList != null && !beforeNickNameMedalList.isEmpty()) {
                for (int i = 0; i < beforeNickNameMedalList.size(); i++) {
                    BarrageMsgExt.InnerMedalConfig innerMedalConfig = new BarrageMsgExt.InnerMedalConfig();
                    innerMedalConfig.setPicId(beforeNickNameMedalList.get(i));
                    beforeNickNameConfigList.add(innerMedalConfig);
                }
            }
            if (afterNickNameMedalList != null && !afterNickNameMedalList.isEmpty()) {
                for (int i = 0; i < afterNickNameMedalList.size(); i++) {
                    BarrageMsgExt.InnerMedalConfig innerMedalConfig = new BarrageMsgExt.InnerMedalConfig();
                    innerMedalConfig.setPicId(afterNickNameMedalList.get(i));
                    afterNickNameConfigList.add(innerMedalConfig);
                }
            }
            medalConfigMessage.setBeforeNickNameCofigList(beforeNickNameConfigList);
            medalConfigMessage.setAfterNickNameCofigList(afterNickNameConfigList);
            BarrageMsg.InnerGlobalRoomMessageExt innerGlobalRoomMessageExt = new BarrageMsg.InnerGlobalRoomMessageExt();
            innerGlobalRoomMessageExt.setMedalConfigMessage(medalConfigMessage);
            innerGlobalRoomMessageExt.setType(BarrageMsg.INNER_GLOBAL_MEDAL_TYPE);


            BarrageMsg.GlobalRoomMessageExt globalRoomMessageExt = msg.getGlobalRoomMessageExt() == null ? new BarrageMsg.GlobalRoomMessageExt() : msg.getGlobalRoomMessageExt();

            List<BarrageMsg.InnerGlobalRoomMessageExt> innerGlobalRoomMessageExtList = globalRoomMessageExt.getInnerGlobalRoomMessageExtList() == null ? new ArrayList<BarrageMsg.InnerGlobalRoomMessageExt>() : globalRoomMessageExt.getInnerGlobalRoomMessageExtList();
            innerGlobalRoomMessageExtList.add(0, innerGlobalRoomMessageExt);
            globalRoomMessageExt.setInnerGlobalRoomMessageExtList(innerGlobalRoomMessageExtList);

            msg.setGlobalRoomMessageExt(globalRoomMessageExt);
        }

        return msg;
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
                            EventBus.getDefault().post(new BarrageMsgEvent.ReceivedBarrageMsgEvent(barrageMsgList, "processSystemMessage"));
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
            if (msg.getGlobalRoomMessageExt() != null) {
                LiveMessageProto.GlobalRoomMessageExt.Builder builder2 = LiveMessageProto.GlobalRoomMessageExt.newBuilder();
                List<BarrageMsg.InnerGlobalRoomMessageExt> list = msg.getGlobalRoomMessageExt().getInnerGlobalRoomMessageExtList();
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (msg.getGlobalRoomMessageExt().getInnerGlobalRoomMessageExtList().get(i).getType() != BarrageMsg.INNER_GLOBAL_VFAN) {
                            builder2.addInnerGlobalRoomMsgExt(i, LiveMessageProto.InnerGlobalRoomMessageExt.newBuilder().setType(msg.getGlobalRoomMessageExt().getInnerGlobalRoomMessageExtList().get(i).getType()).build());
                        }
                    }
                }

                builder.setGlobalRoomMsgExt(builder2.build());
            }
            if (!TextUtils.isEmpty(msg.getOpponentRoomId())) {

                LiveMessageProto.PKRoomInfo.Builder pkRoomInfoBuilder = LiveMessageProto.PKRoomInfo.newBuilder();
                pkRoomInfoBuilder.setPkRoomId(msg.getOpponentRoomId())
                        .setPkZuid(msg.getOpponentAnchorId());
                builder.setPkRoomInfo(pkRoomInfoBuilder.build());
            }
            packetData.setData(builder.build().toByteArray());
            MyLog.v(TAG, "BarrageMsg send:" + msg.getSenderMsgId() + "body :" + msg.getBody());
            MiLinkClientAdapter.getsInstance().sendAsync(packetData);
            StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_BARRAGE_CUSTOM_SEND_ALL, StatisticUtils.SUCCESS);
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
            StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_BARRAGE_CUSTOM_SEND_ALL, StatisticUtils.SUCCESS);
        }
        return false;
    }

    public void sendHuYaBarrageMessage(LiveMessageProto.HuyaSendMessageReq huyaSendMessageReq) {
        if (huyaSendMessageReq != null) {
            if (huyaSendMessageReq.getFromUid() <= 0 || huyaSendMessageReq.getAnchorHuyaUid() <= 0) {
                MyLog.w(TAG, "send chaMessageReadRequest from or to or cid is null,so cancel");
                return;
            }
            PacketData packetData = new PacketData();
            packetData.setCommand(MiLinkCommand.COMMAND_HUYA_MSG);
            packetData.setData(huyaSendMessageReq.toByteArray());
            MiLinkClientAdapter.getsInstance().sendAsync(packetData);
        }
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
//        TODO
//        if (SixinMessageManager.getInstance().mCustomHandlerThread != null) {
//            Message message = Message.obtain();
//            message.what = SixinMessageManager.MESSAGE_BARRAGE_MSG_TIME_OUT_CHECK;
//            SixinMessageManager.getInstance().mCustomHandlerThread.sendMessage(message);
//        }
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
                    BarrageMessageManager.getInstance().sendBarrageMessageAsync(barrageMsg, false);
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
}