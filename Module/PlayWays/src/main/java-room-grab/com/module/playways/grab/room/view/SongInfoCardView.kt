package com.module.playways.grab.room.view


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.grab.room.model.NewChorusLyricModel
import com.module.playways.room.song.model.MiniGameInfoModel
import com.component.lyrics.LyricsManager
import com.common.utils.U
import com.common.view.ex.ExTextView

import com.component.busilib.view.BitmapTextView
import com.module.playways.room.song.model.SongModel
import com.module.playways.R
import com.zq.live.proto.Common.StandPlayType

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.lang.Exception


/**
 * 转场时的歌曲信息页
 */
class SongInfoCardView : ConstraintLayout {

    val mTag = "SongInfoCardView"

    private val mSongNameTv: ExTextView
    private val mSongTagTv: TextView
    private val mWriterTv: TextView
    private val mCurrentSeq: BitmapTextView
    private val mTotalSeq: BitmapTextView
    private val mSongLyrics: ExTextView
    private val mGrabCd: ImageView
    private val mGrabChorus: ImageView
    private val mGrabPk: ImageView

    internal var mRotateAnimation: RotateAnimation? = null    // cd的旋转
    internal var mEnterTranslateAnimation: TranslateAnimation? = null // 飞入的进场动画
    internal var mLeaveTranslateAnimation: TranslateAnimation? = null // 飞出的离场动画

    private var mDisposable: Disposable? = null

