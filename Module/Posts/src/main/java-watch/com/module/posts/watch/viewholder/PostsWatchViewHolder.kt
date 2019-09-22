package com.module.posts.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.view.*
import com.module.posts.watch.adapter.PostsWatchListener
import com.module.posts.watch.adapter.PostsWatchViewAdapter
import com.module.posts.watch.model.PostsRedPkgModel
import com.module.posts.watch.model.PostsWatchModel


// posts_watch_view_item_layout
class PostsWatchViewHolder(item: View, val listener: PostsWatchListener) : RecyclerView.ViewHolder(item) {

    val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    val nicknameTv: TextView = item.findViewById(R.id.nickname_tv)
    val timeTv: TextView = item.findViewById(R.id.time_tv)
    val moreIv: ImageView = item.findViewById(R.id.more_iv)
    val content: ExpandTextView = item.findViewById(R.id.content)

    val postsSongView: PostsSongView = item.findViewById(R.id.posts_song_view)
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
    var isPlaying = false

    init {
        avatarIv.setDebounceViewClickListener { listener.onClickCommentAvatar(pos, mModel) }
        moreIv.setDebounceViewClickListener { listener.onClickPostsMore(pos, mModel) }
        postsAudioView.setDebounceViewClickListener { listener.onClickPostsAudio(pos, mModel, isPlaying) }
        postsSongView.setDebounceViewClickListener { listener.onClickPostsSong(pos, mModel, isPlaying) }

        nineGridVp.clickListener = { i, url, _ ->
            listener.onClickCommentImage(pos, mModel, i, url)
        }

        redPkgIv.setAnimateDebounceViewClickListener { listener.onClickPostsRedPkg(pos, mModel) }
        topicTv.setAnimateDebounceViewClickListener { listener.onClickPostsTopic(pos, mModel) }
        postsLikeTv.setAnimateDebounceViewClickListener { listener.onClickPostsLike(pos, mModel) }
        postsCommentTv.setAnimateDebounceViewClickListener { listener.onClickPostsComment(pos, mModel) }

        commentView.setListener(object : PostsCommentListener {
            override fun onClickSong() {
                listener.onClickCommentSong(pos, mModel, isPlaying)
            }

            override fun onClickLike() {
                listener.onClickCommentLike(pos, mModel)
            }

            override fun onClickName() {
                listener.onClickCommentAvatar(pos, mModel)
            }

            override fun onClickAudio() {
                listener.onClickCommentAudio(pos, mModel, isPlaying)
            }

            override fun onClickImage(index: Int, url: String) {
                listener.onClickCommentImage(pos, mModel, index, url)
            }
        })

        voteGroupView.clickListener = {
            listener.onClickPostsVote(pos, mModel, it)
        }

        content.setListener(object : ExpandTextView.ExpandListener {
            override fun onClickExpand(isExpand: Boolean) {
                mModel?.isExpend = isExpand
            }
        })

        itemView.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_DETAIL)
                    .withSerializable("model", mModel)
                    .navigation()
        }
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
            postsAudioView.bindData(mModel?.posts?.audios!![0].duration)
        }

        // 歌曲
        if (mModel?.posts?.song == null) {
            postsSongView.visibility = View.GONE
        } else {
            postsSongView.visibility = View.VISIBLE
            postsSongView.bindData(mModel?.posts?.song)
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

        refreshRedPkg()
        refreshLikes()
    }

    fun startPlay(playStatus: Int) {
        isPlaying = true
        when (playStatus) {
            PostsWatchViewAdapter.PLAY_POSTS_AUDIO -> {
                postsAudioView.setPlay(true)
                postsSongView.setPlay(false)
                commentView.setAudioPlay(false)
                commentView.setSongPlay(false)
            }
            PostsWatchViewAdapter.PLAY_POSTS_SONG -> {
                postsAudioView.setPlay(false)
                postsSongView.setPlay(true)
                commentView.setAudioPlay(false)
                commentView.setSongPlay(false)
            }
            PostsWatchViewAdapter.PLAY_POSTS_COMMENT_AUDIO -> {
                postsAudioView.setPlay(false)
                postsSongView.setPlay(false)
                commentView.setAudioPlay(true)
                commentView.setSongPlay(false)
            }
            PostsWatchViewAdapter.PLAY_POSTS_COMMENT_SONG -> {
                postsAudioView.setPlay(false)
                postsSongView.setPlay(false)
                commentView.setAudioPlay(false)
                commentView.setSongPlay(true)
            }
            else -> {
                MyLog.e("PostsWatchViewHolder", "什么播放状态 startPlay playStatus = $playStatus")
                // todo donothing
            }
        }

    }

    fun stopPlay() {
        isPlaying = false
        postsAudioView.setPlay(false)
        postsSongView.setPlay(false)
        commentView.setAudioPlay(false)
        commentView.setSongPlay(false)
    }

    fun refreshLikes() {
        // 评论数和点赞数
        if (mModel?.numeric == null) {
            postsCommentTv.text = mModel?.numeric?.commentCnt.toString()
            postsLikeTv.text = mModel?.numeric?.starCnt.toString()
        } else {
            postsCommentTv.text = "0"
            postsLikeTv.text = "0"
        }
    }

    fun refreshCommentLike() {

    }

    fun refreshRedPkg() {
        // 红包
        mModel?.posts?.let {
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
        }

    }
}