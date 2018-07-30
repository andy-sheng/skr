package com.wali.live.watchsdk.channel.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.utils.display.DisplayUtils;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.viewmodel.ChannelLiveViewModel;

/**
 * Created by fengminchao on 17/9/4
 */

public class RecommendCardHolder extends BaseBannerHolder{

    private TextView mTitleTv;
    private BaseImageView mAnchorIv;

    public RecommendCardHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mTitleTv = $(R.id.title_tv);
        mAnchorIv = $(R.id.anchor_iv);
    }

    @Override
    protected void bindLiveModel(final ChannelLiveViewModel viewModel) {
        super.bindLiveModel(viewModel);
        resetItem();
        ViewGroup.LayoutParams lp = mBannerView.getLayoutParams();
        if (viewModel.getItemDatas() != null && viewModel.getItemDatas().size() > 0) {
            if(viewModel.getItemDatas().get(0).getWidth() == 0){
                lp.height = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(6.66f))* 460 / 1060;
            }else {
                lp.height = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(6.66f))* viewModel.getItemDatas().get(0).getHeight() / viewModel.getItemDatas().get(0).getWidth();
            }
            if (!TextUtils.isEmpty(viewModel.getItemDatas().get(0).getLineOneText())) {
                mTitleTv.setText(viewModel.getItemDatas().get(0)
                        .getLineOneText());
            }
            if (viewModel.getItemDatas().get(0).getWidgetInfo() != null && !TextUtils.isEmpty(viewModel.getItemDatas().get(0).getWidgetInfo().getIconUrl())) {
                mAnchorIv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mAnchorIv,
                        ImageFactory.newHttpImage(viewModel.getItemDatas().get(0).getWidgetInfo().getIconUrl())
                                .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                                .build());
                mAnchorIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mJumpListener != null) {
                            mJumpListener.jumpScheme(viewModel.getItemDatas().get(0).getSchemeUri());
                        }
                    }
                });
            }
        }
    }

    private void resetItem() {
        mAnchorIv.setVisibility(View.GONE);
    }
}
