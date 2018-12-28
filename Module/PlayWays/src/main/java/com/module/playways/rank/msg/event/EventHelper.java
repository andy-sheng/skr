package com.module.playways.rank.msg.event;

import android.text.TextUtils;

import com.common.core.myinfo.MyUserInfoManager;
import com.module.playways.rank.msg.BasePushInfo;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.UserInfo;

import org.greenrobot.eventbus.EventBus;


public class EventHelper {
    /**
     * 假装是一个服务器的评论push
     *
     * @param text
     */
    public static void pretendCommentPush(String text, int roomId) {
        if (!TextUtils.isEmpty(text)) {
            BasePushInfo basePushInfo = new BasePushInfo();
            basePushInfo.setRoomID(roomId);

            UserInfo userInfo = new UserInfo((int) MyUserInfoManager.getInstance().getUid()
                    , MyUserInfoManager.getInstance().getNickName()
                    , MyUserInfoManager.getInstance().getAvatar()
                    , ESex.SX_FEMALE
                    , ""
                    , false);

            basePushInfo.setSender(userInfo);
            EventBus.getDefault().post(new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text));
        }
    }
}
