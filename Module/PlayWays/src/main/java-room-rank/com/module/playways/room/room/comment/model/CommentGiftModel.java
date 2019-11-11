package com.module.playways.room.room.comment.model;
import android.text.SpannableStringBuilder;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.utils.SpanUtils;
import com.module.playways.room.gift.model.GPrensentGiftMsgModel;
import com.module.playways.room.room.gift.model.GiftPlayModel;

public class CommentGiftModel extends CommentModel {

    public CommentGiftModel(GiftPlayModel giftPlayModel) {
        setCommentType(CommentModel.Companion.getTYPE_GIFT());
        setUserInfo(giftPlayModel.getSender());
        setAvatarColor(CommentModel.Companion.getAVATAR_COLOR());

        SpannableStringBuilder nameBuilder = new SpanUtils()
                .append(giftPlayModel.getSender().getNicknameRemark() + " ").setForegroundColor(CommentModel.Companion.getGRAB_NAME_COLOR())
                .create();
        setNameBuilder(nameBuilder);

        if (giftPlayModel.getReceiver().getUserId() == MyUserInfoManager.INSTANCE.getUid()) {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append("对 你 送出了").setForegroundColor(CommentModel.Companion.getGRAB_TEXT_COLOR())
                    .append(giftPlayModel.getGift().getGiftName()).setForegroundColor(CommentModel.Companion.getGRAB_TEXT_COLOR())
                    .create();
            setStringBuilder(stringBuilder);
        } else {
            SpannableStringBuilder stringBuilder = new SpanUtils()
                    .append("对").setForegroundColor(CommentModel.Companion.getGRAB_TEXT_COLOR())
                    .append(" " + giftPlayModel.getReceiver().getNicknameRemark() + " ").setForegroundColor(CommentModel.Companion.getGRAB_NAME_COLOR())
                    .append("送出了").setForegroundColor(CommentModel.Companion.getGRAB_TEXT_COLOR())
                    .append(giftPlayModel.getGift().getGiftName()).setForegroundColor(CommentModel.Companion.getGRAB_TEXT_COLOR())
                    .create();
            setStringBuilder(stringBuilder);
        }
    }
}
