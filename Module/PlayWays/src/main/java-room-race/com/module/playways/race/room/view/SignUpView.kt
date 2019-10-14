package com.module.playways.race.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExImageView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.zq.live.proto.RaceRoom.ERaceRoundStatus

class SignUpView : ConstraintLayout {
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
                if (it != SignUpType.ALLCATION) {
                    clickSignUpBtn?.invoke()
                }
            }
        }
    }

    fun setCountDownTime(progress: Int, leaveTime: Int) {
        circleCountDownView.cancelAnim()
        circleCountDownView.setMax(360)
        circleCountDownView.setProgress(0)
        circleCountDownView.visibility = View.VISIBLE

        circleCountDownView.go(progress, leaveTime)
    }

    fun setType(type: SignUpType) {
        signUpType = type

        if (type == SignUpType.SIGN_UP_START) {
            signUpBtn.background = U.getDrawable(R.drawable.paiwei_baomingzhong)
            roomData?.let { raceRoomData ->
                val info = raceRoomData.realRoundInfo as RaceRoundInfoModel
                info?.let {
                    if (it.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
                        circleCountDownView.cancelAnim()
                    } else {
                        setCountDownTime(0, 8 * 1000)
                    }
                }
            }
        } else if (type == SignUpType.SIGN_UP_FINISH) {
            circleCountDownView.visibility = View.GONE
            circleCountDownView.cancelAnim()
            signUpBtn.background = U.getDrawable(R.drawable.paiwei_yibaoming)
        } else if (type == SignUpType.ALLCATION) {
            circleCountDownView.visibility = View.GONE
            circleCountDownView.cancelAnim()
            signUpBtn.background = U.getDrawable(R.drawable.paiwei_fenpeizhong)
        }
    }

    enum class SignUpType(val type: Int) {
        SIGN_UP_START(1), SIGN_UP_FINISH(2), ALLCATION(3);

        val value: Int
            get() = type
    }
}