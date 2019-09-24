package com.module.posts.publish.topic

import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.posts.R
import java.io.Serializable

class PostsTopicClassifyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selectListener: ((model: Category?) -> Unit)? = null
    var dataList = ArrayList<Category>()
    var selectModel: Category? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_publish_item_category_layout, parent, false)
        return CategoryItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = dataList.get(position)
        val reportItemHolder = holder as CategoryItemHolder
        reportItemHolder.bindData(model)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class CategoryItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var contentTv: ExTextView
        lateinit var tagView: View

        init {
            contentTv = itemView.findViewById(R.id.content_tv)
            tagView = itemView.findViewById(R.id.tag_view)
            itemView.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    if (selectModel != model) {
                        selectListener?.invoke(model)
                    }
                }
            })
        }

        var model: Category? = null
        fun bindData(model: Category) {
            this.model = model
            contentTv.text = this.model?.categoryDesc
            if (this.model == selectModel) {
                tagView.visibility = View.VISIBLE
                contentTv.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                itemView.setBackgroundColor(Color.WHITE)
            } else {
                tagView.visibility = View.GONE
                contentTv.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                itemView.setBackgroundColor(Color.parseColor("#f2f2f2"))
            }
        }
    }

}

