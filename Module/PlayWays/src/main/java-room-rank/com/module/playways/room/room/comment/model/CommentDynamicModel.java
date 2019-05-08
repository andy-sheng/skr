package com.module.playways.room.room.comment.model;

import android.graphics.Color;
import android.text.TextUtils;

import com.common.core.userinfo.model.UserInfoModel;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.dynamicmsg.DynamicModel;
import com.module.playways.room.msg.event.DynamicEmojiMsgEvent;

public class CommentDynamicModel extends CommentModel {

    private DynamicModel mDynamicModel;

    public CommentDynamicModel(){
        setCommentType(TYPE_DYNAMIC);
    }

    // 动态表情消息
    public static CommentDynamicModel parseFromEvent(DynamicEmojiMsgEvent event, BaseRoomData roomData) {
        CommentDynamicModel commentModel = new CommentDynamicModel();
        commentModel.setUserId(event.info.getSender().getUserID());
        if (!TextUtils.isEmpty(event.info.getSender().getNickName())) {
            commentModel.setUserName(event.info.getSender().getNickName());
        } else {
            UserInfoModel userInfoModel = roomData.getUserInfo(event.info.getSender().getUserID());
            commentModel.setUserName(userInfoModel.getNickname());
        }

        if (roomData != null) {
            UserInfoModel sender = roomData.getUserInfo(event.info.getSender().getUserID());
            commentModel.setAvatarColor(Color.WHITE);
            if (sender != null) {
                commentModel.setAvatar(sender.getAvatar());
            } else {
                commentModel.setAvatar(event.info.getSender().getAvatar());
            }
        }

        commentModel.setDynamicModel(event.mDynamicModel);
        return commentModel;
    }

    public DynamicModel getDynamicModel() {
        return mDynamicModel;
    }

    public void setDynamicModel(DynamicModel dynamicModel) {
        mDynamicModel = dynamicModel;
    }

}
