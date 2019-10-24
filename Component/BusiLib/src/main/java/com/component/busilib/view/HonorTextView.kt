package com.component.busilib.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.common.core.userinfo.model.HonorInfo
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.zq.live.proto.Common.ESex

class HonorTextView : ExTextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var sexDrawable: Drawable? = null
    var vipDrawable: Drawable? = null

    fun setText(name: String, sex: Int? = null, honorInfo: HonorInfo? = null) {
        sexDrawable = null
        vipDrawable = null

        val spanUtils = SpanUtils()
                .append(name)
        sex?.let {
            if (sex == ESex.SX_MALE.value) {
                sexDrawable = U.getDrawable(R.drawable.sex_man_icon)
            } else if (sex == ESex.SX_FEMALE.value) {
                sexDrawable = U.getDrawable(R.drawable.sex_woman_icon)
            }
        }
        sexDrawable?.let {
            spanUtils.appendImage(it, SpanUtils.ALIGN_CENTER)
        }

        honorInfo?.let {
            if (it.status == HonorInfo.EHS_HAS_ACTIVE && it.honorType == HonorInfo.EHT_COMMON) {
                vipDrawable = U.getDrawable(R.drawable.person_vip_icon)
            }
        }

        vipDrawable?.let {
            spanUtils.appendImage(it, SpanUtils.ALIGN_CENTER)
        }

        val stringBuilder = spanUtils.create()
        text = stringBuilder
    }
}
