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
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.grab.room.view.video.DoubleSelfSingCardView
import com.module.playways.room.song.model.SongModel


class DoubleSingCardView : ConstraintLayout {
    companion object {
        const val TAG = "DoubleSingCardView"
        const val TAG_ADD_SONG = 1
        const val TAG_CHANGE_SONG = 2
    }

    var mCurMusic: LocalCombineRoomMusic? = null
    var mSongOwnerIv: BaseImageView
    var mSongNameTv: TextView
    var mNextSongTipTv: TextView
    var mCutSongTv: ExTextView
    var mDoubleSelfSingCardView: DoubleSelfSingCardView
    var mEnterTranslateAnimation: TranslateAnimation? = null
    var mScaleAnimation: ScaleAnimation? = null
    var mListener: DoubleSingCardView.Listener? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.double_sing_card_view_layout, this)
        mSongOwnerIv = findViewById(com.module.playways.R.id.song_owner_iv)
        mSongNameTv = findViewById(com.module.playways.R.id.song_name_tv)
        mNextSongTipTv = findViewById(com.module.playways.R.id.next_song_tip_tv)
        mCutSongTv = findViewById(com.module.playways.R.id.cut_song_tv)
        mDoubleSelfSingCardView = DoubleSelfSingCardView(this@DoubleSingCardView)

        mCutSongTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (mCutSongTv.tag == TAG_ADD_SONG) {
                    mListener?.clickToAddMusic()
                } else if (mCutSongTv.tag == TAG_CHANGE_SONG) {
                    mListener?.clickChangeSong()
                }
            }
        })
    }

    fun playLyric(roomData: DoubleRoomData, avatar: String = "", mCur: LocalCombineRoomMusic?, mNext: String?, hasNext: Boolean) {
        this.mCurMusic = mCur
        AvatarUtils.loadAvatarByUrl(mSongOwnerIv,
                AvatarUtils.newParamsBuilder(avatar)
                        .setBorderColor(U.getColor(R.color.white))
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setCircle(true)
                        .build())
        mSongNameTv?.text = mCur?.music?.itemName
        mDoubleSelfSingCardView.playLyric(mCur, roomData)
        updateNextSongDec(mNext, hasNext)
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

        if (hasNext) {
            if (mCurMusic?.userID == MyUserInfoManager.getInstance().uid.toInt()) {
                mCutSongTv.tag = TAG_CHANGE_SONG
                mCutSongTv?.text = "切歌"
                mCutSongTv.visibility = View.VISIBLE
            } else {
                mCutSongTv.visibility = View.GONE
            }
        } else {
            mCutSongTv.tag = TAG_ADD_SONG
            mCutSongTv?.text = "去点歌"
            mCutSongTv.visibility = View.VISIBLE
        }
    }

    abstract class Listener {
        abstract fun clickChangeSong()
        abstract fun clickToAddMusic()
    }

}