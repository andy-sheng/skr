package com.module.home.game.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View

import com.alibaba.android.arouter.launcher.ARouter
import com.common.banner.BannerImageLoader
import com.common.statistics.StatisticsAdapter
import com.module.RouterConstants
import com.module.home.R
import com.module.home.game.model.BannerModel
import com.module.home.model.SlideShowModel
import com.youth.banner.Banner
import com.youth.banner.BannerConfig
import com.youth.banner.listener.OnBannerListener

import java.util.ArrayList

class BannerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var mBannerView: Banner = itemView.findViewById(R.id.banner_view)

    init {
        mBannerView.setIndicatorGravity(BannerConfig.RIGHT)
    }

    fun bindData(bannerModel: BannerModel) {
        mBannerView.setImages(getSlideUrlList(bannerModel.slideShowModelList))
                .setImageLoader(BannerImageLoader())
                .setOnBannerListener { position ->
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                            .withString("uri", bannerModel.slideShowModelList[position].schema)
                            .navigation()
                    StatisticsAdapter.recordCountEvent("game","express_banner",null)
                }
                .start()
    }

    private fun getSlideUrlList(slideShowModelList: List<SlideShowModel>): ArrayList<String> {
        val urlList = ArrayList<String>()
        for (slideShowModel in slideShowModelList) {
            urlList.add(slideShowModel.coverURL)
        }

        return urlList
    }
}
