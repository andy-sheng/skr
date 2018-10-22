package com.wali.live.modulechannel.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by zhaomin on 17/11/2.
 */
public class TwoCardWideHolder extends FixedHolder {
    protected ViewGroup[] mParentViews;
    protected int[] mParentIds;
    protected RelativeLayout[] mCoverLayouts;
    protected BaseImageView[] mCoverIvs;
    protected ImageView[] mAppTypeIcons;
    protected TextView[] mAnchorNameTvs;
    protected TextView[] mViewerCntTvs;
    protected TextView[] mBottomTextTvs;

    private List<ChannelLiveViewModel.BaseLiveItem> mLiveItems;

    public TwoCardWideHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mParentIds = new int[]{
                R.id.single_card_1,
                R.id.single_card_2
        };
        int size = mParentIds.length;

        mParentViews = new ViewGroup[size];
        mCoverLayouts = new RelativeLayout[size];
        mCoverIvs = new BaseImageView[size];
        mAppTypeIcons = new ImageView[size];
        mAnchorNameTvs = new TextView[size];
        mViewerCntTvs = new TextView[size];
        mBottomTextTvs = new TextView[size];

        for (int i = 0; i < size; i++) {
            mParentViews[i] = $(mParentIds[i]);
            mCoverLayouts[i] = $(mParentViews[i], R.id.cover_layout);
            mCoverIvs[i] = $(mParentViews[i], R.id.iv_cover);
            mAppTypeIcons[i] = $(mParentViews[i], R.id.appType_icon);
            mAnchorNameTvs[i] = $(mParentViews[i], R.id.tv_anchor_name);
            mViewerCntTvs[i] = $(mParentViews[i], R.id.tv_viewer_cnt);
            mBottomTextTvs[i] = $(mParentViews[i], R.id.bottom_text);

            int width = (U.getDisplayUtils().getScreenWidth() -
                    U.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_20) * 2
                    - U.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_18)) / 2;
            mCoverLayouts[i].getLayoutParams().height = width / 16 * 9;

            final int position = i;
            mParentViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mLiveItems != null && mLiveItems.size() > position) {
                        ChannelLiveViewModel.BaseLiveItem item = mLiveItems.get(position);
                        jumpItem(item);
                    }
                }
            });
        }
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        List<ChannelLiveViewModel.BaseItem> items = viewModel.getItemDatas();
        if (items != null && !items.isEmpty()) {
            if (mLiveItems == null) {
                mLiveItems = new ArrayList<>();
            } else {
                mLiveItems.clear();
            }
            for (ChannelLiveViewModel.BaseItem item : items) {
                if (mLiveItems.size() < mParentIds.length && item instanceof ChannelLiveViewModel.BaseLiveItem) {
                    mLiveItems.add((ChannelLiveViewModel.BaseLiveItem) item);
                }
            }
            bindCardView();
        }
    }

    private void bindCardView() {
        if (mLiveItems == null || mLiveItems.isEmpty()) {
            return;
        }
        for (int i = 0; i < mParentIds.length; i ++) {
            if (mLiveItems.size() <= i) {
                mParentViews[i].setVisibility(View.INVISIBLE);
                return;
            } else {
                mParentViews[i].setVisibility(View.VISIBLE);
            }

            ChannelLiveViewModel.BaseLiveItem baseLiveItem = mLiveItems.get(i);

            FrescoWorker.loadImage(mCoverIvs[i],
                    ImageFactory.newHttpImage(baseLiveItem.getImageUrl())
                            .setIsCircle(false)
                            .setCornerRadius(U.getDisplayUtils().dip2px(5.33f))
                            .setLoadingDrawable(U.app().getResources().getDrawable(R.drawable.live_show_avatar_loading))
                            .setFailureDrawable(U.app().getResources().getDrawable(R.drawable.live_show_avatar_loading))
                            .build()
            );

            if (!TextUtils.isEmpty(baseLiveItem.getUserNickName())) {
                mAnchorNameTvs[i].setText(baseLiveItem.getUserNickName());
            }
            if (baseLiveItem instanceof ChannelLiveViewModel.LiveItem) {
                mViewerCntTvs[i].setText(parseCountString(((ChannelLiveViewModel.LiveItem)baseLiveItem).getHotScore()));
            } else {
                mViewerCntTvs[i].setText(parseCountString(baseLiveItem.getViewerCnt()));
            }
            mViewerCntTvs[i].setCompoundDrawablesWithIntrinsicBounds(U.app().getResources().getDrawable(getRandomIconRes()), null, null, null);
            mViewerCntTvs[i].setCompoundDrawablePadding(U.app().getResources().getDimensionPixelSize(R.dimen.view_dimen_7));

            if (baseLiveItem instanceof ChannelLiveViewModel.LiveItem) {
                mAppTypeIcons[i].setVisibility(View.VISIBLE);
                if (((ChannelLiveViewModel.LiveItem) baseLiveItem).isMiLive()) {
                    mAppTypeIcons[i].setImageDrawable(U.app().getResources().getDrawable(R.drawable.logo_xiaomi_small));
                } else if (((ChannelLiveViewModel.LiveItem) baseLiveItem).isHuyaLive()) {
                    mAppTypeIcons[i].setImageDrawable(U.app().getResources().getDrawable(R.drawable.logo_huya_small));
                } else if (((ChannelLiveViewModel.LiveItem) baseLiveItem).isPandaLive()) {
                    mAppTypeIcons[i].setImageDrawable(U.app().getResources().getDrawable(R.drawable.logo_panda_small));
                } else {
                    mAppTypeIcons[i].setVisibility(View.GONE);
                }
            } else {
                mAppTypeIcons[i].setVisibility(View.GONE);
            }

            String liveTitle = baseLiveItem.getTitle() == null ? "" : baseLiveItem.getTitleText();
            if (baseLiveItem.getLabel() != null && !baseLiveItem.getLabel().isEmpty()) {
                bindLabel(baseLiveItem.getLabel(), liveTitle, mBottomTextTvs[i]);
            } else {
                mBottomTextTvs[i].setText(liveTitle);
            }

            exposureItem(baseLiveItem);
        }
    }

    private List<Integer> mHotScoreIconList;
    private Random mRandom;

    private int getRandomIconRes() {
        if (mHotScoreIconList == null) {
            mHotScoreIconList = new ArrayList<>();
            mHotScoreIconList.add(R.drawable.game_live_icon_flame_blue);
            mHotScoreIconList.add(R.drawable.game_live_icon_flame_red);
            mHotScoreIconList.add(R.drawable.game_live_icon_flame_yellow);
        }
        if (mRandom == null) {
            mRandom = new Random();
        }
        int random = mRandom.nextInt(mHotScoreIconList.size());
        return mHotScoreIconList.get(random);
    }
}

