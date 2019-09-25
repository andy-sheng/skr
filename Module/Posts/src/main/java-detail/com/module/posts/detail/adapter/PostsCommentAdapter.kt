package com.module.posts.detail.adapter

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.constraint.Barrier
import android.support.constraint.Group
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.callback.Callback
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.recyclerview.DiffAdapter
import com.component.busilib.view.AvatarView
import com.imagebrowse.ImageBrowseView
import com.imagebrowse.big.BigImageBrowseFragment
import com.imagebrowse.big.DefaultImageBrowserLoader
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.activity.PostsDetailActivity
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.view.*
import com.module.posts.watch.model.PostsRedPkgModel
import com.module.posts.watch.model.PostsWatchModel

class PostsCommentAdapter : DiffAdapter<Any, RecyclerView.ViewHolder> {
    companion object {
        val REFRESH_COMMENT_CTN = 0
        val DESTROY_HOLDER = 1
        val REFRESH_PLAY_STATE = 2
        val REFRESH_LIKE = 3
        val REFRESH_VOTE = 4
        val REFRESH_FOLLOW_STATE = 5
    }

    private val mPostsType = 0
    private val mCommentType = 1

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

    var mPlayingUrl: String
        set(value) {
            value?.let {
                mIDetailClickListener?.setCurPlayingUrl(value)
            }
        }
        get() = mIDetailClickListener?.getCurPlayingUrl() ?: ""

    var mPlayingPosition
        set(value) {
            value?.let {
                mIDetailClickListener?.setCurPlayintPosition(value)
            }
        }
        get() = mIDetailClickListener?.getCurPlayingPosition() ?: 0

    var mIDetailClickListener: IDetailClickListener? = null

    var mClickContent: ((PostFirstLevelCommentModel) -> Unit)? = null

    var mContext: FragmentActivity? = null

