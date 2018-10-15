package com.wali.live.common.smiley;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ResImage;
import com.facebook.drawee.view.SimpleDraweeView;
import com.base.log.MyLog;
import com.live.module.common.R;

/**
 * @module smileypick
 * <p/>
 * Created by MK on 15/10/20.
 */
public class SmileyItem extends RelativeLayout {

    private Context mContext;

    private View mRootView;
    private TextView mDescView;
    private SimpleDraweeView mSmileyImage;

    private boolean isLongClicked;

    private int mResId;

    public SmileyItem(Context context) {
        this(context, null);
    }

    public SmileyItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        // TODO Auto-generated constructor stub
    }

    public SmileyItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        mContext = context;
    }

    public SmileyItem(Context context, int resId) {
        this(context);
        setResId(resId);
    }

    public void setResId(int resId) {
        if (resId > 0 && mResId <= 0) {
            if ((mRootView = inflate(mContext, resId, null)) != null) {

                addView(mRootView);

                LayoutParams params = (LayoutParams) mRootView.getLayoutParams();
                params.addRule(RelativeLayout.CENTER_IN_PARENT);

                mSmileyImage = (SimpleDraweeView) mRootView.findViewById(R.id.smiley_image);
                mResId = resId;

                mDescView = new TextView(mContext);
                mDescView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.3f);
                mDescView.setEllipsize(TextUtils.TruncateAt.END);
                mDescView.setSingleLine(true);
                mDescView.setTextColor(GlobalData.app().getResources().getColor(R.color.color_black_trans_40));
                addView(mDescView);

                params = (LayoutParams) mDescView.getLayoutParams();
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
        }
    }

    public void setImageDrawable(Drawable drawable) {
        mSmileyImage.setImageDrawable(drawable);
    }

    public void setImageDrawableResId(int resId){
        ResImage resImage = new ResImage(resId);
        FrescoWorker.loadImage(mSmileyImage,resImage);
    }

    public SimpleDraweeView getImageView() {
        return mSmileyImage;
    }

    public void reset() {
        setImageDrawable(null);
        setOnClickListener(null);
        setOnLongClickListener(null);
        mSmileyImage.setImageBitmap(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
        }
        boolean result = false;

        try {
            return super.dispatchTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            MyLog.e(ex);
        }

        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            MyLog.e(ex);
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        try {
            return super.onTouchEvent(event);
        } catch (IllegalArgumentException ex) {
            MyLog.e(ex);
        }

        return false;
    }
}
