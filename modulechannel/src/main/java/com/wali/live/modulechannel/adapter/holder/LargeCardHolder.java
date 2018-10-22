package com.wali.live.modulechannel.adapter.holder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 和OneCardHolder样式基本一致，尺寸更大，文案嵌入图片底部
 */
public class LargeCardHolder extends FixedHolder {
    private BaseImageView mAvatarIv;
    private TextView mNameTv;
    private TextView mDisplayTv;
    private TextView mTypeTv;
    private TextView mCountTv;

    public LargeCardHolder(View itemView) {
        super(itemView);
        changeImageSize();
    }

    @Override
    protected void initContentView() {
        mAvatarIv = $(R.id.avatar_iv);
        mNameTv = $(R.id.name_tv);
        mTypeTv = $(R.id.type_tv);
        mDisplayTv = $(R.id.display_tv);
        mCountTv = $(R.id.count_tv);
    }

    @Override
    protected void initTitleView() {
        super.initTitleView();
        mSplitLine.setVisibility(View.GONE);
    }

    protected void changeImageSize() {
        ViewGroup.MarginLayoutParams mlp;
        mlp = (ViewGroup.MarginLayoutParams) mAvatarIv.getLayoutParams();
        mlp.width = getImageWidth();
        mlp.height = getImageHeight();
    }

    protected int getImageWidth() {
        return ViewGroup.MarginLayoutParams.MATCH_PARENT;
    }

    protected int getImageHeight() {
        // 比例按设计尺寸
        return (int) ((U.getDisplayUtils().getScreenWidth() - SIDE_MARGIN * 2) * IMAGE_RATIO);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel model) {
        final ChannelLiveViewModel.BaseItem item = model.getFirstItem();
        if (item == null) {
            return;
        }
        exposureItem(item);

        bindImageWithBorder(mAvatarIv, item.getImageUrl(ImageUtils.SIZE.SIZE_640), false, 640, 640, ScalingUtils.ScaleType.CENTER_CROP);
        mAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
            }
        });

        bindText(mNameTv, item.getLineOneText());
        bindText(mDisplayTv, item.getLineTwoText());
        if (item.getUser() != null) {
            mNameTv.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //person info
                        }
                    }
            );
            mDisplayTv.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //person info
                        }
                    }
            );
        } else {
            mNameTv.setOnClickListener(null);
            mDisplayTv.setOnClickListener(null);
        }

        bindText(mTypeTv, item.getUpRightText());

        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        }
    }

    private void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item) {
        bindText(mCountTv, item.getCountString());
    }
}
