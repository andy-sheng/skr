package com.wali.live.common.flybarrage.manager;

import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.flybarrage.model.FlyBarrageInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;

/**
 * Created by chengsimin on 16/3/22.
 */
public class FlyBarrageManager {
    public static final String TAG = "FlyBarrageManager";

    public static BarrageMsg createFlyBarrageMessage(int giftId, String content, int zhuboAsset, long zhuboAssetTs, String roomId, String ownerId) {
        // 直接丢到队列
        BarrageMsg msg = new BarrageMsg();
        msg.setRoomId(roomId);
        msg.setAnchorId(Long.parseLong(ownerId));
        msg.setMsgType(BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE);
        msg.setSender(MyUserInfoManager.getInstance().getUser().getUid());
        String senderName = MyUserInfoManager.getInstance().getUser().getNickname();
        msg.setSenderName(senderName);
        msg.setSenderLevel(MyUserInfoManager.getInstance().getUser().getLevel());
        msg.setCertificationType(MyUserInfoManager.getInstance().getUser().getCertificationType());
        msg.setSentTime(System.currentTimeMillis());
        msg.setBody(content);
        BarrageMsg.GiftMsgExt ext = new BarrageMsg.GiftMsgExt();
        ext.giftId = giftId;
        ext.giftName = "付费弹幕";
        ext.giftCount = 1;
        ext.zhuboAsset = zhuboAsset;
        ext.zhuboAssetTs = zhuboAssetTs;
        ext.continueId = 0;
        ext.msgBody = content;
        ext.avatarTimestamp = MyUserInfoManager.getInstance().getUser().getAvatar();
        msg.setMsgExt(ext);
        return msg;
    }

    public static void processFlyBarrageMsg(BarrageMsg msg) {
        // 放到GiftManager中处理
        if (msg != null) {
            // 不是本房间的不显示
            if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE) {
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                // 付费弹幕
                FlyBarrageInfo model = new FlyBarrageInfo();
                model.setName(msg.getSenderName());
                model.setSenderId(msg.getSender());
                model.setContent(ext.msgBody);
                model.setAvatarTimestamp(ext.avatarTimestamp);
                model.setCertificationType(msg.getCertificationType());
                model.setLevel(msg.getSenderLevel());
                EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.FlyBarrage(model));
            }
        }
    }

    public static void testFlyBarrage(String roomId,String ownerId){
        BarrageMessageManager.getInstance().pretendPushBarrage(createFlyBarrageMessage(123123, new Date().toString(),123123,System.currentTimeMillis(),roomId,ownerId));
    }

}
