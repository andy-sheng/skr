package com.module.rankingmode.msg.process;

import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.common.log.MyLog;
import com.module.msg.CustomMsgType;
import com.module.msg.IPushMsgProcess;
import com.module.rankingmode.msg.manager.ChatRoomChatMsgManager;
import com.module.rankingmode.msg.manager.ChatRoomGameMsgManager;
import com.zq.live.proto.Room.ERoomMsgType;
import com.zq.live.proto.Room.RoomMsg;

import java.io.IOException;

public class ChatRoomMsgProcess implements IPushMsgProcess {
    public final static String TAG = "ChatRoomMsgProcess";

    @Override
    public void process(int messageType, JSONObject jsonObject) {

    }

    @Override
    public void process(int messageType, byte[] data) {
        MyLog.d(TAG, "process" + " messageType=" + messageType + " data=" + data);
        switch (messageType) {
            case CustomMsgType.MSG_TYPE_TEXT:
                break;
            case CustomMsgType.MSG_TYPE_ENTER:
                break;
            case CustomMsgType.MSG_TYPE_QUIT:
                break;
            case CustomMsgType.MSG_TYPE_ROOM:
                processRoomMsg(data);
                break;
        }

    }

    @Override
    public int[] acceptType() {
        return new int[]{
                CustomMsgType.MSG_TYPE_ROOM
        };
    }

    // 处理房间消息
    private void processRoomMsg(byte[] data) {
        try {
            RoomMsg msg = RoomMsg.parseFrom(data);

            if (msg == null){
                MyLog.e(TAG, "processRoomMsg" + " msg == null ");
                return;
            }

            if (msg.getMsgType() == ERoomMsgType.RM_UNKNOWN) {
                MyLog.w(TAG, "processRoomMsg" + " unknown msg ");
            }else if (msg.getMsgType() == ERoomMsgType.RM_COMMENT) {
                ChatRoomChatMsgManager.processRMComment(msg.getCommentMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_SPECIAL_EMOJI) {
                ChatRoomChatMsgManager.processRMSpecialEmoji(msg.getSpecialEmojiMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_DYNAMIC_EMOJI) {
                ChatRoomChatMsgManager.processRMDynamicEmoji(msg.getDynamicemojiMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_JOIN_ACTION) {
                ChatRoomGameMsgManager.processJoinActionMsg(msg.getJoinActionMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_JOIN_NOTICE) {
                ChatRoomGameMsgManager.processJoinNoticeMsg(msg.getJoinNoticeMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_READY_NOTICE) {
                ChatRoomGameMsgManager.processReadyNoticeMsg(msg.getReadyNoticeMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_READY_AND_START_NOTICE) {
                ChatRoomGameMsgManager.processReadyAndStartNoticeMsg(msg.getReadyAndStartNoticeMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_OVER) {
                ChatRoomGameMsgManager.processRoundOverMsg(msg.getRoundOverMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_ROUND_AND_GAME_OVER) {
                ChatRoomGameMsgManager.processRoundAndGameOverMsg(msg.getRoundAndGameOverMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_QUIT_GAME) {
                ChatRoomGameMsgManager.processQuitGameMsg(msg.getQuitGameMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_APP_SWAP) {
                ChatRoomGameMsgManager.processAppSwapMsg(msg.getAppSwapMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_SYNC_STATUS){
                ChatRoomGameMsgManager.processSyncStatusMsg(msg.getSyncStatusMsg());
            } else if (msg.getMsgType() == ERoomMsgType.RM_ROOM_IN_OUT){
                ChatRoomGameMsgManager.processRoomInOutMsg(msg.getRoomInOutMsg());
            }
        } catch (IOException e) {
            MyLog.e(e);
        }

    }
}
