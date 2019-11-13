package com.component.busilib.view

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.userinfo.model.HonorInfo
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.zq.live.proto.Common.ESex

class NickNameView : ConstraintLayout {
    constructor(context: Context?) : super(context) {
        initView(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    private val nameTv: TextView
    private val sexIv: ImageView
    private val honorIv: ImageView
    private val specialTv: ExTextView

    private var textColor: Int = Color.parseColor("#3B4E79")   // 昵称TextView颜色
    private var textSize: Float = 18.dp().toFloat()                      // 昵称TextView文字大小
    private var textType: Int = 0                                        // 昵称TextView文字风格

    private fun initView(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.NickNameView)
        array?.let {
            textColor = it.getColor(R.styleable.NickNameView_textColor, Color.parseColor("#3B4E79"))
            textSize = it.getDimension(R.styleable.NickNameView_textSize, 18.dp().toFloat())
            textType = it.getInt(R.styleable.NickNameView_textStyle, 0)
        }
        array?.recycle()
    }

    init {
        View.inflate(context, R.layout.nickname_view_layout, this)

        nameTv = this.findViewById(R.id.name_tv)
        sexIv = this.findViewById(R.id.sex_iv)
        honorIv = this.findViewById(R.id.honor_iv)
        specialTv = this.findViewById(R.id.special_tv)

    }

    private fun initNameTv() {
        nameTv.setTextColor(textColor)
        nameTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        nameTv.setTypeface(Typeface.defaultFromStyle(textType))
    }

    /**
     * 昵称和VIP信息
     */
    fun setHonorText(name: String?, honorInfo: HonorInfo? = null) {
        initNameTv()
        sexIv.visibility = View.GONE
        nameTv.visibility = View.VISIBLE
        nameTv.text = name

        if (honorInfo?.isHonor() == true) {
            honorIv.visibility = View.VISIBLE
        } else {
            honorIv.visibility = View.GONE
        }
    }

    fun setAllStateText(model: UserInfoModel?) {
        if (model?.isSPFollow == true) {
            specialTv.visibility = View.VISIBLE
        } else {
            specialTv.visibility = View.GONE
        }
        setAllStateText(model?.nicknameRemark, model?.sex, model?.honorInfo)
    }

    /**
     * 昵称，性别和VIP信息
     */
    fun setAllStateText(name: String?, sex: Int?, honorInfo: HonorInfo?) {
        initNameTv()
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
