package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

/**
 * Created by zhaomin on 16-12-22.
 *
 * @module 频道
 * @description 和LargeCardHolder 类似，顶部title浮在图片上
 */
public class LargeCardFloatHeaderHolder extends FixedHolder {


    private BaseImageView mCover;
    private BaseImageView mCoverSecond;

    private TextView mTextView;
    private TextView mCountTv;

    public LargeCardFloatHeaderHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        mCover = $(R.id.cover_iv);
        mCoverSecond = $(R.id.cover_iv_second);
        mTextView = $(R.id.name_tv);
        mCountTv = $(R.id.count_tv);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel viewModel) {
        final ChannelLiveViewModel.BaseItem item = viewModel.getFirstItem();
        if (item == null) {
            return;
        }
        int width = DisplayUtils.getScreenWidth() - SIDE_MARGIN * 2;
        int height = (int) (width * 0.5625f);   // 控件保持16 : 9 的比例
        mCover.getLayoutParams().height = height;
        bindImageWithBorder(mCover, item.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_XLARGE), false, width, height, ScalingUtils.ScaleType.CENTER_CROP);
        height = (int) (width * 0.6851f);  // cover 比例 是16 : 9 + title的高度（130） 比例是 0.6851
        mCoverSecond.getLayoutParams().height = height;
        if (!TextUtils.isEmpty(item.getImgUrl2())) {
            bindImageWithBorder(mCoverSecond, item.getImgUrl2(), false, width, height, ScalingUtils.ScaleType.CENTER_INSIDE);
        }
        mCoverSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpItem(item);
            }
        });

        bindText(mTextView, item.getLineOneText());
        if (item.getUser() != null) {
            mTextView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //跳转到个人主页
                        }
                    }
            );
        } else {
            mTextView.setOnClickListener(null);
        }

        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            mCountTv.setVisibility(View.VISIBLE);
            mCountTv.setText(((ChannelLiveViewModel.BaseLiveItem) item).getCountString());
        }
    }
}
