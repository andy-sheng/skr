package com.module.playways.race.room.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.log.MyLog
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.race.room.model.RaceWantSingInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RaceSelectSongView : ExConstraintLayout {
    val mTag = "RaceSelectSongView"
    var bg: ImageView
    private val progressBg: ExImageView
    private val progressBar: ProgressBar
    private val forthSongItem: RaceSelectSongItemView
    private val firstSongItem: RaceSelectSongItemView
    private val secondSongItem: RaceSelectSongItemView
    private val thirdSongItem: RaceSelectSongItemView
    var countDownTv: ExTextView
    var blurBg: ImageView
    private val itemList: ArrayList<RaceSelectSongItemView> = ArrayList()
    var animator: ValueAnimator? = null
    var mRoomData: RaceRoomData? = null
    var mSelectCall: ((Int) -> Unit)? = null
    var mNoSelectCall: (() -> Unit)? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_select_song_layout, this)
        bg = findViewById(R.id.bg)
        progressBg = findViewById(R.id.progress_bg)
        progressBar = findViewById(R.id.progress_bar)
        firstSongItem = findViewById(R.id.first_song_item)
        secondSongItem = findViewById(R.id.second_song_item)
        thirdSongItem = findViewById(R.id.third_song_item)
        forthSongItem = findViewById(R.id.forth_song_item)
        countDownTv = rootView.findViewById(R.id.count_down_tv)
        blurBg = rootView.findViewById(R.id.blur_bg)

        itemList.add(firstSongItem)
        itemList.add(secondSongItem)
        itemList.add(thirdSongItem)
        itemList.add(forthSongItem)
        progressBar.max = 360
        progressBar.progress = 0
        firstSongItem.setAnimateDebounceViewClickListener {
            firstSongItem.getSong()?.let {
                mSelectCall?.invoke(1)
                mNoSelectCall = null
            }
        }

        secondSongItem.setAnimateDebounceViewClickListener {
            secondSongItem.getSong()?.let {
                mSelectCall?.invoke(2)
                mNoSelectCall = null
            }
        }

        thirdSongItem.setAnimateDebounceViewClickListener {
            thirdSongItem.getSong()?.let {
                mSelectCall?.invoke(3)
                mNoSelectCall = null
            }
        }

        forthSongItem.setAnimateDebounceViewClickListener {
            forthSongItem.getSong()?.let {
                mSelectCall?.invoke(4)
                mNoSelectCall = null
            }
        }
    }

    fun setRoomData(roomData: RaceRoomData, selectCall: ((Int) -> Unit)) {
        mRoomData = roomData
        firstSongItem.setRaceRoomData(roomData)
        secondSongItem.setRaceRoomData(roomData)
        thirdSongItem.setRaceRoomData(roomData)
        forthSongItem.setRaceRoomData(roomData)
        mSelectCall = selectCall
    }

    var scaleAnimatorSet: AnimatorSet? = null

    fun setSongName(noSelectCall: (() -> Unit)?) {
        progressBar.progress = 0
        scaleAnimatorSet = AnimatorSet()
        val scaleX = ObjectAnimator.ofFloat(this@RaceSelectSongView, "scaleX", 0.85f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this@RaceSelectSongView, "scaleY", 0.85f, 1f)
        val alpha = ObjectAnimator.ofFloat(this@RaceSelectSongView, "alpha", 0.6f, 1f)
        scaleAnimatorSet?.setDuration(500)
        scaleAnimatorSet?.setInterpolator(OvershootInterpolator())
        scaleAnimatorSet?.play(scaleX)?.with(scaleY)?.with(alpha)
        scaleAnimatorSet?.start()

        mNoSelectCall = noSelectCall
        firstSongItem.reset()
        secondSongItem.reset()
        thirdSongItem.reset()
        forthSongItem.reset()
        mRoomData?.let {
            val info = it.realRoundInfo as RaceRoundInfoModel
            info?.let {
                for (i in 0 until it.games.size) {
                    if (i < 4) {
                        itemList[i].setSong(it.games[i])
                    }
                }
            }
        }

        launch(Dispatchers.Main) {
            enableSelectSong(false)
            countDownTv.visibility = View.VISIBLE
            blurBg.visibility = View.VISIBLE

            repeat(3) {
                countDownTv.text = (3 - it).toString()
                delay(1000)
            }

            countDownTv.visibility = View.GONE
            blurBg.visibility = View.GONE
            startCountDown()
        }
        updateSelectState()
    }

    private fun enableSelectSong(isEnabled: Boolean) {
        firstSongItem.isEnabled = isEnabled
        secondSongItem.isEnabled = isEnabled
        thirdSongItem.isEnabled = isEnabled
        forthSongItem.isEnabled = isEnabled
    }

    fun updateSelectState() {
        mRoomData?.let {
            it.realRoundInfo?.let { raceRoundInfoModel ->
                val map = HashMap<Int, ArrayList<RaceWantSingInfo>>()
                map.put(1, ArrayList<RaceWantSingInfo>())
                map.put(2, ArrayList<RaceWantSingInfo>())
                map.put(3, ArrayList<RaceWantSingInfo>())
                map.put(4, ArrayList<RaceWantSingInfo>())
                raceRoundInfoModel.wantSingInfos.forEach {
                    map[it.choiceID]?.add(it)
                    if (MyUserInfoManager.getInstance().uid == it.userID.toLong()) {
                        enableSelectSong(false)
                    }
                }

                for (i in 0 until map.size) {
                    if (i < 4) {
                        itemList[i].bindData(map[i + 1])
                    }
                }
            }
        }
    }

    fun startCountDown() {
        MyLog.d(mTag, "startCountDown")
        enableSelectSong(true)
        animator = ValueAnimator.ofInt(0, 360)
        animator?.duration = 6000
        animator?.addUpdateListener {
            progressBar.progress = it.animatedValue as Int
        }
        animator?.start()
        animator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                mNoSelectCall?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            mNoSelectCall = null
            animator?.cancel()
            scaleAnimatorSet?.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mNoSelectCall = null
        animator?.cancel()
        scaleAnimatorSet?.cancel()
    }
}
