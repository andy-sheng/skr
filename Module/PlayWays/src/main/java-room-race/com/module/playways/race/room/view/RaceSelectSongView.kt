package com.module.playways.race.room.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.race.room.model.RaceWantSingInfo

class RaceSelectSongView : ExConstraintLayout {
    val mTag = "RaceSelectSongView"
    var bg: ImageView
    private val progressBg: ExImageView
    private val progressBar: ProgressBar
    private val forthSongItem: RaceSelectSongItemView
    private val firstSongItem: RaceSelectSongItemView
    private val secondSongItem: RaceSelectSongItemView
    private val thirdSongItem: RaceSelectSongItemView
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
        itemList.add(firstSongItem)
        itemList.add(secondSongItem)
        itemList.add(thirdSongItem)
        itemList.add(forthSongItem)
        progressBar.max = 360
        progressBar.progress = 0
        firstSongItem.setDebounceViewClickListener {
            firstSongItem.getSong()?.let {
                mSelectCall?.invoke(1)
                mNoSelectCall = null
            }
        }

        secondSongItem.setDebounceViewClickListener {
            secondSongItem.getSong()?.let {
                mSelectCall?.invoke(2)
                mNoSelectCall = null
            }
        }

        thirdSongItem.setDebounceViewClickListener {
            thirdSongItem.getSong()?.let {
                mSelectCall?.invoke(3)
                mNoSelectCall = null
            }
        }

        forthSongItem.setDebounceViewClickListener {
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

    fun setSongName(noSelectCall: (() -> Unit)?) {
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

        updateSelectState()
        startCountDown()
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
            animator?.cancel()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
