package com.component.person.producation.holder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView

import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.image.model.oss.OssImgFactory
import com.common.log.MyLog
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.busilib.SkrConfig
import com.component.person.producation.adapter.ProducationAdapter
import com.component.person.producation.model.ProducationModel
import com.facebook.drawee.view.SimpleDraweeView

class ProducationHolder(itemView: View, mIsSelf: Boolean,
                        var mOnClickDeleListener: ((position: Int, model: ProducationModel?) -> Unit)?,
                        var mOnClickShareListener: ((position: Int, model: ProducationModel?) -> Unit)?,
                        var mOnClickPlayListener: ((view: View, play: Boolean, position: Int, model: ProducationModel?) -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    val TAG = "ProducationHolder"

    internal var mPosition: Int = 0
    internal var mIsPlay: Boolean = false
    internal var mModel: ProducationModel? = null

    private val mCoverIv: SimpleDraweeView = itemView.findViewById(R.id.cover_iv)
    private val mCoverMask: ExImageView = itemView.findViewById(R.id.cover_mask)
    private val mPlayBackIv: ExImageView = itemView.findViewById(R.id.play_back_iv)
    internal val mSongNameTv: ExTextView = itemView.findViewById(R.id.song_name_tv)
    private val mSongOwnerTv: ExTextView = itemView.findViewById(R.id.song_owner_tv)
    private val mShareArea: RelativeLayout = itemView.findViewById(R.id.share_area)
    private val mDeleArea: RelativeLayout = itemView.findViewById(R.id.dele_area)
    private val mPlayNumArea: RelativeLayout = itemView.findViewById(R.id.play_num_area)
    private val mPlayNumTv: TextView = itemView.findViewById(R.id.play_num_tv)


    init {
        itemView.tag = this
        if (mIsSelf) {
            mDeleArea.visibility = View.VISIBLE
        } else {
            mDeleArea.visibility = View.GONE
        }

        mShareArea.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mOnClickShareListener?.invoke(mPosition, mModel)
            }
        })

        mDeleArea.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mOnClickDeleListener?.invoke(mPosition, mModel)
            }
        })

        mCoverMask.setOnClickListener(object : DebounceViewClickListener(1000) {
            override fun clickValid(v: View) {
                if (mIsPlay) {
                    mOnClickPlayListener?.invoke(v, !mIsPlay, mPosition, mModel)
                } else {
                    mOnClickPlayListener?.invoke(v, !mIsPlay, mPosition, mModel)
                }
            }
        })

        mPlayBackIv.setOnClickListener(object : DebounceViewClickListener(1000) {
            override fun clickValid(v: View) {
                if (mIsPlay) {
                    mOnClickPlayListener?.invoke(v, !mIsPlay, mPosition, mModel)
                } else {
                    mOnClickPlayListener?.invoke(v, !mIsPlay, mPosition, mModel)
                }
            }
        })
    }

    fun bindData(position: Int, model: ProducationModel?, isPlay: Boolean) {
        MyLog.d(TAG, "bindData position=$position model=$model isPlay=$isPlay")
        this.mPosition = position
        this.mModel = model
        this.mIsPlay = isPlay

        if (model != null) {
            mSongNameTv.text = "" + model.name!!
            mSongOwnerTv.text = MyUserInfoManager.nickName
            mPlayNumTv.text = "" + model.playCnt + "次播放"
        }

        mSongOwnerTv.text = UserInfoManager.getInstance().getRemarkName(model!!.userID, model.nickName)
        if (!TextUtils.isEmpty(model.cover)) {
            FrescoWorker.loadImage(mCoverIv,
                    ImageFactory.newPathImage(model.cover)
                            .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.w).build())
                            .build<BaseImage>())
        } else {
            FrescoWorker.loadImage(mCoverIv,
                    ImageFactory.newResImage(R.drawable.xuanzegequ_wufengmian)
                            .setCornerRadius(U.getDisplayUtils().dip2px(7f).toFloat())
                            .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_160.w).build())
                            .build<BaseImage>())
        }
        if (SkrConfig.getInstance().worksShareOpen()) {
            mShareArea.visibility = View.VISIBLE
        } else {
            mShareArea.visibility = View.GONE
        }
        setPlayBtn(isPlay)
    }

    fun setPlayBtn(play: Boolean) {
        mIsPlay = play
        if (play) {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_pause)
        } else {
            mPlayBackIv.setBackgroundResource(R.drawable.grab_works_play)
        }
    }

    fun setPlaycnt(playCnt: Int) {
        mPlayNumTv.text = "" + playCnt + "次播放"
    }
}
