package com.wali.live.moduletest.fragment;

import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.base.BuildConfig;
import com.common.statistics.TimeStatistics;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.common.view.titlebar.CommonTitleBar;
import com.wali.live.moduletest.R;

public class ShowTextViewFragment extends BaseFragment {


    @Override
    public int initView() {
        return R.layout.test_fragment_show_textview_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        {
            View mTvTest = mRootView.findViewById(R.id.tv_test1);
            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                    .setGradientAngle(0)
                    .setGradientColor(Color.parseColor("#63B8FF"), Color.parseColor("#4F94CD"))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }

        {
            View mTvTest = mRootView.findViewById(R.id.tv_test2);
            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                    .setShape(DrawableCreator.Shape.Rectangle)
                    .setPressedDrawable(new ColorDrawable(Color.parseColor("#7CCD7C")))
                    .setUnPressedDrawable(new ColorDrawable(Color.parseColor("#7CFC00")))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }

        {
            View mTvTest = mRootView.findViewById(R.id.tv_test3);
            Drawable drawable = new DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20))
                    .setShape(DrawableCreator.Shape.Rectangle)
                    .setPressedStrokeColor(Color.parseColor("#000000"), Color.parseColor("#ffcc0000"))
                    .setPressedTextColor(Color.parseColor("#0000ff"))
                    .setUnPressedTextColor(Color.parseColor("#ffffff"))
                    .setRipple(true, Color.parseColor("#71C671"))
                    .setSolidColor(Color.parseColor("#7CFC00"))
                    .setStrokeWidth(U.getDisplayUtils().dip2px(2))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }

        {
            View mTvTest = mRootView.findViewById(R.id.tv_test4);
            Drawable drawable = new DrawableCreator.Builder()
                    .setPressedDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circle_like_pressed))
                    .setUnPressedDrawable(ContextCompat.getDrawable(getContext(), R.drawable.circle_like_normal))
                    .build();
            mTvTest.setClickable(true);
            mTvTest.setBackground(drawable);
        }


        {
            View v = mRootView.findViewById(R.id.image_view_btn);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        {
            View v = mRootView.findViewById(R.id.image_button_btn);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
