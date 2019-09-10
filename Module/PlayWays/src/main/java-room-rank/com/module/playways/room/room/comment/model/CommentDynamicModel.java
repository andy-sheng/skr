package com.module.playways.room.room.comment.model;

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
        if (roomData != null) {
            UserInfoModel sender = roomData.getPlayerOrWaiterInfo(event.info.getSender().getUserID());
            commentModel.setAvatarColor(CommentModel.AVATAR_COLOR);
            if (sender != null) {
                commentModel.setUserInfo(sender);
            } else {
                commentModel.setUserInfo(UserInfoModel.parseFromPB(event.info.getSender()));
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
