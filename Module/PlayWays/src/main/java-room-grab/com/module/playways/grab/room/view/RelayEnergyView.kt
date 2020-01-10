package com.module.playways.grab.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.common.view.DiffuseView
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.component.busilib.view.WaveProgressView
import com.module.playways.room.room.score.bar.ScoreTipsView
import com.zq.live.proto.RelayRoom.RExpMsg
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class RelayEnergyView : ExRelativeLayout {
    val MSG_WAVE = 0
    val mTag = "RelayEnergyView"

//    private var mRoomData: GrabRoomData? = null

    private var mLastItem: ScoreTipsView.Item? = null

    private var waveProgressView: WaveProgressView
    var diffuseView: DiffuseView? = null
    private var fullCountTv: ExTextView

    private val maxScore = 500

    var mHandler: Handler? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, com.module.playways.R.layout.relay_energy_view_layout, this)
        waveProgressView = rootView.findViewById(com.module.playways.R.id.wave_progress_view)
        fullCountTv = rootView.findViewById(com.module.playways.R.id.full_count_tv)
        fullCountTv.text = "x0"
        waveProgressView.setCurrent(0, "")
        waveProgressView.setMaxProgress(maxScore)
        waveProgressView.setmWaveSpeed(20)

        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MSG_WAVE -> {
                        diffuseView?.start(2000)
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
        mHandler?.removeCallbacksAndMessages(null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RExpMsg) {
        waveProgressView.setMaxProgress(event.totalExp)
        fullCountTv.text = "x${event.afterStar}"
        if (event.afterStar > event.beforeStar) {
            waveProgressView.setCurrent(event.totalExp, "")
            playScaleAnim {
                waveProgressView.setCurrent(event.afterExp, "")
            }

            mHandler?.sendEmptyMessageDelayed(MSG_WAVE, 1000)
            mHandler?.sendEmptyMessageDelayed(MSG_WAVE, 2000)
            mHandler?.sendEmptyMessageDelayed(MSG_WAVE, 3000)
        } else {
            waveProgressView.setCurrent(event.afterExp, "")
        }
    }

    private fun playScaleAnim(call: (() -> Unit)) {
        val scaleAnimation1 = ScaleAnimation(1.0f, 1.3f, 1.0f, 1.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation1!!.fillAfter = true
        scaleAnimation1!!.duration = 200
        val scaleAnimation2 = ScaleAnimation(1.3f, 1.0f, 1.3f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleAnimation2!!.fillAfter = true
        scaleAnimation2!!.duration = 200
        waveProgressView?.startAnimation(scaleAnimation1)
        launch {
            delay(300)
            waveProgressView?.startAnimation(scaleAnimation2)
            delay(200)
            call.invoke()
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    fun reset() {
        mLastItem = null
    }
}
