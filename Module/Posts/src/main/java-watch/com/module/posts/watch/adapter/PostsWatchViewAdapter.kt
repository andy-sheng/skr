package com.module.posts.watch.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.watch.viewholder.PostsWatchViewHolder
import java.util.*

class PostsWatchViewAdapter : RecyclerView.Adapter<PostsWatchViewHolder>() {
    var mDataList = ArrayList<PostsWatchModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsWatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_watch_view_item_layout, parent, false)
        return PostsWatchViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PostsWatchViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: PostsWatchViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bindData(position, mDataList[position])
        } else {
            // 局部刷新
            payloads.forEach {

            }
        }
    }
}