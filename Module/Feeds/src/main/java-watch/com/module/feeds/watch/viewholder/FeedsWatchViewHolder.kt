package com.module.feeds.watch.viewholder

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.component.busilib.view.BitmapTextView
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R

open class FeedsWatchViewHolder(it: View, l: FeedsListener?) : FeedViewHolder(it, l) {

    private val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    private val mNicknameTv: TextView = itemView.findViewById(R.id.nickname_tv)
    private val mTimeTv: TextView = itemView.findViewById(R.id.time_tv)
    private val mContentTv: TextView = itemView.findViewById(R.id.content_tv)
    private val mPeopleNumTv: BitmapTextView = itemView.findViewById(R.id.people_num_tv)
    private val mPeopleDesc:TextView = itemView.findViewById(R.id.people_desc)
    private val mHitIv: ImageView = itemView.findViewById(R.id.hit_iv)

    init {
        mHitIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.onClickHitListener(model)
            }
        })
    }

    override fun bindData(position: Int, watchModel: FeedsWatchModel) {
        super.bindData(position, watchModel)
        watchModel.user?.let {
            AvatarUtils.loadAvatarByUrl(mSongAreaBg, AvatarUtils.newParamsBuilder(it.avatar)
                    .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                    .setBlur(true)
                    .build())
            mRecordView.setAvatar(it.avatar ?: "")

            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(it.avatar)
                    .setCircle(true)
                    .build())
            mNicknameTv.text = UserInfoManager.getInstance().getRemarkName(it.userID
                    ?: 0, it.nickname)
        }

        if (watchModel.song?.needChallenge == true) {
            mHitIv.visibility = View.VISIBLE
            mPeopleNumTv.visibility = View.VISIBLE
            mPeopleDesc.visibility = View.VISIBLE
        } else {
            mHitIv.visibility = View.GONE
            mPeopleNumTv.visibility = View.GONE
            mPeopleDesc.visibility = View.GONE
        }

        mTimeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(watchModel.song?.createdAt
                ?: 0L, System.currentTimeMillis())
        var recomendTag = ""
        if (watchModel.song?.needRecommentTag == true) {
            recomendTag = "#小编推荐# "
        }
        var songTag = watchModel.song?.tags?.get(0)?.tagDesc ?: ""
        if (!TextUtils.isEmpty(songTag)) {
            songTag = "#$songTag# "
        }
        val title = watchModel.song?.title ?: ""
        if (TextUtils.isEmpty(recomendTag) && TextUtils.isEmpty(songTag) && TextUtils.isEmpty(title)) {
            mContentTv.visibility = View.GONE
        } else {
            val stringBuilder = SpanUtils()
                    .append(recomendTag).setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append(songTag).setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append(title).setForegroundColor(U.getColor(R.color.black_trans_80))
                    .create()
            mContentTv.visibility = View.VISIBLE
            mContentTv.text = stringBuilder
        }

        mPeopleNumTv.setText(watchModel.challengeCnt.toString())
    }


}