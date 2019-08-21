package com.module.feeds.watch.viewholder

import android.support.constraint.ConstraintLayout
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.ex.ExTextView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.watch.adapter.FeedCollectListener
import com.module.feeds.watch.model.FeedRecommendTagModel

class HeaderFeedsCollectViewHolder(item: View, listener: FeedCollectListener) : FeedsCollectViewHolder(item, listener) {
    private val topOne: SimpleDraweeView = itemView.findViewById(R.id.top_one)
    private val topTwo: SimpleDraweeView = itemView.findViewById(R.id.top_two)
    private val topThree: SimpleDraweeView = itemView.findViewById(R.id.top_three)
    private val topMore: SimpleDraweeView = itemView.findViewById(R.id.top_more)
    private val countTv: ExTextView = itemView.findViewById(R.id.count_tv)
    private val headerView: View = itemView.findViewById(R.id.include_view_header)
    private val contentView: View = itemView.findViewById(R.id.include_view_content)

    var modelList = ArrayList<FeedRecommendTagModel>()

    init {
        topOne.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if (view!!.visibility == View.VISIBLE) {
                    if (modelList.isNotEmpty() && modelList.size >= 1) {
                        toTagDetail(modelList[0])
                    }
                }
            }
        })

        topTwo.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if (modelList.isNotEmpty() && modelList.size >= 2) {
                    if (view!!.visibility == View.VISIBLE) {
                        toTagDetail(modelList[1])
                    }
                }
            }
        })

        topThree.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if (view!!.visibility == View.VISIBLE) {
                    if (modelList.isNotEmpty() && modelList.size >= 3) {
                        toTagDetail(modelList[2])
                    }
                }
            }
        })

        topMore.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if (view!!.visibility == View.VISIBLE) {
                    if (modelList.isNotEmpty() && modelList.size == 4) {
                        toTagDetail(modelList[3])
                    } else if (modelList.isNotEmpty() && modelList.size > 4) {
                        toMoreTag()
                    }
                }
            }
        })
    }

    fun toTagDetail(model: FeedRecommendTagModel) {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_TAG_DETAIL)
                .withSerializable("model", model)
                .navigation()
    }

    fun toMoreTag() {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_TAG)
                .withInt("from", 0)
                .navigation()
    }

    fun showContent(show: Boolean) {
        contentView.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun bindData(list: List<FeedRecommendTagModel>, count: Int) {
        countTv.text = "单曲（共${count}首）"

        modelList.clear()
        modelList.addAll(list)

        if (modelList.size > 0) {
            headerView.visibility = View.VISIBLE
            (countTv.layoutParams as ConstraintLayout.LayoutParams).topMargin = U.getDisplayUtils().dip2px(116f)
            (contentView.layoutParams as ConstraintLayout.LayoutParams).topMargin = U.getDisplayUtils().dip2px(143f)
        } else {
            (countTv.layoutParams as ConstraintLayout.LayoutParams).topMargin = U.getDisplayUtils().dip2px(5f)
            (contentView.layoutParams as ConstraintLayout.LayoutParams).topMargin = U.getDisplayUtils().dip2px(33f)
            headerView.visibility = View.GONE
        }

        if (modelList.isNotEmpty() && modelList.size >= 1) {
            topOne.visibility = View.VISIBLE
            FrescoWorker.loadImage(topOne, ImageFactory.newPathImage(modelList[0].smallImgURL)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .setCornerRadius(8.dp().toFloat())
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_160)
                    .build<BaseImage>())
        } else {
            topOne.visibility = View.INVISIBLE
        }

        if (modelList.isNotEmpty() && modelList.size >= 2) {
            topTwo.visibility = View.VISIBLE
            FrescoWorker.loadImage(topTwo, ImageFactory.newPathImage(modelList[1].smallImgURL)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .setCornerRadius(8.dp().toFloat())
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_160)
                    .build<BaseImage>())
        } else {
            topTwo.visibility = View.INVISIBLE
        }

        if (modelList.isNotEmpty() && modelList.size >= 3) {
            topThree.visibility = View.VISIBLE
            FrescoWorker.loadImage(topThree, ImageFactory.newPathImage(modelList[2].smallImgURL)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .setCornerRadius(8.dp().toFloat())
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_160)
                    .build<BaseImage>())
        } else {
            topThree.visibility = View.INVISIBLE
        }

        if (modelList.isNotEmpty() && modelList.size >= 4) {
            topMore.visibility = View.VISIBLE
            if (modelList.size == 4) {
                FrescoWorker.loadImage(topMore, ImageFactory.newPathImage(modelList[3].smallImgURL)
                        .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .setCornerRadius(8.dp().toFloat())
                        .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_160)
                        .build<BaseImage>())
            } else {
                FrescoWorker.loadImage(topMore, ImageFactory.newResImage(R.drawable.feed_recomend_more_icon)
                        .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                        .setCornerRadius(8.dp().toFloat())
                        .build<BaseImage>())
            }
        } else {
            topMore.visibility = View.INVISIBLE
        }
    }
}