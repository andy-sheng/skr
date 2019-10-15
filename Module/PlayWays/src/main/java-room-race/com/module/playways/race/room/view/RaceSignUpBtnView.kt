package com.module.playways.race.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExImageView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.zq.live.proto.RaceRoom.ERaceRoundStatus

class RaceSignUpBtnView : ConstraintLayout {
    val TAG = "RaceSignUpBtnView"
    var signUpBtn: ExImageView
    var circleCountDownView: CircleCountDownView
    var signUpType: SignUpType? = null
    var clickSignUpBtn: (() -> Unit)? = null
    var roomData: RaceRoomData? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_sign_up_view_layout, this)

        signUpBtn = rootView.findViewById(R.id.sign_up_btn)
        circleCountDownView = rootView.findViewById(R.id.circle_count_down_view)

        signUpBtn.setDebounceViewClickListener {
            signUpType?.let {
                if (it != SignUpType.ALLOCATION) {
                    clickSignUpBtn?.invoke()
                }
            }
        }
    }

    val mLastedTime = 10000

    fun setCountDownTime() {
        var lastedTime = mLastedTime
        if (roomData?.realRoundInfo?.enterStatus == ERaceRoundStatus.ERRS_CHOCING.value) {
            roomData?.realRoundInfo?.elapsedTimeMs?.let {
                //多3秒是因为中间动画（显示结果3秒|（无人抢唱+下一首）3秒）
                lastedTime = 14400 - it
                MyLog.d(TAG, "setSongName elapsedTimeMs is $it")
                if (lastedTime > mLastedTime) {
                    lastedTime = mLastedTime
                }
            }
        }

        circleCountDownView.cancelAnim()
        circleCountDownView.max = 360
        circleCountDownView.progress = 0
        circleCountDownView.visibility = View.VISIBLE

        circleCountDownView.go(mLastedTime - lastedTime, lastedTime)
    }

    fun cancelCountDown() {
        circleCountDownView.cancelAnim()
        circleCountDownView.visibility = View.GONE
    }

    fun setType(type: SignUpType) {
        visibility = View.VISIBLE
        signUpType = type
        when (type) {
            SignUpType.SIGN_UP_START -> {
                signUpBtn.background = U.getDrawable(R.drawable.paiwei_baomingzhong)
                roomData?.let { raceRoomData ->

                    var info: RaceRoundInfoModel? = raceRoomData.realRoundInfo as RaceRoundInfoModel
                    if (info == null) {
                        info = raceRoomData.expectRoundInfo as RaceRoundInfoModel
                    }

                    if (info != null) {
                        if (info?.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
                            circleCountDownView.visibility = View.GONE
                            circleCountDownView.cancelAnim()
                        } else {
                            setCountDownTime()
                        }
                    }
                }
            }
            SignUpType.SIGN_UP_FINISH -> {
                circleCountDownView.visibility = View.GONE
                circleCountDownView.cancelAnim()
                signUpBtn.background = U.getDrawable(R.drawable.paiwei_yibaoming)
            }
            SignUpType.ALLOCATION -> {
                circleCountDownView.visibility = View.GONE
                circleCountDownView.cancelAnim()
                signUpBtn.background = U.getDrawable(R.drawable.paiwei_fenpeizhong)
            }
        }
    }

    override fun setVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            if (roomData?.realRoundInfo?.enterStatus == ERaceRoundStatus.ERRS_CHOCING.value
                    && roomData?.realRoundInfo?.status == ERaceRoundStatus.ERRS_CHOCING.value) {
                MyLog.w(TAG, "中途进来的，而且是选歌阶段，不展示报名按钮")
            } else {
                super.setVisibility(visibility)
            }
        } else {
            super.setVisibility(visibility)
        }
    }

    enum class SignUpType(val type: Int) {
        SIGN_UP_START(1), SIGN_UP_FINISH(2), ALLOCATION(3);

        val value: Int
            get() = type
    }
}