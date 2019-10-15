package com.module.playways.race.room.view

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.pagetransformerhelp.cardtransformer.AlphaAndScalePageTransformer
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.adapter.RaceSelectSongAdapter
import com.module.playways.race.room.event.RaceWantSingChanceEvent
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RacePagerSelectSongView : ExConstraintLayout {
    val TAG = "RacePagerSelectSongView"
    var closeIv: ExImageView
    var countDonwTv: ExTextView
    var hideClickArea: View
    var mPagerRootView: View
    var bannerPager: ViewPager
    var mRoomData: RaceRoomData? = null
    var mPagerAdapter: RaceSelectSongAdapter? = null
    var countDonwJob: Job? = null
    var mSeq = -1 // 当前轮次
    var mHasSignUpChoiceID = -1
    var mSignUpMethed: ((Int, Int, RaceGamePlayInfo?) -> Unit)? = null
    var mShowingSongSeq = -1 ////正在显示的歌曲信息是哪个轮次的

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
        try {
            bannerPager.beginFakeDrag()
            bannerPager.fakeDragBy(1.0f)
            bannerPager.endFakeDrag()
        } catch (e: Exception) {
            MyLog.w(TAG, e.toString())
        }
    }

    init {
        View.inflate(context, com.module.playways.R.layout.race_pager_select_song_view_layout, this)
        closeIv = rootView.findViewById(com.module.playways.R.id.close_iv)
        mPagerRootView = rootView.findViewById(com.module.playways.R.id.pager_root_view)
        countDonwTv = rootView.findViewById(com.module.playways.R.id.count_down_tv)
        hideClickArea = rootView.findViewById(com.module.playways.R.id.hide_click_area)
        bannerPager = rootView.findViewById(com.module.playways.R.id.banner_pager)
        bannerPager.offscreenPageLimit = 2
        bannerPager.setPageMargin(U.getDisplayUtils().dip2px(15f))
        bannerPager.setPageTransformer(true, AlphaAndScalePageTransformer())

        mPagerAdapter = RaceSelectSongAdapter(context, object : RaceSelectSongAdapter.IRaceSelectListener {
            override fun onSignUp(choiceID: Int, model: RaceGamePlayInfo?) {
                if (canSelectSong()) {
                    mSignUpMethed?.invoke(choiceID, mSeq, model)
                } else {
                    U.getToastUtil().showShort("报名结束")
                }
            }

            override fun getSignUpChoiceID(): Int {
                return mHasSignUpChoiceID
            }
        })

        bannerPager.adapter = mPagerAdapter

        closeIv.setDebounceViewClickListener {
            hideView()
        }

        mPagerRootView.setDebounceViewClickListener {
            //拦截
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
                val tips = "新一轮报名开始"
                if (it.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
                    val showingSongSeq = mSeq + 1
                    if (showingSongSeq != mShowingSongSeq) {
                        mShowingSongSeq = showingSongSeq
                        mHasSignUpChoiceID = -1
                        mPagerAdapter?.setData(raceRoomData.couldChoiceGames)
                        countDonwTv.visibility = View.GONE
                        if (preDataCount > 0) {
                            U.getToastUtil().showShort(tips)
                        }
                    }
                } else {
                    mHasSignUpChoiceID = -1
                    mShowingSongSeq = mSeq
                    mPagerAdapter?.setData(it.games)
                    if (preDataCount > 0) {
                        U.getToastUtil().showShort(tips)
                    }
                    countDown(noSelectCall)
                }
            }

            if (preDataCount > 0) {
                mUiHandler.sendEmptyMessageDelayed(PAGER_BUG, 0)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    fun canSelectSong(): Boolean {
        if (mRoomData?.realRoundSeq == mSeq) {
            return true
        }

        return false
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWantSingChanceEvent) {
        if (event.userID == MyUserInfoManager.getInstance().uid.toInt()) {
            mHasSignUpChoiceID = event.choiceID
        }
    }

    val mLastedTime = 10000

    private fun countDown(noSelectCall: (() -> Unit)?) {
        var lastedTime = mLastedTime
        countDonwTv.visibility = View.VISIBLE
        countDonwTv.text = ""

        if (mRoomData?.realRoundInfo?.enterStatus == ERaceRoundStatus.ERRS_CHOCING.value) {
            mRoomData?.realRoundInfo?.elapsedTimeMs?.let {
                //多3秒是因为中间动画（显示结果3秒|（无人抢唱+下一首）3秒）
                lastedTime = 14400 - it
                MyLog.d(TAG, "setSongName elapsedTimeMs is $it")
                if (lastedTime > mLastedTime) {
                    lastedTime = mLastedTime
                }
            }
        }

        countDonwJob?.cancel()
        countDonwJob = launch {
            repeat(lastedTime / 1000) {
                countDonwTv.text = "${(lastedTime / 1000 - it) - 1}s"
                delay(1000)
            }

            countDonwTv.visibility = View.GONE
            noSelectCall?.invoke()
        }
    }

    fun cancelCountDown() {
        countDonwJob?.cancel()
        countDonwTv.visibility = View.GONE
    }

    fun showView() {
        if (visibility == View.VISIBLE) {
            return
        }

        if (mRoomData?.realRoundInfo?.enterStatus == ERaceRoundStatus.ERRS_CHOCING.value
                && mRoomData?.realRoundInfo?.status == ERaceRoundStatus.ERRS_CHOCING.value) {
            MyLog.w(TAG, "中途进来的，而且是选歌阶段，不展示选歌界面")
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
        mUiHandler.post({
            fakeDrag()
        })
    }

    fun hideView() {
        if (mUiHandler.hasMessages(HIDE_PANEL) || View.GONE == visibility) {
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
        EventBus.getDefault().unregister(this)
    }

    companion object {
        val HIDE_PANEL = 1
        val PAGER_BUG = 2
        val ANIMATION_DURATION = 300
    }
}