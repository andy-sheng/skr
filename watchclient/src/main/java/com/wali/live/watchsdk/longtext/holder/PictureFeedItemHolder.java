package com.wali.live.watchsdk.longtext.holder;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.longtext.model.interior.item.PictureFeedItemModel;

/**
 * Created by lan on 2017/9/20.
 */
public class PictureFeedItemHolder extends BaseFeedItemHolder<PictureFeedItemModel> {
    public BaseImageView mItemIv;
    public TextView mDescTv;

    public PictureFeedItemHolder(View view) {
        super(view);
    }

    @Override
    protected void initView() {
        mItemIv = $(R.id.item_iv);
        mDescTv = $(R.id.desc_tv);
    }

    @Override
    protected void bindView() {
        int height;
        int width = DisplayUtils.getScreenWidth() - (DisplayUtils.dip2px(13.33f) << 1);
        if (mViewModel.getWidth() > 0 && mViewModel.getHeight() > 0) {
            height = width * mViewModel.getHeight() / mViewModel.getWidth();
        } else {
            height = DisplayUtils.getScreenWidth() >> 1;
        }

        ViewGroup.LayoutParams params = mItemIv.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(width, height);
        } else {
            params.width = width;
            params.height = height;
        }
        mItemIv.setLayoutParams(params);

        FrescoWorker.loadImage(mItemIv,
                ImageFactory.newHttpImage(mViewModel.getUrl())
                        .setLoadingDrawable(new ColorDrawable(itemView.getResources().getColor(R.color.color_f2f2f2)))
                        .setWidth(width)
                        .setHeight(height)
                        .build());

        bindText(mDescTv, mViewModel.getDesc());
    }
}
