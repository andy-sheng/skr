package com.wali.live.watchsdk.longtext.holder;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.BaseImage;
import com.base.image.fresco.image.ImageFactory;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.model.interior.item.CoverFeedItemModel;

/**
 * Created by lan on 2017/9/20.
 */
public class CoverFeedItemHolder extends BaseFeedItemHolder<CoverFeedItemModel> {
    private BaseImageView mCoverIv;

    public CoverFeedItemHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mCoverIv = $(R.id.cover_iv);
        adjustSize();
    }

    private void adjustSize() {
        int width = GlobalData.screenWidth;
        int height = GlobalData.screenWidth * 3 / 4;

        ViewGroup.LayoutParams lp = mCoverIv.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(width, height);
            mCoverIv.setLayoutParams(lp);
        } else {
            lp.width = width;
            lp.height = height;
        }
    }

    @Override
    protected void bindView() {
        BaseImage baseImage = ImageFactory.newHttpImage(mViewModel.getCoverUrl())
                .setLoadingDrawable(new ColorDrawable(itemView.getResources().getColor(R.color.color_f2f2f2)))
                .setWidth(GlobalData.screenWidth)
                .build();
        FrescoWorker.loadImage(mCoverIv, baseImage);
    }
}
