package com.module.playways.race.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData

class RaceSignUpBtnView : ConstraintLayout {
    val TAG = "RaceSignUpBtnView"
    var signUpBtn: ExImageView
    var signUpType: SignUpType? = null
    var clickSignUpBtn: (() -> Unit)? = null
    var roomData: RaceRoomData? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.race_sign_up_view_layout, this)

        signUpBtn = rootView.findViewById(R.id.sign_up_btn)

        signUpBtn.setDebounceViewClickListener {
            clickSignUpBtn?.invoke()
        }
    }

    fun setType(type: SignUpType) {
        visibility = View.VISIBLE
        signUpType = type
        when (type) {
            SignUpType.SIGN_UP_START -> {
                signUpBtn.background = U.getDrawable(R.drawable.paiwei_baoming)
            }
            SignUpType.SIGN_UP_FINISH -> {
                signUpBtn.background = U.getDrawable(R.drawable.paiwei_yibaoming)
            }
        }
    }

    enum class SignUpType(val type: Int) {
        SIGN_UP_START(1), SIGN_UP_FINISH(2);

        val value: Int
            get() = type
    }
}