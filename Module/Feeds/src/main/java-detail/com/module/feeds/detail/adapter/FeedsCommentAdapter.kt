package com.module.feeds.detail.adapter

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.component.busilib.view.AvatarView
import com.component.person.utils.StringFromatUtils
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.model.CommentCountModel
import com.module.feeds.detail.model.FeedsCommentEmptyModel
import com.module.feeds.detail.model.FirstLevelCommentModel

class FeedsCommentAdapter(val mIsSecond: Boolean) : DiffAdapter<Any, RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_LIKE = 1
        const val TYPE_REF_CTN = 2
    }

    private val mCommentType = 0
    private val mCountType = 1
    private val mEmptyType = 2
    var mIFirstLevelCommentListener: IFirstLevelCommentListener? = null
    var mCommentNum: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        when (viewType) {
            mCommentType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_comment_item_view_layout, parent, false)
                return CommentHolder(view!!)
            }
            mCountType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_comment_num_item_view_layout, parent, false)
                return CommentNumHolder(view!!)
            }
            mEmptyType -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_empty_item_view_layout, parent, false)
                return EmptyHolder(view!!)
            }
            else -> return CommentHolder(view!!)
        }

    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.size > 0) {
            if (holder is CommentHolder) {
                val type = payloads[0] as Int
                val model = mDataList[position] as FirstLevelCommentModel
                holder.setData(model, position)
                if (type == TYPE_LIKE) {
                    holder.mLikeNum.text = StringFromatUtils.formatTenThousand(model.comment.likedCnt)
                    holder.mXinIv.isSelected = model.isLiked()
                } else if (type == TYPE_REF_CTN) {
                    holder.updateRefCount()
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun updatePart(position: Int, model: FirstLevelCommentModel?, refreshType: Int) {
        if (mDataList[position] is FirstLevelCommentModel) {
            if ((mDataList[position] as FirstLevelCommentModel).comment.commentID == model?.comment?.commentID) {
                notifyItemChanged(position, refreshType)
            } else {
                for (i in 0 until mDataList.size) {
                    if (mDataList[position] is FirstLevelCommentModel && (mDataList[position] as FirstLevelCommentModel).comment.commentID == model?.comment?.commentID) {
                        mDataList[i] = model!!
                        notifyItemChanged(i, refreshType)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CommentHolder) {
            holder.bindData(mDataList.get(position) as FirstLevelCommentModel, position)
        } else if (holder is CommentNumHolder) {
            holder.bindData(mCommentNum)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (dataList[position] is FirstLevelCommentModel) {
            return mCommentType
        } else if (dataList[position] is CommentCountModel) {
            return mCountType
        } else if (dataList[position] is FeedsCommentEmptyModel) {
            return mEmptyType
        }

        return mCommentType
    }

    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mCommenterAvaterIv: AvatarView = itemView.findViewById(R.id.commenter_avater_iv)
        private val mNameTv: ExTextView = itemView.findViewById(R.id.name_tv)
        private val mBottomDivider: View = itemView.findViewById(R.id.bottom_divider)
        private val mCommentTimeTv: ExTextView = itemView.findViewById(R.id.comment_time_tv)
        val mXinIv: ExImageView = itemView.findViewById(R.id.xin_iv)
        val mLikeNum: ExTextView  = itemView.findViewById(R.id.like_num)
        private val mContentTv: ExTextView = itemView.findViewById(R.id.content_tv)
        private val mReplyNum: ExTextView= itemView.findViewById(R.id.reply_num)

        var mModel: FirstLevelCommentModel? = null
        var mPosition: Int? = null

        fun updateRefCount() {
            mModel?.let {
                if (it.comment.commentType == 1) {
                    mContentTv.text = it.comment.content
                    if (it.comment.subCommentCnt > 0 && !mIsSecond) {
                        mReplyNum.visibility = View.VISIBLE
                        mReplyNum.text = "${StringFromatUtils.formatTenThousand(it.comment.subCommentCnt)}条回复"
                    } else {
                        mReplyNum.visibility = View.GONE
                    }
                } else if (it.comment.commentType == 2) {
                    mReplyNum.visibility = View.GONE
                }
            }
        }

        fun setData(model: FirstLevelCommentModel, position: Int) {
            mModel = model
            mPosition = position
        }

        fun bindData(model: FirstLevelCommentModel, position: Int) {
            mModel = model
            mPosition = position
            if (position == 0 && mIsSecond) {
                mBottomDivider.setBackgroundColor(U.getColor(R.color.transparent))
            } else {
                mBottomDivider.setBackgroundColor(U.getColor(R.color.black_trans_10))
            }
            mCommenterAvaterIv.bindData(mModel?.commentUser)

            mNameTv.text = UserInfoManager.getInstance().getRemarkName(model.commentUser.userId, model.commentUser?.nickname)
            mCommentTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.comment.createdAt
                    ?: 0L, System.currentTimeMillis())
            mLikeNum.text = StringFromatUtils.formatTenThousand(model.comment.likedCnt)

            mReplyNum.visibility = View.GONE
            MyLog.d("CommentHolder", "${model.comment.content}")
            if (model.comment.commentType == 1) {
                mContentTv.text = model.comment.content
                if (model.comment.subCommentCnt > 0 && !mIsSecond) {
                    mReplyNum.visibility = View.VISIBLE
                    mReplyNum.text = "${StringFromatUtils.formatTenThousand(model.comment.subCommentCnt)}条回复"
                } else {
                    mReplyNum.visibility = View.GONE
                }
            } else if (model.comment.commentType == 2) {
                mReplyNum.visibility = View.GONE
                if (model.comment.replyType == 1) {
                    mContentTv.text = model.comment.content
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
                    mContentTv.text = stringBuilder
                }
            }

            mXinIv.isSelected = model.isLiked()

            mXinIv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mIFirstLevelCommentListener?.onClickLike(mModel!!, !v!!?.isSelected, mPosition!!)
                }
            })

            mReplyNum.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mIFirstLevelCommentListener?.onClickMore(mModel!!)
                }
            })

            mContentTv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mIFirstLevelCommentListener?.onClickContent(mModel!!)
                }
            })

            mCommenterAvaterIv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mModel!!.commentUser?.userId?.let {
                        mIFirstLevelCommentListener?.onClickIcon(it)
                    }
                }
            })
        }
    }

    inner class CommentNumHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCountTv: ExTextView

        init {
            mCountTv = itemView.findViewById(com.module.feeds.R.id.count_tv)
        }

        fun bindData(num: Int) {
            if (mIsSecond) {
                mCountTv.text = "全部回复（${StringFromatUtils.formatTenThousand(num)}）"
            } else {
                mCountTv.text = "精彩评论（${StringFromatUtils.formatTenThousand(num)}）"
            }
        }
    }

    inner class EmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface IFirstLevelCommentListener {
        fun onClickLike(firstLevelCommentModel: FirstLevelCommentModel, like: Boolean, position: Int)
        fun onClickContent(firstLevelCommentModel: FirstLevelCommentModel)
        fun onClickIcon(userID: Int)
        fun onClickMore(firstLevelCommentModel: FirstLevelCommentModel)
    }
}