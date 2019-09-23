package com.module.posts.detail.adapter

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.Barrier
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.view.setDebounceViewClickListener
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.component.busilib.view.AvatarView
import com.component.relation.view.DefaultFollowView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.detail.model.PostsSecondLevelCommentModel
import com.module.posts.view.*

class PostsCommentDetailAdapter : DiffAdapter<Any, RecyclerView.ViewHolder> {
    companion object {
        val REFRESH_COMMENT_CTN = 0
        val DESTROY_HOLDER = 1
        val REFRESH_PLAY_STATE = 2
        val playerTag = "PostsCommentDetailAdapter"
    }

    val mPostsType = 0
    val mCommentType = 1

    //评论数量
    var mCommentCtn = 0

    var mPlayingUrl = ""

    var mPlayingPosition = -1

    var mClickContentListener: ((PostsSecondLevelCommentModel) -> Unit)? = null

    constructor() : super() {
        SinglePlayer.addCallback(playerTag, object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                mPlayingUrl = ""
                notifyItemChanged(mPlayingPosition, REFRESH_PLAY_STATE)
                mPlayingPosition = -1
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        when (viewType) {
            mPostsType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_comment_detail_posts_type_layout, parent, false)
                return PostsFirstLevelCommentHolder(view!!)
            }
            mCommentType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.posts_comment_detail_item_view_layout, parent, false)
                return PostsSecondLevelCommentHolder(view!!)
            }

            else -> return PostsFirstLevelCommentHolder(view!!)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            if (holder is PostsFirstLevelCommentHolder) {
                holder.bindData(position, mDataList[position] as PostFirstLevelCommentModel)

            } else if (holder is PostsSecondLevelCommentHolder) {
                holder.bindData(position, mDataList[position] as PostsSecondLevelCommentModel)
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
            if (holder is PostsFirstLevelCommentHolder) {
                holder.refreshCommentCtn(position, mDataList[position] as PostFirstLevelCommentModel)
            }
        } else if (refreshType == DESTROY_HOLDER) {
            if (holder is PostsFirstLevelCommentHolder) {
                holder.destroyHolder(position, mDataList[position] as PostFirstLevelCommentModel)
            }
        } else if (refreshType == PostsCommentAdapter.REFRESH_PLAY_STATE) {
            if (holder is PostsFirstLevelCommentHolder) {
                holder.refreshPlayState(position, mDataList[position] as PostFirstLevelCommentModel)
            } else if (holder is PostsSecondLevelCommentHolder) {
                holder.refreshPlayState(position, mDataList[position] as PostsSecondLevelCommentModel)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    inner class PostsFirstLevelCommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var followTv: DefaultFollowView
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

        var isGetRelation: Boolean = false

        init {
            followTv = itemView.findViewById(R.id.follow_tv)
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
                    if (postsAudioView.isPlaying) {
                        mPlayingUrl = ""
                        mPlayingPosition = -1
                        SinglePlayer.stop(playerTag)
                        postsAudioView.setPlay(false)
                    } else {
                        mModel?.comment?.audios?.let {
                            mPlayingUrl = it[0]?.url ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(playerTag, mPlayingUrl)
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
                        SinglePlayer.stop(playerTag)
                        postsSongView.setPlay(false)
                    } else {
                        mModel?.comment?.songInfo?.let {
                            mPlayingUrl = it.playURL ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(playerTag, mPlayingUrl)
                            postsSongView.setPlay(true)
                        }
                    }
                }
            })
        }

        fun refreshCommentCtn(pos: Int, model: PostFirstLevelCommentModel) {
            this.pos = pos
            this.mModel = model

            commentCtnTv.text = "评论(${mCommentCtn}条)"
            if (mCommentCtn == 0) {
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

        fun destroyHolder(pos: Int, model: PostFirstLevelCommentModel) {
            this.pos = pos
            this.mModel = model
            followTv.destroy()
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
                content.text = model.comment?.content
            } else {
                content.visibility = View.GONE
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

            // 图片
            if (mModel?.comment?.pictures.isNullOrEmpty()) {
                nineGridVp.visibility = View.GONE
            } else {
                nineGridVp.visibility = View.VISIBLE
                nineGridVp.setUrlList(mModel?.comment?.pictures!!)
            }

            followTv.userID = mModel?.commentUser?.userId

            if (!isGetRelation) {
                followTv.getRelation()
                isGetRelation = true
            }

            commentCtnTv.text = "评论(${mCommentCtn}条)"

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

    inner class PostsSecondLevelCommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var commenterAvaterIv: AvatarView
        var nameTv: ExTextView
        var commentTimeTv: ExTextView
        var contentTv: ExTextView
        var postsAudioView: PostsCommentAudioView
        var nineGridVp: PostsNineGridLayout
        var postsSongView: PostsSongView
        var postsBarrier: Barrier

        var pos: Int = -1
        var mModel: PostsSecondLevelCommentModel? = null

        init {
            commenterAvaterIv = itemView.findViewById(R.id.commenter_avater_iv)
            nameTv = itemView.findViewById(R.id.name_tv)
            commentTimeTv = itemView.findViewById(R.id.comment_time_tv)
            contentTv = itemView.findViewById(R.id.content_tv)
            postsSongView = itemView.findViewById(R.id.posts_song_view)
            postsAudioView = itemView.findViewById(R.id.posts_audio_view)
            nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
            postsBarrier = itemView.findViewById(R.id.posts_barrier)

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
                    if (postsAudioView.isPlaying) {
                        mPlayingUrl = ""
                        mPlayingPosition = -1
                        SinglePlayer.stop(playerTag)
                        postsAudioView.setPlay(false)
                    } else {
                        mModel?.comment?.audios?.let {
                            mPlayingUrl = it[0]?.url ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(playerTag, mPlayingUrl)
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
                        SinglePlayer.stop(playerTag)
                        postsSongView.setPlay(false)
                    } else {
                        mModel?.comment?.songInfo?.let {
                            mPlayingUrl = it.playURL ?: ""
                            mPlayingPosition = pos
                            SinglePlayer.startPlay(playerTag, mPlayingUrl)
                            postsSongView.setPlay(true)
                        }
                    }
                }
            })

            contentTv.setDebounceViewClickListener {
                mClickContentListener?.invoke(mModel!!)
            }
        }

        fun refreshPlayState(pos: Int, model: PostsSecondLevelCommentModel) {
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

        fun bindData(pos: Int, model: PostsSecondLevelCommentModel) {
            this.pos = pos
            this.mModel = model

            commenterAvaterIv.bindData(model.commentUser)
            nameTv.text = model.commentUser.nicknameRemark

            commentTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.comment.createdAt, System.currentTimeMillis())

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

            // 图片
            if (mModel?.comment?.pictures.isNullOrEmpty()) {
                nineGridVp.visibility = View.GONE
            } else {
                nineGridVp.visibility = View.VISIBLE
                nineGridVp.setUrlList(mModel?.comment?.pictures!!)
            }

            if (!TextUtils.isEmpty(model.comment.content)) {
                contentTv.visibility = View.VISIBLE
                if (model.comment.replyType == 1) {
                    contentTv.text = model.comment.content
                } else if (model.comment.replyType == 2) {
                    val spanUtils = SpanUtils()
                            .append(model.commentUser.nickname.toString()).setClickSpan(object : ClickableSpan() {
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
                            .append(model.replyUser.nickname.toString()).setClickSpan(object : ClickableSpan() {
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
                            .append(model.comment.content).setForegroundColor(U.getColor(R.color.black))
                    val stringBuilder = spanUtils.create()
                    contentTv.text = stringBuilder
                }
            } else {
                contentTv.visibility = View.GONE
            }

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

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return mPostsType
        }

        return mCommentType
    }
}