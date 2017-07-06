package com.wali.live.watchsdk.channel.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.utils.display.DisplayUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

import java.util.Arrays;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 列表样式，左图右文的item，和OneListHolder细节不同
 */
public class OneLiveListHolder extends RepeatHolder {
    protected int[] mTypeTvIds;
    protected int[] mCountTvIds;
    //    protected int[] mTimeTvIds;
    private int[] mAvatarIvIds;
    private int[] mNameTvIds;
    private int[] mUserContainerIds;

    protected TextView[] mTypeTvs;
    protected TextView[] mCountTvs;
    //    protected TextView[] mTimeTvs;
    private BaseImageView[] mAvatarIvs;
    private TextView[] mNameTvs;
    private View[] mUserContainers;

    private View mContentContainer;
    private View mListSplitLine;

    public OneLiveListHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentViewId() {
        mViewSize = 1;
        mParentIds = new int[]{
                R.id.content_area,
        };
        mIvIds = new int[mViewSize];
        Arrays.fill(mIvIds, R.id.single_iv);
        mTvIds = new int[mViewSize];
        Arrays.fill(mTvIds, R.id.single_tv);

        mTypeTvIds = new int[mViewSize];
        Arrays.fill(mTypeTvIds, R.id.type_tv);
        mCountTvIds = new int[mViewSize];
        Arrays.fill(mCountTvIds, R.id.count_tv);
//        mTimeTvIds = new int[mViewSize];
//        Arrays.fill(mTimeTvIds, R.id.time_tv);

        mAvatarIvIds = new int[mViewSize];
        Arrays.fill(mAvatarIvIds, R.id.avatar_iv);
        mNameTvIds = new int[mViewSize];
        Arrays.fill(mNameTvIds, R.id.name_tv);
        mUserContainerIds = new int[mViewSize];
        Arrays.fill(mUserContainerIds, R.id.user_container);
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mTypeTvs = new TextView[mViewSize];
        mCountTvs = new TextView[mViewSize];
//        mTimeTvs = new TextView[mViewSize];
        mAvatarIvs = new BaseImageView[mViewSize];
        mNameTvs = new TextView[mViewSize];
        mUserContainers = new View[mViewSize];

        for (int i = 0; i < mViewSize; i++) {
            mAvatarIvs[i] = $(mAvatarIvIds[i]);
            mTypeTvs[i] = $(mTypeTvIds[i]);
            mCountTvs[i] = $(mCountTvIds[i]);
//            mTimeTvs[i] = $(mTimeTvIds[i]);
            mAvatarIvs[i] = $(mAvatarIvIds[i]);
            mNameTvs[i] = $(mNameTvIds[i]);
            mUserContainers[i] = $(mUserContainerIds[i]);
        }

        mContentContainer = $(R.id.content_area);
        mListSplitLine = $(R.id.list_split_line);
    }

    @Override
    protected boolean isChangeImageSize() {
        return true;
    }

    @Override
    protected int getImageWidth() {
        return DisplayUtils.dip2px(101.33f);
    }

    @Override
    protected int getImageHeight() {
        // 比例按设计尺寸
        return (int) (getImageWidth() * NEW_IMAGE_RATIO);
    }

    @Override
    protected boolean isCircle() {
        return false;
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        super.bindLiveModel(viewModel);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mContentContainer.getLayoutParams();
        if (mViewModel.isFirst()) {
            mSplitLine.setVisibility(View.VISIBLE);
            mListSplitLine.setVisibility(View.VISIBLE);

//            lp.topMargin = DisplayUtils.dip2px(6.67f);
            lp.bottomMargin = 0;
        } else if (mViewModel.isLast()) {
            mSplitLine.setVisibility(View.GONE);
            mListSplitLine.setVisibility(View.GONE);

            lp.topMargin = 0;
//            lp.bottomMargin = DisplayUtils.dip2px(6.67f);
        } else {
            mSplitLine.setVisibility(View.GONE);
            mListSplitLine.setVisibility(View.VISIBLE);

            lp.topMargin = 0;
            lp.bottomMargin = 0;
        }
    }

    @Override
    protected void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item, int i) {
        super.bindItemOnLiveModel(item, i);
        bindText(mTypeTvs[i], item.getUpRightText());
        bindText(mTextViews[i], item.getLineOneText(), item.getLineTwoText());

//        if (item.getPublishTime() != 0) {
//            mTimeTvs[i].setText(DateTimeUtils.formatTimeString(GlobalData.app(), item.getPublishTime()));
//            mTimeTvs[i].setVisibility(View.VISIBLE);
//        } else {
//            mTimeTvs[i].setVisibility(View.GONE);
//        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (item.getUser() != null) {
            AvatarUtils.loadAvatarByUidTs(mAvatarIvs[i],
                    item.getUser().getUid(),
                    item.getUser().getAvatar(),
                    AvatarUtils.SIZE_TYPE_AVATAR_SMALL,
                    true);
            mNameTvs[i].setText(item.getUser().getNickname());
            mUserContainers[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mUserContainers[i].setVisibility(View.VISIBLE);
        } else {
            mUserContainers[i].setVisibility(View.GONE);
        }
    }

    @Override
    protected void resetItem(int i) {
        mCountTvs[i].setVisibility(View.GONE);

    }

    @Override
    protected void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item, int i) {

        mCountTvs[i].setText(GlobalData.app().getResources().getString(R.string.read_count, item.getViewerCnt()));
        mCountTvs[i].setVisibility(View.VISIBLE);
    }

    @Override
    public void bindVideoItem(ChannelLiveViewModel.VideoItem item, int i) {
        mCountTvs[i].setText(GlobalData.app().getResources().getString(R.string.read_count, item.getViewCount()));
        mCountTvs[i].setVisibility(View.VISIBLE);
    }
}
