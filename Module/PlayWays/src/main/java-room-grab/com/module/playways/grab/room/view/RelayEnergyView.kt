package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.common.log.MyLog
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.component.busilib.view.WaveProgressView
import com.module.playways.R
import com.module.playways.room.room.score.bar.ScoreTipsView

class RelayEnergyView : ExRelativeLayout {

    val mTag = "RelayEnergyView"

//    private var mRoomData: GrabRoomData? = null

    private var mLastItem: ScoreTipsView.Item? = null

    private var waveProgressView: WaveProgressView
    private var fullCountTv: ExTextView

    private val maxScore = 500

    private var currentScore = 0

    private var currentFullCount = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.relay_energy_view_layout, this)
        waveProgressView = rootView.findViewById(R.id.wave_progress_view)
        fullCountTv = rootView.findViewById(R.id.full_count_tv)
        fullCountTv.text = "x$currentFullCount"
        waveProgressView.setCurrent(0, "")
        waveProgressView.setMaxProgress(maxScore)
//        waveProgressView.setWave()
        waveProgressView.setWaveColor("#FF8484")
        waveProgressView.setmWaveSpeed(40)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun updateScore(score1: Int, songLineNum: Int) {
        MyLog.d(mTag, "updateScore score1=$score1 songLineNum=$songLineNum")

        currentScore += score1

        if (currentScore > maxScore) {
            //TODO 动画
            currentFullCount++
            fullCountTv.text = "x$currentFullCount"
            currentScore -= maxScore
            waveProgressView.setCurrent(currentScore, "")
        } else {
            waveProgressView.setCurrent(currentScore, "")
        }
    }

    fun reset() {
        mLastItem = null
    }
}
