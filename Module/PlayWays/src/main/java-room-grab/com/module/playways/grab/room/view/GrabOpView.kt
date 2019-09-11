package com.module.playways.grab.room.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout

import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent
import com.module.playways.grab.room.model.GrabConfigModel
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.Common.StandPlayType

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 抢唱模式操作面板
 * 倒计时 抢 灭 等按钮都在上面
 */
class GrabOpView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
    val TAG = "GrabOpView"

    internal var mSeq = -1

    // 抢唱按钮模块
    internal val mGrabContainer: RelativeLayout
    internal val mGrabIv: ExImageView
    internal var mRrlProgress: RoundRectangleView
    // 挑战按钮模块
    internal val mGrab2Container: RelativeLayout
    internal val mGrab2Iv: ExImageView
    internal val mRrl2Progress: RoundRectangleView
    internal val mCoinFlagIv: ExImageView
    internal var mIvLightOff: ExTextView
    internal var mIvBurst: ExImageView


    internal var mStatus: Int = 0

    internal var mListener: Listener? = null

    internal var mCountDownTask: HandlerTaskTimer? = null

    internal var mGrabRoomData: GrabRoomData? = null

    internal var mGrabPreRound = false // 标记上一轮是否抢了

    internal var mExitAnimation: Animation? = null

    internal var mSongModel: SongModel? = null


    init {
        View.inflate(context, R.layout.grab_op_view_layout, this)

        mGrabIv = this.findViewById(R.id.grab_iv)
        mRrlProgress = findViewById(R.id.rrl_progress)
        mGrabContainer = findViewById(R.id.grab_container)
        mGrabIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                MyLog.d(TAG, "mBtnIv mStatus ==$mStatus")
                if (mStatus == STATUS_GRAP) {
                    mListener?.clickGrabBtn(mSeq, false)
                }
            }
        })
        mGrab2Iv = this.findViewById(R.id.grab2_iv)
        mRrl2Progress = findViewById(R.id.rrl2_progress)
        mGrab2Container = findViewById(R.id.grab2_container)
        mCoinFlagIv = findViewById(R.id.coin_flag_iv)

        mGrab2Iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                MyLog.d(TAG, "mBtnIv mStatus ==$mStatus")
                if (mStatus == STATUS_GRAP) {
                    mListener?.clickGrabBtn(mSeq, true)
                    mListener?.hideChallengeTipView()
                }
            }
        })


        mIvBurst = findViewById(R.id.iv_burst)
        mIvBurst.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                MyLog.d(TAG, "mIvBurst mStatus ==$mStatus")
                if (mStatus == STATUS_CAN_OP) {
                    mListener?.clickBurst(mSeq)
                }
            }
        })
        mIvLightOff = findViewById(R.id.iv_light_off)
        mIvLightOff.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                MyLog.d(TAG, "mIvLightOff mStatus ==$mStatus")
                if (mStatus == STATUS_CAN_OP) {
                    mListener?.clickLightOff()
                }
            }
        })
    }

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_HIDE_FROM_END_GUIDE_AUDIO -> {
                    //                    if (mGrabRoomData.getGrabGuideInfoModel() == null) {
                    hide("MSG_HIDE_FROM_END_GUIDE_AUDIO")
                    mListener?.grabCountDownOver()
                }
                MSG_HIDE -> {
                    mIvLightOff.visibility = View.GONE
                    mIvBurst.visibility = View.GONE
                    mGrabContainer.visibility = View.GONE
                    mGrab2Container.visibility = View.GONE
                    mListener?.hideChallengeTipView()
                    mListener?.hideGrabTipView()
                    mListener?.hideBurstTipView()
                }
                MSG_SHOW_BRUST_BTN -> {
                    MyLog.d(TAG, "handleMessage msg=$MSG_SHOW_BRUST_BTN")
                    val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
                    animation.duration = 200
                    animation.repeatMode = Animation.REVERSE
                    animation.interpolator = OvershootInterpolator()
                    animation.fillAfter = true
                    mIvBurst.visibility = View.VISIBLE
                    mIvBurst.startAnimation(animation)
                    mIvBurst.isEnabled = true
                    if (mGrabRoomData?.isNewUser == true) {
                        mListener?.showBurstTipView()
                    }
                }
            }//                    } else {
            //                        MyLog.d(TAG, "新手引导不隐藏抢唱按钮");
            //                    }
        }
    }

    fun setGrabRoomData(grabRoomData: GrabRoomData?) {
        mGrabRoomData = grabRoomData
    }

    fun setListener(listener: Listener?) {
        mListener = listener
    }

    /**
     * @param num 倒计时时间，倒计时结束后变成想唱
     */
    fun playCountDown(seq: Int, num: Int, songModel: SongModel?) {
        if (songModel == null) {
            return
        }
        mSongModel = songModel
        //等待想唱时间
        val waitNum = songModel.standIntroEndT - songModel.standIntroBeginT
        MyLog.d(TAG, "playCountDown seq=$seq num=$num waitNum=$waitNum")
        mSeq = seq
        mStatus = STATUS_COUNT_DOWN
        onChangeState()
        mUiHandler.removeCallbacksAndMessages(null)

        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
        animation.duration = 200
        animation.repeatMode = Animation.REVERSE
        animation.interpolator = OvershootInterpolator()
        animation.fillAfter = true
        startAnimation(animation)

        cancelCountDownTask()
        // 播放 3 2 1 导唱倒计时
        var interval: Long = 1000
        if (mGrabPreRound) {
            val config = mGrabRoomData?.grabConfigModel?.wantSingDelayTimeMs ?: 0
            val delay = config / 3
            if (delay > 0) {
                interval += delay.toLong()
            }
        }
        MyLog.d(TAG, "playCountDown interval=$interval")
        mCountDownTask = HandlerTaskTimer.newBuilder().interval(interval)
                .take(num)
                .compose(context as BaseActivity)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        val num1 = num - integer
                        var drawable: Drawable? = null
                        when (num1) {
                            3 -> drawable = U.getDrawable(R.drawable.xiangchang_3)
                            2 -> drawable = U.getDrawable(R.drawable.xiangchang_2)
                            1 -> drawable = U.getDrawable(R.drawable.xiangchang_1)
                        }
                        mIvBurst.visibility = View.GONE
                        mGrabIv.setImageDrawable(drawable)
                        mGrab2Iv.setImageDrawable(drawable)
                    }

                    override fun onComplete() {
                        super.onComplete()
                            mListener?.countDownOver()
                        // 按钮变成抢唱，且可点击
                        mUiHandler.removeMessages(MSG_HIDE_FROM_END_GUIDE_AUDIO)
                        if (waitNum <= 0) {
                            MyLog.e(TAG, "等待时间是0")
                            val msg = mUiHandler.obtainMessage(MSG_HIDE_FROM_END_GUIDE_AUDIO)
                            mUiHandler.sendMessageDelayed(msg, 0)
                        } else {
                            mRrlProgress.visibility = View.VISIBLE
                            mRrlProgress.startCountDown((waitNum - 2000).toLong())

                            mRrl2Progress.visibility = View.VISIBLE
                            mRrl2Progress.startCountDown((waitNum - 2000).toLong())
                            val msg = mUiHandler.obtainMessage(MSG_HIDE_FROM_END_GUIDE_AUDIO)
                            mUiHandler.sendMessageDelayed(msg, (waitNum - 2000).toLong())
                        }
                        mStatus = STATUS_GRAP
                        onChangeState()
                    }
                })
    }

    /**
     * 状态发生变化
     */
    private fun onChangeState() {
        MyLog.d(TAG, "onChangeState mStatus=$mStatus")
        when (mStatus) {
            STATUS_COUNT_DOWN -> {
                mIvLightOff.visibility = View.GONE
                mIvBurst.clearAnimation()
                mIvBurst.visibility = View.GONE
                mGrabContainer.visibility = View.VISIBLE
                mGrabIv.isEnabled = false
                mGrabIv.setImageDrawable(null)
                mGrabIv.background = U.getDrawable(R.drawable.ycdd_qiangchang_bj)

                if (mGrabRoomData?.isChallengeAvailable==true && mSongModel?.isChallengeAvailable==true) {
                    mGrab2Container.visibility = View.VISIBLE
                    mGrab2Iv.isEnabled = false
                    mGrab2Iv.setImageDrawable(null)
                    mGrab2Iv.background = U.getDrawable(R.drawable.ycdd_tiaozhan_bg)
                    mCoinFlagIv.visibility = View.GONE
                    mListener?.showChallengeTipView()
                }

                if (mGrabRoomData?.isNewUser == true) {
                        mListener?.showGrabTipView()
                }
            }
            STATUS_GRAP -> {
                mIvLightOff.visibility = View.GONE
                mIvBurst.clearAnimation()
                mIvBurst.visibility = View.GONE
                run {
                    mGrabContainer.visibility = View.VISIBLE
                    mGrabIv.isEnabled = true
                    mGrabIv.setImageDrawable(null)
                    var drawable: Drawable? = null
                    if (mSongModel?.playType == StandPlayType.PT_CHO_TYPE.value) {
                        // 合唱
                        drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .setShape(DrawableCreator.Shape.Rectangle)
                                .setPressedDrawable(U.getDrawable(R.drawable.ycdd_chorus_pressed))
                                .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_chorus_normal))
                                .build()
                    } else if (mSongModel?.playType == StandPlayType.PT_SPK_TYPE.value) {
                        // pk
                        drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .setShape(DrawableCreator.Shape.Rectangle)
                                .setPressedDrawable(U.getDrawable(R.drawable.ycdd_pk_pressed))
                                .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_pk_normal))
                                .build()
                    } else {
                        drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .setShape(DrawableCreator.Shape.Rectangle)
                                .setPressedDrawable(U.getDrawable(R.drawable.ycdd_qiangchang_anxia))
                                .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_qiangchang))
                                .build()
                    }
                    mGrabIv.background = drawable
                    mGrabIv.setOnTouchListener { v, event ->
                        //MyLog.d(TAG, "onTouch" + " v=" + v + " event=" + event);
                        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                            mRrlProgress.visibility = View.GONE
                        } else {
                            mRrlProgress.visibility = View.VISIBLE
                        }
                        false
                    }
                }
                run {
                    if (mGrabRoomData?.isChallengeAvailable==true && mSongModel?.isChallengeAvailable==true) {
                        mGrab2Container.visibility = View.VISIBLE
                        mGrab2Iv.isEnabled = true
                        mGrab2Iv.setImageDrawable(null)
                        val drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .setShape(DrawableCreator.Shape.Rectangle)
                                .setPressedDrawable(U.getDrawable(R.drawable.ycdd_tiaozhan_anxia))
                                .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_tiaozhan))
                                .build()
                        mGrab2Iv.background = drawable
                        mCoinFlagIv.visibility = View.VISIBLE
                        mGrab2Iv.setOnTouchListener { v, event ->
                            //MyLog.d(TAG, "onTouch" + " v=" + v + " event=" + event);
                            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                                mRrl2Progress.visibility = View.GONE
                            } else {
                                mRrl2Progress.visibility = View.VISIBLE
                            }
                            false
                        }
                    }
                }
            }
            STATUS_CAN_OP -> {
                mGrabContainer.visibility = View.GONE
                mGrab2Container.visibility = View.GONE
                mIvLightOff.visibility = View.GONE
                mIvBurst.visibility = View.GONE
                mIvLightOff.isEnabled = false
            }
            STATUS_HAS_OP -> hide("STATUS_HAS_OP")
        }
    }

    fun setGrabPreRound(grabPreRound: Boolean) {
        mGrabPreRound = grabPreRound
    }

    fun hide(from: String) {
        MyLog.d(TAG, "hide from=$from")
        cancelCountDownTask()
        mGrabIv.clearAnimation()
        mRrlProgress.stopCountDown()
        mGrab2Iv.clearAnimation()
        mRrl2Progress.stopCountDown()
        if (mExitAnimation == null) {
            mExitAnimation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            mExitAnimation?.duration = 200
            mExitAnimation?.repeatMode = Animation.REVERSE
            mExitAnimation?.interpolator = OvershootInterpolator()
            mExitAnimation?.fillAfter = true
            mExitAnimation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    mIvLightOff.visibility = View.GONE
                    mIvBurst.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
        if (mExitAnimation?.hasStarted()==true && mExitAnimation?.hasEnded()==false) {

        } else {
            startAnimation(mExitAnimation)
        }
        mUiHandler.removeCallbacksAndMessages(null)
        val msg = mUiHandler.obtainMessage(MSG_HIDE)
        mUiHandler.sendMessageDelayed(msg, 200)
    }

    /**
     * 开始演唱
     */
    fun toOtherSingState() {
        MyLog.d(TAG, "toOtherSingState")

        mStatus = STATUS_CAN_OP
        onChangeState()

        mUiHandler.removeCallbacksAndMessages(null)

        cancelCountDownTask()
        mCountDownTask = HandlerTaskTimer.newBuilder().delay(mGrabRoomData?.grabConfigModel?.enableShowMLightWaitTimeMs?.toLong() ?:0L)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {

                        val animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
                        animation.duration = 200
                        animation.repeatMode = Animation.REVERSE
                        animation.interpolator = OvershootInterpolator()
                        animation.fillAfter = true
                        startAnimation(animation)
                        visibility = View.VISIBLE

                        mIvLightOff.isEnabled = true
                        mIvLightOff.visibility = View.VISIBLE
                        val drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .setShape(DrawableCreator.Shape.Rectangle)
                                .setPressedDrawable(U.getDrawable(R.drawable.ycdd_miedeng_anxia))
                                .setUnPressedDrawable(U.getDrawable(R.drawable.ycdd_miedeng))
                                .build()

                        mIvLightOff.background = drawable
                    }
                })

        mUiHandler.removeMessages(MSG_SHOW_BRUST_BTN)
        val msg = Message.obtain()
        msg.what = MSG_SHOW_BRUST_BTN
        mUiHandler.sendMessageDelayed(msg, mGrabRoomData?.grabConfigModel?.enableShowBLightWaitTimeMs?.toLong()?:0L)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightBurstEvent) {
        if (mSeq == event.getRoundInfo().roundSeq && event.uid.toLong() == MyUserInfoManager.getInstance().uid) {
            mStatus = STATUS_HAS_OP
            onChangeState()
        } else {
            mIvLightOff.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightOffEvent) {
        if (mSeq == event.getRoundInfo().roundSeq && event.uid.toLong() == MyUserInfoManager.getInstance().uid) {
            mStatus = STATUS_HAS_OP
            onChangeState()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
            mGrabIv.clearAnimation()
            mGrab2Iv.clearAnimation()
        cancelCountDownTask()
        mUiHandler.removeCallbacksAndMessages(null)
        clearAnimation()
            mIvBurst.clearAnimation()
    }

    private fun cancelCountDownTask() {
            mCountDownTask?.dispose()
    }

    interface Listener {
        fun clickGrabBtn(seq: Int, challenge: Boolean)

        fun clickLightOff()

        fun clickBurst(seq: Int)

        fun grabCountDownOver()

        fun countDownOver()

        fun showChallengeTipView()

        fun hideChallengeTipView()

        fun showGrabTipView()

        fun hideGrabTipView()

        fun showBurstTipView()

        fun hideBurstTipView()
    }

    companion object {

        const val MSG_HIDE_FROM_END_GUIDE_AUDIO = 0
        const val MSG_HIDE = 1
        const val MSG_SHOW_BRUST_BTN = 2

        // 抢唱阶段可抢
        const val STATUS_GRAP = 1
        // 抢唱阶段倒计时
        const val STATUS_COUNT_DOWN = 2
        // 演唱阶段可操作
        const val STATUS_CAN_OP = 3
        // 演唱阶段已经操作完成
        const val STATUS_HAS_OP = 4
    }
}
