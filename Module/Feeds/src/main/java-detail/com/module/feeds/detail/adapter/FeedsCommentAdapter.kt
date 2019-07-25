package com.module.feeds.detail.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.module.feeds.detail.model.FirstLevelCommentModel


class FeedsCommentAdapter : DiffAdapter<FirstLevelCommentModel, FeedsCommentAdapter.CommentHolder>() {
    var mIFirstLevelCommentListener: IFirstLevelCommentListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsCommentAdapter.CommentHolder {
        val view = LayoutInflater.from(parent.context).inflate(com.module.feeds.R.layout.feeds_comment_item_view_layout, parent, false)
        return CommentHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size;
    }

    override fun onBindViewHolder(holder: FeedsCommentAdapter.CommentHolder, position: Int) {
        holder.bindData(mDataList.get(position), position)
    }

    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mCommenterAvaterIv: BaseImageView
        val mNameTv: ExTextView
        val mCommentTimeTv: ExTextView
        val mXinIv: ExImageView
        val mLikeNum: ExTextView
        val mContentTv: ExTextView
        val mReplyNum: ExTextView
        var mModel: FirstLevelCommentModel? = null
        var mPosition: Int? = null

        init {
            mCommenterAvaterIv = itemView.findViewById(com.module.feeds.R.id.commenter_avater_iv)
            mNameTv = itemView.findViewById(com.module.feeds.R.id.name_tv)
            mCommentTimeTv = itemView.findViewById(com.module.feeds.R.id.comment_time_tv)
            mXinIv = itemView.findViewById(com.module.feeds.R.id.xin_iv)
            mLikeNum = itemView.findViewById(com.module.feeds.R.id.like_num)
            mContentTv = itemView.findViewById(com.module.feeds.R.id.content_tv)
            mReplyNum = itemView.findViewById(com.module.feeds.R.id.reply_num)
        }

        fun bindData(model: FirstLevelCommentModel, position: Int) {
            mModel = model
            mPosition = position

            AvatarUtils.loadAvatarByUrl(mCommenterAvaterIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                    .setCircle(true)
                    .build())

            mNameTv.text = model.user?.nickname
            mCommentTimeTv.text = U.getDateTimeUtils().formatTimeStringForDate(model.comment.createdAt, "MM-dd HH:mm")
            mLikeNum.text = model.comment.starCnt.toString()
            mContentTv.text = model.comment.content
            if (model.comment.subCommentCnt > 0) {
                mReplyNum.visibility = View.VISIBLE
                mReplyNum.text = "${model.comment.subCommentCnt}条回复"
            } else {
                mReplyNum.visibility = View.GONE
            }

            mXinIv.isSelected = model.comment.isLiked

            mXinIv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mIFirstLevelCommentListener?.onClickLike(mModel!!, !v!!?.isSelected, mPosition!!)
                }
            })

            mReplyNum.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mIFirstLevelCommentListener?.onClickMoreComment(mModel!!)
                }
            })

            mContentTv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mIFirstLevelCommentListener?.onClickMoreComment(mModel!!)
                }
            })

            mCommenterAvaterIv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mModel!!.user?.userID?.let {
                        mIFirstLevelCommentListener?.onClickIcon(it)
                    }
                }
            })
        }
    }

    interface IFirstLevelCommentListener {
        fun onClickLike(firstLevelCommentModel: FirstLevelCommentModel, like: Boolean, position: Int)
        fun onClickMoreComment(firstLevelCommentModel: FirstLevelCommentModel)
        fun onClickIcon(userID: Int)
    }
}