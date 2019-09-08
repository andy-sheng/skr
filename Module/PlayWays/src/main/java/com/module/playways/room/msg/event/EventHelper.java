package com.module.playways.room.msg.event;

import android.text.TextUtils;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.grab.room.dynamicmsg.DynamicModel;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.Common.ESex;
import com.zq.live.proto.Common.EVIPType;
import com.zq.live.proto.Common.UserInfo;

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

            ESex sex = ESex.SX_UNKNOWN;
            if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
                sex = ESex.SX_MALE;
            } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
                sex = ESex.SX_FEMALE;
            }

            UserInfo userInfo = new UserInfo((int) MyUserInfoManager.getInstance().getUid()
                    , MyUserInfoManager.getInstance().getNickName()
                    , MyUserInfoManager.getInstance().getAvatar()
                    , sex
                    , ""
                    , false
                    , 0
                    , EVIPType.EVT_GOLDEN_V, null);

            basePushInfo.setSender(userInfo);
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

            ESex sex = ESex.SX_UNKNOWN;
            if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
                sex = ESex.SX_MALE;
            } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
                sex = ESex.SX_FEMALE;
            }
            EVIPType vipType = EVIPType.EVT_UNKNOWN;
            if (MyUserInfoManager.getInstance().getVipType() == EVIPType.EVT_RED_V.getValue()) {
                vipType = EVIPType.EVT_RED_V;
            } else if (MyUserInfoManager.getInstance().getVipType() == EVIPType.EVT_GOLDEN_V.getValue()) {
                vipType = EVIPType.EVT_GOLDEN_V;
            }
            UserInfo userInfo = new UserInfo((int) MyUserInfoManager.getInstance().getUid()
                    , MyUserInfoManager.getInstance().getNickName()
                    , MyUserInfoManager.getInstance().getAvatar()
                    , sex
                    , ""
                    , false
                    , 0
                    , vipType, null);

            basePushInfo.setSender(userInfo);
            EventBus.getDefault().post(new DynamicEmojiMsgEvent(basePushInfo, DynamicEmojiMsgEvent.MSG_TYPE_SEND, dynamicModel));
        }
    }


    public static void pretendAudioPush(String localPath, long duration, String url, int roomId) {
        BasePushInfo basePushInfo = new BasePushInfo();
        basePushInfo.setRoomID(roomId);

        ESex sex = ESex.SX_UNKNOWN;
        if (MyUserInfoManager.getInstance().getSex() == ESex.SX_MALE.getValue()) {
            sex = ESex.SX_MALE;
        } else if (MyUserInfoManager.getInstance().getSex() == ESex.SX_FEMALE.getValue()) {
            sex = ESex.SX_FEMALE;
        }
        EVIPType vipType = EVIPType.EVT_UNKNOWN;
        if (MyUserInfoManager.getInstance().getVipType() == EVIPType.EVT_RED_V.getValue()) {
            vipType = EVIPType.EVT_RED_V;
        } else if (MyUserInfoManager.getInstance().getVipType() == EVIPType.EVT_GOLDEN_V.getValue()) {
            vipType = EVIPType.EVT_GOLDEN_V;
        }
        UserInfo userInfo = new UserInfo((int) MyUserInfoManager.getInstance().getUid()
                , MyUserInfoManager.getInstance().getNickName()
                , MyUserInfoManager.getInstance().getAvatar()
                , sex
                , ""
                , false
                , 0
                , vipType, null);
        basePushInfo.setSender(userInfo);
        EventBus.getDefault().post(new AudioMsgEvent(basePushInfo, AudioMsgEvent.MSG_TYPE_SEND, localPath, duration, url));
    }
}
