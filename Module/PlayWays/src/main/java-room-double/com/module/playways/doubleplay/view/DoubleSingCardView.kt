package com.module.playways.doubleplay.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.animation.TranslateAnimation
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo


class DoubleSingCardView : ConstraintLayout {
    var mCurMusic: LocalCombineRoomMusic? = null
    var mSongOwnerIv: BaseImageView
    var mSongNameTv: TextView
    var mNextSongTipTv: TextView
    var mCutSongTv: ExTextView
    val mDoubleSelfSingCardView: DoubleSelfSingCardView by lazy {
        DoubleSelfSingCardView(this@DoubleSingCardView, mDoubleRoomData)
    }
    var mEnterTranslateAnimation: TranslateAnimation? = null
    var mScaleAnimation: ScaleAnimation? = null
    var mDoubleRoomData: DoubleRoomData? = null
    var mOnClickNextSongListener: (() -> Unit)? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflate(context, R.layout.double_sing_card_view_layout, this)
        mSongOwnerIv = findViewById(com.module.playways.R.id.song_owner_iv)
        mSongNameTv = findViewById(com.module.playways.R.id.song_name_tv)
        mNextSongTipTv = findViewById(com.module.playways.R.id.next_song_tip_tv)
        mCutSongTv = findViewById(com.module.playways.R.id.cut_song_tv)

        mCutSongTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mOnClickNextSongListener?.invoke()
            }
        })
    }

    fun playLyric(roomData: DoubleRoomData, mCur: LocalCombineRoomMusic?, mNext: String?, hasNext: Boolean) {
        this.mCurMusic = mCur
        this.mDoubleRoomData = roomData

        AvatarUtils.loadAvatarByUrl(mSongOwnerIv,
                AvatarUtils.newParamsBuilder(roomData.getAvatarById(mCur?.userID ?: 0))
                        .setBorderColor(U.getColor(R.color.white))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setCircle(true)
                        .build())
        mSongNameTv?.text = "《${mCur?.music?.itemName}》"
        mDoubleSelfSingCardView.playLyric(mCur, roomData)
        updateNextSongDec(mNext, hasNext)
    }

    fun playLyric(localGameItemInfo: LocalGameItemInfo?) {
        mSongNameTv?.text = localGameItemInfo?.desc
        mDoubleSelfSingCardView.showDoubleGame(localGameItemInfo)
    }

    /**
     * 锁发生变化
     */
    fun updateLockState() {
        AvatarUtils.loadAvatarByUrl(mSongOwnerIv,
                AvatarUtils.newParamsBuilder(mDoubleRoomData?.getAvatarById(mCurMusic?.userID ?: 0))
                        .setBorderColor(U.getColor(R.color.white))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setCircle(true)
                        .build())

        mDoubleSelfSingCardView.updateLockState()
    }

    /**
     * 进入中心
     */
    fun goOut() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation(0.0f, (-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f)
            mEnterTranslateAnimation?.setDuration(300)
        }
        this.startAnimation(mEnterTranslateAnimation)

        mEnterTranslateAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })
    }

    fun centerScale() {
        visibility = View.VISIBLE
        if (mScaleAnimation == null) {
            mScaleAnimation = ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            mScaleAnimation?.setDuration(300)
        }

        mScaleAnimation?.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {

            }
        })

        this.startAnimation(mScaleAnimation)
    }

    fun updateNextSongDec(mNext: String?, hasNext: Boolean) {
        if (TextUtils.isEmpty(mNext)) {
            mNextSongTipTv?.text = "没有歌曲啦～"
        } else {
            mNextSongTipTv?.text = mNext
        }
    }
}