package com.module.home.game.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.banner.BannerImageLoader;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.game.model.BannerModel;
import com.module.home.model.SlideShowModel;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

public class BannerViewHolder extends RecyclerView.ViewHolder {

    Banner mBannerView;

    public BannerViewHolder(View itemView) {
        super(itemView);

        mBannerView = (Banner) itemView.findViewById(R.id.banner_view);
        mBannerView.setIndicatorGravity(BannerConfig.RIGHT);
    }

    public void bindData(BannerModel bannerModel) {
        mBannerView.setImages(getSlideUrlList(bannerModel.getSlideShowModelList()))
                .setImageLoader(new BannerImageLoader())
                .setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int position) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                                .withString("uri", bannerModel.getSlideShowModelList().get(position).getSchema())
                                .navigation();
                    }
                })
                .start();
    }

    private ArrayList<String> getSlideUrlList(List<SlideShowModel> slideShowModelList) {
        ArrayList<String> urlList = new ArrayList<>();
        for (SlideShowModel slideShowModel :
                slideShowModelList) {
            urlList.add(slideShowModel.getCoverURL());
        }

        return urlList;
    }
}
