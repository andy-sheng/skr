package com.module.playways.room.room.comment.model;

import android.text.SpannableStringBuilder;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.SpanUtils;
import com.component.busilib.constans.GameModeType;
import com.module.playways.BaseRoomData;
import com.module.playways.room.msg.event.CommentMsgEvent;

/**
 * 普通文本消息
 */
public class CommentTextModel extends CommentModel {

    public CommentTextModel() {
        setCommentType(TYPE_TEXT);
    }

    // 处理真的消息，即聊天消息
    public static CommentTextModel parseFromEvent(CommentMsgEvent event, BaseRoomData roomData) {
        CommentTextModel commentModel = new CommentTextModel();
        commentModel.setUserId(event.info.getSender().getUserID());

        if (roomData != null) {
            UserInfoModel sender = roomData.getUserInfo(event.info.getSender().getUserID());
            commentModel.setUserName(sender.getNicknameRemark());
            commentModel.setAvatarColor(CommentModel.AVATAR_COLOR);
            if (sender != null) {
                commentModel.setAvatar(sender.getAvatar());
                commentModel.setUserName(sender.getNicknameRemark());
            } else {
                commentModel.setAvatar(event.info.getSender().getAvatar());
                commentModel.setUserName(event.info.getSender().getNickName());
            }
        }

        if (roomData != null && roomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
            if (event.mUserInfoModelList == null || event.mUserInfoModelList.size() == 0) {
                SpannableStringBuilder ssb = new SpanUtils()
                        .append(commentModel.getUserName() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                        .append(event.text).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                        .create();
                commentModel.setStringBuilder(ssb);
            } else {
                SpannableStringBuilder ssb = new SpanUtils()
                        .append(commentModel.getUserName() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                        .append("@ ").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                        .append(event.mUserInfoModelList.get(0).getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                        .append(event.text).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                        .create();
                commentModel.setStringBuilder(ssb);
            }
        } else {
            SpannableStringBuilder ssb = new SpanUtils()
                    .append(commentModel.getUserName() + " ").setForegroundColor(CommentModel.RANK_NAME_COLOR)
                    .append(event.text).setForegroundColor(CommentModel.RANK_TEXT_COLOR)
                    .create();
            commentModel.setStringBuilder(ssb);
        }
        return commentModel;
    }
}
