package com.wali.live.common.flybarrage.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.live.module.common.R;
import com.wali.live.common.flybarrage.model.FlyBarrageInfo;
import com.wali.live.common.gift.utils.DataformatUtils;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.utils.AvatarUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 付费弹幕view
 * Created by chengsimin on 16/3/22.
 */
public class FlyBarrageView extends RelativeLayout {

    BaseImageView mSenderIv;
    TextView mNameTv;
    ImageView mUserbadgeIv;
    ImageView mUserbadgeVipIv;
    TextView mContentTv;

    public FlyBarrageView(Context context) {
        super(context);
        init(context);
    }

    public FlyBarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public FlyBarrageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.fly_barrage_view, this);
        mSenderIv = (BaseImageView) findViewById(R.id.sender_iv);
        mNameTv = (TextView) findViewById(R.id.name_tv);
        mUserbadgeIv = (ImageView) findViewById(R.id.user_badge_iv);
        mUserbadgeVipIv = (ImageView) findViewById(R.id.user_badge_vip_iv);
        mContentTv = (TextView) findViewById(R.id.content_tv);
        ButterKnife.bind(this, this);
    }

    public void setFlyBarrageInfo(FlyBarrageInfo model) {
        mNameTv.setText(model.getName());
        if (model.getCertificationType() > 0) {
            mUserbadgeIv.setVisibility(View.GONE);
            mUserbadgeVipIv.setVisibility(View.VISIBLE);
            mUserbadgeVipIv.setImageDrawable(DataformatUtils.getCertificationImgSource(model.getCertificationType()));
        } else {
            mUserbadgeIv.setVisibility(View.VISIBLE);
            mUserbadgeVipIv.setVisibility(View.GONE);
            mUserbadgeIv.setImageDrawable(DataformatUtils.getLevelSmallImgSource(model.getLevel()));
        }
        long userId = model.getSenderId();
        AvatarUtils.loadAvatarByUidTs(mSenderIv, userId, model.getAvatarTimestamp(), true);
        setContent(model.getContent());
    }

    private void setContent(String content) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        String commentTrim = content == null ? "" : content.replaceAll("\n", " ");
        CharSequence spannableSubject = SmileyParser.getInstance().addSmileySpans(GlobalData.app(),
                commentTrim,
                mContentTv.getTextSize(), true, false, true);
        // 评论加颜色
        SpannableString comment = new SpannableString(spannableSubject);
//        comment.setSpan(new ForegroundColorSpan(mActivity.getResources().getColor(commentColorId)),
//                0, spannableSubject.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssb.append(comment);
        mContentTv.setText(ssb);
    }
}
