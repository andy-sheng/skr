package com.module.playways.room.room.comment.model;
import android.text.SpannableStringBuilder;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.SpanUtils;
import com.module.playways.room.gift.model.GPrensentGiftMsgModel;
import com.module.playways.room.room.gift.model.GiftPlayModel;

public class CommentGiftModel extends CommentModel {

    public CommentGiftModel(GPrensentGiftMsgModel giftMsgModel) {
        setCommentType(CommentModel.TYPE_GIFT);
        setUserInfo(giftMsgModel.getSendUserInfo());
        setAvatarColor(CommentModel.AVATAR_COLOR);

        if (giftMsgModel.getReceiveUserInfo().getUserId() == MyUserInfoManager.getInstance().getUid()) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftMsgModel.getSendUserInfo().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("对 你 送出了").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .append(giftMsgModel.getGiftInfo().getGiftName()).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftMsgModel.getSendUserInfo().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("对").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .append(" " + giftMsgModel.getReceiveUserInfo().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("送出了").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .append(giftMsgModel.getGiftInfo().getGiftName()).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create();
            setStringBuilder(stringBuilder);
        }
    }

    public CommentGiftModel(GiftPlayModel giftPlayModel) {
        setCommentType(CommentModel.TYPE_GIFT);
        setUserInfo(giftPlayModel.getSender());
        setAvatarColor(CommentModel.AVATAR_COLOR);

        if (giftPlayModel.getReceiver().getUserId() == MyUserInfoManager.getInstance().getUid()) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftPlayModel.getSender().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("对 你 送出了").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .append(giftPlayModel.getGift().getGiftName()).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftPlayModel.getSender().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("对").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .append(" " + giftPlayModel.getReceiver().getNicknameRemark() + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("送出了").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .append(giftPlayModel.getGift().getGiftName()).setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create();
            setStringBuilder(stringBuilder);
        }
    }
}
