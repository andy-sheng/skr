package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.widget.TextView;

import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 6个方形图片的item
 */
public class SixMakeupHolder extends RepeatHolder {
    private int[] mCountTvIds;
    private TextView[] mCountTvs;

    private int mWidth;

    public SixMakeupHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 6;
        mParentIds = new int[]{
                R.id.single_makeup_1,
                R.id.single_makeup_2,
                R.id.single_makeup_3,
                R.id.single_makeup_4,
                R.id.single_makeup_5,
                R.id.single_makeup_6,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.avatar_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.name_tv);

        mCountTvIds = new int[mViewSize];
        Arrays.fill(mCountTvIds, R.id.count_tv);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mCountTvs = new TextView[mViewSize];
        for (int i = 0; i < mViewSize; i++) {
            mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
        }

        mWidth = (DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN * 2) / 3 + 1;
        if (mViewSize != 0) {
            mParentViews[0].getLayoutParams().height = mParentViews[0].getLayoutParams().width = mWidth * 2 + MIDDLE_MARGIN;
            for (int i = 1; i < mViewSize; i++) {
                mParentViews[i].getLayoutParams().height = mParentViews[i].getLayoutParams().width = mWidth;
            }
        }
    }

    @Override
    protected boolean isChangeImageSize() {
        return false;
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        List<ChannelLiveViewModel.BaseItem> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        setParentVisibility(minSize);
        for (int i = 0; i < minSize; i++) {
            mParentViews[i].setVisibility(View.VISIBLE);
            final ChannelLiveViewModel.BaseItem item = itemDatas.get(i);
            if (item != null) {
                bindImage(mImageViews[i], item.getImageUrl(), isCircle(), 320, 320, getScaleType());
                mImageViews[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        jumpItem(item);
                    }
                });

                bindText(mTextViews[i], item.getNameText());

                if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
                    ChannelLiveViewModel.BaseLiveItem liveItem = (ChannelLiveViewModel.BaseLiveItem) item;
                    mCountTvs[i].setText(String.valueOf(liveItem.getViewerCnt()));
                    mCountTvs[i].setVisibility(View.VISIBLE);
                } else if (item instanceof ChannelLiveViewModel.VideoItem) {
                    ChannelLiveViewModel.VideoItem videoItem = (ChannelLiveViewModel.VideoItem) item;
                    mCountTvs[i].setText(String.valueOf(videoItem.getViewCount()));
                    mCountTvs[i].setVisibility(View.VISIBLE);
                } else {
                    mCountTvs[i].setVisibility(View.GONE);
                }
            }
        }
    }
}
