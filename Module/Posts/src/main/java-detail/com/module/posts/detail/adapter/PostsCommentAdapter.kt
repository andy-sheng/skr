package com.module.posts.detail.adapter

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.constraint.Barrier
import android.support.constraint.Group
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
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.recyclerview.DiffAdapter
import com.component.busilib.view.AvatarView
import com.component.relation.view.DefaultFollowView
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.view.*
import com.module.posts.watch.model.PostsRedPkgModel
import com.module.posts.watch.model.PostsWatchModel

class PostsCommentAdapter : DiffAdapter<Any, RecyclerView.ViewHolder>() {
    companion object {
        val REFRESH_COMMENT_CTN = 0
        val DESTROY_HOLDER = 1
    }

    private val mPostsType = 0
    private val mCommentType = 1

    //评论数量
    var mCommentCtn = 0

    var mIDetailClickListener: IDetailClickListener? = null

    val mLikeDrawable = DrawableCreator.Builder()
            .setSelectedDrawable(U.getDrawable(R.drawable.posts_like_selected_icon))
            .setUnSelectedDrawable(U.getDrawable(R.drawable.posts_black_big_icon))
            .build()

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
                holder.bindData(position, mDataList[position] as PostFirstLevelCommentModel)
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
        } else if (refreshType == DESTROY_HOLDER) {
            if (holder is PostsHolder) {
                holder.destroyHolder(position, mDataList[position] as PostsWatchModel)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PostsHolder) {
            holder.bindData(position, mDataList[position] as PostsWatchModel)
        } else if (holder is PostsCommentHolder) {
            holder.bindData(position, mDataList[position] as PostFirstLevelCommentModel)
        }
    }

    inner class PostsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var followTv: DefaultFollowView
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

        init {
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
                mIDetailClickListener?.replayPosts()
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
        }

        fun refreshCommentCnt(pos: Int, model: PostsWatchModel) {
            this.pos = pos
            this.mModel = model

            commentCtnTv.text = "评论（${mCommentCtn}条）"

            if (mCommentCtn == 0) {
                emptyTv.visibility = View.VISIBLE
            } else {
                emptyTv.visibility = View.GONE
            }
        }

        fun destroyHolder(pos: Int, model: PostsWatchModel) {
            this.pos = pos
            this.mModel = model
            followTv.destroy()
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

            commentCtnTv.text = "评论(${mCommentCtn}条)"

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
                postsAudioView.bindData(mModel?.posts?.audios!![0].duration)
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
            if (mModel?.numeric == null) {
                postsCommentTv.text = mModel?.numeric?.commentCnt.toString()
                postsLikeTv.text = mModel?.numeric?.starCnt.toString()
            } else {
                postsCommentTv.text = "0"
                postsLikeTv.text = "0"
            }

            postsLikeTv.isSelected = mModel?.isLiked ?: false

            followTv.userID = mModel?.user?.userId

            if (!isGetRelation) {
                followTv.getRelation()
                isGetRelation = true
            }

            commentCtnTv.text = "评论（${mCommentCtn}条）"
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
        var bottomBarrier: Barrier
        var pos: Int = -1
        var mModel: PostFirstLevelCommentModel? = null

        init {
            commenterAvaterIv = itemView.findViewById(R.id.commenter_avater_iv)
            nameTv = itemView.findViewById(R.id.name_tv)
            commentTimeTv = itemView.findViewById(R.id.comment_time_tv)
            xinIv = itemView.findViewById(R.id.xin_iv)
            likeNum = itemView.findViewById(R.id.like_num)
            contentTv = itemView.findViewById(R.id.content_tv)
            postsAudioView = itemView.findViewById(R.id.posts_audio_view)
            nineGridVp = itemView.findViewById(R.id.nine_grid_vp)
            postsBarrier = itemView.findViewById(R.id.posts_barrier)
            replyNum = itemView.findViewById(R.id.reply_num)
            bottomBarrier = itemView.findViewById(R.id.bottom_barrier)

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
        }

        fun bindData(pos: Int, model: PostFirstLevelCommentModel) {
            this.pos = pos
            this.mModel = model

            commenterAvaterIv.bindData(model.commentUser)
            nameTv.text = model.commentUser.nicknameRemark
            commentTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.comment.createdAt, System.currentTimeMillis())
            likeNum.text = model.comment.likedCnt.toString()

            if (!TextUtils.isEmpty(model.comment.content)) {
                contentTv.text = model.comment.content
                contentTv.visibility = View.VISIBLE
            } else {
                contentTv.visibility = View.GONE
            }

            if (mModel?.comment?.audios.isNullOrEmpty()) {
                postsAudioView.visibility = View.GONE
            } else {
                postsAudioView.visibility = View.VISIBLE
                postsAudioView.bindData(mModel!!.comment!!.audios!![0].duration)
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
                        .append(" 等人 共").setForegroundColor(U.getColor(R.color.black))
                        .append("${mModel?.secondLevelComments?.size}条回复>").setForegroundColor(Color.parseColor("#FF6295C4"))

                val stringBuilder = spanUtils.create()
                replyNum.text = stringBuilder
            } else {
                replyNum.visibility = View.GONE
            }

            xinIv.isSelected = mModel?.isIsLiked ?: false
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return mPostsType
        }

        return mCommentType
    }

    interface IDetailClickListener {
        fun replayPosts()

        fun likePosts(model: PostsWatchModel)

        fun clickFirstLevelComment()

        fun likeFirstLevelComment(model: PostFirstLevelCommentModel)
    }
}