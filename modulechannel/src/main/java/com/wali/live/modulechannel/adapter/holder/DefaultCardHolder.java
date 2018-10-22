package com.wali.live.modulechannel.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;
import com.wali.live.proto.CommonChannel.ListWidgetInfo;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 默认的方形item，和最早的热门、达人的item样式一致
 */
public class DefaultCardHolder extends FixedHolder {
    private static final int MARGIN = U.getDisplayUtils().dip2px(3.33f);

    private BaseImageView mAvatarIv;
    private ImageView mBadgeIv;
    private TextView mNameTv;
    private TextView mLocationTv;
    private TextView mCountTv;
    private TextView mShopTv;
    private RelativeLayout mTopInfoRl; // 顶部显示用户信息头像的区域

    private BaseImageView mCoverIv;
    private TextView mLiveTv;
    private TextView mTitleTv;
    private BaseImageView mLeftLabelIv; // 左上角显示活动图片

    public DefaultCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mAvatarIv = $(R.id.avatar_iv);
        mBadgeIv = $(R.id.badge_iv);
        mNameTv = $(R.id.name_tv);
        mLocationTv = $(R.id.location_tv);
        mCountTv = $(R.id.count_tv);
        mShopTv = $(R.id.shop_tv);
        mTopInfoRl = $(R.id.top_info_rl);

        mCoverIv = $(R.id.cover_iv);
        mLiveTv = $(R.id.live_tv);
        mTitleTv = $(R.id.title_tv);
        mLeftLabelIv = $(R.id.anchor_activity_icon);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel model) {
        final ChannelLiveViewModel.BaseItem item = model.getFirstItem();
        if (item == null) {
            return;
        }
        exposureItem(item);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCoverIv.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(U.getDisplayUtils().getScreenWidth() - MARGIN * 2, U.getDisplayUtils().getScreenWidth() - MARGIN * 2);
        } else {
            lp.width = U.getDisplayUtils().getScreenWidth() - MARGIN * 2;
            lp.height = U.getDisplayUtils().getScreenWidth() - MARGIN * 2;
        }
        mCoverIv.setLayoutParams(lp);
        bindImageWithBorder(mCoverIv, item.getImageUrl(ImageUtils.SIZE.SIZE_640), false, 640, 640, ScalingUtils.ScaleType.CENTER_CROP);
        mCoverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
            }
        });

        resetItem();
        String title = item.getLineOneText();
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
            title = ((ChannelLiveViewModel.BaseLiveItem) item).getTitleText();
        }
        // 有标题或者有标签
        if (!TextUtils.isEmpty(title) || (item.getLabel() != null && !item.getLabel().isEmpty())) {
            mTitleTv.setVisibility(View.VISIBLE);
            bindLabel(item.getLabel(), title, mTitleTv);
        } else {
            mTitleTv.setVisibility(View.GONE);
        }

        bindLeftWidgetInfo(item, mLeftLabelIv);

        if (item.getUser() == null) {
            mTopInfoRl.setVisibility(View.GONE);
            return;
        }

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(item.getUser().getUserId()).setTimestamp(item.getUser().getAvatar()).setCircle(true).build());
        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //person info
            }
        });
        // 徽章角标
        if (item.getUser().getCertificationType() > 0) {
            mBadgeIv.getLayoutParams().height = U.getDisplayUtils().dip2px(14.4f);
            //Todo-暂时注释用户勋章
//            mBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.getUser().getCertificationType()));
        } else {
            mBadgeIv.getLayoutParams().height = U.getDisplayUtils().dip2px(12.0f);
            //Todo-暂时注释用户勋章
//            mBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(item.getUser().getLevel()));
        }
        mNameTv.setText(item.getUser().getUserNickname());
    }


    /**
     * 左上角标签 配图片 对应ListWidgetInfo
     */
    private void bindLeftWidgetInfo(ChannelLiveViewModel.BaseItem item, BaseImageView leftLabelIv) {
        if (leftLabelIv == null) {
            return;
        }
        ListWidgetInfo widgetInfo = item.getWidgetInfo();
        if (widgetInfo != null) {
            String iconUrl = widgetInfo.getIconUrl();
            final String jumpUrl = widgetInfo.getJumpSchemeUri();
            if (!TextUtils.isEmpty(iconUrl)) {
                leftLabelIv.setVisibility(View.VISIBLE);
                bindImage(leftLabelIv, iconUrl, false, U.getDisplayUtils().dip2px(160f),
                        U.getDisplayUtils().dip2px(80f), ScalingUtils.ScaleType.FIT_START);
                leftLabelIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(jumpUrl)) {
                            mJumpListener.jumpScheme(jumpUrl);
                        } else {
                            MyLog.e(TAG, "leftLabelIv onClick jumpUrl is empty");
                        }
                    }
                });
            }
        }
    }

    private void resetItem() {
        mShopTv.setVisibility(View.GONE);
        mCountTv.setVisibility(View.GONE);
        mLiveTv.setVisibility(View.GONE);
        mLocationTv.setVisibility(View.GONE);
        mLeftLabelIv.setVisibility(View.GONE);
        mTopInfoRl.setVisibility(View.VISIBLE); // 顶部区域默认显示，除非user is null
    }

    private void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item) {
        mCountTv.setText(String.valueOf(item.getCountString()));
        mCountTv.setVisibility(View.VISIBLE);

        bindText(mLiveTv, item.getUpRightText());

        if (!TextUtils.isEmpty(item.getLocation())) {
            mLocationTv.setText(item.getLocation());
        } else {
            mLocationTv.setText(U.app().getString(R.string.channel_live_location_unknown));
        }
        mLocationTv.setVisibility(View.VISIBLE);

        if (item instanceof ChannelLiveViewModel.LiveItem) {
            bindLiveItem((ChannelLiveViewModel.LiveItem) item);
        }
    }

    private void bindLiveItem(ChannelLiveViewModel.LiveItem item) {
        if (item.isShowShop() && item.getShopCnt() >= 0) {
            mShopTv.setText(U.app().getString(R.string.channel_shop_cnt, item.getShopCnt()));
            mShopTv.setVisibility(View.VISIBLE);
        }
    }
}
