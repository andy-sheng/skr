package com.module.feeds.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.module.feeds.watch.view.RecordAnimationView
import com.facebook.drawee.view.SimpleDraweeView
import com.common.view.ex.ExConstraintLayout
import com.component.busilib.view.BitmapTextView
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsWatchModel

class FeedsWatchViewHolder(item: View,
                           var onClickMoreListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickLikeListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickCommentListener: ((watchModel: FeedsWatchModel?) -> Unit)?,
                           var onClickHitListener: ((watchModel: FeedsWatchModel?) -> Unit)?) : RecyclerView.ViewHolder(item) {

    val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    val mNicknameTv: TextView = itemView.findViewById(R.id.nickname_tv)
    val mTimeTv: TextView = itemView.findViewById(R.id.time_tv)
    val mMoreIv: ImageView = itemView.findViewById(R.id.more_iv)
    val mContentTv: TextView = itemView.findViewById(R.id.content_tv)
    val mTagArea: ExConstraintLayout = itemView.findViewById(R.id.tag_area)
    val mTagIv: ImageView = itemView.findViewById(R.id.tag_iv)
    val mTagTv: TextView = itemView.findViewById(R.id.tag_tv)
    val mSongAreaBg: SimpleDraweeView = itemView.findViewById(R.id.song_area_bg)
    val mRecordView: RecordAnimationView = itemView.findViewById(R.id.record_view)
    val mLikeNumTv: TextView = itemView.findViewById(R.id.like_num_tv)
    val mCommentNumTv: TextView = itemView.findViewById(R.id.comment_num_tv)

    val mHitIv: ImageView = itemView.findViewById(R.id.hit_iv)
    val mPeopleNumTv: BitmapTextView = itemView.findViewById(R.id.people_num_tv)

    var mPosition: Int = 0
    var model: FeedsWatchModel? = null

    init {
        mMoreIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickMoreListener?.invoke(model)
            }
        })

        mLikeNumTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickLikeListener?.invoke(model)
            }
        })

        mCommentNumTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickCommentListener?.invoke(model)
            }
        })

        mHitIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickHitListener?.invoke(model)
            }
        })
    }

    fun bindData(position: Int, watchModel: FeedsWatchModel) {
        this.mPosition = position
        this.model = watchModel

        AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCircle(true)
                .build())
        mNicknameTv.text = MyUserInfoManager.getInstance().nickName

        AvatarUtils.loadAvatarByUrl(mSongAreaBg, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                .setBlur(true)
                .build())
        mRecordView.bindData(MyUserInfoManager.getInstance().avatar)
        mPeopleNumTv.setText("2019")

    }
}