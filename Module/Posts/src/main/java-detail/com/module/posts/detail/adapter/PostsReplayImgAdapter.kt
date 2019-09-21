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
import com.respicker.model.ImageItem

class PostsReplayImgAdapter : DiffAdapter<ImageItem, RecyclerView.ViewHolder>() {


    var delClickListener: ((model: ImageItem?, pos: Int) -> Unit)? = null
    var imgClickListener: ((model: ImageItem?, pos: Int) -> Unit)? = null

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
        var pos: Int = 0
        var model: ImageItem? = null

        init {
            imgIv = itemView.findViewById(R.id.img_iv)
            deleteIv = itemView.findViewById(R.id.delete_iv)
            imgIv.setDebounceViewClickListener {
                imgClickListener?.invoke(model,pos)
            }
            deleteIv.setDebounceViewClickListener {
                delClickListener?.invoke(model,pos)
            }
        }

        fun bindData(pos: Int, model: ImageItem) {
            this.model = model
            this.pos = pos
            FrescoWorker.loadImage(imgIv,
                    ImageFactory.newPathImage(model.path)
                            .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                            .setFailureDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.load_img_error))
                            .setLoadingDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.loading_place_holder_img))
                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.w).build())
                            .setBorderColor(Color.parseColor("#3B4E79")).build())
        }
    }
}