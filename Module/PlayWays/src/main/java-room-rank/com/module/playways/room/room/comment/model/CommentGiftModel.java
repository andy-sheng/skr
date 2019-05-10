package com.module.playways.room.room.comment.model;

import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.SpanUtils;
import com.module.playways.room.gift.model.GPrensentGiftMsgModel;
import com.module.playways.room.room.gift.model.GiftPlayModel;

public class CommentGiftModel extends CommentModel {

    public CommentGiftModel(GPrensentGiftMsgModel giftMsgModel) {
        setCommentType(CommentModel.TYPE_GIFT);
        setUserId(giftMsgModel.getSendUserInfo().getUserId());
        setUserName(giftMsgModel.getSendUserInfo().getNickname());
        setAvatar(giftMsgModel.getSendUserInfo().getAvatar());
        setAvatarColor(Color.WHITE);

        if (giftMsgModel.getReceiveUserInfo().getUserId() == MyUserInfoManager.getInstance().getUid()) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftMsgModel.getSendUserInfo().getNickname() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("对 你 送出了").setForegroundColor(Color.WHITE)
                    .append(giftMsgModel.getGiftInfo().getGiftName()).setForegroundColor(TEXT_RED)
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftMsgModel.getSendUserInfo().getNickname() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("对").setForegroundColor(Color.WHITE)
                    .append(" " + giftMsgModel.getReceiveUserInfo().getNickname() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("送出了").setForegroundColor(Color.WHITE)
                    .append(giftMsgModel.getGiftInfo().getGiftName()).setForegroundColor(TEXT_RED)
                    .create();
            setStringBuilder(stringBuilder);
        }
    }

    public CommentGiftModel(GiftPlayModel giftPlayModel) {
        setCommentType(CommentModel.TYPE_GIFT);
        setUserId(giftPlayModel.getSender().getUserId());
        setUserName(giftPlayModel.getSender().getNickname());
        setAvatar(giftPlayModel.getSender().getAvatar());
        setAvatarColor(Color.WHITE);

        if (giftPlayModel.getReceiver().getUserId() == MyUserInfoManager.getInstance().getUid()) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftPlayModel.getSender().getNickname() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("对 你 送出了").setForegroundColor(Color.WHITE)
                    .append(giftPlayModel.getGift().getGiftName()).setForegroundColor(TEXT_RED)
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append(giftPlayModel.getSender().getNickname() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("对").setForegroundColor(Color.WHITE)
                    .append(" " + giftPlayModel.getReceiver().getNickname() + " ").setForegroundColor(Color.parseColor("#DF7900"))
                    .append("送出了").setForegroundColor(Color.WHITE)
                    .append(giftPlayModel.getGift().getGiftName()).setForegroundColor(TEXT_RED)
                    .create();
            setStringBuilder(stringBuilder);
        }
    }
}
