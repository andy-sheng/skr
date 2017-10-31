package com.wali.live.watchsdk.sixin.data;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.google.protobuf.ByteString;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.dao.SixinMessage;
import com.wali.live.proto.LiveMessageProto;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by anping on 16-7-4.
 * <p>
 * 操作私信云端增删改查
 */
public class SixinMessageCloudStore {
    private final static String TAG = SixinMessageCloudStore.class.getSimpleName();

    private static final int TIME_OUT = 30 * 1000;

    /**
     * 发送一条私信消息
     *
     * @param sixinMessage
     */
    public void send(SixinMessage sixinMessage) {
        PacketData packetData = conversToSentPackData(sixinMessage);
        if (null != packetData) {
            MiLinkClientAdapter.getsInstance().sendAsync(packetData);
        }
    }

    /**
     * 发送一条带response的消息
     *
     * @param sixinMessage
     * @return
     */
    public Observable<LiveMessageProto.ChatMessageResponse> sendAndWaitResponse(SixinMessage sixinMessage) {
        final PacketData packetData = conversToSentPackData(sixinMessage);

        return Observable.create(new Observable.OnSubscribe<LiveMessageProto.ChatMessageResponse>() {
            @Override
            public void call(Subscriber<? super LiveMessageProto.ChatMessageResponse> subscriber) {
                PacketData result = MiLinkClientAdapter.getsInstance().sendSync(packetData, TIME_OUT);
                LiveMessageProto.ChatMessageResponse chatMessageResponse = null;
                if (result != null && result.getData() != null) {
                    try {
                        chatMessageResponse = LiveMessageProto.ChatMessageResponse.parseFrom(result.getData());
                    } catch (Exception e) {

                    }
                }
                subscriber.onNext(chatMessageResponse);
                subscriber.onCompleted();
            }
        });
    }

    /**
     * 发送一条ack消息
     *
     * @param target
     * @param cid
     * @param msgSeq
     * @param followerType
     */
    public void sendReadAck(long target, long cid, long msgSeq, int followerType) {
        if (cid < 0 || msgSeq < 0) {
            return;
        }
        long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
        if (mySelfId == 0) {
            return;
        }
        LiveMessageProto.ChatMessageReadRequest chatMessageReadRequest = LiveMessageProto.ChatMessageReadRequest.newBuilder().
                setToUser(target).setFromUser(mySelfId).setReadMsgSeq(msgSeq).setFollowType(followerType).setCid(cid).build();
        if (chatMessageReadRequest == null) {
            MyLog.w(TAG + "sendMessage chaMessageReadRequest is null,so cancel");
            return;
        }
        if (chatMessageReadRequest.getFromUser() < 0 || chatMessageReadRequest.getToUser() < 0 || chatMessageReadRequest.getCid() <= 0) {
            MyLog.w(TAG + "sendMessage chaMessageReadRequest from or to or cid is null,so cancel");
            return;
        }
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_SEND_READ_MSG);
        packetData.setData(chatMessageReadRequest.toByteArray());
        MiLinkClientAdapter.getsInstance().sendAsync(packetData, TIME_OUT);
    }


    /**
     * 发送同步消息
     *
     * @return
     */
    public long sendSyncUnreadMessage() {
        //断网重连初次sync
        long cid = System.currentTimeMillis();
        long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
        if (mySelfId == 0) {
            return 0;
        }
        LiveMessageProto.SyncUnreadRequest syncUnreadRequest = LiveMessageProto.SyncUnreadRequest.newBuilder()
                .setCid(cid).setPageId("").setLimit(50).setFromUser(mySelfId).build();
        PreferenceUtils.setSettingString(GlobalData.app(), PreferenceUtils.PREF_KEY_SIXIN_SYNC_PAGE_ID, "");
        sendSyncUnreadRequest(syncUnreadRequest);
        return cid;
    }


    /**
     * @param followerType
     * @return
     */
    public long sendSyncUnreadMessage(int followerType) {
        //断网重连初次sync
        long cid = System.currentTimeMillis();
        long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
        if (mySelfId == 0) {
            return 0;
        }
        LiveMessageProto.SyncUnreadRequest syncUnreadRequest = LiveMessageProto.SyncUnreadRequest.newBuilder()
                .setCid(cid).setPageId("").setLimit(50).setFromUser(mySelfId).setFollowType(followerType).build();
        PreferenceUtils.setSettingString(GlobalData.app(), PreferenceUtils.PREF_KEY_SIXIN_SYNC_PAGE_ID, "");
        sendSyncUnreadRequest(syncUnreadRequest);
        return cid;
    }

    public long sendSyncUnreadMessage(String pageId) {
        //在私信列表页　手动往下滚，滚到最后sync,类似　米聊pullold机制
        long cid = System.currentTimeMillis();
        long mySelfId = UserAccountManager.getInstance().getUuidAsLong();
        LiveMessageProto.SyncUnreadRequest syncUnreadRequest = LiveMessageProto.SyncUnreadRequest.newBuilder()
                .setCid(cid).setPageId(pageId).setLimit(50).setFromUser(mySelfId).build();
        sendSyncUnreadRequest(syncUnreadRequest);
        return cid;
    }


    private PacketData conversToSentPackData(SixinMessage msg) {
        LiveMessageProto.ChatMessageRequest.Builder chatMessageRequest = LiveMessageProto.ChatMessageRequest.newBuilder();
        chatMessageRequest
                .setCid(msg.getSenderMsgId())
                .setFromUser(msg.getSender())
                .setToUser(msg.getTarget())
                .setMsgType(msg.getMsgTyppe())
                .setMsgBody(msg.getBody());

        ByteString extData = msg.toExt();
        if (extData != null) {
            chatMessageRequest.setMsgExt(extData);
        }

        if (!msg.extNeedNull && extData == null) {
            return null;
        }

        LiveMessageProto.ChatMessageRequest chatMessageReadRequest = chatMessageRequest.build();
        if (chatMessageReadRequest == null) {
            MyLog.w(TAG + "sendMessage chaMessageReadRequest is null,so cancel");
            return null;
        }
        if (chatMessageReadRequest.getFromUser() < 0 || chatMessageReadRequest.getToUser() < 0 || chatMessageReadRequest.getCid() <= 0) {
            MyLog.w(TAG + "sendMessage chaMessageReadRequest from or to or cid is null,so cancel");
            return null;
        }
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_SEND_CHAT_MSG);
        packetData.setData(chatMessageReadRequest.toByteArray());
        return packetData;
    }

    private void sendSyncUnreadRequest(LiveMessageProto.SyncUnreadRequest syncUnreadRequest) {
        if (syncUnreadRequest == null) {
            MyLog.w(TAG + "sendMessage chaMessageReadRequest is null,so cancel");
            return;
        }
        if (syncUnreadRequest.getFromUser() <= 0 || syncUnreadRequest.getCid() <= 0) {
            MyLog.w(TAG + "sendMessage chaMessageReadRequest from or to or cid is null,so cancel");
            return;
        }
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_SYNC_CHAT_MSG);
        packetData.setData(syncUnreadRequest.toByteArray());
        MiLinkClientAdapter.getsInstance().sendAsync(packetData, TIME_OUT);
    }
}


