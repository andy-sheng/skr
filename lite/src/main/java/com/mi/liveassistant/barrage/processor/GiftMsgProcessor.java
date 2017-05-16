package com.mi.liveassistant.barrage.processor;

import android.text.TextUtils;

import com.mi.liveassistant.barrage.data.Message;
import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.BarrageMsgType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by wuxiaoshan on 17-5-8.
 */
public class GiftMsgProcessor extends MsgProcessor {

    private static final String TAG = GiftMsgProcessor.class.getSimpleName();

    private static HashSet<String> mGiftSetForDistinct = new HashSet();

    private static LinkedList<String> mGiftQueueForDistinct = new LinkedList();

    private static final int DUPLICATE_CACHE_NUMBER = 100;


    public GiftMsgProcessor(IMsgDispenser msgDispenser){
        super(msgDispenser);
    }

    @Override
    public void process(BarrageMsg msg, String roomId) {
        if(msg == null || !roomId.equals(msg.getRoomId())){
            return;
        }
        switch (msg.getMsgType()) {
            case BarrageMsgType.B_MSG_TYPE_GIFT: {
                // 礼物
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                int count = ext.giftCount;
                List<Message> messageList = new ArrayList<>();
                messageList.add(Message.loadFromBarrage(msg));
                if (count <= 10) {
                    mIMsgDispenser.addChatMsg(messageList);
                }
                mIMsgDispenser.addInternalMsgListener(messageList);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE: {
                List<Message> messageList = new ArrayList<>();
                messageList.add(Message.loadFromBarrage(msg));
                mIMsgDispenser.addChatMsg(messageList);
                mIMsgDispenser.addInternalMsgListener(messageList);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE: {
                // 红包-礼物
                BarrageMsg.RedEnvelopMsgExt ext = (BarrageMsg.RedEnvelopMsgExt) msg.getMsgExt();
                if (ext != null) {
                    List<Message> messageList = new ArrayList<>();
                    messageList.add(Message.loadFromBarrage(msg));
                    mIMsgDispenser.addChatMsg(messageList);
//                    mIMsgDispenser.addInternalMsgListener(messageList);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT: {
                // 礼物--背景礼物
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                if (ext != null) {
                    List<Message> messageList = new ArrayList<>();
                    messageList.add(Message.loadFromBarrage(msg));
                    mIMsgDispenser.addChatMsg(messageList);
                    mIMsgDispenser.addInternalMsgListener(messageList);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT: {
                // 礼物 点亮礼物-珍视明
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                if (ext != null) {
                    List<Message> messageList = new ArrayList<>();
                    messageList.add(Message.loadFromBarrage(msg));
                    mIMsgDispenser.addChatMsg(messageList);
                    mIMsgDispenser.addInternalMsgListener(messageList);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_GLABAL_MSG: {
                // 礼物 大金龙
                BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                if (ext != null) {
                    List<Message> messageList = new ArrayList<>();
                    messageList.add(Message.loadFromBarrage(msg));
                    mIMsgDispenser.addChatMsg(messageList);
                    mIMsgDispenser.addInternalMsgListener(messageList);
                }
            }
            break;
        }
    }

    @Override
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

    /**
     * 用作礼物去重
     *
     * @param orderId
     * @return
     */
    private static synchronized boolean checkIsDuplicateGift(String orderId) {
        if (TextUtils.isEmpty(orderId)) {
            //为空放行
            return false;
        }
        if (mGiftSetForDistinct.contains(orderId)) {
            return true;
        } else {
            if (mGiftQueueForDistinct.size() > DUPLICATE_CACHE_NUMBER) {
                String old = mGiftQueueForDistinct.remove();
                mGiftSetForDistinct.remove(old);
            }
            mGiftQueueForDistinct.add(orderId);
            mGiftSetForDistinct.add(orderId);
            return false;
        }
    }
}
