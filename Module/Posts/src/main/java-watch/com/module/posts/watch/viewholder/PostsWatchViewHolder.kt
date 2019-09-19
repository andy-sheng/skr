package com.module.posts.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.view.ExpandTextView
import com.module.posts.view.PostsNineGridLayout
import com.module.posts.view.PostsVoteGroupView
import com.module.posts.view.PostsWatchCommentView


// posts_watch_view_item_layout
class PostsWatchViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    val nicknameTv: TextView = item.findViewById(R.id.nickname_tv)
    val timeTv: TextView = item.findViewById(R.id.time_tv)
    val moreIv: ImageView = item.findViewById(R.id.more_iv)
    val nineGridVp: PostsNineGridLayout = item.findViewById(R.id.nine_grid_vp)
    val content: ExpandTextView = item.findViewById(R.id.content)
    val commentView: PostsWatchCommentView = PostsWatchCommentView(item.findViewById(R.id.comment_layout_stub))
    val voteGroupView: PostsVoteGroupView = PostsVoteGroupView(item.findViewById(R.id.vote_layout_stub))

    val postsLikeTv: TextView = item.findViewById(R.id.posts_like_tv)
    val postsCommentTv: TextView = item.findViewById(R.id.posts_comment_tv)
    val redPkgIv: ImageView = item.findViewById(R.id.red_pkg_iv)
    val topicTv: ExTextView = item.findViewById(R.id.topic_tv)

    var pos = -1
    var mModel: PostsWatchModel? = null

    var imageClickListener: ((pos: Int, model: PostsWatchModel?, index: Int, url: String?) -> Unit)? = null

    init {
        nineGridVp.clickListener = { i, url, _ ->
            imageClickListener?.invoke(pos, mModel, i, url)
        }
        content.setListener(object : ExpandTextView.ExpandListener {
            override fun onClickExpand(isExpand: Boolean) {
                mModel?.isExpend = isExpand
            }
        })
    }

    fun bindData(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model

        mModel?.user?.let {
            avatarIv.bindData(it)
            nicknameTv.text = it.nicknameRemark
        }
        mModel?.posts?.let {
            timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(it.createdAt, System.currentTimeMillis())

            content.initWidth(U.getDisplayUtils().screenWidth - 20.dp())
            content.maxLines = 3
            if (!model.isExpend) {
                content.setCloseText(it.title)
            } else {
                content.setExpandText(it.title)
            }
        }

        mModel?.getImageList()?.let {
            nineGridVp.visibility = View.VISIBLE
            nineGridVp.setUrlList(it)
        }
        if (mModel?.getImageList().isNullOrEmpty()) {
            nineGridVp.visibility = View.GONE
        }

        commentView.bindData("")
        voteGroupView.bindData(false)
    }
}