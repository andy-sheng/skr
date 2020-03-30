package com.module.posts.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.module.common.IBooleanCallback
import com.module.posts.watch.adapter.PostsWatchListener
import com.module.posts.watch.view.DynamicRoomView

class DynamicPostsViewHolder(var item: View, val listener: PostsWatchListener) : RecyclerView.ViewHolder(item) {


    fun cancel() {
        if (itemView is DynamicRoomView) {
            itemView.destroy()
        }
    }


    fun loadData(clubId: Int, callback: IBooleanCallback) {
        if (itemView is DynamicRoomView) {
            itemView.loadData(clubId, callback)
        }
    }

    fun loadMoreData(callback: IBooleanCallback) {
        if (itemView is DynamicRoomView) {
            itemView.loadMoreData(callback)
        }
    }


}