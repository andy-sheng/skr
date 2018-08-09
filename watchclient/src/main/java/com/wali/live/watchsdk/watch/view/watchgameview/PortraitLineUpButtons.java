package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuting on 18-8-8.
 */

public class PortraitLineUpButtons extends RelativeLayout {
    private final int mDefautMargin = getResources().getDimensionPixelSize(R.dimen.view_dimen_50);
    private final int mDefautIconSie = getResources().getDimensionPixelSize(R.dimen.view_dimen_87);

    private List<View> mButtonList;
    private OnPortraitButtonClickListener mOnPortraitButtonClickListener;

    public PortraitLineUpButtons(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 添加按钮　自上而下的顺序
     * @param drawableRes
     * @param resId
     */
    public void addButton(int drawableRes, @IdRes int resId) {
        if (mButtonList == null) {
            mButtonList = new ArrayList<>();
        }

        final ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(getResources().getDrawable(drawableRes));
        LayoutParams layoutParams = new LayoutParams(mDefautIconSie, mDefautIconSie);
        layoutParams.addRule(ALIGN_PARENT_TOP);
        layoutParams.addRule(CENTER_HORIZONTAL);
        layoutParams.topMargin = (mDefautMargin + mDefautIconSie) * mButtonList.size();
        imageView.setLayoutParams(layoutParams);
        imageView.setId(resId);

        addView(imageView);
        mButtonList.add(imageView);

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                if (mOnPortraitButtonClickListener != null) {
                    mOnPortraitButtonClickListener.onPortraitButtonClick(v);
                }
            }
        });
    }

    public void setOnButtonClickListener(OnPortraitButtonClickListener listener) {
        this.mOnPortraitButtonClickListener = listener;
    }

    public interface OnPortraitButtonClickListener {
        void onPortraitButtonClick(View v);
    }
}
