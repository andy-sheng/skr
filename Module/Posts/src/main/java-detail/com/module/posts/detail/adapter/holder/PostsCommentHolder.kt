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
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import com.component.person.view.CommonAudioView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentAdapter
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.view.PostsNineGridLayout
import com.module.posts.view.PostsSongView

class PostsCommentHolder(itemView: View, val iDetailClickListener: PostsCommentAdapter.IDetailClickListener, val postsOwnerID: Int) : RecyclerView.ViewHolder(itemView) {
    var commenterAvaterIv: AvatarView
    var nicknameTv: NickNameView
    var commentTimeTv: ExTextView
    var xinIv: ExImageView
    var likeNum: ExTextView
    var contentTv: ExTextView
    var postsAudioView: CommonAudioView
    var nineGridVp: PostsNineGridLayout
    var postsBarrier: Barrier
    var replyNum: ExTextView
    var redPkgTv: ExTextView
    val ownerTv: ExTextView
    var bottomBarrier: Barrier
    var pos: Int = -1
    var mModel: PostFirstLevelCommentModel? = null
    var postsSongView: PostsSongView
    var bottomDivider: View

    init {
        postsSongView = itemView.findViewById(R.id.posts_song_view)
        commenterAvaterIv = itemView.findViewById(R.id.commenter_avater_iv)
        nicknameTv = itemView.findViewById(R.id.nickname_tv)
        commentTimeTv = itemView.findViewById(R.id.comment_time_tv)
        xinIv = itemView.findViewById(R.id.xin_iv)
        likeNum = itemView.findViewById(R.id.like_num)
        contentTv = itemView.findViewById(R.id.content_tv)
        postsAudioView = itemView.findViewById(R.id.posts_audio_view)
        nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
        postsBarrier = itemView.findViewById(R.id.posts_barrier)
        redPkgTv = itemView.findViewById(R.id.red_pkg_tv)
        ownerTv = itemView.findViewById(R.id.owner_tv)
        replyNum = itemView.findViewById(R.id.reply_num)
        bottomBarrier = itemView.findViewById(R.id.bottom_barrier)
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

        xinIv.setDebounceViewClickListener {
            mModel?.let {
                StatisticsAdapter.recordCountEvent("posts", "content_like", null)
                iDetailClickListener?.likeFirstLevelComment(it)
            }
        }

        contentTv.setDebounceViewClickListener {
            iDetailClickListener?.clickFirstLevelComment()
        }

        itemView.setDebounceViewClickListener {
            iDetailClickListener?.goSecondLevelCommentDetail(mModel!!, pos)
        }

        postsAudioView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("posts", "content_voice_click", null)
                if (postsAudioView.isPlaying) {
                    iDetailClickListener?.stopPlay()
                    postsAudioView.setPlay(false)
                } else {
                    mModel?.comment?.audios?.let {
                        iDetailClickListener?.startPlay(it[0]?.url ?: "", pos)
                        postsAudioView.setPlay(true)
                    }
                }
            }
        })

        postsSongView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                StatisticsAdapter.recordCountEvent("posts", "content_music_click", null)
                if (postsSongView.isPlaying) {
                    iDetailClickListener?.stopPlay()
                    postsSongView.setPlay(false)
                } else {
                    mModel?.comment?.songInfo?.let {
                        iDetailClickListener?.startPlay(it.playURL ?: "", pos)
                        postsSongView.setPlay(true)
                    }
                }
            }
        })

        contentTv.setDebounceViewClickListener {
            iDetailClickListener.clickContent(mModel!!, pos)
        }

        nineGridVp?.clickListener = { i, url, urlList ->
            StatisticsAdapter.recordCountEvent("posts", "content_picture_click", null)
            iDetailClickListener.goBigImageBrowse(i, urlList)
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

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel!!.comment!!.audios!![0].url) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
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

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel?.comment?.songInfo?.playURL) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
                postsSongView.setPlay(true)
            } else {
                postsSongView.setPlay(false)
            }
        }
    }

    fun refreshReplyNum(pos: Int, model: PostFirstLevelCommentModel) {
        this.pos = pos
        this.mModel = model

        if ((mModel?.secondLevelComments?.size
                        ?: 0) > 0 && mModel?.comment?.subCommentCnt ?: 0 > 0) {
            replyNum.visibility = View.VISIBLE
            if (model.secondLevelComments?.get(0)?.commentUser?.userId == postsOwnerID) {
                val spanUtils = SpanUtils()
                        .append(model.secondLevelComments?.get(0)?.commentUser?.nicknameRemark.toString()).setClickSpan(object : ClickableSpan() {
                            override fun onClick(widget: View?) {
                                val bundle = Bundle()
                                bundle.putInt("bundle_user_id", model.commentUser?.userId ?: 0)
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
                        .appendImage(R.drawable.posts_louzhu, SpanUtils.ALIGN_CENTER)
                        .append(" 等人 共").setForegroundColor(U.getColor(R.color.black))
                        .append("${mModel?.comment?.subCommentCnt}条回复>").setForegroundColor(Color.parseColor("#FF6295C4"))

                val stringBuilder = spanUtils.create()
                replyNum.text = stringBuilder
            } else {
                val spanUtils = SpanUtils()
                        .append(model.secondLevelComments?.get(0)?.commentUser?.nicknameRemark.toString()).setClickSpan(object : ClickableSpan() {
                            override fun onClick(widget: View?) {
                                val bundle = Bundle()
                                bundle.putInt("bundle_user_id", model.commentUser?.userId ?: 0)
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
                        .append(" 等人 共").setForegroundColor(U.getColor(R.color.black))
                        .append("${mModel?.comment?.subCommentCnt}条回复>").setForegroundColor(Color.parseColor("#FF6295C4"))

                val stringBuilder = spanUtils.create()
                replyNum.text = stringBuilder
            }
        } else {
            replyNum.visibility = View.GONE
        }
    }

    fun refreshLike(pos: Int, model: PostFirstLevelCommentModel) {
        this.pos = pos
        this.mModel = model

        likeNum.text = model.comment?.likedCnt.toString()
        xinIv.isSelected = mModel?.isLiked ?: false
    }

    fun refreshPosition(pos: Int, model: PostFirstLevelCommentModel) {
        this.pos = pos
        this.mModel = model
    }

    fun bindData(pos: Int, model: PostFirstLevelCommentModel, isLast: Boolean) {
        this.pos = pos
        this.mModel = model

        if (isLast) {
            bottomDivider.setBackgroundColor(U.getColor(R.color.transparent))
        } else {
            bottomDivider.setBackgroundColor(U.getColor(R.color.black_trans_10))
        }

        commenterAvaterIv.bindData(model.commentUser)
        if (model.commentUser?.userId == postsOwnerID) {
            ownerTv.visibility = View.VISIBLE
        } else {
            ownerTv.visibility = View.GONE
        }
        nicknameTv.setHonorText(model.commentUser?.nicknameRemark!!, model.commentUser?.honorInfo)
        commentTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.comment?.createdAt
                ?: 0, System.currentTimeMillis())
        likeNum.text = model.comment?.likedCnt.toString()

        if (!TextUtils.isEmpty(model.comment?.content)) {
            contentTv.text = model.comment?.content
            contentTv.visibility = View.VISIBLE
        } else {
            contentTv.visibility = View.GONE
        }

        if (mModel?.comment?.audios.isNullOrEmpty()) {
            postsAudioView.visibility = View.GONE
        } else {
            postsAudioView.visibility = View.VISIBLE
            postsAudioView.bindData(mModel!!.comment!!.audios!![0].duration)

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel!!.comment!!.audios!![0].url) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
                postsAudioView.setPlay(true)
                iDetailClickListener.setCurPlayingPosition(pos)
            } else {
                postsAudioView.setPlay(false)
            }
        }

        if (mModel?.isHasRedpacket ?: false) {
            redPkgTv.visibility = View.VISIBLE
        } else {
            redPkgTv.visibility = View.GONE
        }

        // 图片
        if (mModel?.comment?.pictures.isNullOrEmpty()) {
            nineGridVp.visibility = View.GONE
        } else {
            nineGridVp.visibility = View.VISIBLE
            nineGridVp.setUrlList(mModel?.comment?.pictures!!)
        }

        refreshReplyNum(pos, mModel!!)

        xinIv.isSelected = mModel?.isLiked ?: false

        if (mModel?.comment?.songInfo == null) {
            postsSongView.visibility = View.GONE
        } else {
            postsSongView.visibility = View.VISIBLE
            postsSongView.bindData(mModel?.comment?.songInfo)

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel?.comment?.songInfo?.playURL) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
                postsSongView.setPlay(true)
                iDetailClickListener.setCurPlayingPosition(pos)
            } else {
                postsSongView.setPlay(false)
            }
        }
    }
}