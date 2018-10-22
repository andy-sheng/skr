package com.wali.live.modulechannel.adapter.holder;

import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.avatar.AvatarUtils;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

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

    //Todo-关注的暂时去了,回头从写下
//    private ConcernHelper mConcernHelper;

    public ConcernCardHolder(View itemView) {
        super(itemView);
        //Todo-关注的暂时去了,回头从写下
//        mConcernHelper = new ConcernHelper(itemView.getContext());
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

//        AvatarUtils.loadAvatarByUidTs(mAvatarIv,
//                item.getUser().getUid(),
//                item.getUser().getAvatar(),
//                true);
        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(item.getUser().getUserId()).setTimestamp(item.getUser().getAvatar()).setCircle(true).build());
        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show person info
            }
        });

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCoverIv.getLayoutParams();
        if (lp == null) {
            lp = new RelativeLayout.LayoutParams(U.getDisplayUtils().getPhoneWidth(), U.getDisplayUtils().getPhoneWidth());
        } else {
            lp.width = U.getDisplayUtils().getPhoneWidth();
            lp.height = U.getDisplayUtils().getPhoneWidth();
        }
        mCoverIv.setLayoutParams(lp);
        loadCoverByUrlRoundCorner(mCoverIv, item.getImageUrl(ImageUtils.SIZE.SIZE_640), 1080, 480, 480);
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
            //Todo-暂时注释用户勋章
//            mTitleTv.setText(ItemDataFormatUtils.getLiveTitle("", item.getLineOneText()));
            mTitleTv.setMovementMethod(new LinkMovementMethod());
        } else {
            mTitleTv.setVisibility(View.GONE);
        }

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

        resetItem();
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        }

        //Todo-关注的暂时去了,回头从写下
//        mConcernHelper.initFocusData(item.getUser(), mConcernBtn);
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
            mLocationTv.setText(U.app().getString(R.string.channel_live_location_unknown));
        }
        mLocationTv.setVisibility(View.VISIBLE);
    }
}
