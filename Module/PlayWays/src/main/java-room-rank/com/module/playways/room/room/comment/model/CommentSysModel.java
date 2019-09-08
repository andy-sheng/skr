package com.module.playways.room.room.comment.model;

import android.text.SpannableStringBuilder;

import com.common.core.account.UserAccountManager;
import com.common.utils.SpanUtils;
import com.component.busilib.constans.GameModeType;

/**
 * 系统消息
 */
public class CommentSysModel extends CommentModel {

    // 普通系统消息
    public CommentSysModel(int gameType, String text) {
        setCommentType(CommentModel.TYPE_SYSTEM);
        setUserInfo(UserAccountManager.getInstance().getSystemModel());
        setAvatarColor(CommentModel.AVATAR_COLOR);

        if (gameType == GameModeType.GAME_MODE_GRAB) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(text).setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(text).setForegroundColor(CommentModel.RANK_SYSTEM_COLOR)
                    .create();
            setStringBuilder(stringBuilder);
        }
    }

    public static final int TYPE_ENTER_ROOM = 1;
    public static final int TYPE_MODIF_ROOM_NAME = 2;

    // 进入房间消息 一唱到底
    public CommentSysModel(String roomName, int type) {
        setCommentType(CommentModel.TYPE_SYSTEM);
        setAvatarColor(CommentModel.AVATAR_COLOR);
        setUserInfo(UserAccountManager.getInstance().getSystemModel());
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (type == TYPE_ENTER_ROOM) {
            stringBuilder = new SpanUtils()
                    .append("欢迎加入 ").setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .append(roomName + "").setForegroundColor(CommentModel.GRAB_SYSTEM_HIGH_COLOR)
                    .append("房间 撕歌倡导文明游戏，遇到恶意玩家们可以发起投票将ta踢出房间哦～").setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .create();
        } else if (type == TYPE_MODIF_ROOM_NAME) {
            stringBuilder = new SpanUtils()
                    .append("房主已将房间名称修改为 ").setForegroundColor(CommentModel.GRAB_SYSTEM_COLOR)
                    .append(roomName + "").setForegroundColor(CommentModel.GRAB_SYSTEM_HIGH_COLOR)
                    .create();
        }

        setStringBuilder(stringBuilder);
    }

    // 离开系统消息
    public CommentSysModel(String nickName, String leaveText) {
        setCommentType(CommentModel.TYPE_SYSTEM);
        setUserInfo(UserAccountManager.getInstance().getSystemModel());
        setAvatarColor(CommentModel.AVATAR_COLOR);

        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append(nickName + " ").setForegroundColor(CommentModel.RANK_NAME_COLOR)
                .append(leaveText).setForegroundColor(CommentModel.RANK_SYSTEM_COLOR)
                .create();
        setStringBuilder(stringBuilder);
    }
}
