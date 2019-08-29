package com.module.playways.room.gift.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.race.room.RaceRoomData
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent
import com.module.playways.room.gift.inter.IContinueSendView
import com.module.playways.room.gift.model.BaseGift
import com.module.playways.room.gift.presenter.BuyGiftPresenter
import com.module.playways.room.gift.presenter.BuyGiftPresenter.*
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import org.greenrobot.eventbus.EventBus

class ContinueSendView : FrameLayout, IContinueSendView {
    val TAG = "ContinueSendView"

    enum class EGameScene(val scene: Int) {
        GS_Stand(0),
        GS_Race(1);

        val value: Int
            get() = scene
    }

    internal lateinit var mIvBg: ImageView
    internal lateinit var mTvContinueNum: ContinueTextView

    internal var mBaseGift: BaseGift?=null

    internal var mRoomData: BaseRoomData<*>?=null

    internal var mBuyGiftPresenter: BuyGiftPresenter?=null

    internal var mScaleAnimatorSet: AnimatorSet? = null

    internal var mJumpAnimatorSet: AnimatorSet? = null

    internal var mReceiver: UserInfoModel?=null

    internal var mOnVisibleStateListener: OnVisibleStateListener? = null

    private val mCanContinueDuration: Long = 3000L

    var mScene: EGameScene = EGameScene.GS_Stand

