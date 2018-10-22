package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.text.DecimalFormat;
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
    protected int mLiveStatusIds;
    protected int mContentId;

    protected TextView[] mTypeTvs;
    protected TextView[] mDisplayTvs;
    protected TextView[] mCountTvs;
    protected TextView[] mShadowTvs;
    protected TextView[] mMiddleTvs;
    protected ImageView[] mLiveStatusIvs;

    protected ViewGroup[] mContentViews; // 卡片内容区域 封面的容器

    protected int mDisplayTvEmptyNum; //  副标题不为空的数量， 当holder中所有displayTv 显示的都空时， 缩短mBottomContainers长度

    protected float mSingleCardRadio = 1; // 单个卡片的宽高比

    protected int mDefaultNameMaxWidth = Integer.MAX_VALUE; // 昵称默认的maxWidth

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
        mSingleCardRadio = 1;
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.cover_iv);
        mTypeTvIds = new int[mViewSize];
        Arrays.fill(mTypeTvIds, R.id.type_tv);
        mDisplayTvIds = new int[mViewSize];
        Arrays.fill(mDisplayTvIds, R.id.display_tv);
        mCountTvIds = new int[mViewSize];
        Arrays.fill(mCountTvIds, R.id.count_tv);
        mShadowTvIds = new int[mViewSize];
        Arrays.fill(mShadowTvIds, R.id.shadow_tv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.user_name_tv);
        mBottomContainerId = R.id.bottom_container;
        mLeftLabelIvId = R.id.anchor_activity_icon;
        mMiddleTvIds = R.id.middle_text;
        mLeftLabelTvId = R.id.left_label;
        mMarkTvId = R.id.mark_tv;
        mLiveStatusIds = R.id.live_status_iv;
        mContentId = R.id.content_area;
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mMarginArea = $(R.id.margin_area);
        mArrayArea = $(R.id.array_area);
        mImageBorderWidth = 0;
        mItemWidth = (U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_TWO) / 2;
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mArrayArea.getLayoutParams();
        lp.rightMargin = lp.leftMargin = SIDE_MARGIN;
        int height = (int) (mItemWidth * mSingleCardRadio);
        int childWidth = 0;
        for (int i = 0; i < mViewSize; i++) {
            mImageViews[i].getLayoutParams().height = height;
            if (i == mViewSize - 1) {
                // 转成int值后 有些手机可能会有1px 的偏差，会看到一条白边
                int leftWidth = U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2 - MIDDLE_MARGIN_TWO - childWidth;
                mImageViews[i].getLayoutParams().width = leftWidth;
            } else {
                mImageViews[i].getLayoutParams().width = mItemWidth;
                childWidth += mItemWidth;
            }
        }
    }

    @Override
    protected void newContentView() {
        super.newContentView();
        mTypeTvs = new TextView[mViewSize];
        mDisplayTvs = new TextView[mViewSize];
        mCountTvs = new TextView[mViewSize];
        mMarkTvs = new TextView[mViewSize];
        mShadowTvs = new TextView[mViewSize];
        mBottomContainers = new View[mViewSize];
        mLeftLabelIvs = new BaseImageView[mViewSize];
        mMiddleTvs = new TextView[mViewSize];
        mLeftLabelTvs = new TextView[mViewSize];
        mLiveStatusIvs = new ImageView[mViewSize];
        mContentViews = new ViewGroup[mViewSize];
    }

    @Override
    protected void bindSingleCardView(int i) {
        super.bindSingleCardView(i);
        mTypeTvs[i] = $(mParentViews[i], mTypeTvIds[i]);
        mDisplayTvs[i] = $(mParentViews[i], mDisplayTvIds[i]);
        mCountTvs[i] = $(mParentViews[i], mCountTvIds[i]);
        mMarkTvs[i] = $(mParentViews[i], mMarkTvId);
        mShadowTvs[i] = $(mParentViews[i], mShadowTvIds[i]);
        mBottomContainers[i] = $(mParentViews[i], mBottomContainerId);
        mLeftLabelIvs[i] = $(mParentViews[i], mLeftLabelIvId);
        mMiddleTvs[i] = $(mParentViews[i], mMiddleTvIds);
        mLeftLabelTvs[i] = $(mParentViews[i], mLeftLabelTvId);
        mLiveStatusIvs[i] = $(mParentViews[i], mLiveStatusIds);
        mContentViews[i] = $(mParentViews[i], mContentId);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
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
    }

    @Override
    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        bindText(mMiddleTvs[i], item.getMiddleInfo() != null ? item.getMiddleInfo().getText1() : "");
        mLeftLabelImageWidth = U.getDisplayUtils().dip2px(90);
        mLeftLabelImageHeight = U.getDisplayUtils().dip2px(45); // 双排的角标是 135 * 270 比三排的大。。。
        bindLeftTopCorner(item, i);
    }

    protected void resetItem(int i) {
        mCountTvs[i].setVisibility(View.GONE);
        mMarkTvs[i].setVisibility(View.GONE);
        mShadowTvs[i].setVisibility(View.GONE);
        mLeftLabelIvs[i].setVisibility(View.GONE);
        mLeftLabelTvs[i].setVisibility(View.GONE);
        if (mLiveStatusIvs[i] != null) {
            mLiveStatusIvs[i].setVisibility(View.GONE);
        }
        mMiddleTvs[i].setVisibility(View.GONE);
        mDisplayTvs[i].setVisibility(View.GONE);
    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {
        super.bindBaseLiveItem(item, i);
        bindText(mCountTvs[i], item.getCountString());
        mShadowTvs[i].setVisibility(View.VISIBLE);
        if (item instanceof ChannelLiveViewModel.LiveItem) {
            bindLiveStatusImage((ChannelLiveViewModel.LiveItem) item, i);
        }
        // 右下角如果显示了pk等图标 就不显示城市
        if (mLiveStatusIvs[i] == null || mLiveStatusIvs[i].getVisibility() != View.VISIBLE ) {
            bindText(mDisplayTvs[i], item.getLocation());
        }
    }

    @Override
    protected void bindVideoItem(ChannelLiveViewModel.VideoItem item, int i) {
        super.bindVideoItem(item, i);
        mCountTvs[i].setText(item.getCountString());
        mCountTvs[i].setVisibility(View.VISIBLE);
        mShadowTvs[i].setVisibility(View.VISIBLE);
        bindDisplayText(item, i);
    }

    @Override
    protected void bindUserItem(ChannelLiveViewModel.UserItem item, int i) {
        bindDisplayText(item, i);
    }

    @Override
    protected void bindTVItem(ChannelLiveViewModel.TVItem item, int i) {
        bindDisplayText(item, i);
    }

    @Override
    protected void bindSimpleItem(ChannelLiveViewModel.SimpleItem item, int index) {
        super.bindSimpleItem(item, index);
        bindDisplayText(item, index);
    }


    /**
     * 右下角 显示城市
     *
     * @param item
     * @param i
     */
    protected void bindDisplayText(ChannelLiveViewModel.BaseItem item, int i) {
        if (mDisplayTvs == null || mDisplayTvs[i] == null) {
            MyLog.d(TAG, " bindDisplayText NULL  ");
            return;
        }
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            if (((ChannelLiveViewModel.BaseLiveItem) item).getDistance() > 0) {
                double distance = ((ChannelLiveViewModel.BaseLiveItem) item).getDistance() / 1000.0;
                DecimalFormat df = new DecimalFormat("0.#");
                String s = df.format(distance);
                if (Float.valueOf(s) < 0.1) {
                    s = "< 0.1";
                } else if (Float.valueOf(s) > 100) {
                    s = "> 100";
                }
                bindText(mDisplayTvs[i], String.format(U.app().getResources().getString(R.string.channel_km), s));
            } else {
                bindText(mDisplayTvs[i], ((ChannelLiveViewModel.BaseLiveItem) item).getLocation());
            }
        } else {
            bindText(mDisplayTvs[i], item.getDisplayText());
        }
    }

    /**
     * 右上角 显示直播状态
     *
     * @param item
     * @param i
     */
    protected void bindLiveStatusImage(ChannelLiveViewModel.LiveItem item, int i) {
        if (item.isPK()) {
            //bind PK image
            mLiveStatusIvs[i].setImageResource(R.drawable.milive_homepage_icon_pk);
            mLiveStatusIvs[i].setVisibility(View.VISIBLE);
        } else if (item.isMic()) {
            if (item.getMicType() == 0) {
                //bind 观众连麦 image
                mLiveStatusIvs[i].setImageResource(R.drawable.milive_homepage_icon_lianmai);
                mLiveStatusIvs[i].setVisibility(View.VISIBLE);
            } else if (item.getMicType() == 1) {
                //bind 主播连麦 image
                mLiveStatusIvs[i].setImageResource(R.drawable.milive_homepage_icon_manypelple);
                mLiveStatusIvs[i].setVisibility(View.VISIBLE);
            }
        }
    }

}
