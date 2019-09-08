package com.module.playways.room.room.comment.model;

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

    public CommentLightModel(int gameType, PlayerInfoModel voter, PlayerInfoModel singer, boolean isBao, boolean isChorus, boolean isMiniGame) {
        setCommentType(CommentModel.TYPE_LIGHT);
        this.mGameType = gameType;
        this.voter = voter;
        this.singer = singer;

        setUserInfo(voter.getUserInfo());
        setAvatarColor(CommentModel.AVATAR_COLOR);
        SpannableStringBuilder stringBuilder = null;
        if (mGameType == GameModeType.GAME_MODE_GRAB) {
            SpanUtils spanUtils = new SpanUtils()
                    .append(voter.getUserInfo().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR);
            if (isMiniGame) {
                stringBuilder = spanUtils.append(isBao ? "对表演爆灯啦" : "对表演灭灯啦").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                        .create();
            } else {
                if (isChorus) {
                    spanUtils.append("为").setForegroundColor(CommentModel.GRAB_TEXT_COLOR);
                    spanUtils.append("合唱").setForegroundColor(CommentModel.GRAB_TEXT_COLOR);
                } else {
                    spanUtils.append("对").setForegroundColor(CommentModel.GRAB_TEXT_COLOR);
                    spanUtils.append(singer.getUserInfo().getNicknameRemark()).setForegroundColor(CommentModel.GRAB_NAME_COLOR);
                }
                stringBuilder = spanUtils.append(isBao ? "爆灯啦" : "灭了盏灯").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                        .create();
            }
        } else if (mGameType == GameModeType.GAME_MODE_CLASSIC_RANK) {
            stringBuilder = new SpanUtils()
                    .append(voter.getUserInfo().getNicknameRemark() + " ").setForegroundColor(CommentModel.RANK_NAME_COLOR)
                    .append("对").setForegroundColor(CommentModel.RANK_TEXT_COLOR)
                    .append(singer.getUserInfo().getNicknameRemark()).setForegroundColor(CommentModel.RANK_NAME_COLOR)
                    .append(isBao ? "爆了个灯" : "按了“x”").setForegroundColor(CommentModel.RANK_TEXT_COLOR)
                    .create();
        }

        setStringBuilder(stringBuilder);
    }
}
