package com.wali.live.modulechannel.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.model.ImageFactory;
import com.common.utils.U;
import com.facebook.drawee.drawable.ScalingUtils;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.viewmodel.ChannelLiveViewModel;

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
                lp.height = (U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(6.66f))* 460 / 1060;
            }else {
                lp.height = (U.getDisplayUtils().getScreenWidth() - U.getDisplayUtils().dip2px(6.66f))* viewModel.getItemDatas().get(0).getHeight() / viewModel.getItemDatas().get(0).getWidth();
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
