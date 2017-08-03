package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelNavigateViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public class NavigateHolder extends RepeatHolder {
    protected int[] mIconIvIds;
    protected BaseImageView[] mIconIvs;
    protected int[] mSubTitleTvIds;
    protected TextView[] mSubTitleTvs;


    public NavigateHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 3;
        mParentIds = new int[]{
                R.id.single_navigate_1,
                R.id.single_navigate_2,
                R.id.single_navigate_3,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.single_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.single_tv);
        mIconIvIds = new int[mViewSize];
        Arrays.fill(mIconIvIds, R.id.icon_iv);
        mSubTitleTvIds = new int[mViewSize];
        Arrays.fill(mSubTitleTvIds, R.id.sub_single_tv);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mIconIvs = new BaseImageView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mIconIvs[i] = $(mParentViews[i], mIconIvIds[i]);
        }

        mSubTitleTvs = new TextView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mSubTitleTvs[i] = $(mParentViews[i], mSubTitleTvIds[i]);
        }
    }

    @Override
    protected boolean needTitleView() {
        return false;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    protected void bindItemOnNavigateModel(final ChannelNavigateViewModel.NavigateItem item, int i) {
        mImageViews[i].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mJumpListener.jumpScheme(item.getSchemeUri());
            }
        });

        if (!TextUtils.isEmpty(item.getIconUrl())) {
            mIconIvs[i].setVisibility(View.VISIBLE);
            FrescoWorker.loadImage(mIconIvs[i],
                    ImageFactory.newHttpImage(item.getIconUrl())
                            .setIsCircle(false)
                            .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                            .setWidth(160)
                            .setHeight(160)
                            .build()
            );
        } else {
            mIconIvs[i].setVisibility(View.GONE);
        }

        bindText(mSubTitleTvs[i], item.getSubText());
    }
}
