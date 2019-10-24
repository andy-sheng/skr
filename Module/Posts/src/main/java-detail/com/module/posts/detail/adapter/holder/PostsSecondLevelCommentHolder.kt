package com.module.posts.detail.adapter.holder

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.Barrier
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.view.setDebounceViewClickListener
import com.common.statistics.StatisticsAdapter
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.component.busilib.view.HonorTextView
import com.component.person.view.CommonAudioView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentDetailAdapter
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.view.PostsNineGridLayout
import com.module.posts.view.PostsSongView

class PostsSecondLevelCommentHolder(itemView: View, val mIDetailClickListener: PostsCommentDetailAdapter.ICommentDetailClickListener, val mPostsOwnerID: Int) : RecyclerView.ViewHolder(itemView) {
    var commenterAvaterIv: AvatarView
    var nameTv: HonorTextView
    val ownerTv: ExTextView
    var commentTimeTv: ExTextView
    var contentTv: ExTextView
    var postsAudioView: CommonAudioView
    var nineGridVp: PostsNineGridLayout
    var postsSongView: PostsSongView
    var postsBarrier: Barrier
    var bottomDivider: View

    var pos: Int = -1
    var mModel: PostsSecondLevelCommentModel? = null

    init {
        commenterAvaterIv = itemView.findViewById(R.id.commenter_avater_iv)
        nameTv = itemView.findViewById(R.id.name_tv)
        ownerTv = itemView.findViewById(R.id.owner_tv)
        commentTimeTv = itemView.findViewById(R.id.comment_time_tv)
        contentTv = itemView.findViewById(R.id.content_tv)
        postsSongView = itemView.findViewById(R.id.posts_song_view)
        postsAudioView = itemView.findViewById(R.id.posts_audio_view)
        nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
        postsBarrier = itemView.findViewById(R.id.posts_barrier)
        bottomDivider = itemView.findViewById(R.id.bottom_divider)

        commenterAvaterIv?.setDebounceViewClickListener {
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
                        mIDetailClickListener.startPlay(it[0]?.url ?: "", pos)
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

        itemView.setDebounceViewClickListener {
            mIDetailClickListener?.clickSecondLevelCommentContent(mModel!!, pos)
        }

        nineGridVp?.clickListener = { i, url, urlList ->
            StatisticsAdapter.recordCountEvent("posts", "content_picture_click", null)
            mIDetailClickListener?.goBigImageBrowse(i, urlList)
        }
    }

    fun refreshPosition(pos: Int, model: PostsSecondLevelCommentModel) {
        this.pos = pos
        this.mModel = model
    }

    fun refreshPlayState(pos: Int, model: PostsSecondLevelCommentModel) {
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

    fun bindData(pos: Int, model: PostsSecondLevelCommentModel, isLast: Boolean) {
        this.pos = pos
        this.mModel = model

        if (isLast) {
            bottomDivider.setBackgroundColor(U.getColor(R.color.transparent))
        } else {
            bottomDivider.setBackgroundColor(U.getColor(R.color.black_trans_10))
        }

        commenterAvaterIv.bindData(model.commentUser)
        if (model.commentUser.userId == mPostsOwnerID) {
            ownerTv.visibility = View.VISIBLE
        } else {
            ownerTv.visibility = View.GONE
        }
        nameTv.setHonorText(model.commentUser.nicknameRemark!!, model.commentUser.honorInfo)

        commentTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.comment.createdAt, System.currentTimeMillis())

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

        if (!TextUtils.isEmpty(model.comment.content)) {
            contentTv.visibility = View.VISIBLE
        } else {
            contentTv.visibility = View.GONE
        }

        if (model.comment.replyType == 1) {
            contentTv.text = model.comment.content
        } else if (model.comment.replyType == 2) {
            contentTv.visibility = View.VISIBLE
            val spanUtils = SpanUtils()
                    .append(model.commentUser.nicknameRemark.toString()).setClickSpan(object : ClickableSpan() {
                        override fun onClick(widget: View?) {
                            val bundle = Bundle()
                            bundle.putInt("bundle_user_id", model.commentUser.userId)
                            ARouter.getInstance()
                                    .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                                    .with(bundle)
                                    .navigation()
                        }

                        override fun updateDrawState(ds: TextPaint?) {
                            ds!!.setColor(Color.parseColor("#FF6295C4"))
                            ds!!.setUnderlineText(false)
                        }
                    })
                    .append("回复").setForegroundColor(U.getColor(R.color.black))
                    .append(model.replyUser.nicknameRemark.toString()).setClickSpan(object : ClickableSpan() {
                        override fun onClick(widget: View?) {
                            val bundle = Bundle()
                            bundle.putInt("bundle_user_id", model.replyUser.userId)
                            ARouter.getInstance()
                                    .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                                    .with(bundle)
                                    .navigation()
                        }

                        override fun updateDrawState(ds: TextPaint?) {
                            ds!!.setColor(Color.parseColor("#FF6295C4"))
                            ds!!.setUnderlineText(false)
                        }
                    })
                    .append(model.comment.content
                            ?: "").setForegroundColor(U.getColor(R.color.black))
            val stringBuilder = spanUtils.create()
            contentTv.text = stringBuilder
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