package com.wali.live.watchsdk.sixin.recycler;

import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.base.view.MLTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.user.User;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.dao.SixinMessage;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.lit.recycler.holder.BaseHolder;
import com.wali.live.watchsdk.sixin.constant.SixinConstants;
import com.wali.live.watchsdk.sixin.message.SixinMessageModel;
import com.wali.live.watchsdk.sixin.recycler.adapter.SixinMessageAdapter;

/**
 * Created by lan on 16-5-20.
 */
public class SixinMessageHolder extends BaseHolder<SixinMessageModel> {
    private MLTextView mTimestampTv;

    private SimpleDraweeView mAvatarDv;
    private ImageView mCertificationIv;

    private ViewGroup mBubbleArea;

    protected SixinMessageAdapter mAdapter;

    public SixinMessageHolder(View itemView, SixinMessageAdapter adapter) {
        super(itemView);
        mAdapter = adapter;
    }

    @Override
    protected void initView() {
        mTimestampTv = $(R.id.timestamp_tv);

        mAvatarDv = $(R.id.avatar_dv);
        mCertificationIv = $(R.id.certification_iv);

        mBubbleArea = $(R.id.bubble_area);
    }

    @Override
    protected void bindView() {
        setTimestamp();

        setAvatar();
        setCertification();

        setBubbleOnType();
    }

    protected boolean setTimestamp() {
        if (mPosition - 1 >= 0) {
            SixinMessageModel lastModel = mAdapter.getItem(mPosition - 1);
            if (lastModel != null) {
                if (Math.abs(mViewModel.getReceiveTime() - lastModel.getReceiveTime()) < SixinConstants.MESSAGE_TIMESTAMP_INTERVAL) {
                    mTimestampTv.setVisibility(View.GONE);
                    mTimestampTv.setText("");
                    return false;
                }
            }
        }

        mTimestampTv.setVisibility(View.VISIBLE);
        mTimestampTv.setText(mViewModel.getFormatSentTime());
        return true;
    }

    protected void setAvatar() {
        if (mViewModel.isInbound()) { // 收到的消息
            User sender = mViewModel.getSender();
            if (sender != null) {
                AvatarUtils.loadAvatarByUidTsCorner(mAvatarDv, sender.getUid(), sender.getAvatar(), 14, 0, 0);
            }
        } else { // 自己的消息
            AvatarUtils.loadAvatarByUidTsCorner(mAvatarDv, MyUserInfoManager.getInstance().getUuid(), MyUserInfoManager.getInstance().getAvatar(), 14, 0, 0);
        }
    }

    protected void setCertification() {
        Drawable result;
        if (mViewModel.isInbound()) { // 收到的消息
            result = ItemDataFormatUtils.getCertificationImgSource(mViewModel.getCertificationType());
        } else { // 自己的消息
            result = ItemDataFormatUtils.getCertificationImgSource(MyUserInfoManager.getInstance().getUser().getCertificationType());
        }
        if (result == null) {
            mCertificationIv.setVisibility(View.GONE);
        } else {
            mCertificationIv.setVisibility(View.VISIBLE);
        }
    }

    protected void setBubbleOnType() {
        if (mViewModel.getMsgType() == SixinMessage.S_MSG_TYPE_TEXT) {
            bindTextMessage(mViewModel.getBody());
        } else {
            bindTextMessage(itemView.getResources().getString(R.string.message_not_supported));
        }
    }

    protected void bindTextMessage(String body) {
        if (mBubbleArea.getTag() != null && mBubbleArea.getTag().equals(String.valueOf(mViewModel.getMsgId()))) {
            return;
        } else {
            mBubbleArea.setTag(String.valueOf(mViewModel.getMsgId()));
        }
        ViewGroup bubbleArea = addBubbleView(R.layout.message_bubble_text, mBubbleArea);
        TextView bodyTextView = $(mBubbleArea, R.id.text_view);

        if (!TextUtils.isEmpty(body)) {
            CharSequence charSequence = SmileyParser.getInstance().addSmileySpans(GlobalData.app(),
                    body, DisplayUtils.dip2px(16.0f), true, false, true);
            SpannableStringBuilder ssb = (SpannableStringBuilder) charSequence;
            bodyTextView.setText(ssb);
        } else {
            bodyTextView.setText("");
        }

        bodyTextView.setLinkTextColor(GlobalData.app().getResources().getColor(R.color.blue));
        bodyTextView.setMovementMethod(LinkMovementMethod.getInstance());

        if (mViewModel.isInbound()) {
            bodyTextView.setTextColor(bubbleArea.getResources().getColor(R.color.color_black_trans_90));
            bodyTextView.setPadding(58, bodyTextView.getPaddingTop(), 40, bodyTextView.getPaddingBottom());
        } else {
            bodyTextView.setTextColor(bubbleArea.getResources().getColor(R.color.color_black_trans_90));
            bodyTextView.setPadding(40, bodyTextView.getPaddingTop(), 58, bodyTextView.getPaddingBottom());
        }

        setPaddingByInbound(mViewModel.isInbound(), R.id.text_view, bubbleArea);
    }

    protected ViewGroup addBubbleView(int layoutId, ViewGroup bubbleArea) {
        View bubbleView = null;
        if (bubbleArea != null) {
            int count = bubbleArea.getChildCount();

            for (int i = 0; i < count; i++) {
                if (bubbleArea.getChildAt(i).getTag().equals(mViewModel.getMsgId())) {
                    bubbleView = bubbleArea.getChildAt(i);
                    continue;
                } else {
                    bubbleArea.removeView(bubbleArea.getChildAt(i));
                }
            }

            if (bubbleView == null) {
                bubbleView = LayoutInflater.from(itemView.getContext()).inflate(layoutId, null, false);
                bubbleView.setTag(mViewModel.getMsgId());
                bubbleArea.addView(bubbleView);
            }

            bubbleView.setVisibility(View.VISIBLE);
        }
        return bubbleArea;
    }

    protected void setPaddingByInbound(final boolean isInbound, final int backgroudResId, final ViewGroup viewGroup) {
        final View backgroundContainer = viewGroup.findViewById(backgroudResId);
        final int bottom = backgroundContainer.getPaddingBottom();
        final int top = backgroundContainer.getPaddingTop();
        final int left = backgroundContainer.getPaddingLeft();
        final int right = backgroundContainer.getPaddingRight();
        if (isInbound) {
            backgroundContainer.setBackgroundResource(R.drawable.sms_msg_receive_bg);
            backgroundContainer.setPadding(Math.max(left, right), top, Math.min(left, right), bottom);
        } else {
            backgroundContainer.setBackgroundResource(R.drawable.sms_msg_sent_bg);
            backgroundContainer.setPadding(Math.min(left, right), top, Math.max(left, right), bottom);
        }
    }
}