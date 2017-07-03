package com.wali.live.watchsdk.channel.holder;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.log.MyLog;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;

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
        return (int) ((GlobalData.screenWidth - SIDE_MARGIN * 2) * IMAGE_RATIO);
    }

    @Override
    protected void bindLiveModel(ChannelLiveViewModel model) {
        final ChannelLiveViewModel.BaseItem item = model.getFirstItem();
        if (item == null) {
            return;
        }
        bindImageWithBorder(mAvatarIv, item.getImageUrl(AvatarUtils.SIZE_TYPE_AVATAR_XLARGE),
                false, 640, 640, ScalingUtils.ScaleType.CENTER_CROP);
        mAvatarIv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SchemeSdkActivity.openActivity((BaseSdkActivity) itemView.getContext(),
                                Uri.parse(item.getSchemeUri()));
                    }
                });

        bindText(mNameTv, item.getLineOneText());
        bindText(mDisplayTv, item.getLineTwoText());
        bindText(mTypeTv, item.getUpRightText());
        if (item instanceof ChannelLiveViewModel.BaseLiveItem) {
            bindBaseLiveItem((ChannelLiveViewModel.BaseLiveItem) item);
        }
    }

    private void bindBaseLiveItem(ChannelLiveViewModel.BaseLiveItem item) {
        MyLog.w(TAG, "item.getCountString()= " + item.getCountString());
        bindText(mCountTv, item.getCountString());
    }
}
