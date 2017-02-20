package com.base.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.common.R;


/**
 * Created by lan on 15-11-13.
 * 带有返回按钮的titlebar
 */
public class BackTitleBar extends RelativeLayout {
    private RelativeLayout mRootView;

    private TextView mBackBtn;
    //    private TextView mTitleTv;
    private ImageView mRightImageBtn;
    private TextView mRightTextBtn;

    private View mBottomLine;

    private TextView mCenterTitleTv;

    /*是否设置名片页沉浸模式*/
    private boolean mIsProfileMode;

    public BackTitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.back_title_bar, this);

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
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mRootView.getLayoutParams();
            lp.topMargin = BaseActivity.getStatusBarHeight();
        }

        mBackBtn = (TextView) findViewById(R.id.back_iv);
//        mTitleTv = (TextView) findViewById(R.id.title_tv);
        mRightImageBtn = (ImageView) findViewById(R.id.right_image_btn);
        mRightTextBtn = (TextView) findViewById(R.id.right_text_btn);

        mBottomLine = findViewById(R.id.bottom_line);
        mCenterTitleTv = (TextView)findViewById(R.id.center_title_tv);
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

    public TextView getTitleTv() {
        return mBackBtn;
    }

    public void hideBottomLine() {
        mBottomLine.setVisibility(View.GONE);
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

    public TextView getBackBtn() {
        return mBackBtn;
    }

    public void setCenterTitleText(int res){
        mCenterTitleTv.setText(res);
    }

    public void hideCenterTitle(){
        mCenterTitleTv.setVisibility(GONE);
    }

    public void showCenterTitle(){
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
}

