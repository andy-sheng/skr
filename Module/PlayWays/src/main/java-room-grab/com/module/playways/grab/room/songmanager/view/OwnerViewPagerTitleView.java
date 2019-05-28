package com.module.playways.grab.room.songmanager.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.playways.R;
import com.module.playways.grab.room.songmanager.model.RecommendTagModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 修改房间名字
 */
public class OwnerViewPagerTitleView extends LinearLayout {
    ViewPager mViewPager;
    int mSelectedPosition = -1;
    ExTextView mSelectedExTextView;
    List<ExTextView> mTagTextView = new ArrayList<>();

    Drawable mSelectedDrawable = new DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(12))
            .setSolidColor(U.getColor(R.color.black_trans_20))
            .build();

    public OwnerViewPagerTitleView(Context context) {
        super(context);
    }

    public OwnerViewPagerTitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public OwnerViewPagerTitleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateCurTag(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setRecommendTagModelList(List<RecommendTagModel> tagModelList) {
        if (tagModelList == null || tagModelList.size() == 0) {
            return;
        }

        for (int i = 0; i < tagModelList.size(); i++) {
            FrameLayout frameLayout = new FrameLayout(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            frameLayout.setLayoutParams(layoutParams);

            ExTextView tagTextView = new ExTextView(getContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tagTextView.setPadding(U.getDisplayUtils().dip2px(12), U.getDisplayUtils().dip2px(2)
                    , U.getDisplayUtils().dip2px(12), U.getDisplayUtils().dip2px(2));
            params.gravity = Gravity.CENTER;
            tagTextView.setGravity(Gravity.CENTER);
            tagTextView.setLayoutParams(params);
            tagTextView.setText(tagModelList.get(i).getName());
            tagTextView.setTextColor(U.getColor(R.color.white_trans_50));
            tagTextView.setTextSize(14);

            final int index = i;

            tagTextView.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    mViewPager.setCurrentItem(index);
                    updateCurTag(index);
                }
            });

            frameLayout.addView(tagTextView);
            addView(frameLayout);
            mTagTextView.add(tagTextView);
        }

        updateCurTag(0);
    }

    private void updateCurTag(int position) {
        if (mSelectedExTextView != null) {
            mSelectedExTextView.setTextColor(U.getColor(R.color.white_trans_50));
            mSelectedExTextView.setBackground(null);
        }

        mSelectedExTextView = mTagTextView.get(position);
        mSelectedExTextView.setBackground(mSelectedDrawable);
        mSelectedExTextView.setTextColor(U.getColor(R.color.white));
        mSelectedPosition = position;
    }

    public void updateSelectedSongNum(int num) {
        mTagTextView.get(0).setText("已点" + num);
    }
}
