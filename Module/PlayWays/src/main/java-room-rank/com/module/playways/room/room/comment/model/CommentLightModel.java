package com.module.playways.room.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.common.utils.SpanUtils;
import com.component.busilib.constans.GameModeType;
import com.module.playways.room.prepare.model.PlayerInfoModel;

/**
 * 爆灭灯消息
 */
public class CommentLightModel extends CommentModel {
    int mGameType;
    PlayerInfoModel voter;   // 投票者
    PlayerInfoModel singer;  // 演唱者

    public CommentLightModel(int gameType, PlayerInfoModel voter, PlayerInfoModel singer, boolean isBao, boolean isChorus) {
        setCommentType(CommentModel.TYPE_LIGHT);
        this.mGameType = gameType;
        this.voter = voter;
        this.singer = singer;

        setUserId(voter.getUserID());
        setAvatar(voter.getUserInfo().getAvatar());
        setUserName(voter.getUserInfo().getNickname());
        setAvatarColor(Color.WHITE);
        SpannableStringBuilder stringBuilder = null;
        if (mGameType == GameModeType.GAME_MODE_GRAB) {
            SpanUtils spanUtils = new SpanUtils()
                    .append(voter.getUserInfo().getNickname() + " ").setForegroundColor(CommentModel.TEXT_YELLOW);
            if (isChorus) {
                spanUtils.append("为").setForegroundColor(CommentModel.TEXT_3B4E79);
                spanUtils.append("合唱").setForegroundColor(CommentModel.TEXT_YELLOW);
            } else {
                spanUtils.append("对").setForegroundColor(CommentModel.TEXT_3B4E79);
                spanUtils.append(singer.getUserInfo().getNickname()).setForegroundColor(CommentModel.TEXT_YELLOW);
            }
            stringBuilder = spanUtils.append(isBao ? "爆灯啦" : "灭了盏灯").setForegroundColor(CommentModel.TEXT_3B4E79)
                    .create();
        } else if (mGameType == GameModeType.GAME_MODE_CLASSIC_RANK) {
            stringBuilder = new SpanUtils()
                    .append(voter.getUserInfo().getNickname() + " ").setForegroundColor(CommentModel.TEXT_YELLOW)
                    .append("对").setForegroundColor(CommentModel.TEXT_WHITE)
                    .append(singer.getUserInfo().getNickname()).setForegroundColor(CommentModel.TEXT_YELLOW)
                    .append(isBao ? "爆了个灯" : "按了“x”").setForegroundColor(CommentModel.TEXT_WHITE)
                    .create();
        }

        setStringBuilder(stringBuilder);
    }
}
