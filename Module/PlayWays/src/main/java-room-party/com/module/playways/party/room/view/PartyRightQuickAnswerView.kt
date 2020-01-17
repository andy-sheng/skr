package com.module.playways.party.room.view

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import android.widget.RelativeLayout
import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent
import com.module.playways.grab.room.view.RoundRectangleView
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.room.data.H
import com.zq.live.proto.PartyRoom.PBeginQuickAnswer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 抢唱模式操作面板
 * 倒计时 抢 灭 等按钮都在上面
 */
class PartyRightQuickAnswerView(mViewStub: ViewStub?) : ExViewStub(mViewStub) {
    val TAG = "PartyRightQuickAnswerView"

    companion object {
        const val MSG_HIDE = 1
        // 抢唱阶段倒计时
        const val STATUS_COUNT_DOWN = 1
        // 抢唱阶段可抢
        const val STATUS_GRAB = 2
    }

    internal var mStatus: Int = 0
    lateinit var grabContainer: RelativeLayout
    lateinit var rrlProgress: RoundRectangleView
    lateinit var grabIv: ExImageView

    private var countDownJob: Job? = null
    private var enterAnimation: Animation? = null
    private var exitAnimation: Animation? = null

    val api = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    override fun init(rootView: View) {
        grabContainer = rootView.findViewById(R.id.grab_container)
        rrlProgress = rootView.findViewById(R.id.rrl_progress)
        grabIv = rootView.findViewById(R.id.grab_iv)

        grabIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                MyLog.d(TAG, "mBtnIv mStatus ==$mStatus")
                if (mStatus == STATUS_GRAB) {
                    launch {
                        val map = HashMap<String, Any?>()
                        map["roomID"] = H.partyRoomData?.gameId
                        map["quickAnswerTag"] = H.partyRoomData?.quickAnswerTag

                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                        launch {
                            var result = subscribe(RequestControl("responseQuickAnswer", ControlType.CancelThis)) {
                                api.responseQuickAnswer(body)
                            }
                            if (result.errno == 0) {
                                mUiHandler.removeMessages(MSG_HIDE)
                                val msg = mUiHandler.obtainMessage(MSG_HIDE)
                                mUiHandler.sendMessage(msg)
                            } else {
                                U.getToastUtil().showShort(result.errmsg)
                            }
                        }
                    }
                }
            }
        })

    }

    override fun layoutDesc(): Int {
        return R.layout.party_right_quick_answer_view_layout
    }

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_HIDE -> {
                    hide("handleMessage", true)
                }
            }//                    } else {
            //                        MyLog.d(TAG, "新手引导不隐藏抢唱按钮");
            //                    }
        }
    }

    private fun getCountDownTs(beginTs: Long): Int {
        val now = System.currentTimeMillis()
        return (beginTs - (now - BaseRoomData.shiftTsForRelay)).toInt()
    }

    private fun reset() {
        tryInflate()
        setVisibility(View.GONE)
        countDownJob?.cancel()
        mStatus = STATUS_COUNT_DOWN
        onChangeState()
        realView?.clearAnimation()
        grabIv.clearAnimation()
        enterAnimation?.cancel()
        mUiHandler.removeCallbacksAndMessages(null)
    }

    /**
     * @param num 倒计时时间，倒计时结束后变成想唱
     */
    fun playCountDown(beginTs: Long, endTs: Long) {
        reset()
        setVisibility(View.VISIBLE)
        enterAnimation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
        enterAnimation?.duration = 200
        enterAnimation?.repeatMode = Animation.REVERSE
        enterAnimation?.interpolator = OvershootInterpolator()
        enterAnimation?.fillAfter = true
        realView?.startAnimation(enterAnimation)
        // 播放 3 2 1 导唱倒计时
        var countDownTs = getCountDownTs(beginTs)
        MyLog.d(TAG, "countDownTs1=$countDownTs")
        if (countDownTs > 0) {
            countDownJob = launch {
                val t = countDownTs % 1000
                val times = countDownTs / 1000
                grabIv.setImageDrawable(U.getDrawable(R.drawable.xiangchang_3))
                delay(t.toLong())
                repeat(times) {
                    var drawable: Drawable? = null
                    drawable = when (times - it) {
                        2 -> U.getDrawable(R.drawable.xiangchang_2)
                        1 -> U.getDrawable(R.drawable.xiangchang_1)
                        else -> {
                            U.getDrawable(R.drawable.xiangchang_3)
                        }
                    }
                    grabIv.setImageDrawable(drawable)
                    delay(1000)
                }
                showBtn(beginTs, endTs)
            }
        } else {
            showBtn(beginTs, endTs)
        }
    }

    private fun showBtn(beginTs: Long, endTs: Long) {
        val countDownTs = getCountDownTs(endTs)
        if (countDownTs > 0) {
            MyLog.d(TAG, "countDownTs2=$countDownTs")
            if (countDownTs > 0) {
                // 开始抢答倒计时
                mStatus = STATUS_GRAB
                onChangeState()
                rrlProgress.visibility = View.VISIBLE
                rrlProgress.startCountDown(countDownTs.toLong())
                val msg = mUiHandler.obtainMessage(MSG_HIDE)
                mUiHandler.sendMessageDelayed(msg, countDownTs.toLong())
            }
        }

    }

    /**
     * 状态发生变化
     */
    private fun onChangeState() {
        MyLog.d(TAG, "onChangeState mStatus=$mStatus")
        when (mStatus) {
            STATUS_COUNT_DOWN -> {
                grabIv.isEnabled = false
                grabIv.setImageDrawable(null)
                grabIv.background = U.getDrawable(R.drawable.ycdd_qiangchang_bj)

            }
            STATUS_GRAB -> {
                grabIv.isEnabled = true
                grabIv.setImageDrawable(null)
                var drawable: Drawable? = null
                if(H.partyRoomData?.myUserInfo?.isHost() == true){
                    drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                            .setShape(DrawableCreator.Shape.Rectangle)
                            .setPressedDrawable(U.getDrawable(R.drawable.party_quick_answer_ing))
                            .setUnPressedDrawable(U.getDrawable(R.drawable.party_quick_answer_ing))
                            .build()
                    grabIv.isEnabled = false
                }else{
                    drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                            .setShape(DrawableCreator.Shape.Rectangle)
                            .setPressedDrawable(U.getDrawable(R.drawable.party_qiangda_disable))
                            .setUnPressedDrawable(U.getDrawable(R.drawable.party_qiangda_enable))
                            .build()
                    grabIv.isEnabled = true
                }
                grabIv.background = drawable
                grabIv.setOnTouchListener { v, event ->
                    //MyLog.d(TAG, "onTouch" + " v=" + v + " event=" + event);
                    if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                        rrlProgress.visibility = View.GONE
                    } else {
                        rrlProgress.visibility = View.VISIBLE
                    }
                    false
                }
            }
        }
    }

    fun hide(from: String, useAnimation: Boolean) {
        MyLog.d(TAG, "hide from=$from")
        grabIv.clearAnimation()
        rrlProgress.stopCountDown()
        if (useAnimation) {
            if (exitAnimation == null) {
                exitAnimation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
                exitAnimation?.duration = 200
                exitAnimation?.repeatMode = Animation.REVERSE
                exitAnimation?.interpolator = OvershootInterpolator()
                exitAnimation?.fillAfter = true
                exitAnimation?.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            }
            if (exitAnimation?.hasStarted() == true && exitAnimation?.hasEnded() == false) {

            } else {
                realView?.startAnimation(exitAnimation)
            }
            mUiHandler.postDelayed({
                reset()
            }, 200)
        } else {
            reset()
        }
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
//        if (!EventBus.getDefault().isRegistered(this)) {
//            EventBus.getDefault().register(this)
//        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
//        EventBus.getDefault().unregister(this)
        reset()
    }

}
