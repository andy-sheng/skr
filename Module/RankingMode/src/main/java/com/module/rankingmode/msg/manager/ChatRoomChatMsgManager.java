package com.module.rankingmode.msg.manager;

import android.text.TextUtils;
import android.widget.TextView;

import com.common.log.MyLog;
import com.module.rankingmode.msg.event.CommentMsgEvent;
import com.module.rankingmode.msg.event.DynamicEmojiMsgEvent;
import com.module.rankingmode.msg.event.SpecialEmojiMsgEvent;
import com.zq.live.proto.Room.CommentMsg;
import com.zq.live.proto.Room.DynamicEmojiMsg;
import com.zq.live.proto.Room.SpecialEmojiMsg;

import org.greenrobot.eventbus.EventBus;

/**
 * 处理聊天室内弹幕信息，包括文本，动态表情和特殊表情
 */
// todo 仅加入接受通过融云服务器的请求，发送请求需要与服务器对再加上
public class ChatRoomChatMsgManager {
    public final static String TAG = "ChatRoomChatMsgManager";

    // 评论消息
    public static void processRMComment(CommentMsg commentMsg) {
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
    public static void processRMSpecialEmoji(SpecialEmojiMsg specialEmojiMsg) {
        if (specialEmojiMsg == null) {
            MyLog.e(TAG, "processRMSpecialEmoji" + " specialEmojiMsg == null");
            return;
        }

        int emojiId = specialEmojiMsg.getId();
        EventBus.getDefault().post(new SpecialEmojiMsgEvent(SpecialEmojiMsgEvent.MSG_TYPE_RECE, emojiId));
    }

    // 动态表情消息
    public static void processRMDynamicEmoji(DynamicEmojiMsg dynamicEmojiMsg) {
        if (dynamicEmojiMsg == null) {
            MyLog.e(TAG, "processRMDynamicEmoji" + " dynamicEmojiMsg == null");
            return;
        }

        int emojiId = dynamicEmojiMsg.getId();
        EventBus.getDefault().post(new DynamicEmojiMsgEvent(DynamicEmojiMsgEvent.MSG_TYPE_RECE, emojiId));
    }
}
