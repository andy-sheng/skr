package com.module.playways.race.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.pagetransformerhelp.cardtransformer.AlphaAndScalePageTransformer
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.adapter.RaceSelectSongAdapter
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RacePagerSelectSongView : ExConstraintLayout {
    val TAG = "RacePagerSelectSongView"
    var closeIv: ExImageView
    var countDonwTv: ExTextView
    var hideClickArea: View
    var bannerPager: ViewPager
    var mRoomData: RaceRoomData? = null
    var mPagerAdapter: RaceSelectSongAdapter? = null
    var countDonwJob: Job? = null
    var mSeq = -1
    var mSignUpMethed: ((Int, Int, RaceGamePlayInfo?) -> Unit)? = null

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == HIDE_PANEL) {
                clearAnimation()
                visibility = View.GONE
            } else if (msg.what == PAGER_BUG) {
                fakeDrag()
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun fakeDrag() {
        bannerPager.beginFakeDrag()
        bannerPager.fakeDragBy(1.0f)
        bannerPager.endFakeDrag()
    }

    init {
        View.inflate(context, R.layout.race_pager_select_song_view_layout, this)
        closeIv = rootView.findViewById(R.id.close_iv)
        countDonwTv = rootView.findViewById(R.id.count_down_tv)
        hideClickArea = rootView.findViewById(R.id.hide_click_area)
        bannerPager = rootView.findViewById(R.id.banner_pager)
        bannerPager.offscreenPageLimit = 8
        bannerPager.setPageMargin(U.getDisplayUtils().dip2px(15f))
        bannerPager.setPageTransformer(true, AlphaAndScalePageTransformer())

        mPagerAdapter = RaceSelectSongAdapter(context) { choiceID, model ->
            if (canSelectSong()) {
                mSignUpMethed?.invoke(choiceID, mSeq, model)
            }
        }

        bannerPager.adapter = mPagerAdapter

        closeIv.setDebounceViewClickListener {
            hideView()
        }

        hideClickArea.setDebounceViewClickListener {
            hideView()
        }
    }

    //只有在唱歌的阶段就用到RaceRoomData couldChoiceGames的数据，别的时候都用到之前的歌曲的数据
    //倒计时3秒，选择6秒
    fun setSongData(seq: Int, noSelectCall: (() -> Unit)?) {
        MyLog.d(TAG, "setSongName seq = $seq, noSelectCall = $noSelectCall")
        mSeq = seq
        mRoomData?.let { raceRoomData ->
            val info = raceRoomData.realRoundInfo as RaceRoundInfoModel
            val preDataCount: Int = mPagerAdapter?.count ?: 0
            info?.let {
                if (it.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
                    mPagerAdapter?.setData(raceRoomData.couldChoiceGames)
                    countDonwTv.visibility = View.GONE
                } else {
                    countDown(noSelectCall)
                    mPagerAdapter?.setData(it.games)
                }
            }

            if (preDataCount > 0) {
                mUiHandler.sendEmptyMessageDelayed(PAGER_BUG, 0)
            }
        }
    }

    fun canSelectSong(): Boolean {
        if (mRoomData?.realRoundSeq == mSeq) {
            return true
        }

        return false
    }

    fun countDown(noSelectCall: (() -> Unit)?) {
        var lastedTime = 8000
        countDonwTv.visibility = View.VISIBLE
//        if (mRoomData?.realRoundInfo?.enterStatus == ERaceRoundStatus.ERRS_CHOCING.value) {
//            mRoomData?.realRoundInfo?.elapsedTimeMs?.let {
//                //多3秒是因为中间动画（显示结果3秒|（无人抢唱+下一首）3秒）
//                lastedTime = 13400 - it
//                MyLog.d(TAG, "setSongName elapsedTimeMs is $it")
//                if (lastedTime < 0) {
//                    lastedTime = 1000
//                } else if (lastedTime > 9000) {
////                    lastedTime = 9000
//                }
//            }
//        }

        countDonwJob?.cancel()
        countDonwJob = launch {
            repeat(lastedTime / 1000) {
                countDonwTv.text = "${(lastedTime / 1000 - it)}s"
                delay(1000)
            }

            countDonwTv.visibility = View.GONE
            noSelectCall?.invoke()
        }
    }

    fun showView() {
        if (visibility == View.VISIBLE) {
            return
        }

        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)
        visibility = View.VISIBLE
    }

    fun hideView() {
        if (mUiHandler.hasMessages(HIDE_PANEL)) {
            return
        }

        mUiHandler.removeMessages(HIDE_PANEL)
        clearAnimation()
        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1.0f)
        animation.duration = ANIMATION_DURATION.toLong()
        animation.repeatMode = Animation.REVERSE
        animation.fillAfter = true
        startAnimation(animation)

        mUiHandler.sendMessageDelayed(mUiHandler.obtainMessage(HIDE_PANEL), ANIMATION_DURATION.toLong())
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDonwJob?.cancel()
    }

    companion object {
        val HIDE_PANEL = 1
        val PAGER_BUG = 2
        val ANIMATION_DURATION = 300
    }
}