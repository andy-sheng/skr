package com.module.posts.detail.adapter.holder

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.constraint.Barrier
import android.support.constraint.Group
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.adapter.PostsCommentAdapter
import com.module.posts.view.*
import com.module.posts.watch.model.PostsRedPkgModel
import com.module.posts.watch.model.PostsWatchModel

class PostsHolder(itemView: View, val iDetailClickListener: PostsCommentAdapter.IDetailClickListener) : RecyclerView.ViewHolder(itemView) {
    val TAG = "PostsHolder"
    var followTv: ExTextView
    var timeTv: TextView
    var nicknameTv: NickNameView
    var avatarIv: AvatarView
    var content: ExpandTextView
    var postsAudioView: PostsAudioView
    var nineGridVp: PostsNineGridLayout
    var postsBarrier: Barrier
    var postsLikeTv: TextView
    var postsCommentTv: TextView
    var topicTv: ExTextView
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
    var voteGroupView: PostsVoteGroupView
    var pos: Int = -1
    var mModel: PostsWatchModel? = null
    var postsSongView: PostsSongView

    val followState = DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .setSolidColor(U.getColor(com.component.busilib.R.color.white))
            .setStrokeColor(Color.parseColor("#AD6C00"))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
            .build()

    val friendState = DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .setSolidColor(U.getColor(com.component.busilib.R.color.white))
            .setStrokeColor(Color.parseColor("#AD6C00"))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
            .build()

    val strangerState = DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
            .setSolidColor(Color.parseColor("#FFC15B"))
            .build()