    val mLikeDrawable = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.posts_like_selected_icon))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.posts_like_black_icon))
            .build()

    constructor(context: FragmentActivity) : super() {
        mContext = context
    }

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

    inner class PostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var followTv: ExTextView
        var timeTv: TextView
        var nicknameTv: TextView
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
        var isGetRelation: Boolean = false
        var postsSongView: PostsSongView

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
                mIDetailClickListener?.replayPosts(mModel!!)
            }

            postsLikeTv.setDebounceViewClickListener {
                mModel?.let {
                    mIDetailClickListener?.likePosts(it)
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
                    if (postsAudioView.isPlaying) {
                        mPlayingUrl = ""
                        mPlayingPosition = -1
                        SinglePlayer.stop(PostsDetailActivity.playerTag)
                        postsAudioView.setPlay(false)
                    } else {
                        mModel?.posts?.audios?.let {
                            mIDetailClickListener?.playAnotherSong()
                            mPlayingUrl = it[0]?.url ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(PostsDetailActivity.playerTag, mPlayingUrl)
                            postsAudioView.setPlay(true)
                        }
                    }
                }
            })

            postsSongView.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    if (postsSongView.isPlaying) {
                        mPlayingUrl = ""
                        mPlayingPosition = -1
                        SinglePlayer.stop(PostsDetailActivity.playerTag)
                        postsSongView.setPlay(false)
                    } else {
                        mModel?.posts?.song?.let {
                            mIDetailClickListener?.playAnotherSong()
                            mPlayingUrl = it.playURL ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(PostsDetailActivity.playerTag, mPlayingUrl)
                            postsSongView.setPlay(true)
                        }
                    }
                }
            })

            redPkgBg.setDebounceViewClickListener {
                mIDetailClickListener?.showRedPkg(mModel!!)
            }

            nineGridVp?.clickListener = { i, url, urlList ->
                goBigImageBrowse(i, urlList)
            }

            voteGroupView.clickListener = {
                mIDetailClickListener?.onClickPostsVote(pos, mModel, it)
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

                if (mPlayingUrl.equals(mModel?.posts?.song?.playURL) && !TextUtils.isEmpty(mPlayingUrl)) {
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

                if (mPlayingUrl.equals(mModel!!.posts!!.audios!![0].url) && !TextUtils.isEmpty(mPlayingUrl)) {
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
                nicknameTv.text = mModel?.user?.nicknameRemark
            } else {
                MyLog.e("PostsWatchViewHolder", "bindData error pos = $pos, model = $model")
            }

            commentCtnTv.text = "评论(${model.numeric?.commentCnt ?: 0}条)"

            mModel?.posts?.let {
                timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(it.createdAt, System.currentTimeMillis())

                content.initWidth(U.getDisplayUtils().screenWidth - 20.dp())
                content.maxLines = 3
                if (!model.isExpend) {
                    content.setCloseText(it.title)
                } else {
                    content.setExpandText(it.title)
                }

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

                if (mPlayingUrl.equals(mModel!!.posts!!.audios!![0].url) && !TextUtils.isEmpty(mPlayingUrl)) {
                    postsAudioView.setPlay(true)
                    mPlayingPosition = pos
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

            if (mModel?.relationShip == null && mModel?.posts?.userID != MyUserInfoManager.getInstance().uid.toInt()) {
                mIDetailClickListener?.getRelation(mModel?.posts?.userID ?: 0)
            }

            refreshFollowState(pos, mModel!!)

            commentCtnTv.text = "评论（${model.numeric?.commentCnt ?: 0}条）"

            if (mModel?.posts?.song == null) {
                postsSongView.visibility = View.GONE
            } else {
                postsSongView.visibility = View.VISIBLE
                postsSongView.bindData(mModel?.posts?.song)

                if (mPlayingUrl.equals(mModel?.posts?.song?.playURL) && !TextUtils.isEmpty(mPlayingUrl)) {
                    postsSongView.setPlay(true)
                    mPlayingPosition = pos
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

            if (mModel?.posts?.userID == MyUserInfoManager.getInstance().uid.toInt()) {
                followTv.visibility = View.GONE
            } else {
                followTv.visibility = View.VISIBLE
            }
        }
    }

    inner class PostsCommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var commenterAvaterIv: AvatarView
        var nameTv: ExTextView
        var commentTimeTv: ExTextView
        var xinIv: ExImageView
        var likeNum: ExTextView
        var contentTv: ExTextView
        var postsAudioView: PostsCommentAudioView
        var nineGridVp: PostsNineGridLayout
        var postsBarrier: Barrier
        var replyNum: ExTextView
        var redPkgTv: ExTextView
        var bottomBarrier: Barrier
        var pos: Int = -1
        var mModel: PostFirstLevelCommentModel? = null
        var postsSongView: PostsSongView
        var bottomDivider: View

        init {
            postsSongView = itemView.findViewById(R.id.posts_song_view)
            commenterAvaterIv = itemView.findViewById(R.id.commenter_avater_iv)
            nameTv = itemView.findViewById(R.id.name_tv)
            commentTimeTv = itemView.findViewById(R.id.comment_time_tv)
            xinIv = itemView.findViewById(R.id.xin_iv)
            likeNum = itemView.findViewById(R.id.like_num)
            contentTv = itemView.findViewById(R.id.content_tv)
            postsAudioView = itemView.findViewById(R.id.posts_audio_view)
            nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
            postsBarrier = itemView.findViewById(R.id.posts_barrier)
            redPkgTv = itemView.findViewById(R.id.red_pkg_tv)
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
                    mIDetailClickListener?.likeFirstLevelComment(it)
                }
            }

            contentTv.setDebounceViewClickListener {
                mIDetailClickListener?.clickFirstLevelComment()
            }

            itemView.setDebounceViewClickListener {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_POSTS_COMMENT_DETAIL)
                        .withSerializable("postFirstLevelCommentModel", mModel)
                        .withSerializable("postsWatchModel", dataList[0] as PostsWatchModel)
                        .navigation()
            }

            postsAudioView.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    if (postsAudioView.isPlaying) {
                        mPlayingUrl = ""
                        mPlayingPosition = -1
                        SinglePlayer.stop(PostsDetailActivity.playerTag)
                        postsAudioView.setPlay(false)
                    } else {
                        mModel?.comment?.audios?.let {
                            mIDetailClickListener?.playAnotherSong()
                            mPlayingUrl = it[0]?.url ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(PostsDetailActivity.playerTag, mPlayingUrl)
                            postsAudioView.setPlay(true)
                        }
                    }
                }
            })

            postsSongView.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    if (postsSongView.isPlaying) {
                        mPlayingUrl = ""
                        mPlayingPosition = -1
                        SinglePlayer.stop(PostsDetailActivity.playerTag)
                        postsSongView.setPlay(false)
                    } else {
                        mModel?.comment?.songInfo?.let {
                            mIDetailClickListener?.playAnotherSong()
                            mPlayingUrl = it.playURL ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(PostsDetailActivity.playerTag, mPlayingUrl)
                            postsSongView.setPlay(true)
                        }
                    }
                }
            })

            contentTv.setDebounceViewClickListener {
                mClickContent?.invoke(mModel!!)
            }

            nineGridVp?.clickListener = { i, url, urlList ->
                goBigImageBrowse(i, urlList)
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

                if (mPlayingUrl.equals(mModel!!.comment!!.audios!![0].url) && !TextUtils.isEmpty(mPlayingUrl)) {
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

                if (mPlayingUrl.equals(mModel?.comment?.songInfo?.playURL) && !TextUtils.isEmpty(mPlayingUrl)) {
                    postsSongView.setPlay(true)
                } else {
                    postsSongView.setPlay(false)
                }
            }
        }

        fun refreshLike(pos: Int, model: PostFirstLevelCommentModel) {
            this.pos = pos
            this.mModel = model

            likeNum.text = model.comment?.likedCnt.toString()
            xinIv.isSelected = mModel?.isLiked ?: false
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
            nameTv.text = model.commentUser?.nicknameRemark
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

                if (mPlayingUrl.equals(mModel!!.comment!!.audios!![0].url) && !TextUtils.isEmpty(mPlayingUrl)) {
                    postsAudioView.setPlay(true)
                    mPlayingPosition = pos
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

            if ((mModel?.secondLevelComments?.size ?: 0) > 0) {
                replyNum.visibility = View.VISIBLE
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
            } else {
                replyNum.visibility = View.GONE
            }

            xinIv.isSelected = mModel?.isLiked ?: false

            if (mModel?.comment?.songInfo == null) {
                postsSongView.visibility = View.GONE
            } else {
                postsSongView.visibility = View.VISIBLE
                postsSongView.bindData(mModel?.comment?.songInfo)

                if (mPlayingUrl.equals(mModel?.comment?.songInfo?.playURL) && !TextUtils.isEmpty(mPlayingUrl)) {
                    postsSongView.setPlay(true)
                    mPlayingPosition = pos
                } else {
                    postsSongView.setPlay(false)
                }
            }
        }
    }

    private fun goBigImageBrowse(index: Int, pictures: List<String>) {
        BigImageBrowseFragment.open(true, mContext as FragmentActivity, object : DefaultImageBrowserLoader<String>() {
            override fun init() {

            }

            override fun load(imageBrowseView: ImageBrowseView, position: Int, item: String) {
                imageBrowseView.load(item)
            }

            override fun getInitCurrentItemPostion(): Int {
                return index
            }

            override fun getInitList(): List<String>? {
                return pictures
            }

            override fun loadMore(backward: Boolean, position: Int, data: String, callback: Callback<List<String>>?) {
                if (backward) {
                    // 向后加载
                }
            }

            override fun hasMore(backward: Boolean, position: Int, data: String): Boolean {
                return if (backward) {
                    return false
                } else false
            }

            override fun hasMenu(): Boolean {
                return false
            }
        })
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
    }
}