    private var mChorusDrawable: Drawable
    private var mPKDrawable: Drawable
    private var mMiniGameDrawable: Drawable

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.grab_song_info_card_layout, this)

        mSongNameTv = findViewById(R.id.song_name_tv)
        mSongTagTv = findViewById(R.id.song_tag_tv)
        mWriterTv = findViewById(R.id.writer_tv)

        mCurrentSeq = findViewById(R.id.current_seq)
        mTotalSeq = findViewById(R.id.total_seq)
        mSongLyrics = findViewById(R.id.song_lyrics)
        mGrabCd = findViewById(R.id.grab_cd)
        mGrabChorus = findViewById(R.id.grab_chorus)
        mGrabPk = findViewById(R.id.grab_pk)

        mChorusDrawable = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#7088FF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setStrokeColor(Color.WHITE)
                .build()

        mPKDrawable = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#E55088"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setStrokeColor(Color.WHITE)
                .build()

        mMiniGameDrawable = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#61B14F"))
                .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
                .setStrokeWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setStrokeColor(Color.WHITE)
                .build()
    }

    // 该动画需要循环播放
    fun bindSongModel(curRoundSeq: Int, totalSeq: Int, songModel: SongModel?) {
        MyLog.d(mTag, "bindSongModel songModel=$songModel")
        if (songModel == null) {
            return
        }

        visibility = View.VISIBLE
        mSongLyrics.text = "歌词加载中..."
        mCurrentSeq.setText("" + curRoundSeq)
        mTotalSeq.setText("" + totalSeq)

        if (TextUtils.isEmpty(songModel.songDesc)) {
            mWriterTv.visibility = View.GONE
        } else {
            mWriterTv.visibility = View.VISIBLE
            mWriterTv.text = songModel.songDesc
        }

        when {
            songModel.playType == StandPlayType.PT_CHO_TYPE.value -> {
                // 合唱
                mSongNameTv.text = "" + songModel.displaySongName
                mGrabCd.clearAnimation()
                mGrabCd.visibility = View.GONE
                mGrabChorus.visibility = View.VISIBLE
                mGrabPk.visibility = View.GONE
                mSongTagTv.text = "合唱"
                mSongTagTv.visibility = View.VISIBLE
                mSongTagTv.background = mChorusDrawable
                // 入场动画
                animationGo(false)
            }
            songModel.playType == StandPlayType.PT_SPK_TYPE.value -> {
                // PK
                mSongNameTv.text = "" + songModel.displaySongName
                mGrabCd.clearAnimation()
                mGrabCd.visibility = View.GONE
                mGrabChorus.visibility = View.GONE
                mGrabPk.visibility = View.VISIBLE
                mSongTagTv.text = "PK"
                mSongTagTv.visibility = View.VISIBLE
                mSongTagTv.background = mPKDrawable
                // 入场动画
                animationGo(false)
            }
            songModel.playType == StandPlayType.PT_MINI_GAME_TYPE.value -> {
                // 小游戏
                if (songModel.miniGame != null) {
                    mSongNameTv.text = "【" + songModel.miniGame.gameName + "】"
                } else {
                    MyLog.w(mTag, "bindSongModel" + " 给的是小游戏类型，但是结构给的空？？？服务器BYD和佳胜")
                }
                mGrabCd.clearAnimation()
                mGrabCd.visibility = View.GONE
                // 和合唱一样的卡片
                mGrabChorus.visibility = View.VISIBLE
                mGrabPk.visibility = View.GONE
                mSongTagTv.text = "双人游戏"
                mSongTagTv.visibility = View.VISIBLE
                mSongTagTv.background = mMiniGameDrawable
                // 入场动画
                animationGo(false)
            }
            else -> {
                // 普通
                mSongNameTv.text = "《" + songModel.itemName + "》"
                mGrabCd.visibility = View.VISIBLE
                mGrabChorus.visibility = View.GONE
                mGrabPk.visibility = View.GONE
                mSongTagTv.visibility = View.GONE
                // 入场动画
                animationGo(true)
            }
        }
        playLyric(songModel)
    }

    fun playLyric(songModel: SongModel?) {
        if (songModel == null) {
            MyLog.w(mTag, "songModel 是空的")
            return
        }
        if (songModel.playType == StandPlayType.PT_MINI_GAME_TYPE.value) {
            val gameInfoModel = songModel.miniGame
            if (gameInfoModel != null) {
                mSongLyrics.text = gameInfoModel.displayGameRule
            } else {
                MyLog.w(mTag, "miniGameInfo 是空的")
            }
        } else {
            LyricsManager
                    .loadGrabPlainLyric(songModel.standLrc)
                    .subscribe(Consumer<String> { o ->
                        mSongLyrics.text = ""
                        if (U.getStringUtils().isJSON(o)) {
                            val newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel::class.java)
                            var i = 0
                            while (i < newChorusLyricModel.items.size && i < 2) {
                                mSongLyrics.append(newChorusLyricModel.items[i].words)
                                if (i == 0) {
                                    mSongLyrics.append("\n")
                                }
                                i++
                            }
                        } else {
                            mSongLyrics.text = o
                        }
                    }, Consumer<Throwable> { throwable -> MyLog.e(mTag, throwable) })
        }
    }

    /**
     * 入场动画
     *
     * @param isFlag 标记cd是否转动
     */
    private fun animationGo(isFlag: Boolean) {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation((-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f, 0.0f)
            mEnterTranslateAnimation!!.duration = 200
        }
        this.startAnimation(mEnterTranslateAnimation)

        if (isFlag) {
            if (mRotateAnimation == null) {
                mRotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                mRotateAnimation!!.duration = 3000
                mRotateAnimation!!.repeatCount = Animation.INFINITE
                mRotateAnimation!!.fillAfter = true
                mRotateAnimation!!.interpolator = AccelerateDecelerateInterpolator()
            }
            mGrabCd.startAnimation(mRotateAnimation)
        }
    }

    fun hide() {
        if (this != null && this.visibility == View.VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = TranslateAnimation(0.0f, U.getDisplayUtils().screenWidth.toFloat(), 0.0f, 0.0f)
                mLeaveTranslateAnimation!!.duration = 200
            }
            mLeaveTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    mGrabCd.clearAnimation()
                    clearAnimation()
                    if (mRotateAnimation != null) {
                        mRotateAnimation!!.cancel()
                    }
                    visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
            this.startAnimation(mLeaveTranslateAnimation)
        } else {
            if (mRotateAnimation != null) {
                mRotateAnimation!!.cancel()
            }
            mGrabCd.clearAnimation()
            clearAnimation()
            visibility = View.GONE
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation!!.setAnimationListener(null)
            mEnterTranslateAnimation!!.cancel()
        }
        if (mRotateAnimation != null) {
            mRotateAnimation!!.setAnimationListener(null)
            mRotateAnimation!!.cancel()
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation!!.setAnimationListener(null)
            mLeaveTranslateAnimation!!.cancel()
        }
        if (mDisposable != null) {
            mDisposable!!.dispose()
        }
    }
}

