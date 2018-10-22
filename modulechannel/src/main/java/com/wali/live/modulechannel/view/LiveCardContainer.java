package com.wali.live.modulechannel.view;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;
import com.wali.live.modulechannel.util.HolderUtils;

public class LiveCardContainer {

    private View root;

    protected int mCountTvIds;
    protected int mMiddleTvIds;
    protected int mUserNickNameTvIds;
    protected int mLiveStatusIds;
    protected int mIvIds;
    protected int mLeftLabelTvId;
    protected int mLeftLabelIvId;


    protected BaseImageView mImageViews; // 封面
    protected TextView mCountTvs;
    protected TextView mUserNickNameTvs;
    protected TextView mMiddleTvs;
    protected ImageView mLiveStatusIvs;
    protected BaseImageView mLeftLabelIvs;
    protected TextView mLeftLabelTvs;

    public LiveCardContainer(View root) {
        this.root = root;
        init();
    }

    private void init() {
        mLeftLabelIvId = R.id.anchor_activity_icon;
        mUserNickNameTvIds = R.id.user_name_tv;
        mMiddleTvIds = R.id.middle_text;
        mLeftLabelTvId = R.id.left_label;
        mLiveStatusIds = R.id.live_status_iv;
        mIvIds = R.id.cover_iv;
        mCountTvIds = R.id.count_tv;

        mImageViews = (BaseImageView) root.findViewById(mIvIds);
        mCountTvs = (TextView) root.findViewById(mCountTvIds);
        mLeftLabelIvs = (BaseImageView) root.findViewById( mLeftLabelIvId);
        mUserNickNameTvs = (TextView) root.findViewById( mUserNickNameTvIds);
        mMiddleTvs = (TextView) root.findViewById( mMiddleTvIds);
        mLeftLabelTvs = (TextView) root.findViewById(mLeftLabelTvId);
        mLiveStatusIvs = (ImageView) root.findViewById( mLiveStatusIds);
    }


    public void bindItemOnLiveModel(ChannelLiveViewModel.BaseItem item) {
        HolderUtils.bindImage(mImageViews, item.getImageUrl(), false, 600, 600, ScalingUtils.ScaleType.CENTER_CROP);
        HolderUtils.bindText(mUserNickNameTvs, item.getUserNickName());
        HolderUtils.bindText(mMiddleTvs, item.getMiddleInfo() != null ? item.getMiddleInfo().getText1() : "");
        bindLeftTopCorner(item);
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        }
    }

    public void resetItem() {
        mCountTvs.setVisibility(View.GONE);
        mLeftLabelIvs.setVisibility(View.GONE);
        mLeftLabelTvs.setVisibility(View.GONE);
        if (mLiveStatusIvs != null) {
            mLiveStatusIvs.setVisibility(View.GONE);
        }
        mUserNickNameTvs.setMaxWidth(600);
        mMiddleTvs.setVisibility(View.GONE);
        mImageViews.setImageDrawable(U.app().getResources().getDrawable(R.color.channel_color_f2f2f2));
    }

    private void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item) {
        //这里因为产品的需要，会复用人数来显示位置
        HolderUtils.bindText(mCountTvs, item.getDistanceOrLocationOrCount());
        if (item instanceof ChannelLiveViewModel.LiveItem) {
            HolderUtils.bindLiveStatusImage((ChannelLiveViewModel.LiveItem) item, mLiveStatusIvs);
        }
    }

    /**
     * 左上角有2个元素：  展示图片的label， 展示文字的label ,优先级递增
     */
    private void bindLeftTopCorner(ChannelLiveViewModel.BaseItem item) {
        HolderUtils.bindLeftLabel(item, mLeftLabelTvs);
        if (mLeftLabelTvs != null && mLeftLabelTvs.getVisibility() != View.VISIBLE) {
            HolderUtils.bindLeftWidgetInfo(item, mLeftLabelIvs);
        }
    }

}
