package com.module.posts.detail.adapter

import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.view.recyclerview.DiffAdapter
import com.module.posts.R
import com.module.posts.detail.adapter.holder.PostsFirstLevelCommentHolder
import com.module.posts.detail.adapter.holder.PostsSecondLevelCommentHolder
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.model.PostsSecondLevelCommentModel

class PostsCommentDetailAdapter : DiffAdapter<Any, RecyclerView.ViewHolder> {
    companion object {
        val REFRESH_COMMENT_CTN = 0
        val REFRESH_PLAY_STATE = 2
    }

    val mPostsType = 0
    val mCommentType = 1

    var mContext: FragmentActivity? = null
    var mPostsOwnerID: Int? = null  // 帖子属于谁

    var mIDetailClickListener: ICommentDetailClickListener? = null

    constructor(context: FragmentActivity, postsOwnerID: Int?) : super() {
        mContext = context
        mPostsOwnerID = postsOwnerID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        when (viewType) {
            mPostsType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_comment_detail_posts_type_layout, parent, false)
                return PostsFirstLevelCommentHolder(view!!, mIDetailClickListener!!)
            }
            mCommentType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_comment_detail_item_view_layout, parent, false)
                return PostsSecondLevelCommentHolder(view!!, mIDetailClickListener!!, mPostsOwnerID!!)
            }

            else -> return PostsFirstLevelCommentHolder(view!!, mIDetailClickListener!!)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            if (holder is PostsFirstLevelCommentHolder) {
                holder.bindData(position, mDataList[position] as PostFirstLevelCommentModel)

            } else if (holder is PostsSecondLevelCommentHolder) {
                holder.bindData(position, mDataList[position] as PostsSecondLevelCommentModel, position == mDataList.size - 1)
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

    private fun refreshHolder(holder: RecyclerView.ViewHolder, position: Int, refreshType: Int) {
        if (refreshType == REFRESH_PLAY_STATE) {
            if (holder is PostsFirstLevelCommentHolder) {
                holder.refreshPlayState(position, mDataList[position] as PostFirstLevelCommentModel)
            } else if (holder is PostsSecondLevelCommentHolder) {
                holder.refreshPlayState(position, mDataList[position] as PostsSecondLevelCommentModel)
            }
        } else if (refreshType == REFRESH_COMMENT_CTN) {
            (holder as PostsFirstLevelCommentHolder).refreshCtn(position, mDataList[position] as PostFirstLevelCommentModel)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return mPostsType
        }

        return mCommentType
    }

    interface ICommentDetailClickListener {
        fun getCurPlayingUrl(): String

        fun getCurPlayingPosition(): Int

        fun setCurPlayingUrl(url: String)

        fun setCurPlayingPosition(pos: Int)

        fun clickSecondLevelCommentContent(model: PostsSecondLevelCommentModel, pos: Int)

        fun playAnotherSong()

        fun goBigImageBrowse(index: Int, pictures: List<String>)
    }
}