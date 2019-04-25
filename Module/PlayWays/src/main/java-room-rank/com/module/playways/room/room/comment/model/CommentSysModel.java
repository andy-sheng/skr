package com.module.playways.room.room.comment.model;

import android.graphics.Color;
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
        setUserId(UserAccountManager.SYSTEM_ID);
        setAvatar(UserAccountManager.SYSTEM_AVATAR);
        setUserName("系统消息");
        setAvatarColor(Color.WHITE);

        if (gameType == GameModeType.GAME_MODE_GRAB) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(text).setForegroundColor(Color.parseColor("#EA596B"))
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(text).setForegroundColor(CommentModel.TEXT_RED)
                    .create();
            setStringBuilder(stringBuilder);
        }
    }

    public static final int TYPE_ENTER_ROOM = 1;
    public static final int TYPE_MODIF_ROOM_NAME = 2;

    // 进入房间消息 一唱到底
    public CommentSysModel(String roomName, int type) {
        setCommentType(CommentModel.TYPE_SYSTEM);
        setUserId(UserAccountManager.SYSTEM_ID);
        setAvatar(UserAccountManager.SYSTEM_AVATAR);
        setUserName("系统消息");
        setAvatarColor(Color.WHITE);

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        if (type == TYPE_ENTER_ROOM) {
            stringBuilder = new SpanUtils()
                    .append("欢迎加入 ").setForegroundColor(Color.parseColor("#EA596B"))
                    .append(roomName).setForegroundColor(Color.parseColor("#DF7900"))
                    .append("房间 撕歌唱到文明游戏，遇到恶意玩家们可以发起投票将ta踢出房间哦～").setForegroundColor(Color.parseColor("#EA596B"))
                    .create();
        } else if (type == TYPE_MODIF_ROOM_NAME) {
            stringBuilder = new SpanUtils()
                    .append("房主已将房间名称修改为 ").setForegroundColor(Color.parseColor("#EA596B"))
                    .append(roomName).setForegroundColor(Color.parseColor("#DF7900"))
                    .create();
        }

        setStringBuilder(stringBuilder);
    }

    // 离开系统消息
    public CommentSysModel(String nickName, String leaveText) {
        setCommentType(CommentModel.TYPE_SYSTEM);
        setUserId(UserAccountManager.SYSTEM_ID);
        setAvatar(UserAccountManager.SYSTEM_AVATAR);
        setUserName("系统消息");
        setAvatarColor(Color.WHITE);

        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append(nickName + " ").setForegroundColor(CommentModel.TEXT_GRAY)
                .append(leaveText).setForegroundColor(CommentModel.TEXT_GRAY)
                .create();
        setStringBuilder(stringBuilder);
    }
}
