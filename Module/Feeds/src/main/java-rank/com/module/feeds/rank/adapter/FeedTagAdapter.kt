package com.module.feeds.rank.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.watch.model.FeedRecommendTagModel

class FeedTagAdapter : RecyclerView.Adapter<FeedTagAdapter.FeedTagHolder>() {

    var mDataList = ArrayList<FeedRecommendTagModel>()
    var onClickTagListener: ((position: Int, model: FeedRecommendTagModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedTagHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_tag_item_layout, parent, false)
        return FeedTagHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedTagHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class FeedTagHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mTagIcon: SimpleDraweeView = itemView.findViewById(R.id.tag_icon)

        var mPosition = 0
        var mModel: FeedRecommendTagModel? = null

        init {
            mTagIcon.setOnClickListener(object : AnimateClickListener() {
                override fun click(view: View?) {
                    onClickTagListener?.invoke(mPosition, mModel)
                }
            })
        }

        fun bindData(pos: Int, model: FeedRecommendTagModel?) {
            this.mPosition = pos
            this.mModel = model

            FrescoWorker.loadImage(mTagIcon, ImageFactory.newPathImage(model?.smallImgURL)
                    .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                    .setCornerRadius(8.dp().toFloat())
                    .build<BaseImage>())
        }
    }

}

