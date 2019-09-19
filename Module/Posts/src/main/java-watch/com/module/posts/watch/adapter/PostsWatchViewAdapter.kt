package com.module.posts.watch.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.watch.viewholder.PostsWatchViewHolder
import java.util.*

class PostsWatchViewAdapter(val listener: PostsWatchListener) : RecyclerView.Adapter<PostsWatchViewHolder>() {
    var mDataList = ArrayList<PostsWatchModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsWatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_watch_view_item_layout, parent, false)
        return PostsWatchViewHolder(view, listener)
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

interface PostsWatchListener {
    fun onClickPostsAvatar(position: Int, model: PostsWatchModel?)  // 发帖头像
    fun onClickPostsMore(position: Int, model: PostsWatchModel?)    // 发帖更多
    fun onClickPostsAudio(position: Int, model: PostsWatchModel?)   // 发帖音频
    fun onClickPostsImage(position: Int, model: PostsWatchModel?, index: Int, url: String?)   // 发帖图片

    fun onClickPostsRedPkg(position: Int, model: PostsWatchModel?)  // 发帖红包
    fun onClickPostsTopic(position: Int, model: PostsWatchModel?)   // 发帖话题
    fun onClickPostsComment(position: Int, model: PostsWatchModel?) // 发帖评论
    fun onClickPostsLike(position: Int, model: PostsWatchModel?)    // 发帖点赞
    fun onClickPostsVote(position: Int, model: PostsWatchModel?, index: Int)  // 发帖投票

    fun onClickCommentAvatar(position: Int, model: PostsWatchModel?)  // 发帖精彩评论昵称
    fun onClickCommentLike(position: Int, model: PostsWatchModel?)    // 发帖精彩评论点赞
    fun onClickCommentAudio(position: Int, model: PostsWatchModel?)   // 发帖精彩评论音频
    fun onClickCommentImage(position: Int, model: PostsWatchModel?, index: Int, url: String?)   // 发帖精彩评论图片

}