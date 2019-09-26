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
import com.common.view.ex.ExTextView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.view.*
import com.module.posts.watch.adapter.PostsWatchListener
import com.module.posts.watch.adapter.PostsWatchViewAdapter
import com.module.posts.watch.model.PostsRedPkgModel
import com.module.posts.watch.model.PostsWatchModel

open class PostsViewHolder(item: View, val listener: PostsWatchListener) : RecyclerView.ViewHolder(item) {

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

    val debugTv: TextView = item.findViewById(R.id.debug_tv)

    var pos = -1
    var mModel: PostsWatchModel? = null
    var isPlaying = false

    init {
        moreIv.setDebounceViewClickListener { listener.onClickPostsMore(pos, mModel) }
        postsAudioView.setDebounceViewClickListener { listener.onClickPostsAudio(pos, mModel, isPlaying) }
        postsSongView.setDebounceViewClickListener { listener.onClickPostsSong(pos, mModel, isPlaying) }

        nineGridVp.clickListener = { i, url, _ ->
            listener.onClickPostsImage(pos, mModel, i, url)
        }

        redPkgIv.setAnimateDebounceViewClickListener { listener.onClickPostsRedPkg(pos, mModel) }
        topicTv.setAnimateDebounceViewClickListener { listener.onClickPostsTopic(pos, mModel) }
        postsLikeTv.setAnimateDebounceViewClickListener { listener.onClickPostsLike(pos, mModel) }

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
            override fun onClickText() {
                listener.onClickPostsDetail(pos, mModel)
            }

            override fun onClickExpand(isExpand: Boolean) {
                mModel?.isExpend = isExpand
            }
        })

        itemView.setDebounceViewClickListener {
            listener.onClickPostsDetail(pos, mModel)
        }
    }

    open fun bindData(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model
        this.isPlaying = false

        if (MyLog.isDebugLogOpen()) {
            debugTv.visibility = View.VISIBLE
            debugTv.text = "NO:$pos ${model.toDebugString()}"
        } else {
            debugTv.visibility = View.GONE
        }

        mModel?.posts?.let {
            if (TextUtils.isEmpty(it.title)) {
                content.visibility = View.GONE
            } else {
                content.visibility = View.VISIBLE
                content.initWidth(U.getDisplayUtils().screenWidth - 20.dp())
                content.maxLines = 3
                if (!model.isExpend) {
                    content.setCloseText(it.title)
                } else {
                    content.setExpandText(it.title)
                }
            }
            // 话题
            if (it.topicInfo == null || TextUtils.isEmpty(it.topicInfo?.topicDesc)) {
                topicTv.visibility = View.GONE
            } else {
                topicTv.visibility = View.VISIBLE
                topicTv.text = it.topicInfo?.topicTitle
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

        refreshVote()

        // 评论数
        if (mModel?.numeric == null) {
            postsCommentTv.text = "0"
        } else {
            postsCommentTv.text = mModel?.numeric?.commentCnt.toString()
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
                commentView.postsAudioView?.setPlay(false)
                commentView.postsSongView?.setPlay(false)
            }
            PostsWatchViewAdapter.PLAY_POSTS_SONG -> {
                postsAudioView.setPlay(false)
                postsSongView.setPlay(true)
                commentView.postsAudioView?.setPlay(false)
                commentView.postsSongView?.setPlay(false)
            }
            PostsWatchViewAdapter.PLAY_POSTS_COMMENT_AUDIO -> {
                postsAudioView.setPlay(false)
                postsSongView.setPlay(false)
                commentView.postsAudioView?.setPlay(true)
                commentView.postsSongView?.setPlay(false)
            }
            PostsWatchViewAdapter.PLAY_POSTS_COMMENT_SONG -> {
                postsAudioView.setPlay(false)
                postsSongView.setPlay(false)
                commentView.postsAudioView?.setPlay(false)
                commentView.postsSongView?.setPlay(true)
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
        commentView.postsAudioView?.setPlay(false)
        commentView.postsSongView?.setPlay(false)
    }

    fun refreshLikes() {
        // 点赞数和图片
        if (mModel?.numeric == null) {
            postsLikeTv.text = "0"
        } else {
            postsLikeTv.text = mModel?.numeric?.starCnt.toString()
        }
        var drawable = U.getDrawable(R.drawable.posts_like_black_icon)
        if (mModel?.isLiked == true) {
            drawable = U.getDrawable(R.drawable.posts_like_selected_icon)
        }
        drawable.setBounds(0, 0, 18.dp(), 19.dp())
        postsLikeTv.setCompoundDrawables(null, null, drawable, null)
    }

    fun refreshCommentLike() {
        commentView.refreshCommentLike()
    }

    fun refreshRedPkg() {
        // 红包
        mModel?.posts?.let {
            if (it.redpacketInfo == null) {
                redPkgIv.visibility = View.GONE
            } else {
                redPkgIv.visibility = View.VISIBLE
                when {
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_UN_AUDIT -> redPkgIv.setImageResource(R.drawable.posts_red_s_close_icon)
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_ONGING -> redPkgIv.setImageResource(R.drawable.posts_red_s_close_icon)
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_GET_PART -> redPkgIv.setImageResource(R.drawable.posts_red_s_part_icon)
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_GET_ALL -> redPkgIv.setImageResource(R.drawable.posts_red_s_open_icon)
                    else -> redPkgIv.visibility = View.GONE
                }
            }
        }
    }

    fun refreshVote() {
        // 投票
        if (mModel?.posts?.voteInfo == null) {
            voteGroupView.setVisibility(View.GONE)
        } else {
            voteGroupView.setVisibility(View.VISIBLE)
            voteGroupView.bindData(mModel?.posts?.voteInfo!!)
        }
    }
}