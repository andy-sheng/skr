package com.mi.live.data.push;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.ByteString;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.push.event.BarrageMsgEvent;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.LiveMessageProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chengsimin on 16/9/12.
 */
public class SendBarrageManager {
    public final static String TAG = SendBarrageManager.class.getSimpleName();

    //发送弹幕消息，异步接口
    public static Observable<Void> sendBarrageMessageAsync(final BarrageMsg msg) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                if (msg != null) {
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
                    MyLog.v(TAG, "sendBarrageMessageAsync send:" + msg.getSenderMsgId() + "body :" + msg.getBody());
                    MiLinkClientAdapter.getsInstance().sendAsync(packetData);
                }
                subscriber.onCompleted();
            }
        });
    }

    public static BarrageMsg createBarrage(int msgType, String body, String roomid, long anchorId, long sentTime , BarrageMsg.MsgExt ext){
        BarrageMsg msg = new BarrageMsg();
        msg.setMsgType(msgType);
        msg.setSender(UserAccountManager.getInstance().getUuidAsLong());
        String nickname = MyUserInfoManager.getInstance().getUser().getNickname();
        if(TextUtils.isEmpty(nickname)){
            nickname = String.valueOf(UserAccountManager.getInstance().getUuidAsLong());
        }
        msg.setSenderName(nickname);
        msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
        msg.setRoomId(roomid);
        msg.setBody(body);
        msg.setAnchorId(anchorId);
        msg.setSentTime(sentTime);
        if (MyUserInfoManager.getInstance().getUser() != null) {
            msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
        }
        if (ext != null) {
            msg.setMsgExt(ext);
        }
        msg.setRedName(MyUserInfoManager.getInstance().getUser().isRedName());
        return msg;
    }

    private static void sendRecvEvent(List<BarrageMsg> barrageMsgList) {
        if (barrageMsgList != null) {
            MyLog.v(TAG, "sendRecvEvent list.size:" + barrageMsgList.size());
            EventBus.getDefault().post(new BarrageMsgEvent.ReceivedBarrageMsgEvent(barrageMsgList,"sendRecvEvent"));
        }
    }


    /**
     * 伪装成pushmessage发出去
     *
     * @param msg
     */
    public static void pretendPushBarrage(BarrageMsg msg) {
        ArrayList<BarrageMsg> barrageMsgList = new ArrayList<BarrageMsg>(1);
        if (msg != null) {
            barrageMsgList.add(msg);
        }
        sendRecvEvent(barrageMsgList);
    }
}
