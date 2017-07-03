package com.wali.live.watchsdk.channel.holder;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;

import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 */
public abstract class RepeatHolder extends FixedHolder {
    protected int[] mParentIds;
    protected int[] mIvIds;
    protected int[] mTvIds;

    protected ViewGroup[] mParentViews;
    protected BaseImageView[] mImageViews;
    protected TextView[] mTextViews;

    protected int mViewSize;

    public RepeatHolder(View itemView) {
        super(itemView);
        if (isChangeImageSize()) {
            changeImageSize();
        }
    }
    protected abstract void initContentViewId();

    protected void initContentView() {
        initContentViewId();
        mParentViews = new ViewGroup[mViewSize];
        mImageViews = new BaseImageView[mViewSize];
        mTextViews = new TextView[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mParentViews[i] = $(mParentIds[i]);
            mImageViews[i] = $(mParentViews[i], mIvIds[i]);
            mTextViews[i] = $(mParentViews[i], mTvIds[i]);
        }
    }

    protected boolean isChangeImageSize() {
        return false;
    }

    protected void changeImageSize() {
        ViewGroup.MarginLayoutParams mlp;
        for (int i = 0; i < mViewSize; i++) {
            mlp = (ViewGroup.MarginLayoutParams) mImageViews[i].getLayoutParams();
            mlp.width = getImageWidth();
            mlp.height = getImageHeight();
        }
    }

    protected int getImageWidth() {
        return 0;
    }

    protected int getImageHeight() {
        return 0;
    }

    protected ScalingUtils.ScaleType getScaleType() {
        return ScalingUtils.ScaleType.CENTER_CROP;
    }

    protected abstract boolean isCircle();

    protected void setParentVisibility(int startIndex) {
        for (int i = startIndex; i < mViewSize; i++) {
            mParentViews[i].setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        List<ChannelLiveViewModel.BaseItem> itemDatas = viewModel.getItemDatas();
        int minSize = Math.min(mViewSize, itemDatas.size());
        setParentVisibility(minSize);
        for (int i = 0; i < minSize; i++) {
            mParentViews[i].setVisibility(View.VISIBLE);
            ChannelLiveViewModel.BaseItem item = itemDatas.get(i);
            if (item != null) {
                MyLog.d(TAG, "bindLiveModel imageUrl : " + item.getImageUrl());
                if (viewModel.isFullColumn()) {
                    bindImage(mImageViews[i], item.getImageUrl(), isCircle(), 320, 320, getScaleType());
                } else {
                    bindImageWithBorder(mImageViews[i], item.getImageUrl(), isCircle(), 320, 320, getScaleType());
                }
                mTextViews[i].setText(item.getNameText());

                bindItemOnLiveModel(item, i);

                if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
                    bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item, i);
                }
            }
        }
    }

    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
    }

    protected void bindBaseLiveItem(final ChannelLiveViewModel.BaseLiveItem item, final int i) {
        mImageViews[i].setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!TextUtils.isEmpty(item.getSchemeUri())) {
                            SchemeSdkActivity.openActivity((BaseSdkActivity) itemView.getContext(),
                                    Uri.parse(item.getSchemeUri()));
                        }
                    }
                }
        );
    }
}
