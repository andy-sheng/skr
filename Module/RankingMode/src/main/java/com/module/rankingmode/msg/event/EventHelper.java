package com.module.rankingmode.msg.event;

import android.text.TextUtils;

import com.module.rankingmode.msg.BasePushInfo;

import org.greenrobot.eventbus.EventBus;

public class EventHelper {
    /**
     * 假装是一个服务器的评论push
     * @param text
     */
    public static void pretendCommentPush(String text){
        if (!TextUtils.isEmpty(text)) {
            BasePushInfo basePushInfo = new BasePushInfo();
            EventBus.getDefault().post(new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text));
        }
    }
}
