package com.module.playways.room.msg.event;

import android.text.TextUtils;

import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.dynamicmsg.DynamicModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.EVIPType;
import com.zq.live.proto.Common.UserInfo;
import com.zq.live.proto.Common.VipInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;


public class EventHelper {
    /**
     * 假装是一个服务器的评论push
     *
     * @param text
     */
    public static void pretendCommentPush(String text, int roomId) {
        pretendCommentPush(text, roomId, null);
    }

    public static void pretendCommentPush(String text, int roomId, UserInfoModel receiver) {
        if (!TextUtils.isEmpty(text)) {
            BasePushInfo basePushInfo = new BasePushInfo();
            basePushInfo.setRoomID(roomId);

            basePushInfo.setSender(MyUserInfo.toUserInfoPB(MyUserInfoManager.INSTANCE.getMyUserInfo()));
            CommentMsgEvent commentMsgEvent = new CommentMsgEvent(basePushInfo, CommentMsgEvent.MSG_TYPE_SEND, text);
            if (receiver != null) {
                ArrayList<UserInfoModel> userInfoModels = new ArrayList<>();
                userInfoModels.add(receiver);
                commentMsgEvent.mUserInfoModelList = userInfoModels;
            }
            EventBus.getDefault().post(commentMsgEvent);
        }
    }

    /**
     * 假装是一个服务器的评论push
     */
    public static void pretendDynamicPush(DynamicModel dynamicModel, int roomId) {
        if (dynamicModel != null) {
            BasePushInfo basePushInfo = new BasePushInfo();
            basePushInfo.setRoomID(roomId);

            basePushInfo.setSender(MyUserInfo.toUserInfoPB(MyUserInfoManager.INSTANCE.getMyUserInfo()));
            EventBus.getDefault().post(new DynamicEmojiMsgEvent(basePushInfo, DynamicEmojiMsgEvent.MSG_TYPE_SEND, dynamicModel));
        }
    }


    public static void pretendAudioPush(String localPath, long duration, String url, int roomId) {
        BasePushInfo basePushInfo = new BasePushInfo();
        basePushInfo.setRoomID(roomId);

        basePushInfo.setSender(MyUserInfo.toUserInfoPB(MyUserInfoManager.INSTANCE.getMyUserInfo()));
        EventBus.getDefault().post(new AudioMsgEvent(basePushInfo, AudioMsgEvent.MSG_TYPE_SEND, localPath, duration, url));
    }
}
