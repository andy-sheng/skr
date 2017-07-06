package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.BaseItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.BaseLiveItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.UserItem;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel.VideoItem;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 并列2个方形图片的item
 */
public class TwoCardHolder extends RepeatHolder {
    protected View mMarginArea;
    protected View mArrayArea;

    protected int[] mTypeTvIds;
    protected int[] mDisplayTvIds;
    protected int[] mCountTvIds;
    protected int[] mShadowTvIds;
    protected int mMiddleTvIds;
    protected int mUserNickNameTvIds;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mShadowTvs;
    protected TextView[] mUserNickNameTvs;
    protected TextView[] mMiddleTvs;

    protected int mDisplayTvEmptyNum; //  副标题不为空的数量， 当holder中所有displayTv 显示的都空时， 缩短mBottomContainers长度

    public TwoCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 2;
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.cover_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.name_tv);
        mTypeTvIds = new int[mViewSize];
        Arrays.fill(mTypeTvIds, R.id.type_tv);
        mDisplayTvIds = new int[mViewSize];
        Arrays.fill(mDisplayTvIds, R.id.display_tv);
        mCountTvIds = new int[mViewSize];
        Arrays.fill(mCountTvIds, R.id.count_tv);
        mShadowTvIds = new int[mViewSize];
        Arrays.fill(mShadowTvIds, R.id.shadow_tv);
        mBottomContainerId = R.id.bottom_container;
        mLeftLabelIvId = R.id.anchor_activity_icon;
        mUserNickNameTvIds = R.id.user_name_tv;
        mMiddleTvIds = R.id.middle_text;
        mLeftLabelTvId = R.id.left_label;
        mMarkTvId = R.id.mark_tv;
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mTypeTvs = new TextView[mViewSize];
        mDisplayTvs = new TextView[mViewSize];
        mCountTvs = new TextView[mViewSize];
        mMarkTvs = new TextView[mViewSize];
        mShadowTvs = new TextView[mViewSize];
        mBottomContainers = new View[mViewSize];
        mLeftLabelIvs = new BaseImageView[mViewSize];
        mUserNickNameTvs = new TextView[mViewSize];
        mMiddleTvs = new TextView[mViewSize];
        mLeftLabelTvs = new TextView[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mTypeTvs[i] = $(mParentViews[i], mTypeTvIds[i]);
            mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
            mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
            mMarkTvs[i] = $(mParentViews[i], mMarkTvId);
            mShadowTvs[i] = $(mParentViews[i], mShadowTvIds[i]);
            mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
            mLeftLabelIvs[i] = $(mParentViews[i], mLeftLabelIvId);
            mUserNickNameTvs[i] = $(mParentViews[i], mUserNickNameTvIds);
            mMiddleTvs[i] = $(mParentViews[i], mMiddleTvIds);
            mLeftLabelTvs[i] = $(mParentViews[i], mLeftLabelTvId);
        }

        mMarginArea = $(R.id.margin_area);
        mArrayArea = $(R.id.array_area);

        mImageBorderWidth = 0;
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
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
        super.bindLiveModel(viewModel);

        mDisplayTvEmptyNum = 0;
        // 如果没有标题，而且是组内第一个的话，就显示margin区域
        if (!mViewModel.hasHead() && viewModel.isFirst() && mPosition != 0) {
            mMarginArea.setVisibility(View.VISIBLE);
        } else {
            mMarginArea.setVisibility(View.GONE);
        }

        int height;
        if (mViewModel.isFullColumn()) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = 0;

            height = (GlobalData.screenWidth - DisplayUtils.dip2px(1f)) / 2;
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
            lp.rightMargin = lp.leftMargin = SIDE_MARGIN;

            height = (GlobalData.screenWidth - SIDE_MARGIN * 2 - MIDDLE_MARGIN_TWO) / 2;
        }
        for (int i = 0; i < mViewSize; i++) {
            mImageViews[i].getLayoutParams().height = height;
        }
        mItemWidth = height;
    }

    @Override
    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        //  bindText(mTypeTvs[i], item.getUpRightText());
        bindText(mUserNickNameTvs[i], item.getUserNickName());
        bindText(mMiddleTvs[i], item.getMiddleInfo() != null ? item.getMiddleInfo().getText1() : "");
        mLeftLabelImageWidth = DisplayUtils.dip2px(90);
        mLeftLabelImageHeight = DisplayUtils.dip2px(45); // 双排的角标是 135 * 270 比三排的大。。。
        bindLeftTopCorner(item, i);
    }

    protected void resetItem(int i) {
        mCountTvs[i].setVisibility(View.GONE);
        mMarkTvs[i].setVisibility(View.GONE);
        mShadowTvs[i].setVisibility(View.GONE);
        mBottomContainers[i].getLayoutParams().height = DisplayUtils.dip2px(50f);
        mLeftLabelIvs[i].setVisibility(View.GONE);
        mLeftLabelTvs[i].setVisibility(View.GONE);
        mUserNickNameTvs[i].setMaxWidth(Integer.MAX_VALUE);
    }

    @Override
    protected void bindBaseLiveItem(BaseLiveItem item, int i) {
        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
        bindDisplayText(item, i);
    }

    @Override
    protected void bindVideoItem(VideoItem item, int i) {
        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
        bindDisplayText(item, i);
    }

    @Override
    protected void bindUserItem(UserItem item, int i) {
        bindDisplayText(item, i);
    }

    @Override
    protected void bindTVItem(ChannelLiveViewModel.TVItem item, int i) {
        bindDisplayText(item, i);
    }

    @Override
    protected void bindSimpleItem(ChannelLiveViewModel.SimpleItem item, int index) {
        bindDisplayText(item, index);
    }

    /**
     * 右下角 显示城市
     *
     * @param item
     * @param i
     */
    protected void bindDisplayText(BaseItem item, final int i) {
        if (item instanceof BaseLiveItem) {
//            if (((BaseLiveItem) item).getDistance() > 0) {
//                double distance = ((BaseLiveItem) item).getDistance() / 1000.0;
//                DecimalFormat df = new DecimalFormat("0.#");
//                String s = df.format(distance);
//                if (Float.valueOf(s) < 0.1) {
//                    s = "< 0.1";
//                } else if (Float.valueOf(s) > 100) {
//                    s = "> 100";
//                }
//                bindText(mDisplayTvs[i], String.format(GlobalData.app().getResources().getString(R.string.km), s));
//            } else {
            bindText(mDisplayTvs[i], ((BaseLiveItem) item).getLocation());
//            }
        } else {
            bindText(mDisplayTvs[i], item.getDisplayText());
        }
        if (mDisplayTvs[i].getVisibility() != View.VISIBLE) {
            return;
        }
        mDisplayTvs[i].getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                int totalTextWidth = mUserNickNameTvs[i].getWidth() + mDisplayTvs[i].getWidth();  // 两个文本控件加起来的宽度
                int totalItemWIdth = mItemWidth - DisplayUtils.dip2px(13.33f);        // 整体的宽度
                int gap = DisplayUtils.dip2px(2); // 空隙
                if (totalTextWidth > totalItemWIdth - gap) {
                    mUserNickNameTvs[i].setMaxWidth(totalItemWIdth - mDisplayTvs[i].getWidth() - gap);
                    MyLog.i(TAG, "text name: " + mUserNickNameTvs[i].getText() + "name width: " + mUserNickNameTvs[i].getWidth() + " location width: " + mDisplayTvs[i].getWidth() + "item width: " + mItemWidth);
                }
                mDisplayTvs[i].getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }
}
