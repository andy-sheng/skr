package com.module.posts.detail.adapter.holder

import android.os.Bundle
import android.support.constraint.Barrier
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.view.setDebounceViewClickListener
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.view.ExpandTextView
import com.module.posts.view.PostsAudioView
import com.module.posts.view.PostsNineGridLayout
import com.module.posts.view.PostsSongView

class PostsFirstLevelCommentHolder(itemView: View, val mIDetailClickListener: PostsCommentDetailAdapter.ICommentDetailClickListener) : RecyclerView.ViewHolder(itemView) {
    var timeTv: TextView
    var nicknameTv: TextView
    var avatarIv: AvatarView
    var content: ExpandTextView
    var postsAudioView: PostsAudioView
    var nineGridVp: PostsNineGridLayout
    var postsSongView: PostsSongView
    var postsBarrier: Barrier
    var commentNumDivider: View
    var commentCtnTv: ExTextView
    var emptyTv: ExTextView

    var pos: Int = -1
    var mModel: PostFirstLevelCommentModel? = null


    init {
        timeTv = itemView.findViewById(R.id.time_tv)
        nicknameTv = itemView.findViewById(R.id.nickname_tv)
        avatarIv = itemView.findViewById(R.id.avatar_iv)
        content = itemView.findViewById(R.id.content)
        postsSongView = itemView.findViewById(R.id.posts_song_view)
        postsAudioView = itemView.findViewById(R.id.posts_audio_view)
        nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
        postsBarrier = itemView.findViewById(R.id.posts_barrier)
        commentNumDivider = itemView.findViewById(R.id.comment_num_divider)
        commentCtnTv = itemView.findViewById(R.id.comment_ctn_tv)
        emptyTv = itemView.findViewById(R.id.empty_tv)

        avatarIv?.setDebounceViewClickListener {
            mModel?.commentUser?.userId?.let {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", it)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        }

        postsAudioView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("posts", "content_voice_click", null)
                if (postsAudioView.isPlaying) {
                    mIDetailClickListener.stopPlay()
                    postsAudioView.setPlay(false)
                } else {
                    mModel?.comment?.audios?.let {
                        mIDetailClickListener.startPlay(it[0]?.url, pos)
                        postsAudioView.setPlay(true)
                    }
                }
            }
        })

        postsSongView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("posts", "content_music_click", null)
                if (postsSongView.isPlaying) {
                    mIDetailClickListener.stopPlay()
                    postsSongView.setPlay(false)
                } else {
                    mModel?.comment?.songInfo?.let {
                        mIDetailClickListener.startPlay(it.playURL ?: "", pos)
                        postsSongView.setPlay(true)
                    }
                }
            }
        })

        nineGridVp?.clickListener = { i, url, urlList ->
            StatisticsAdapter.recordCountEvent("posts", "content_picture_click", null)
            mIDetailClickListener.goBigImageBrowse(i, urlList)
        }

        content.setListener(object : ExpandTextView.ExpandListener {
            override fun onClickExpand(isExpand: Boolean) {
                mModel?.isExpend = isExpand
            }
        })
    }

    fun refreshCtn(pos: Int, model: PostFirstLevelCommentModel) {
        this.pos = pos
        this.mModel = model

        commentCtnTv.text = "评论(${mModel?.comment?.subCommentCnt}条)"
        if (mModel?.comment?.subCommentCnt == 0) {
            emptyTv.visibility = View.VISIBLE
        } else {
            emptyTv.visibility = View.GONE
        }
    }

    fun refreshPlayState(pos: Int, model: PostFirstLevelCommentModel) {
        this.pos = pos
        this.mModel = model

        if (mModel?.comment?.audios.isNullOrEmpty()) {
            postsAudioView.visibility = View.GONE
        } else {
            postsAudioView.visibility = View.VISIBLE
            postsAudioView.bindData(mModel!!.comment!!.audios!![0].duration)

            if (mIDetailClickListener.getCurPlayingUrl().equals(mModel!!.comment!!.audios!![0].url) && !TextUtils.isEmpty(mIDetailClickListener.getCurPlayingUrl())) {
                postsAudioView.setPlay(true)
            } else {
                postsAudioView.setPlay(false)
            }
        }

        if (mModel?.comment?.songInfo == null) {
            postsSongView.visibility = View.GONE
        } else {
            postsSongView.visibility = View.VISIBLE
            postsSongView.bindData(mModel?.comment?.songInfo)

            if (mIDetailClickListener.getCurPlayingUrl().equals(mModel?.comment?.songInfo?.playURL) && !TextUtils.isEmpty(mIDetailClickListener.getCurPlayingUrl())) {
                postsSongView.setPlay(true)
            } else {
                postsSongView.setPlay(false)
            }
        }
    }

    fun bindData(pos: Int, model: PostFirstLevelCommentModel) {
        this.pos = pos
        this.mModel = model

        avatarIv.bindData(model.commentUser)
        nicknameTv.text = model.commentUser?.nicknameRemark
        timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.comment?.createdAt
                ?: 0, System.currentTimeMillis())

        if (!TextUtils.isEmpty(model.comment?.content)) {
            content.visibility = View.VISIBLE
            content.bindData(model.comment?.content, 3, model.isExpend)
        } else {
            content.visibility = View.GONE
        }

        if (mModel?.comment?.audios.isNullOrEmpty()) {
            postsAudioView.visibility = View.GONE
        } else {
            postsAudioView.visibility = View.VISIBLE
            postsAudioView.bindData(mModel!!.comment!!.audios!![0].duration)

            if (mIDetailClickListener.getCurPlayingUrl().equals(mModel!!.comment!!.audios!![0].url) && !TextUtils.isEmpty(mIDetailClickListener.getCurPlayingUrl())) {
                postsAudioView.setPlay(true)
                mIDetailClickListener.setCurPlayingPosition(pos)
            } else {
                postsAudioView.setPlay(false)
            }
        }

        // 图片
        if (mModel?.comment?.pictures.isNullOrEmpty()) {
            nineGridVp.visibility = View.GONE
        } else {
            nineGridVp.visibility = View.VISIBLE
            nineGridVp.setUrlList(mModel?.comment?.pictures!!)
        }

        commentCtnTv.text = "评论(${mModel?.comment?.subCommentCnt}条)"
        if (mModel?.comment?.subCommentCnt == 0) {
            emptyTv.visibility = View.VISIBLE
        } else {
            emptyTv.visibility = View.GONE
        }

        if (mModel?.comment?.songInfo == null) {
            postsSongView.visibility = View.GONE
        } else {
            postsSongView.visibility = View.VISIBLE
            postsSongView.bindData(mModel?.comment?.songInfo)

            if (mIDetailClickListener.getCurPlayingUrl().equals(mModel?.comment?.songInfo?.playURL) && !TextUtils.isEmpty(mIDetailClickListener.getCurPlayingUrl())) {
                postsSongView.setPlay(true)
                mIDetailClickListener.setCurPlayingPosition(pos)
            } else {
                postsSongView.setPlay(false)
            }
        }
    }
}