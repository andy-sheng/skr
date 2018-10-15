package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wali.live.watchsdk.R;

/**
 * Created by liuting on 18-9-7.
 */

public class TwoSelectTabLayout extends LinearLayout implements View.OnClickListener {
    private int mTabSelectedColor;
    private String mLeftText;
    private String mRightText;
    private float mCornorRadius;

    private TextView mLeftTextView, mRightTextView;

    private OnTabSelectListener mOnTabSelectListener;

    public TwoSelectTabLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TwoSelectTabLayout);
        mTabSelectedColor = ta.getColor(R.styleable.TwoSelectTabLayout_tabColor, getResources().getColor(R.color.color_14b9c7));
        if(ta.hasValue(R.styleable.TwoSelectTabLayout_leftText)) {
            mLeftText = ta.getString(R.styleable.TwoSelectTabLayout_leftText);
        } else {
            mLeftText = "";
        }
        if(ta.hasValue(R.styleable.TwoSelectTabLayout_rightText)) {
            mRightText = ta.getString(R.styleable.TwoSelectTabLayout_rightText);
        } else {
            mRightText = "";
        }
        mCornorRadius = ta.getDimension(R.styleable.TwoSelectTabLayout_cornorRadius, 0);
        ta.recycle();

        mLeftTextView = addTab(mLeftText, R.id.two_select_tab_left);
        mLeftTextView.setSelected(true);

        mRightTextView = addTab(mRightText, R.id.two_select_tab_right);
    }

    public TextView addTab(String text,int id) {
        TextView textView = new TextView(getContext());
        textView.setId(id);
        textView.setText(text == null ? "" : text);
        textView.setTextSize(13.33f);

        LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        textView.setLayoutParams(layoutParams);
        textView.setGravity(Gravity.CENTER);

        int[][] state = {{android.R.attr.state_selected}, {-android.R.attr.state_selected}};
        int[] color = {getResources().getColor(R.color.white), mTabSelectedColor};
        ColorStateList textCorlorState = new ColorStateList(state, color);
        textView.setTextColor(textCorlorState);

        StateListDrawable stateListDrawable = new StateListDrawable();

        GradientDrawable select = new GradientDrawable();
        select.setShape(GradientDrawable.RECTANGLE);
        if (id == R.id.two_select_tab_left) {
            select.setCornerRadii(new float[]{mCornorRadius, mCornorRadius, 0, 0, 0, 0, mCornorRadius, mCornorRadius});
        } else {
            select.setCornerRadii(new float[]{ 0, 0, mCornorRadius, mCornorRadius, mCornorRadius, mCornorRadius, 0, 0});
        }

        select.setStroke((int) getResources().getDimension(R.dimen.view_dimen_2), mTabSelectedColor);
        select.setColor(mTabSelectedColor);
        stateListDrawable.addState(new int[]{android.R.attr.state_selected}, select);

        GradientDrawable unSelect = new GradientDrawable();
        unSelect.setShape(GradientDrawable.RECTANGLE);
        if (id == R.id.two_select_tab_left) {
            unSelect.setCornerRadii(new float[]{mCornorRadius, mCornorRadius, 0, 0, 0, 0, mCornorRadius, mCornorRadius});
        } else {
            unSelect.setCornerRadii(new float[]{ 0, 0, mCornorRadius, mCornorRadius, mCornorRadius, mCornorRadius, 0, 0});
        }
        unSelect.setStroke((int) getResources().getDimension(R.dimen.view_dimen_2), mTabSelectedColor);
        unSelect.setColor(getResources().getColor(R.color.transparent));
        stateListDrawable.addState(new int[]{-android.R.attr.state_selected}, unSelect);

        textView.setBackground(stateListDrawable);

        textView.setOnClickListener(this);

        addView(textView);
        return textView;
    }

    @Override
    public void onClick(View v) {
        if (v.isSelected()) {
            return;
        }
        if (v.getId() == R.id.two_select_tab_left) {
            setTabSelectedWithCallBack(0);
        } else {
            setTabSelectedWithCallBack(1);
        }
    }

    public void setTabSelectedWithoutCallBack(int position) {
        if (position == 0 && !mLeftTextView.isSelected()) {
            mLeftTextView.setSelected(true);
            mRightTextView.setSelected(false);
        } else if (position == 1 && !mRightTextView.isSelected()) {
            mRightTextView.setSelected(true);
            mLeftTextView.setSelected(false);
        }
    }

    public void setTabSelectedWithCallBack(int position) {
        if (position == 0 && !mLeftTextView.isSelected()) {
            if (mOnTabSelectListener != null && mOnTabSelectListener.onLeftTabSelect()) {
                mLeftTextView.setSelected(true);
                mRightTextView.setSelected(false);
            }
        } else if (position == 1 && !mRightTextView.isSelected()) {
            if (mOnTabSelectListener != null && mOnTabSelectListener.onRightTabSelect()) {
                mRightTextView.setSelected(true);
                mLeftTextView.setSelected(false);
            }
        }
    }

    public void setOnTabSelectListener(OnTabSelectListener listener) {
        mOnTabSelectListener = listener;
    }

    public interface OnTabSelectListener {
        boolean onLeftTabSelect();
        boolean onRightTabSelect();
    }
}
