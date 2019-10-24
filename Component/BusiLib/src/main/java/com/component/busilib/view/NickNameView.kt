package com.component.busilib.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.userinfo.model.HonorInfo
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.zq.live.proto.Common.ESex

class NickNameView : ConstraintLayout {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val nameTv: TextView
    private val sexIv: ImageView
    private val honorIv: ImageView

    init {
        View.inflate(context, R.layout.nickname_view_layout, this)

        nameTv = this.findViewById(R.id.name_tv)
        sexIv = this.findViewById(R.id.sex_iv)
        honorIv = this.findViewById(R.id.honor_iv)
    }

    /**
     * 昵称和VIP信息
     */
    fun setHonorText(name: String, honorInfo: HonorInfo? = null) {
        sexIv.visibility = View.GONE
        nameTv.visibility = View.VISIBLE
        nameTv.text = name

        if (honorInfo?.isHonor() == true) {
            honorIv.visibility = View.VISIBLE
        } else {
            honorIv.visibility = View.GONE
        }
    }

    /**
     * 昵称，性别和VIP信息
     */
    fun setAllStateText(name: String, sex: Int?, honorInfo: HonorInfo?) {
        nameTv.visibility = View.VISIBLE
        nameTv.text = name

        when (sex) {
            ESex.SX_MALE.value -> {
                sexIv.visibility = View.VISIBLE
                sexIv.background = U.getDrawable(R.drawable.sex_man_icon)
            }
            ESex.SX_FEMALE.value -> {
                sexIv.visibility = View.VISIBLE
                sexIv.background = U.getDrawable(R.drawable.sex_woman_icon)
            }
            else -> sexIv.visibility = View.GONE
        }

        if (honorInfo?.isHonor() == true) {
            honorIv.visibility = View.VISIBLE
        } else {
            honorIv.visibility = View.GONE
        }
    }
}
