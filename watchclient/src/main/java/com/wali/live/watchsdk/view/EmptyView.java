package com.wali.live.watchsdk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;

/**
 * Created by chengsimin on 16/8/30.
 *
 * @Module 公共空白页面，支持设置图标和文字
 */
public class EmptyView extends RelativeLayout {
    public EmptyView(Context context) {
        super(context);
        init(context, null);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    TextView mEmptyTv;

    private int mEmptyDrawable = R.drawable.loading_empty;

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.empty_view_inside, this);
        String tips = GlobalData.app().getResources().getString(R.string.empty_tips);
        Drawable emptyIcon = GlobalData.app().getResources().getDrawable(R.drawable.loading_empty);
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EmptyView);
            emptyIcon = ta.getDrawable(R.styleable.EmptyView_emptyIcon);
            mEmptyDrawable = ta.getResourceId(R.styleable.EmptyView_emptyIcon, R.drawable.loading_empty);
            tips = ta.getString(R.styleable.EmptyView_emptyTips);
            ta.recycle();
        }
        mEmptyTv = (TextView) findViewById(R.id.empty_tv);
        mEmptyTv.setCompoundDrawablesWithIntrinsicBounds(null, emptyIcon, null, null);
        mEmptyTv.setText(tips);
    }

    public void setEmptyDrawable(int emptyDrawable) {
        mEmptyTv.setCompoundDrawablesWithIntrinsicBounds(0, emptyDrawable, 0, 0);
        if (emptyDrawable > 0) {
            mEmptyDrawable = emptyDrawable;
        }
    }

    public void setEmptyTips(int emptyTips) {
        mEmptyTv.setText(emptyTips);
    }

    public void setEmptyTips(String emptyTips) {
        mEmptyTv.setText(emptyTips);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        switch (visibility) {
            case VISIBLE: {
                setEmptyDrawable(mEmptyDrawable);
            }
            break;
            case GONE: {
                setEmptyDrawable(0);
            }
            break;
            case INVISIBLE: {
                setEmptyDrawable(0);
            }
            break;
        }
    }
}
