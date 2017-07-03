package com.wali.live.watchsdk.channel.holder;

import android.app.Activity;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 默认的方形item，和最早的热门、达人的item样式一致
 */
public class DefaultCardHolder extends FixedHolder {
    private static final int MARGIN = DisplayUtils.dip2px(3.33f);

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
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCoverIv.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(GlobalData.screenWidth - MARGIN * 2, GlobalData.screenWidth - MARGIN * 2);
        } else {
            lp.width = GlobalData.screenWidth - MARGIN * 2;
            lp.height = GlobalData.screenWidth - MARGIN * 2;
        }
        mCoverIv.setLayoutParams(lp);
        bindImageWithBorder(mCoverIv, item.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_LARGE), false, 640, 640, ScalingUtils.ScaleType.CENTER_CROP);
        mCoverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(item.getSchemeUri())) {
                    Uri uri = Uri.parse(item.getSchemeUri());

                    long playerId = Long.parseLong(uri.getQueryParameter("playerid"));
                    String liveId = uri.getQueryParameter("liveid");
                    String videoUrl = uri.getQueryParameter("videourl");

                    WatchSdkActivity.openActivity((Activity) itemView.getContext(),
                            RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                                    .setAvatar(item.getUser().getAvatar())
                                    .setCoverUrl(item.getImageUrl())
                                    .build());
                }
            }
        });
        resetItem();
        String title = item.getLineOneText();

        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
            title = ((ChannelLiveViewModel.BaseLiveItem) item).getTitleText();
        }
        // 有标题或者有标签
        if (!TextUtils.isEmpty(title)) {
            mTitleTv.setText(title);
            mTitleTv.setVisibility(View.VISIBLE);
        } else {
            mTitleTv.setVisibility(View.GONE);
        }

        if (item.getUser() == null) {
            mTopInfoRl.setVisibility(View.GONE);
            return;
        }

        AvatarUtils.loadAvatarByUidTs(mAvatarIv,
                item.getUser().getUid(),
                item.getUser().getAvatar(),
                true);
        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        // 徽章角标
        if (item.getUser().getCertificationType() > 0) {
            mBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(14.4f);
            mBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.getUser().getCertificationType()));
        } else {
            mBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(12.0f);
            mBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(item.getUser().getLevel()));
        }
        mNameTv.setText(item.getUser().getNickname());
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
            mLocationTv.setText(GlobalData.app().getString(R.string.location_unknown_new));
        }
        mLocationTv.setVisibility(View.VISIBLE);
    }

}
