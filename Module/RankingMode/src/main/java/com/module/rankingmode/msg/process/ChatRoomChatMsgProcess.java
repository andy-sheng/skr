package com.module.rankingmode.msg.process;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.module.rankingmode.msg.BasePushInfo;
import com.module.rankingmode.msg.event.CommentMsgEvent;
import com.module.rankingmode.msg.event.DynamicEmojiMsgEvent;
import com.module.rankingmode.msg.event.SpecialEmojiMsgEvent;
import com.zq.live.proto.Room.CommentMsg;
import com.zq.live.proto.Room.DynamicEmojiMsg;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.SpecialEmojiMsg;

import org.greenrobot.eventbus.EventBus;

public class ChatRoomChatMsgProcess implements IPushChatRoomMsgProcess {

    public final static String TAG = "ChatRoomChatMsgProcess";

    @Override
    public void processRoomMsg(ERoomMsgType messageType, RoomMsg msg) {
        BasePushInfo info = BasePushInfo.parse(msg);

        if (messageType == ERoomMsgType.RM_COMMENT) {
            processRMComment(info, msg.getCommentMsg());
        } else if (messageType == ERoomMsgType.RM_SPECIAL_EMOJI) {
            processRMSpecialEmoji(info, msg.getSpecialEmojiMsg());
        } else if (messageType == ERoomMsgType.RM_DYNAMIC_EMOJI) {
            processRMDynamicEmoji(info, msg.getDynamicemojiMsg());
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
    public void processRMComment(BasePushInfo info, CommentMsg commentMsg) {
        if (commentMsg == null) {
            MyLog.e(TAG, "processRMComment" + " commentMsg == null");
            return;
        }

        String text = commentMsg.getText();
        if (!TextUtils.isEmpty(text)) {
            EventBus.getDefault().post(new CommentMsgEvent(info, CommentMsgEvent.MSG_TYPE_RECE, text));
        }
    }

    // 特殊表情消息
    public void processRMSpecialEmoji(BasePushInfo info, SpecialEmojiMsg specialEmojiMsg) {
        if (specialEmojiMsg == null) {
            MyLog.e(TAG, "processRMSpecialEmoji" + " specialEmojiMsg == null");
            return;
        }

        int emojiId = specialEmojiMsg.getId();
        EventBus.getDefault().post(new SpecialEmojiMsgEvent(info, SpecialEmojiMsgEvent.MSG_TYPE_RECE, emojiId));
    }

    // 动态表情消息
    public void processRMDynamicEmoji(BasePushInfo info, DynamicEmojiMsg dynamicEmojiMsg) {
        if (dynamicEmojiMsg == null) {
            MyLog.e(TAG, "processRMDynamicEmoji" + " dynamicEmojiMsg == null");
            return;
        }

        int emojiId = dynamicEmojiMsg.getId();
        EventBus.getDefault().post(new DynamicEmojiMsgEvent(info, DynamicEmojiMsgEvent.MSG_TYPE_RECE, emojiId));
    }
}
