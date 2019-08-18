package com.module.feeds.rank.holder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.component.busilib.view.BitmapTextView
import com.component.person.utils.StringFromatUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsWatchModel

class FeedsDetailTopViewHolder(rootView: View,
                               onClickPlayListener: ((model: FeedsWatchModel?, position: Int) -> Unit)?,
                               onClickItemListener: ((model: FeedsWatchModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(rootView) {

    private val mCoverIv: SimpleDraweeView = itemView.findViewById(R.id.cover_iv)
    private val mEmptyCover: ExImageView = itemView.findViewById(R.id.empty_cover)
    private val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    val mSongPlayIv: ExImageView = itemView.findViewById(R.id.song_play_iv)
    private val mNameTv: TextView = itemView.findViewById(R.id.name_tv)
    private val mLikeNumTv: TextView = itemView.findViewById(R.id.like_num_tv)
    private val mSeqBtv: BitmapTextView = itemView.findViewById(R.id.seq_btv)

    var mModel: FeedsWatchModel? = null
    var mPosition: Int = 0

    init {
        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickItemListener?.invoke(mModel, mPosition)
            }
        })
        mSongPlayIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickPlayListener?.invoke(mModel, mPosition)
            }
        })
        mAvatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                onClickPlayListener?.invoke(mModel, mPosition)
            }
        })
    }

    fun bindData(position: Int, model: FeedsWatchModel?) {
        this.mPosition = position
        this.mModel = model

        if (model != null) {
            mCoverIv.visibility = View.VISIBLE
            mSongPlayIv.visibility = View.VISIBLE
            mLikeNumTv.visibility = View.VISIBLE
            mSeqBtv.visibility = View.VISIBLE
            mEmptyCover.visibility = View.GONE
            model.user?.let {
                val floatArray = FloatArray(8)
                floatArray[0] = 8.dp().toFloat()
                floatArray[1] = 8.dp().toFloat()
                floatArray[2] = 8.dp().toFloat()
                floatArray[3] = 8.dp().toFloat()
                AvatarUtils.loadAvatarByUrl(mCoverIv, AvatarUtils.newParamsBuilder(it.avatar)
                        .setCornerRadii(floatArray)
                        .setBlur(true)
                        .build())
                AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(it.avatar)
                        .setCircle(true)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(2.dp().toFloat())
                        .build())
                mNameTv.setTextColor(U.getColor(R.color.black_trans_80))
                mNameTv.text = UserInfoManager.getInstance().getRemarkName(it.userID, it.nickname)
            }
            mLikeNumTv.text = "${StringFromatUtils.formatTenThousand(model.starCnt)}赞"
            mSeqBtv.setText(model.rankSeq.toString())
        } else {
            mCoverIv.visibility = View.GONE
            mSongPlayIv.visibility = View.GONE
            mLikeNumTv.visibility = View.GONE
            mSeqBtv.visibility = View.GONE
            mEmptyCover.visibility = View.VISIBLE
            mNameTv.text = "无人占领"
            mNameTv.setTextColor(U.getColor(R.color.black_trans_30))
            FrescoWorker.loadImage(mAvatarIv,
                    ImageFactory.newResImage(R.drawable.feed_rank_empty_avatar)
                            .setCircle(true)
                            .setCornerRadius(8.dp().toFloat())
                            .setBorderColor(Color.WHITE)
                            .setBorderWidth(2.dp().toFloat())
                            .build<BaseImage>())
        }

    }
}