    internal var mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_HIDE) {
                visibility = View.GONE
            } else if (msg.what == MSG_SHOW_RECHARGE) {
                EventBus.getDefault().post(ShowHalfRechargeFragmentEvent())
            }
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun setRoomData(roomData: BaseRoomData<*>) {
        mRoomData = roomData
    }

    fun getGrabRoomData(): BaseRoomData<*>? {
        if (mRoomData is GrabRoomData? || mRoomData is RaceRoomData?) {
            return mRoomData
        }
        return null
    }

    fun setObserver(observer: OnVisibleStateListener) {
        mOnVisibleStateListener = observer
    }

    fun startBuy(baseGift: BaseGift, receiver: UserInfoModel) {
        mBaseGift = baseGift
        mReceiver = receiver
        val infoModel: BaseRoundInfoModel? = getGrabRoomData()?.realRoundInfo
        if (infoModel != null) {
            if (baseGift.isCanContinue) {
                mBuyGiftPresenter?.buyGift(baseGift, getGrabRoomData()?.gameId?.toLong()
                        ?: 0L, getGrabRoomData()?.realRoundSeq
                        ?: 0, receiver, mScene.value)
                visibility = View.VISIBLE

                mHandler.removeMessages(MSG_HIDE)
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE), mCanContinueDuration)
            } else {
                mBuyGiftPresenter?.buyGift(baseGift, getGrabRoomData()?.gameId?.toLong()
                        ?: 0L, getGrabRoomData()?.realRoundSeq
                        ?: 0, receiver, mScene.value)
            }
        } else {
            MyLog.w(TAG, "startBuy baseGift=$baseGift receiver=$receiver")
        }
    }

    private fun init() {
        View.inflate(context, R.layout.continue_send_view_layout, this)
        //        EventBus.getDefault().register(this);
        mIvBg = findViewById<View>(R.id.iv_bg) as ImageView
        mTvContinueNum = findViewById<View>(R.id.tv_continue_num) as ContinueTextView

        mBuyGiftPresenter = BuyGiftPresenter(this)

        setOnClickListener {
            val grabRoundInfoModel: BaseRoundInfoModel? = getGrabRoomData()?.realRoundInfo
            if (grabRoundInfoModel != null) {
                mBuyGiftPresenter?.buyGift(mBaseGift, getGrabRoomData()?.gameId?.toLong()
                        ?: 0L, getGrabRoomData()?.realRoundSeq
                        ?: 0, mReceiver, mScene.value)

                if (mScaleAnimatorSet != null) {
                    mScaleAnimatorSet!!.cancel()
                }

                val objectAnimator1 = ObjectAnimator.ofFloat(this@ContinueSendView, "scaleX", 1.0f, 0.8f, 1.0f)
                val objectAnimator2 = ObjectAnimator.ofFloat(this@ContinueSendView, "scaleY", 1.0f, 0.8f, 1.0f)
                mScaleAnimatorSet = AnimatorSet()
                mScaleAnimatorSet!!.play(objectAnimator1).with(objectAnimator2)
                mScaleAnimatorSet!!.duration = 500
                mScaleAnimatorSet!!.start()

                mHandler.removeMessages(MSG_HIDE)
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE), mCanContinueDuration)
            } else {
                MyLog.w(TAG, "ContinueSendView" + " mBaseRoomData.getRealRoundInfo()=null")
            }
        }
    }

    override fun buySuccess(baseGift: BaseGift, continueCount: Int) {
        mTvContinueNum.setText(continueCount.toString())
        val objectAnimator1 = ObjectAnimator.ofFloat(mTvContinueNum, View.TRANSLATION_Y, 0.0f, -40f)
        val objectAnimator2 = ObjectAnimator.ofFloat(mTvContinueNum, View.ALPHA, 1.0f, 0.7f, 0.0f)

        if (mJumpAnimatorSet != null) {
            mJumpAnimatorSet!!.cancel()
        }

        mJumpAnimatorSet = AnimatorSet()
        mJumpAnimatorSet!!.play(objectAnimator1).with(objectAnimator2)
        mJumpAnimatorSet!!.duration = 500
        mJumpAnimatorSet!!.start()
    }

    override fun buyFaild(erroCode: Int, errorMsg: String) {
        when (erroCode) {
            ErrZSNotEnough -> {
                U.getToastUtil().showShort("钻石余额不足，充值后就可以送礼啦")
                mHandler.removeMessages(MSG_SHOW_RECHARGE)
                mHandler.sendEmptyMessageDelayed(MSG_SHOW_RECHARGE, 1000)
                mHandler.removeMessages(MSG_HIDE)
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDE), 0)
            }
            ErrPresentObjLeave -> {
                U.getToastUtil().showShort("送礼对象已离开，请重新选择")
                mHandler.removeMessages(MSG_HIDE)
                mHandler.sendEmptyMessageDelayed(MSG_HIDE, 0)
            }
            ErrCoinNotEnough -> {
                U.getToastUtil().showShort("金币余额不足")
                mHandler.removeMessages(MSG_HIDE)
                mHandler.sendEmptyMessageDelayed(MSG_HIDE, 0)
            }
            ErrSystem -> U.getToastUtil().showShort(errorMsg)
            else -> U.getToastUtil().showShort(errorMsg)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            if (mScaleAnimatorSet != null) {
                mScaleAnimatorSet!!.cancel()
            }
            if (mJumpAnimatorSet != null) {
                mJumpAnimatorSet!!.cancel()
            }

            mIvBg.clearAnimation()

            if (mOnVisibleStateListener != null) {
                mOnVisibleStateListener!!.onVisible(false)
            }
        } else {
            val rotate = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            val lin = LinearInterpolator()
            rotate.interpolator = lin
            rotate.duration = 1000
            rotate.repeatCount = -1
            rotate.fillAfter = true
            rotate.startOffset = 10
            mIvBg.animation = rotate
            if (mOnVisibleStateListener != null) {
                mOnVisibleStateListener!!.onVisible(true)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (mScaleAnimatorSet != null) {
            mScaleAnimatorSet!!.cancel()
        }
        if (mJumpAnimatorSet != null) {
            mJumpAnimatorSet!!.cancel()
        }
        mIvBg.clearAnimation()
    }

    fun destroy() {
        mBuyGiftPresenter?.destroy()
        mHandler.removeCallbacksAndMessages(null)
    }

    interface OnVisibleStateListener {
        fun onVisible(isVisible: Boolean)
    }

    companion object {

        val MSG_HIDE = 101
        val MSG_SHOW_RECHARGE = 102
    }
}
