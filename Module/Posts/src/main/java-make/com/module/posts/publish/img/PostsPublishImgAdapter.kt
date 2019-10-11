package com.module.posts.publish.img

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.dp
import com.common.view.recyclerview.DiffAdapter
import com.module.posts.R
import com.respicker.model.ImageItem

class PostsPublishImgAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val dataList = ArrayList<ImageItem>()

    var addClickListener: (() -> Unit)? = null
    var delClickListener: ((model: ImageItem?, pos: Int) -> Unit)? = null
    var imgClickListener: ((model: ImageItem?, pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_publish_item_show_image_layout, parent, false)
            return ImageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_publish_item_add_image_layout, parent, false)
            view.setOnClickListener {
                addClickListener?.invoke()
            }
            return AddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            if (holder is ImageViewHolder) {
                holder.bindData(dataList[position], position)
            } else if (holder is AddViewHolder) {

            }
        } else {
            // 局部刷新
            payloads.forEach {
                if (it is Int) {
                    refreshHolder(holder, position, it)
                }
            }
        }
    }

    private fun refreshHolder(holder: RecyclerView.ViewHolder, position: Int, type: Int) {
        if (type == DiffAdapter.REFRESH_POS) {
            if (holder is ImageViewHolder) {
                holder.refreshPos(dataList[position], position)
            } else if (holder is AddViewHolder) {

            }
        }
    }

    override fun getItemCount(): Int {
        if (dataList.size < 9) {
            return dataList.size + 1
        } else {
            return dataList.size
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < dataList.size) {
            return 1
        } else {
            return 2
        }
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val imgIv: BaseImageView
        val delIv: ImageView
        var model: ImageItem? = null
        var pos: Int = 0

        init {
            imgIv = view.findViewById(R.id.img_iv)
            delIv = view.findViewById(R.id.del_iv)
            delIv.setOnClickListener {
                delClickListener?.invoke(model, pos)
            }
            imgIv.setOnClickListener {
                imgClickListener?.invoke(model, pos)
            }
        }

        fun bindData(model: ImageItem, pos: Int) {
            refreshPos(model, pos)
            FrescoWorker.loadImage(imgIv, ImageFactory.newPathImage(this.model?.path)
                    .setCornerRadius(8.dp().toFloat())
                    .build())
        }

        fun refreshPos(imageItem: ImageItem, position: Int) {
            this.model = imageItem
            this.pos = position
        }

    }

    inner class AddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }
}

