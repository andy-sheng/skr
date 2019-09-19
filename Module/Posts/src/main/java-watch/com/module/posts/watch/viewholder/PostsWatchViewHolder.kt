package com.module.posts.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.module.posts.R
import com.module.posts.view.*
import com.module.posts.watch.model.PostsRedPkgModel
import com.module.posts.watch.model.PostsWatchModel
import kotlinx.android.synthetic.main.post_vote_item_layout.view.*


// posts_watch_view_item_layout
class PostsWatchViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    val nicknameTv: TextView = item.findViewById(R.id.nickname_tv)
    val timeTv: TextView = item.findViewById(R.id.time_tv)
    val moreIv: ImageView = item.findViewById(R.id.more_iv)
    val content: ExpandTextView = item.findViewById(R.id.content)

    val postsAudioView: PostsAudioView = item.findViewById(R.id.posts_audio_view)
    val nineGridVp: PostsNineGridLayout = item.findViewById(R.id.nine_grid_vp)

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

        if (mModel?.user != null) {
            avatarIv.bindData(mModel?.user!!)
            nicknameTv.text = mModel?.user?.nicknameRemark
        } else {
            MyLog.e("PostsWatchViewHolder", "bindData error pos = $pos, model = $model")
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

            // 红包
            if (it.redpacketInfo == null) {
                redPkgIv.visibility = View.GONE
            } else {
                redPkgIv.visibility = View.VISIBLE
                if (it.redpacketInfo?.openStatus == PostsRedPkgModel.ROS_HAS_OPEN) {
                    redPkgIv.setImageResource(R.drawable.posts_red_s_open_icon)
                } else {
                    redPkgIv.setImageResource(R.drawable.posts_red_s_close_icon)
                }
            }

            // 话题
            if (it.topicInfo == null || TextUtils.isEmpty(it.topicInfo?.topicDesc)) {
                topicTv.visibility = View.GONE
            } else {
                topicTv.visibility = View.VISIBLE
                topicTv.text = it.topicInfo?.topicDesc
            }
        }

        // 音频
        if (mModel?.posts?.audios.isNullOrEmpty()) {
            postsAudioView.visibility = View.GONE
        } else {
            postsAudioView.visibility = View.VISIBLE
            postsAudioView.bindData(mModel?.posts?.audios!!)
        }

        // 图片
        if (mModel?.posts?.pictures.isNullOrEmpty()) {
            nineGridVp.visibility = View.GONE
        } else {
            nineGridVp.visibility = View.VISIBLE
            nineGridVp.setUrlList(mModel?.posts?.pictures!!)
        }

        // 评论区
        if (mModel?.bestComment == null) {
            commentView.setVisibility(View.GONE)
        } else {
            commentView.setVisibility(View.VISIBLE)
            commentView.bindData(mModel?.bestComment!!)
        }

        // 投票
        if (mModel?.posts?.voteInfo == null) {
            voteGroupView.setVisibility(View.GONE)
        } else {
            voteGroupView.setVisibility(View.VISIBLE)
            voteGroupView.bindData(mModel?.posts?.voteInfo!!)
        }


        // 评论数和点赞数
        if (mModel?.numeric == null) {
            postsCommentTv.text = mModel?.numeric?.commentCnt.toString()
            postsLikeTv.text = mModel?.numeric?.starCnt.toString()
        } else {
            postsCommentTv.text = "0"
            postsLikeTv.text = "0"
        }
    }
}