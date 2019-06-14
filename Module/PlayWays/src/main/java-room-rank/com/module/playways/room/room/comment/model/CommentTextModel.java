package com.module.playways.room.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

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
        if (!TextUtils.isEmpty(event.info.getSender().getNickName())) {
            commentModel.setUserName(event.info.getSender().getNickName());
        }

        if (roomData != null) {
            UserInfoModel sender = roomData.getUserInfo(event.info.getSender().getUserID());
            commentModel.setUserName(sender.getNicknameRemark());
            commentModel.setAvatarColor(Color.WHITE);
            if (sender != null) {
                commentModel.setAvatar(sender.getAvatar());
            } else {
                commentModel.setAvatar(event.info.getSender().getAvatar());
            }
        }

        if (roomData != null && roomData.getGameType() == GameModeType.GAME_MODE_GRAB) {
            if (event.mUserInfoModelList == null || event.mUserInfoModelList.size() == 0) {
                SpannableStringBuilder ssb = new SpanUtils()
                        .append(commentModel.getUserName() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                        .append(event.text).setForegroundColor(Color.parseColor("#586D94"))
                        .create();
                commentModel.setStringBuilder(ssb);
            } else {
                SpannableStringBuilder ssb = new SpanUtils()
                        .append(commentModel.getUserName() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                        .append("@ ").setForegroundColor(Color.parseColor("#586D94"))
                        .append(event.mUserInfoModelList.get(0).getNicknameRemark() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                        .append(event.text).setForegroundColor(Color.parseColor("#586D94"))
                        .create();
                commentModel.setStringBuilder(ssb);
            }
        } else {
            SpannableStringBuilder ssb = new SpanUtils()
                    .append(commentModel.getUserName() + " ").setForegroundColor(TEXT_YELLOW)
                    .append(event.text).setForegroundColor(TEXT_WHITE)
                    .create();
            commentModel.setStringBuilder(ssb);
        }
        return commentModel;
    }
}
