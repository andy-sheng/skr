package com.module.playways.race.room.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.common.log.MyLog
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class RaceSelectSongView : ExConstraintLayout {
    val mTag = "RaceSelectSongView"
    var bg: ImageView
    val progressBg: ExImageView
    val progressBar: ProgressBar
    val forthSongItem: RaceSelectSongItemView
    val firstSongItem: RaceSelectSongItemView
    val secondSongItem: RaceSelectSongItemView
    val thirdSongItem: RaceSelectSongItemView
    val itemList: ArrayList<RaceSelectSongItemView> = ArrayList()
    var animator: ValueAnimator? = null
    var mRoomData: RaceRoomData? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, com.module.playways.R.layout.race_select_song_layout, this)
        bg = findViewById(com.module.playways.R.id.bg) as ImageView
        progressBg = findViewById(com.module.playways.R.id.progress_bg) as ExImageView
        progressBar = findViewById(com.module.playways.R.id.progress_bar) as ProgressBar
        firstSongItem = findViewById(com.module.playways.R.id.first_song_item) as RaceSelectSongItemView
        secondSongItem = findViewById(com.module.playways.R.id.second_song_item) as RaceSelectSongItemView
        thirdSongItem = findViewById(com.module.playways.R.id.third_song_item) as RaceSelectSongItemView
        forthSongItem = findViewById(com.module.playways.R.id.forth_song_item) as RaceSelectSongItemView
        itemList.add(firstSongItem)
        itemList.add(secondSongItem)
        itemList.add(thirdSongItem)
        itemList.add(forthSongItem)
        progressBar.max = 360
        progressBar.progress = 0

        launch {
            delay(1000)
            startCountDown()
            firstSongItem.startSelectedAnimation()

            delay(5000)
            firstSongItem.reset()
        }
    }

    fun setRoomData(roomData: RaceRoomData) {
        mRoomData = roomData
        firstSongItem.roomData = roomData
        secondSongItem.roomData = roomData
        thirdSongItem.roomData = roomData
        forthSongItem.roomData = roomData
    }

    fun setSongName() {
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
    }

    fun updateSelectState() {
        mRoomData?.let {
            val info = it.realRoundInfo as RaceRoundInfoModel
            info?.let {
                for (i in 0 until it.gamesChoiceMap.size) {
                    if (i < 4) {
                        itemList[i].bindData(it.gamesChoiceMap[i])
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
            progressBar.progress = it.getAnimatedValue() as Int
        }
        animator?.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
