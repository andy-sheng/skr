package com.wali.live.watchsdk.watch.presenter.push;

import com.mi.live.data.push.IPushMsgProcessor;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.common.flybarrage.manager.FlyBarrageManager;

/**
 * 礼物push的处理
 * Created by chengsimin on 16/7/4.
 */
public class GiftPresenter implements IPushMsgProcessor {
    LiveRoomChatMsgManager mRoomChatMsgManager;

    boolean mReplay;

    public GiftPresenter(LiveRoomChatMsgManager mRoomChatMsgManager, boolean replay) {
        this.mRoomChatMsgManager = mRoomChatMsgManager;
        this.mReplay = replay;
    }

    public void process(BarrageMsg msg, RoomBaseDataModel roomBaseDataModel) {
        String roomId = roomBaseDataModel.getRoomId();
        switch (msg.getMsgType()) {
            case BarrageMsgType.B_MSG_TYPE_GIFT: {
                // 礼物
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                // 回放不去重
                if (mReplay) {
                    ext.orderId = "";
                }
                if (!mRoomChatMsgManager.isHideGiftMsg()) {
                    GiftRepository.processGiftMsgByPushWay(msg, ext, roomId);
                    int count = ext.giftCount;
                    if (count <= 10) {
                        mRoomChatMsgManager.addChatMsg(msg, true);
                    }
                }
                int maxTicket = Math.max(roomBaseDataModel.getTicket(), ext.zhuboAsset);
                roomBaseDataModel.setTicket(maxTicket);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE: {
                // 付费弹幕 2
                FlyBarrageManager.processFlyBarrageMsg(msg);
                mRoomChatMsgManager.addChatMsg(msg, true);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE: {
                // 红包-礼物
                BarrageMsg.RedEnvelopMsgExt ext = (BarrageMsg.RedEnvelopMsgExt) msg.getMsgExt();
                if (ext != null) {
                    GiftRepository.processRedEnvelopeMsgByPushWay(ext);
                    mRoomChatMsgManager.addChatMsg(msg, true);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT: {
                // 礼物--背景礼物
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                // 回放不去重
                if (mReplay) {
                    ext.orderId = "";
                }
                // 房间背景礼物
                GiftRepository.processGiftMsgByPushWay(msg, ext, roomId);
                mRoomChatMsgManager.addChatMsg(msg, true);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT: {
                // 礼物 点亮礼物-珍视明
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                // 回放不去重
                if (mReplay) {
                    ext.orderId = "";
                }
                // 房间点亮礼物
                GiftRepository.processGiftMsgByPushWay(msg, ext, roomId);
                mRoomChatMsgManager.addChatMsg(msg, true);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_GLABAL_MSG: {
                // 礼物 大金龙
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                // 回放不去重
                if (mReplay) {
                    ext.orderId = "";
                }
                if (!mRoomChatMsgManager.isHideGiftMsg()) {
                    // 全局消息礼物，大金龙
                    GiftRepository.processGiftMsgByPushWay(msg, ext, roomId);
                    mRoomChatMsgManager.addChatMsg(msg, true);
                }
                int maxTicket = Math.max(roomBaseDataModel.getTicket(), ext.zhuboAsset);
                roomBaseDataModel.setTicket(maxTicket);
            }
            break;
        }
    }

    public int[] getAcceptMsgType() {
        return new int[]{
                BarrageMsgType.B_MSG_TYPE_GIFT,
                BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE,
                BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE,
                BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT,
                BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT,
                BarrageMsgType.B_MSG_TYPE_GLABAL_MSG
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

    }
}
