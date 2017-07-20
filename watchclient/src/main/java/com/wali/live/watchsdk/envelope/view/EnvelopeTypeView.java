package com.wali.live.watchsdk.envelope.view;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wali.live.watchsdk.R;

/**
 * Created by wangmengjie on 2017/7/18.
 *
 * @module 自定义红包view
 */
public class EnvelopeTypeView extends RelativeLayout {
    private static final String TAG = "EnvelopeTypeView";

    private ImageView mEnvelopeTypeIv;
    private TextView mDiamondNumTv;
    private ImageView mSelectRightIv;

    private int mSelectedDiamondNum;

    protected final <T extends View> T $(@IdRes int resId) {
        return (T) findViewById(resId);
    }

    public EnvelopeTypeView(Context context) {
        this(context, null);
    }

    public EnvelopeTypeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EnvelopeTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.envelope_type_view, this);
        mEnvelopeTypeIv = $(R.id.envelope_type_iv);
        mDiamondNumTv = $(R.id.diamond_num_tv);
        mSelectRightIv = $(R.id.select_right);
    }

    public void setData(EnvelopeType type) {
        mEnvelopeTypeIv.setImageResource(type.mDrawableId);
        SpannableString diameondSpan = new SpannableString(type.mDiamondNum + "\n" +
                getResources().getString(R.string.gift_item_diamond_text));
        int startPos = diameondSpan.toString().indexOf('\n');
        diameondSpan.setSpan(new AbsoluteSizeSpan((int) getResources().getDimensionPixelSize(R.dimen.text_size_32)),
                startPos + 1, diameondSpan.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mDiamondNumTv.setText(diameondSpan);
        mSelectedDiamondNum = type.mDiamondNum;
        mDiamondNumTv.setTextColor(getResources().getColor(type.mColorId));
    }

    public int getmSelectedDiamondNum() {
        return mSelectedDiamondNum;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (!selected) {
            mSelectRightIv.setVisibility(View.GONE);
            setBackground(null);
        } else if (selected) {
            mSelectRightIv.setVisibility(View.VISIBLE);
            setBackgroundResource(R.drawable.red_send_select_bg);
        }
    }

    public static class EnvelopeType {
        public int mDiamondNum;
        @ColorRes
        public int mColorId;
        @DrawableRes
        public int mDrawableId;

        public EnvelopeType(int diamondNum, int colorId, int drawableId) {
            mDiamondNum = diamondNum;
            mColorId = colorId;
            mDrawableId = drawableId;
        }
    }
}
