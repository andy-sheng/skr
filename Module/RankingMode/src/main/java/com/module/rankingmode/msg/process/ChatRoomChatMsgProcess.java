package com.module.rankingmode.msg.process;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.module.rankingmode.msg.event.CommentMsgEvent;
import com.module.rankingmode.msg.event.DynamicEmojiMsgEvent;
import com.module.rankingmode.msg.event.SpecialEmojiMsgEvent;
import com.zq.live.proto.Room.CommentMsg;
import com.zq.live.proto.Room.DynamicEmojiMsg;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.SpecialEmojiMsg;

import org.greenrobot.eventbus.EventBus;

public class ChatRoomChatMsgProcess implements IPushChatRoomMsgProcess{

    public final static String TAG = "ChatRoomChatMsgProcess";

    @Override
    public void processRoomMsg(ERoomMsgType messageType, RoomMsg msg) {
        if(messageType == ERoomMsgType.RM_COMMENT){
            processRMComment(msg.getCommentMsg());
        }else if (messageType == ERoomMsgType.RM_SPECIAL_EMOJI){
            processRMSpecialEmoji(msg.getSpecialEmojiMsg());
        }else if (messageType == ERoomMsgType.RM_DYNAMIC_EMOJI){
            processRMDynamicEmoji(msg.getDynamicemojiMsg());
        }
    }

    @Override
    public ERoomMsgType[] acceptType() {
        return new ERoomMsgType[]{
                ERoomMsgType.RM_COMMENT,
                ERoomMsgType.RM_SPECIAL_EMOJI,
                ERoomMsgType.RM_DYNAMIC_EMOJI
        };
    }

    // 评论消息
    public void processRMComment(CommentMsg commentMsg) {
        if (commentMsg == null) {
            MyLog.e(TAG, "processRMComment" + " commentMsg == null");
            return;
        }

        String text = commentMsg.getText();
        if (!TextUtils.isEmpty(text)) {
            EventBus.getDefault().post(new CommentMsgEvent(CommentMsgEvent.MSG_TYPE_RECE, text));
        }
    }

    // 特殊表情消息
    public void processRMSpecialEmoji(SpecialEmojiMsg specialEmojiMsg) {
        if (specialEmojiMsg == null) {
            MyLog.e(TAG, "processRMSpecialEmoji" + " specialEmojiMsg == null");
            return;
        }

        int emojiId = specialEmojiMsg.getId();
        EventBus.getDefault().post(new SpecialEmojiMsgEvent(SpecialEmojiMsgEvent.MSG_TYPE_RECE, emojiId));
    }

    // 动态表情消息
    public void processRMDynamicEmoji(DynamicEmojiMsg dynamicEmojiMsg) {
        if (dynamicEmojiMsg == null) {
            MyLog.e(TAG, "processRMDynamicEmoji" + " dynamicEmojiMsg == null");
            return;
        }

        int emojiId = dynamicEmojiMsg.getId();
        EventBus.getDefault().post(new DynamicEmojiMsgEvent(DynamicEmojiMsgEvent.MSG_TYPE_RECE, emojiId));
    }
}
