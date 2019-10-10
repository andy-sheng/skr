package com.module.posts.detail.adapter

import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.view.recyclerview.DiffAdapter
import com.module.posts.R
import com.module.posts.detail.adapter.holder.PostsCommentHolder
import com.module.posts.detail.adapter.holder.PostsHolder
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.watch.model.PostsWatchModel

class PostsCommentAdapter : DiffAdapter<Any, RecyclerView.ViewHolder> {
    companion object {
        val REFRESH_COMMENT_CTN = 0
        val REFRESH_PLAY_STATE = 2
        val REFRESH_LIKE = 3
        val REFRESH_VOTE = 4
        val REFRESH_FOLLOW_STATE = 5
    }

    private val mPostsType = 0
    private val mCommentType = 1
    private var mPostsOwnerID = 0  // 帖子属于谁，楼主

    fun setPostsOwnerID(userID: Int) {
        this.mPostsOwnerID = userID
    }

    var mIDetailClickListener: IDetailClickListener? = null

    var mContext: FragmentActivity? = null

    constructor(context: FragmentActivity) : super() {
        mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        when (viewType) {
            mPostsType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_detail_posts_type_layout, parent, false)
                return PostsHolder(view!!, mIDetailClickListener!!)
            }
            mCommentType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_comment_item_view_layout, parent, false)
                return PostsCommentHolder(view!!, mIDetailClickListener!!, mPostsOwnerID)
            }

            else -> return PostsHolder(view!!, mIDetailClickListener!!)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            if (holder is PostsHolder) {
                holder.bindData(position, mDataList[position] as PostsWatchModel)

            } else if (holder is PostsCommentHolder) {
                holder.bindData(position, mDataList[position] as PostFirstLevelCommentModel, position == mDataList.size - 1)
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
        if (refreshType == REFRESH_COMMENT_CTN) {
            if (holder is PostsHolder) {
                holder.refreshCommentCnt(position, mDataList[position] as PostsWatchModel)
            } else if (holder is PostsCommentHolder) {
                holder.refreshReplyNum(position, mDataList[position] as PostFirstLevelCommentModel)
            }
        } else if (refreshType == REFRESH_PLAY_STATE) {
            if (holder is PostsHolder) {
                holder.refreshPlayState(position, mDataList[position] as PostsWatchModel)
            } else if (holder is PostsCommentHolder) {
                holder.refreshPlayState(position, mDataList[position] as PostFirstLevelCommentModel)
            }
        } else if (refreshType == REFRESH_LIKE) {
            if (holder is PostsHolder) {
                holder.refreshLike(position, mDataList[position] as PostsWatchModel)
            } else if (holder is PostsCommentHolder) {
                holder.refreshLike(position, mDataList[position] as PostFirstLevelCommentModel)
            }
        } else if (refreshType == REFRESH_VOTE) {
            (holder as PostsHolder).refreshVoteState(position, mDataList[position] as PostsWatchModel)
        } else if (refreshType == REFRESH_FOLLOW_STATE) {
            (holder as PostsHolder).refreshFollowState(position, mDataList[position] as PostsWatchModel)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PostsHolder) {
            holder.bindData(position, mDataList[position] as PostsWatchModel)
        } else if (holder is PostsCommentHolder) {
            holder.bindData(position, mDataList[position] as PostFirstLevelCommentModel, position == mDataList.size - 1)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return mPostsType
        }

        return mCommentType
    }

    interface IDetailClickListener {
        fun replayPosts(model: PostsWatchModel)

        fun showRedPkg(model: PostsWatchModel)

        fun likePosts(model: PostsWatchModel)

        fun clickFirstLevelComment()

        fun likeFirstLevelComment(model: PostFirstLevelCommentModel)

        fun getCurPlayingUrl(): String

        fun getCurPlayingPosition(): Int

        fun setCurPlayingUrl(url: String)

        fun setCurPlayintPosition(pos: Int)

        fun playAnotherSong()

        fun onClickPostsVote(position: Int, model: PostsWatchModel?, index: Int)

        fun getRelation(userID: Int)

        fun goSecondLevelCommetDetail(model: PostFirstLevelCommentModel, position: Int)

        fun goBigImageBrowse(index: Int, pictures: List<String>)

        fun clickContent(postFirstLevelModel: PostFirstLevelCommentModel, pos: Int)
    }
}