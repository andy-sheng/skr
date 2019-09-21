package com.module.posts.publish.topic

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.ImageUtils
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.posts.R

class PostsTopicListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectListener: ((model:Topic?)->Unit)?=null

    var dataList = ArrayList<Topic>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_publish_item_topic_layout, parent, false)
        return TopicItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = dataList.get(position)
        val holder = holder as TopicItemHolder
        holder.bindData(model)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class TopicItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var coverIv: BaseImageView
        var titleTv: ExTextView
        var descTv: ExTextView

        init {
            coverIv = itemView.findViewById(R.id.cover_iv)
            titleTv = itemView.findViewById(R.id.title_tv)
            descTv = itemView.findViewById(R.id.desc_tv)
            itemView.setOnClickListener(object:DebounceViewClickListener(){
                override fun clickValid(v: View?) {
                    selectListener?.invoke(model)
                }

            })
        }

        var model: Topic? = null
        fun bindData(model: Topic) {
            this.model = model
            titleTv.text = this.model?.topicTitle
            descTv.text = this.model?.topicDesc
            FrescoWorker.loadImage(coverIv, ImageFactory.newPathImage(this.model?.topicURL)
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_320)
                    .setCornerRadius(8.dp().toFloat())
                    .build()
            )
        }
    }

}

