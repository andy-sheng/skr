package com.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.common.R;
import com.base.global.GlobalData;


/**
 * Created by lan on 15-11-13.
 * 带有返回按钮的titlebar
 */
public class LightBackTitleBar extends RelativeLayout {
    private RelativeLayout mRootView;

    private TextView mBackBtn;
    //    private TextView mTitleTv;
    private ImageView mRightImageBtn;
    private TextView mRightTextBtn;

    private View mBottomLine;

    private TextView mCenterTitleTv;

    /*是否设置名片页沉浸模式*/
    private boolean mIsProfileMode;

    public LightBackTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.light_back_title_bar, this);

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
        if (BaseActivity.isProfileMode() && mIsProfileMode) {
            titleBarHeight += BaseActivity.getStatusBarHeight();
        }
        setMeasuredDimension(width, titleBarHeight);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRootView = (RelativeLayout) findViewById(R.id.root_view);
        if (mIsProfileMode && BaseActivity.isProfileMode()) {
            LayoutParams lp = (LayoutParams) mRootView.getLayoutParams();
            lp.topMargin = BaseActivity.getStatusBarHeight();
        }

        mBackBtn = (TextView) findViewById(R.id.back_iv);
//        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mRightImageBtn = (ImageView) findViewById(R.id.right_image_btn);
        mRightTextBtn = (TextView) findViewById(R.id.right_text_btn);

        mBottomLine = findViewById(R.id.bottom_line);
        mCenterTitleTv = (TextView) findViewById(R.id.center_title_tv);
    }

    /**
     * 显示城市信息
     */
    public void setCity(String city) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.title_bar_location, null);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(RIGHT_OF, mBackBtn.getId());
        lp.addRule(CENTER_VERTICAL, TRUE);
        mRootView.addView(view, lp);

        TextView locationTv = (TextView) view.findViewById(R.id.location_tv);
        locationTv.setText(city);
    }

    public void setBackImg(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mBackBtn.setCompoundDrawables(drawable, null, null, null);
    }

    public TextView getTitleTv() {
        return mBackBtn;
    }

    public void hideBottomLine() {
        mBottomLine.setVisibility(View.GONE);
    }

    public void setBottomLineColor(int color) {
        mBottomLine.setBackground(GlobalData.app().getResources().getDrawable(color));
    }

    public void showBottomLine() {
        mBottomLine.setVisibility(View.VISIBLE);
    }

    public void setTitle(int resId) {
        mBackBtn.setText(resId);
    }

    public void setTitle(String s) {
        mBackBtn.setText(s);
    }

    public void setTitleColor(int color) {
        mBackBtn.setTextColor(color);
    }

    public TextView getBackBtn() {
        return mBackBtn;
    }

    public void setCenterTitleText(int resId) {
        mCenterTitleTv.setText(resId);
    }

    public void setCenterTitle(String s) {
        mCenterTitleTv.setText(s);
    }

    public void hideCenterTitle() {
        mCenterTitleTv.setVisibility(GONE);
    }

    public void showCenterTitle() {
        mCenterTitleTv.setVisibility(VISIBLE);
    }

    public ImageView getRightImageBtn() {
        mRightImageBtn.setVisibility(View.VISIBLE);
        return mRightImageBtn;
    }

    public TextView getRightTextBtn() {
        mRightTextBtn.setVisibility(View.VISIBLE);
        return mRightTextBtn;
    }

    public boolean IsProfileMode() {
        return mIsProfileMode;
    }
}

