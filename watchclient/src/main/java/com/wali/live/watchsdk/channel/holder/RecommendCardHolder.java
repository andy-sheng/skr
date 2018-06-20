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
    protected void initView() {
        super.initView();
        mTitleTv = $(R.id.title_tv);
        mAnchorIv = $(R.id.anchor_iv);
    }

    @Override
    protected void bindView() {
        super.bindView();
        resetItem();
        ViewGroup.LayoutParams lp = mBannerView.getLayoutParams();
        if (mViewModel.getItemDatas() != null && mViewModel.getItemDatas().size() > 0) {
            if(mViewModel.getItemDatas().get(0).getWidth() == 0){
                lp.height = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(6.66f))* 460 / 1060;
            }else {
                lp.height = (DisplayUtils.getScreenWidth() - DisplayUtils.dip2px(6.66f))* mViewModel.getItemDatas().get(0).getHeight() / mViewModel.getItemDatas().get(0).getWidth();
            }
            if (!TextUtils.isEmpty(mViewModel.getItemDatas().get(0).getLineOneText())) {
                mTitleTv.setText(mViewModel.getItemDatas().get(0)
                        .getLineOneText());
            }
            if (mViewModel.getItemDatas().get(0).getWidgetInfo() != null && !TextUtils.isEmpty(mViewModel.getItemDatas().get(0).getWidgetInfo().getIconUrl())) {
                mAnchorIv.setVisibility(View.VISIBLE);
                FrescoWorker.loadImage(mAnchorIv,
                        ImageFactory.newHttpImage(mViewModel.getItemDatas().get(0).getWidgetInfo().getIconUrl())
                                .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                                .build());
                mAnchorIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mJumpListener != null) {
                            mJumpListener.jumpScheme(mViewModel.getItemDatas().get(0).getSchemeUri());
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
