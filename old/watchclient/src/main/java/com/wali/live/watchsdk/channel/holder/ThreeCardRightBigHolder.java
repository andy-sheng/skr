package com.wali.live.watchsdk.channel.holder;

import android.view.View;

import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

/**
 * Created by zhaomin on 16-12-22.
 * @module 频道
 * @description 三个item 右边大 左边小 包括上下两个
 */
public class ThreeCardRightBigHolder extends ThreeCardLeftBigHolder {

    public ThreeCardRightBigHolder(View itemView) {
        super(itemView);
    }

    protected void bindImage(ChannelLiveViewModel.BaseItem item, int i) {
        int height = 0;
        int width = 0;
        if (i > 0 && i < 3) {
            height = DisplayUtils.dip2px(116);
            width = DisplayUtils.dip2px(116);
        } else if (i == 0) {
            width = DisplayUtils.getScreenWidth() - SIDE_MARGIN * 3 - DisplayUtils.dip2px(116);
            height = DisplayUtils.dip2px(305.33f);
        }
        bindImageWithBorder(mImageViews[i], item.getImageUrl(), isCircle(), width, height, getScaleType());
    }
}
