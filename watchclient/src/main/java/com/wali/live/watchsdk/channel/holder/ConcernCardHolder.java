package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.utils.ItemDataFormatUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.helper.ConcernHelper;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 带关注按钮的方形item
 */
public class ConcernCardHolder extends FixedHolder {
    private BaseImageView mAvatarIv;
    private ImageView mBadgeIv;
    private TextView mNameTv;
    private TextView mLocationTv;
    private TextView mConcernBtn;

    private BaseImageView mCoverIv;
    private TextView mLiveTv;
    private TextView mTitleTv;

    private ConcernHelper mConcernHelper;

    public ConcernCardHolder(View itemView) {
        super(itemView);
        mConcernHelper = new ConcernHelper(itemView.getContext());
    }

    @Override
    protected boolean needTitleView() {
        return false;
    }

    @Override
    protected void initContentView() {
        mAvatarIv = $(R.id.avatar_iv);
        mBadgeIv = $(R.id.badge_iv);
        mNameTv = $(R.id.name_tv);
        mLocationTv = $(R.id.location_tv);
        mConcernBtn = $(R.id.concern_tv);

        mCoverIv = $(R.id.cover_iv);
        mLiveTv = $(R.id.live_tv);
        mTitleTv = $(R.id.title_tv);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel model) {
        final ChannelLiveViewModel.BaseItem item = model.getFirstItem();
        if (item == null) {
            return;
        }
        exposureItem(item);

        AvatarUtils.loadAvatarByUidTs(mAvatarIv,
                item.getUser().getUid(),
                item.getUser().getAvatar(),
                true);
        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show person info
            }
        });

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCoverIv.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(GlobalData.screenWidth, GlobalData.screenWidth);
        } else {
            lp.width = GlobalData.screenWidth;
            lp.height = GlobalData.screenWidth;
        }
        mCoverIv.setLayoutParams(lp);
        AvatarUtils.loadCoverByUrl(mCoverIv, item.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_LARGE), false);
        mCoverIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(item.getSchemeUri())) {
                    jumpItem(item);
                } else {
                    MyLog.e(TAG, "clickCover url is empty");
                }
            }
        });

        if (!TextUtils.isEmpty(item.getLineOneText())) {
            mTitleTv.setVisibility(View.VISIBLE);
            mTitleTv.setText(ItemDataFormatUtils.getLiveTitle("", item.getLineOneText()));
            mTitleTv.setMovementMethod(new LinkMovementMethod());
        } else {
            mTitleTv.setVisibility(View.GONE);
        }

        if (item.getUser().getCertificationType() > 0) {
            mBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(14.4f);
            mBadgeIv.setImageDrawable(ItemDataFormatUtils.getCertificationImgSource(item.getUser().getCertificationType()));
        } else {
            mBadgeIv.getLayoutParams().height = DisplayUtils.dip2px(12.0f);
            mBadgeIv.setImageDrawable(ItemDataFormatUtils.getLevelSmallImgSource(item.getUser().getLevel()));
        }

        mNameTv.setText(item.getUser().getNickname());

        resetItem();
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        }

        mConcernHelper.initFocusData(item.getUser(), mConcernBtn);
    }

    private void resetItem() {
        mLiveTv.setVisibility(View.GONE);
        mLocationTv.setVisibility(View.GONE);
    }

    private void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item) {
        mLiveTv.setText(item.getUpRightText());
        mLiveTv.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(item.getLocation())) {
            mLocationTv.setText(item.getLocation());
        } else {
            mLocationTv.setText(GlobalData.app().getString(R.string.live_location_unknown));
        }
        mLocationTv.setVisibility(View.VISIBLE);
    }
}
