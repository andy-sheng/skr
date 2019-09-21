package com.module.posts.detail.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.image.model.oss.OssImgFactory
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.recyclerview.DiffAdapter
import com.module.posts.R

class PostsReplayImgAdapter : DiffAdapter<String, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_replay_select_img_item_view, parent, false)
        return PostsReplayImgHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PostsReplayImgHolder).bindData(position, dataList[position])
    }

    inner class PostsReplayImgHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgIv: BaseImageView
        var deleteIv: ExImageView
        var pos: Int? = null

        init {
            imgIv = itemView.findViewById(R.id.img_iv)
            deleteIv = itemView.findViewById(R.id.delete_iv)

            deleteIv.setDebounceViewClickListener {
                pos?.let {
                    dataList.removeAt(it)
                    notifyDataSetChanged()
                }
            }
        }

        fun bindData(pos: Int, url: String) {
            this.pos = pos
            FrescoWorker.loadImage(imgIv,
                    ImageFactory.newPathImage(url)
                            .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                            .setFailureDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.load_img_error))
                            .setLoadingDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.loading_place_holder_img))
                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.w).build())
                            .setBorderColor(Color.parseColor("#3B4E79")).build())
        }
    }
}