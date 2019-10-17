package com.module.playways.race.room.view.matchview

import android.content.Context
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.module.playways.R
import com.module.playways.listener.AnimationListener
import com.module.playways.race.room.RaceRoomData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


// 匹配中类似赌博机的效果
class RaceMatchView : ConstraintLayout {

    val mTag = "RaceMatchView ${hashCode()}"

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val matchStatusIv: ImageView
    private val leftView: RaceMatchItemView
    private val rightView: RaceMatchItemView
    private var dengView: ImageView? = null
    private val contentTv: TextView
    private val resultIv: ImageView

    var roomData: RaceRoomData? = null

    var timer: HandlerTaskTimer? = null

    internal val MSG_START = 2
    internal val MSG_HIDE = 1
    internal val MSG_FAST_MUSIC = 3
    internal val MSG_DECR_MUSIC = 4
    internal val MSG_RESULT_MUSIC = 5
    internal val MSG_ENSURE_CALLBACK = 9
    internal var mIndex = 0

    internal var mMsgAnimationRes = arrayOf(R.drawable.race_match_view_deng1, R.drawable.race_match_view_deng2, R.drawable.race_match_view_deng3)

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when {
                msg.what == MSG_START -> {
                    dengView?.background = U.getDrawable(mMsgAnimationRes[mIndex++ % mMsgAnimationRes.size])
                    this.sendEmptyMessageDelayed(MSG_START, 400)
                }
                msg.what == MSG_HIDE -> this.removeMessages(MSG_START)
                msg.what == MSG_ENSURE_CALLBACK -> {

                }
                msg.what == MSG_FAST_MUSIC -> {
                    starFastBgMusic()
                }
                msg.what == MSG_DECR_MUSIC -> {
                    starDecrBgMusic()
                }
                msg.what == MSG_RESULT_MUSIC -> {
                    starResultMusic()
                }
            }
        }
    }

    init {
        View.inflate(context, R.layout.race_match_view_layout, this)

        matchStatusIv = this.findViewById(R.id.match_status_iv)
        leftView = this.findViewById(R.id.left_view)
        rightView = this.findViewById(R.id.right_view)
        dengView = this.findViewById(R.id.deng_view)
        contentTv = this.findViewById(R.id.content_tv)
        resultIv = this.findViewById(R.id.result_iv)

        U.getSoundUtils().preLoad(mTag, R.raw.rank_flipsonglist, R.raw.race_select, R.raw.race_unselect)
    }

    fun bindData(listener: () -> Unit) {
        starAnimation(listener)
    }

    private fun starAnimation(listener: () -> Unit) {
        var leftFlag = false
        var rightFlag = false
        var list = roomData?.getInSeatPlayerInfoList() as ArrayList
        leftView.setData(list, roomData?.realRoundInfo?.subRoundInfo?.getOrNull(0)?.userID
                ?: 0) {
            leftFlag = true
            if (rightFlag) {
                timer?.dispose()
                mUiHandler.removeCallbacksAndMessages(null)
                listener.invoke()
            }
        }
        rightView.setData(list, roomData?.realRoundInfo?.subRoundInfo?.getOrNull(1)?.userID
                ?: 0) {
            rightFlag = true
            if (leftFlag) {
                timer?.dispose()
                mUiHandler.removeCallbacksAndMessages(null)
                listener.invoke()
            }
        }

        contentTv.visibility = View.VISIBLE
        resultIv.visibility = View.GONE
        matchStatusIv.background = U.getDrawable(R.drawable.race_matching_icon)
        contentTv.text = "${roomData?.realRoundInfo?.currentRoundChoiceUserCnt}人报名"

        mUiHandler.removeMessages(MSG_START)
        mUiHandler.sendEmptyMessage(MSG_START)

        mUiHandler.removeMessages(MSG_FAST_MUSIC)
        mUiHandler.sendEmptyMessage(MSG_FAST_MUSIC)

        mUiHandler.removeMessages(MSG_DECR_MUSIC)
        if (leftView.getFastTime() >= rightView.getFastTime()) {
            mUiHandler.sendEmptyMessageDelayed(MSG_DECR_MUSIC, leftView.getFastTime())
        } else {
            mUiHandler.sendEmptyMessageDelayed(MSG_DECR_MUSIC, rightView.getFastTime())
        }

        mUiHandler.removeMessages(MSG_RESULT_MUSIC)
        if (leftView.getAnimationTime() >= rightView.getAnimationTime()) {
            mUiHandler.sendEmptyMessageDelayed(MSG_RESULT_MUSIC, leftView.getAnimationTime())
        } else {
            mUiHandler.sendEmptyMessageDelayed(MSG_RESULT_MUSIC, rightView.getAnimationTime())
        }
    }

    // 播放快速滚动音乐
    private fun starFastBgMusic() {
        timer?.dispose()
        timer = HandlerTaskTimer.newBuilder()
                .take(-1)
                .interval(leftView.itemTime.toLong())
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        U.getSoundUtils().play(mTag, R.raw.rank_flipsonglist)
                    }
                })
    }

    // 播放减速滚动音乐
    private fun starDecrBgMusic() {
        timer?.dispose()
        timer = HandlerTaskTimer.newBuilder()
                .take(-1)
                .interval(2 * leftView.itemTime.toLong())
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        U.getSoundUtils().play(mTag, R.raw.rank_flipsonglist)
                    }
                })
    }

    // 播放是否选中音乐
    private fun starResultMusic() {
        timer?.dispose()
        contentTv.visibility = View.INVISIBLE
        resultIv.visibility = View.VISIBLE
        matchStatusIv.background = U.getDrawable(R.drawable.race_match_sucess_icon)

        if (roomData?.hasSignUpSelf == true) {
            if (roomData?.realRoundInfo?.isSingerByUserId(MyUserInfoManager.getInstance().uid.toInt()) == true) {
                resultIv.setImageResource(R.drawable.race_select_icon)
                U.getSoundUtils().play(mTag, R.raw.race_select)
            } else {
                resultIv.setImageResource(R.drawable.race_unselect_icon)
//            U.getSoundUtils().play(mTag, R.raw.race_unselect)
            }
        } else {
            resultIv.setImageResource(R.drawable.race_un_singup_icon)
        }

    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            mUiHandler.removeCallbacksAndMessages(null)
            timer?.dispose()
            leftView.reset()
            rightView.reset()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        U.getSoundUtils().release(mTag)
        mUiHandler.removeCallbacksAndMessages(null)
        timer?.dispose()
    }
}

