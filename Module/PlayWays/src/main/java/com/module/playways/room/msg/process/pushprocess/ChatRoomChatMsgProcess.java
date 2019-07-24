package com.module.playways.room.msg.process.pushprocess;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.module.playways.room.msg.event.AudioMsgEvent;
import com.module.playways.room.msg.event.DynamicEmojiMsgEvent;
import com.module.playways.room.msg.event.SpecialEmojiMsgEvent;
import com.module.playways.room.msg.BasePushInfo;
import com.module.playways.room.msg.event.CommentMsgEvent;
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess;
import com.zq.live.proto.Room.AudioMsg;
import com.zq.live.proto.Room.CommentMsg;
import com.zq.live.proto.Room.DynamicEmojiMsg;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;
import com.zq.live.proto.Room.SpecialEmojiMsg;

import org.greenrobot.eventbus.EventBus;

public class ChatRoomChatMsgProcess implements IPushChatRoomMsgProcess<ERoomMsgType, RoomMsg> {

    public final String TAG = "ChatRoomChatMsgProcess";

    @Override
    public void processRoomMsg(ERoomMsgType messageType, RoomMsg msg) {
        MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType);
        BasePushInfo info = BasePushInfo.parse(msg);

        if (messageType == ERoomMsgType.RM_COMMENT) {
            processRMComment(info, msg.getCommentMsg());
        } else if (messageType == ERoomMsgType.RM_SPECIAL_EMOJI) {
            processRMSpecialEmoji(info, msg.getSpecialEmojiMsg());
        } else if (messageType == ERoomMsgType.RM_DYNAMIC_EMOJI) {
            processRMDynamicEmoji(info, msg.getDynamicemojiMsg());
        } else if (messageType == ERoomMsgType.RM_AUDIO_MSG) {
            processRMAudio(info, msg.getAudioMsg());
        }
    }

    @Override
    public ERoomMsgType[] acceptType() {
        return new ERoomMsgType[]{
                ERoomMsgType.RM_COMMENT,
                ERoomMsgType.RM_SPECIAL_EMOJI,
                ERoomMsgType.RM_DYNAMIC_EMOJI,
                ERoomMsgType.RM_AUDIO_MSG
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
            EventBus.getDefault().post(new CommentMsgEvent(info, CommentMsgEvent.MSG_TYPE_RECE, commentMsg));
        }
    }

    // 特殊表情消息
    public void processRMSpecialEmoji(BasePushInfo info, SpecialEmojiMsg specialEmojiMsg) {
        if (specialEmojiMsg == null) {
            MyLog.e(TAG, "processRMSpecialEmoji" + " specialEmojiMsg == null");
            return;
        }

        SpecialEmojiMsgEvent specialEmojiMsgEvent = new SpecialEmojiMsgEvent(info);
        specialEmojiMsgEvent.emojiType = specialEmojiMsg.getEmojiType();
        specialEmojiMsgEvent.count = specialEmojiMsg.getCount();
        specialEmojiMsgEvent.action = specialEmojiMsg.getEmojiAction();
        specialEmojiMsgEvent.coutinueId = specialEmojiMsg.getContinueId();

        EventBus.getDefault().post(specialEmojiMsgEvent);
    }

    // 动态表情消息
    public void processRMDynamicEmoji(BasePushInfo info, DynamicEmojiMsg dynamicEmojiMsg) {
        if (dynamicEmojiMsg == null) {
            MyLog.e(TAG, "processRMDynamicEmoji" + " dynamicEmojiMsg == null");
            return;
        }

        EventBus.getDefault().post(new DynamicEmojiMsgEvent(info, DynamicEmojiMsgEvent.MSG_TYPE_RECE, dynamicEmojiMsg));
    }

    private void processRMAudio(BasePushInfo info, AudioMsg audioMsg) {
        if (audioMsg == null) {
            MyLog.e(TAG, "processRMAudio" + " info=" + info + " audioMsg = null");
        }

        EventBus.getDefault().post(new AudioMsgEvent(info, AudioMsgEvent.MSG_TYPE_RECE, audioMsg));
    }
}
