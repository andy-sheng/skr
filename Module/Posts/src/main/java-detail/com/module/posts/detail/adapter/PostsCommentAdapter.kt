package com.module.posts.detail.adapter

import android.support.constraint.Barrier
import android.support.constraint.Group
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.component.busilib.view.AvatarView
import com.module.posts.R
import com.module.posts.view.ExpandTextView
import com.module.posts.view.PostsAudioView
import com.module.posts.view.PostsNineGridLayout

class PostsCommentAdapter : DiffAdapter<Any, RecyclerView.ViewHolder>() {
    private val mPostsType = 0
    private val mCommentType = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        when (viewType) {
            mPostsType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_detail_posts_type_layout, parent, false)
                return PostsHolder(view!!)
            }
            mCommentType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_comment_item_view_layout, parent, false)
                return PostsCommentHolder(view!!)
            }

            else -> return PostsHolder(view!!)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    inner class PostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var followTv: ExTextView
        var timeTv: TextView
        var nicknameTv: TextView
        var avatarIv: AvatarView
        var content: ExpandTextView
        var postsAudioView: PostsAudioView
        var nineGridVp: PostsNineGridLayout
        var postsBarrier: Barrier
        var redPkgDriver: View
        var redPkgBg: View
        var redPkgIv: ExImageView
        var coinTv: ExTextView
        var redPkgDes: ExTextView
        var redPkgGroup: Group
        var redPkgBarrier: Barrier
        var commentNumDivider: View
        var commentCtnTv: ExTextView
        var emptyTv: ExTextView

        init {
            followTv = itemView.findViewById(R.id.follow_tv)
            timeTv = itemView.findViewById(R.id.time_tv)
            nicknameTv = itemView.findViewById(R.id.nickname_tv)
            avatarIv = itemView.findViewById(R.id.avatar_iv)
            content = itemView.findViewById(R.id.content)
            postsAudioView = itemView.findViewById(R.id.posts_audio_view)
            nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
            postsBarrier = itemView.findViewById(R.id.posts_barrier)
            redPkgDriver = itemView.findViewById(R.id.red_pkg_driver)
            redPkgBg = itemView.findViewById(R.id.red_pkg_bg)
            redPkgIv = itemView.findViewById(R.id.red_pkg_iv)
            coinTv = itemView.findViewById(R.id.coin_tv)
            redPkgDes = itemView.findViewById(R.id.red_pkg_des)
            redPkgGroup = itemView.findViewById(R.id.red_pkg_group)
            redPkgBarrier = itemView.findViewById(R.id.red_pkg_barrier)
            commentNumDivider = itemView.findViewById(R.id.comment_num_divider)
            commentCtnTv = itemView.findViewById(R.id.comment_ctn_tv)
            emptyTv = itemView.findViewById(R.id.empty_tv)
        }

        fun bindData() {

        }
    }

    inner class PostsCommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var commenterAvaterIv: AvatarView
        var nameTv: ExTextView
        var commentTimeTv: ExTextView
        var xinIv: ExImageView
        var likeNum: ExTextView
        var contentTv: ExTextView
        var postsAudioView: PostsAudioView
        var nineGridVp: PostsNineGridLayout
        var postsBarrier: Barrier
        var replyNum: ExTextView
        var bottomBarrier: Barrier

        init {
            commenterAvaterIv = itemView.findViewById(R.id.commenter_avater_iv)
            nameTv = itemView.findViewById(R.id.name_tv)
            commentTimeTv = itemView.findViewById(R.id.comment_time_tv)
            xinIv = itemView.findViewById(R.id.xin_iv)
            likeNum = itemView.findViewById(R.id.like_num)
            contentTv = itemView.findViewById(R.id.content_tv)
            postsAudioView = itemView.findViewById(R.id.posts_audio_view)
            nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
            postsBarrier = itemView.findViewById(R.id.posts_barrier)
            replyNum = itemView.findViewById(R.id.reply_num)
            bottomBarrier = itemView.findViewById(R.id.bottom_barrier)
        }

        fun bindData() {

        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return mPostsType
        }

        return mCommentType
    }
}