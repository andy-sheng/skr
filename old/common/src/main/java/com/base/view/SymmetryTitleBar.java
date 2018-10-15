package com.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.common.R;

/**
 * Created by lan on 15-11-4.
 * 带有对称按钮的titlebar
 */
public class SymmetryTitleBar extends RelativeLayout {
    private RelativeLayout mRootView;

    private ImageView mLeftImageBtn;
    private TextView mLeftTextBtn;
    private TextView mTitleTv;
    private ImageView mRightImageBtn;
    private TextView mRightTextBtn;

    private View mBottomLine;

    /*是否设置名片页沉浸模式*/
    private boolean mIsProfileMode;


    public SymmetryTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.symmetry_title_bar, this);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitleBar);
        mIsProfileMode = a.getBoolean(R.styleable.TitleBar_is_profile_mode, true);
        a.recycle();
        setBackgroundColor(context.getResources().getColor(R.color.color_title_bar));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);

        int titleBarHeight = getContext().getResources().getDimensionPixelSize(R.dimen.title_bar_height);
        if (BaseSdkActivity.isProfileMode() && mIsProfileMode) {
            titleBarHeight += BaseSdkActivity.getStatusBarHeight();
        }
        setMeasuredDimension(width, titleBarHeight);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRootView = (RelativeLayout) findViewById(R.id.root_view);

        mLeftImageBtn = (ImageView) findViewById(R.id.left_image_btn);
        mLeftTextBtn = (TextView) findViewById(R.id.left_text_btn);
        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mRightImageBtn = (ImageView) findViewById(R.id.right_image_btn);
        mRightTextBtn = (TextView) findViewById(R.id.right_text_btn);

        if (mIsProfileMode && BaseSdkActivity.isProfileMode()) {
            LayoutParams lp = (LayoutParams) mRootView.getLayoutParams();
            lp.topMargin = BaseSdkActivity.getStatusBarHeight();
        }

        mBottomLine = findViewById(R.id.bottom_line);
    }

    public void setProfileMode(boolean isProfileMode) {
        if (mIsProfileMode != isProfileMode) {
            mIsProfileMode = isProfileMode;
            if (mIsProfileMode && BaseSdkActivity.isProfileMode()) {
                LayoutParams lp = (LayoutParams) mRootView.getLayoutParams();
                lp.topMargin = BaseSdkActivity.getStatusBarHeight();
            }
        }
    }

    public TextView getTitleTv() {
        return mTitleTv;
    }

    public void hideBottomLine() {
        mBottomLine.setVisibility(View.GONE);
    }

    public void showBottomLine() {
        mBottomLine.setVisibility(View.VISIBLE);
    }


    public void setTitle(int resId) {
        mTitleTv.setText(resId);
    }

    public void setTitle(String s) {
        mTitleTv.setText(s);
    }

    public ImageView getLeftImageBtn() {
        mLeftImageBtn.setVisibility(View.VISIBLE);
        return mLeftImageBtn;
    }

    public TextView getLeftTextBtn() {
        mLeftTextBtn.setVisibility(View.VISIBLE);
        return mLeftTextBtn;
    }

    public ImageView getRightImageBtn() {
        mRightImageBtn.setVisibility(View.VISIBLE);
        return mRightImageBtn;
    }

    public TextView getRightTextBtn() {
        mRightTextBtn.setVisibility(View.VISIBLE);
        return mRightTextBtn;
    }

    public View getBottomLine(){
        mBottomLine.setVisibility(View.VISIBLE);
        return mBottomLine;
    }
}

