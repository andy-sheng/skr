package com.module.feeds.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.ImageUtils
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedRecommendTagModel

class FeedRecommendTagHolder(val rootView: View, listener: FeedsListener) : RecyclerView.ViewHolder(rootView) {

    private val topOne: SimpleDraweeView = rootView.findViewById(R.id.top_one)
    private val topTwo: SimpleDraweeView = rootView.findViewById(R.id.top_two)
    private val topThree: SimpleDraweeView = rootView.findViewById(R.id.top_three)
    private val topMore: SimpleDraweeView = rootView.findViewById(R.id.top_more)

    var modelList = ArrayList<FeedRecommendTagModel>()

    init {
        topOne.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if (modelList.isNotEmpty() && modelList.size >= 1) {
                    listener.onClickRecommendTag(modelList[0])
                }
            }
        })

        topTwo.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if (modelList.isNotEmpty() && modelList.size >= 2) {
                    listener.onClickRecommendTag(modelList[1])
                }
            }

        })

        topThree.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if (modelList.isNotEmpty() && modelList.size >= 3) {
                    listener.onClickRecommendTag(modelList[2])
                }
            }
        })

        topMore.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener.onClickRecommendTagMore()
            }
        })

    }

    fun bindData(list: List<FeedRecommendTagModel>) {
        modelList.clear()
        modelList.addAll(list)

        if (modelList.isNotEmpty() && modelList.size >= 1) {
            topOne.visibility = View.VISIBLE
            FrescoWorker.loadImage(topOne, ImageFactory.newPathImage(modelList[0].smallImgURL)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .setCornerRadius(8.dp().toFloat())
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_160)
                    .build<BaseImage>())
        } else {
            topOne.visibility = View.GONE
        }

        if (modelList.isNotEmpty() && modelList.size >= 2) {
            topTwo.visibility = View.VISIBLE
            FrescoWorker.loadImage(topTwo, ImageFactory.newPathImage(modelList[1].smallImgURL)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .setCornerRadius(8.dp().toFloat())
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_160)
                    .build<BaseImage>())
        } else {
            topTwo.visibility = View.GONE
        }

        if (modelList.isNotEmpty() && modelList.size >= 3) {
            topThree.visibility = View.VISIBLE
            FrescoWorker.loadImage(topThree, ImageFactory.newPathImage(modelList[2].smallImgURL)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .setCornerRadius(8.dp().toFloat())
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_160)
                    .build<BaseImage>())
        } else {
            topThree.visibility = View.GONE
        }

        FrescoWorker.loadImage(topMore, ImageFactory.newResImage(R.drawable.feed_recomend_more_icon)
                .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                .setCornerRadius(8.dp().toFloat())
                .build<BaseImage>())
    }
}