    val mLikeDrawable = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.posts_like_selected_icon))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.posts_like_black_icon))
            .build()

    init {
        postsSongView = itemView.findViewById(R.id.posts_song_view)
        followTv = itemView.findViewById(R.id.follow_tv)
        timeTv = itemView.findViewById(R.id.time_tv)
        nicknameTv = itemView.findViewById(R.id.nickname_tv)
        avatarIv = itemView.findViewById(R.id.avatar_iv)
        content = itemView.findViewById(R.id.content)
        postsAudioView = itemView.findViewById(R.id.posts_audio_view)
        nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
        postsBarrier = itemView.findViewById(R.id.posts_barrier)
        postsLikeTv = itemView.findViewById(R.id.posts_like_tv)
        postsCommentTv = itemView.findViewById(R.id.posts_comment_tv)
        topicTv = itemView.findViewById(R.id.topic_tv)
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
        voteGroupView = PostsVoteGroupView(itemView.findViewById(R.id.vote_layout_stub))

        postsCommentTv.setDebounceViewClickListener {
            iDetailClickListener?.replayPosts(mModel!!)
        }

        postsLikeTv.setDebounceViewClickListener {
            mModel?.let {
                iDetailClickListener?.likePosts(it)
            }
        }

        mLikeDrawable.setBounds(Rect(0, 0, mLikeDrawable.getIntrinsicWidth(), mLikeDrawable.getIntrinsicHeight()))
        postsLikeTv.setCompoundDrawables(null, null, mLikeDrawable, null)

        avatarIv?.setDebounceViewClickListener {
            mModel?.user?.userId?.let {
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
                    iDetailClickListener?.stopPlay()
                    postsAudioView.setPlay(false)
                } else {
                    mModel?.posts?.audios?.let {
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
                    mModel?.posts?.song?.let {
                        iDetailClickListener?.startPlay(it.playURL ?: "", pos)
                        postsSongView.setPlay(true)
                    }
                }
            }
        })

        redPkgBg.setDebounceViewClickListener {
            iDetailClickListener?.showRedPkg(mModel!!)
        }

        nineGridVp?.clickListener = { i, url, urlList ->
            StatisticsAdapter.recordCountEvent("posts", "content_picture_click", null)
            iDetailClickListener?.goBigImageBrowse(i, urlList)
        }

        content.setListener(object : ExpandTextView.ExpandListener {
            override fun onClickExpand(isExpand: Boolean) {
                mModel?.isExpend = isExpand
            }
        })

        voteGroupView.clickListener = {
            StatisticsAdapter.recordCountEvent("posts", "content_vote_click", null)
            iDetailClickListener?.onClickPostsVote(pos, mModel, it)
        }

        topicTv.setDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("posts", "topic_content_click", null)
            mModel?.posts?.topicInfo?.let {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_TOPIC)
                        .withLong("topicID", it.topicID.toLong())
                        .navigation()
            }
        }
    }

    fun refreshFollowState(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model
        model?.relationShip?.let {
            if (it.isFriend) {
                followTv.text = "已互关"
                followTv.background = friendState
                followTv.setTextColor(Color.parseColor("#AD6C00"))
                followTv.setDebounceViewClickListener {}
            } else if (it.isFollow) {
                followTv.text = "已关注"
                followTv.background = followState
                followTv.setTextColor(Color.parseColor("#AD6C00"))
                followTv.setDebounceViewClickListener {}
            } else {
                followTv.text = "+关注"
                followTv.background = strangerState
                followTv.setTextColor(Color.parseColor("#AD6C00"))
                followTv.setDebounceViewClickListener {
                    mModel?.posts?.userID?.let {
                        UserInfoManager.getInstance().mateRelation(it, UserInfoManager.RA_BUILD, false, 0, null)
                    }
                }
            }
        }
    }

    fun refreshVoteState(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model

        // 投票
        if (mModel?.posts?.voteInfo == null) {
            voteGroupView.setVisibility(View.GONE)
        } else {
            voteGroupView.setVisibility(View.VISIBLE)
            voteGroupView.bindData(mModel?.posts?.voteInfo!!)
        }
    }

    fun refreshCommentCnt(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model

        commentCtnTv.text = "评论（${model.numeric?.commentCnt ?: 0}条）"

        if ((model.numeric?.commentCnt ?: 0).toInt() == 0) {
            emptyTv.visibility = View.VISIBLE
        } else {
            emptyTv.visibility = View.GONE
        }

        if (mModel?.numeric != null) {
            postsCommentTv.text = mModel?.numeric?.commentCnt.toString()
            postsLikeTv.text = mModel?.numeric?.starCnt.toString()
        } else {
            postsCommentTv.text = "0"
            postsLikeTv.text = "0"
        }
    }

    fun refreshLike(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model

        // 评论数和点赞数
        if (mModel?.numeric != null) {
            postsLikeTv.text = mModel?.numeric?.starCnt.toString()
        } else {
            postsLikeTv.text = "0"
        }

        postsLikeTv.isSelected = mModel?.isLiked ?: false
    }

    fun refreshPlayState(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model

        if (mModel?.posts?.song == null) {
            postsSongView.visibility = View.GONE
        } else {
            postsSongView.visibility = View.VISIBLE
            postsSongView.bindData(mModel?.posts?.song)

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel?.posts?.song?.playURL) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
                postsSongView.setPlay(true)
            } else {
                postsSongView.setPlay(false)
            }
        }

        // 音频
        if (mModel?.posts?.audios.isNullOrEmpty()) {
            postsAudioView.visibility = View.GONE
        } else {
            postsAudioView.visibility = View.VISIBLE
            postsAudioView.bindData(mModel?.posts?.audios!![0].duration)

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel!!.posts!!.audios!![0].url) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
                postsAudioView.setPlay(true)
            } else {
                postsAudioView.setPlay(false)
            }
        }
    }

    fun bindData(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.mModel = model
        if (mModel?.user != null) {
            avatarIv.bindData(mModel?.user!!)
            nicknameTv.setHonorText(mModel?.user?.nicknameRemark!!, mModel?.user?.honorInfo)
        } else {
            MyLog.e("PostsWatchViewHolder", "bindData error pos = $pos, model = $model")
        }

        commentCtnTv.text = "评论(${model.numeric?.commentCnt ?: 0}条)"

        mModel?.posts?.let {
            timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(it.createdAt, System.currentTimeMillis())
            content.bindData(it.title, 3, model.isExpend)
            if (TextUtils.isEmpty(it.title)) {
                content.visibility = View.GONE
            } else {
                content.visibility = View.VISIBLE
            }

            // 红包
            if (it.redpacketInfo == null) {
                redPkgGroup.visibility = View.GONE
            } else {
                redPkgGroup.visibility = View.VISIBLE
                coinTv.text = it.redpacketInfo?.redpacketDesc
                // 进详情页面不应该有未审核的红包
                when {
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_UN_AUDIT -> {
                        MyLog.e(TAG, "bindData 为什么详情会有审核未通过的红包")
                        redPkgDes.text = "审核通过后，开始发放倒计时"
                        redPkgIv.setImageResource(R.drawable.posts_red_s_close_icon)
                    }
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_ONGING -> {
                        redPkgIv.setImageResource(R.drawable.posts_red_s_close_icon)
                        it.redpacketInfo?.resTimeMs?.let { time ->
                            redPkgDes.text = "剩余时间：${U.getDateTimeUtils().formatPostsRedMinuTime(time)}"
                        }
                    }
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_GET_PART -> {
                        redPkgIv.setImageResource(R.drawable.posts_red_s_part_icon)
                        redPkgDes.text = "红包已过期"
                    }
                    it.redpacketInfo?.status == PostsRedPkgModel.RS_GET_ALL -> {
                        redPkgIv.setImageResource(R.drawable.posts_red_s_open_icon)
                        redPkgDes.text = "红包已瓜分完毕"
                    }
                    else -> redPkgGroup.visibility = View.GONE
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

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel!!.posts!!.audios!![0].url) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
                postsAudioView.setPlay(true)
                iDetailClickListener?.setCurPlayingPosition(pos)
            } else {
                postsAudioView.setPlay(false)
            }
        }

        // 图片
        if (mModel?.posts?.pictures.isNullOrEmpty()) {
            nineGridVp.visibility = View.GONE
        } else {
            nineGridVp.visibility = View.VISIBLE
            nineGridVp.setUrlList(mModel?.posts?.pictures!!)
        }

        // 投票
        if (mModel?.posts?.voteInfo == null) {
            voteGroupView.setVisibility(View.GONE)
        } else {
            voteGroupView.setVisibility(View.VISIBLE)
            voteGroupView.bindData(mModel?.posts?.voteInfo!!)
        }

        // 评论数和点赞数
        if (mModel?.numeric != null) {
            postsCommentTv.text = mModel?.numeric?.commentCnt.toString()
            postsLikeTv.text = mModel?.numeric?.starCnt.toString()
        } else {
            postsCommentTv.text = "0"
            postsLikeTv.text = "0"
        }

        postsLikeTv.isSelected = mModel?.isLiked ?: false

        if (mModel?.relationShip == null && mModel?.posts?.userID != MyUserInfoManager.uid.toInt()) {
            iDetailClickListener?.getRelation(mModel?.posts?.userID ?: 0)
        }

        refreshFollowState(pos, mModel!!)

        commentCtnTv.text = "评论（${model.numeric?.commentCnt ?: 0}条）"

        if (mModel?.posts?.song == null) {
            postsSongView.visibility = View.GONE
        } else {
            postsSongView.visibility = View.VISIBLE
            postsSongView.bindData(mModel?.posts?.song)

            if (iDetailClickListener.getCurPlayingUrl().equals(mModel?.posts?.song?.playURL) && !TextUtils.isEmpty(iDetailClickListener.getCurPlayingUrl())) {
                postsSongView.setPlay(true)
                iDetailClickListener?.setCurPlayingPosition(pos)
            } else {
                postsSongView.setPlay(false)
            }
        }

        commentCtnTv.text = "评论（${model.numeric?.commentCnt ?: 0}条）"

        if ((model.numeric?.commentCnt ?: 0).toInt() == 0) {
            emptyTv.visibility = View.VISIBLE
        } else {
            emptyTv.visibility = View.GONE
        }

        if (mModel?.posts?.userID == MyUserInfoManager.uid.toInt()) {
            followTv.visibility = View.GONE
        } else {
            followTv.visibility = View.VISIBLE
        }
    }
}