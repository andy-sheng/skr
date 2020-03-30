package com.common.core.policy

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.R
import com.common.view.ex.ExTextView

class PrivacyPolicyDialogView : ConstraintLayout {
    constructor(context: Context?) : super(context){
    }
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs){

    }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
    }

    private var mConfirmTv:ExTextView? = null
    init {
        View.inflate(context, R.layout.privacy_policy_view_layout, this)
        mConfirmTv = findViewById(R.id.privacy_ok_tv)
    }

    fun setOkClikListener(okClick:(() ->Unit)) {
        mConfirmTv?.setOnClickListener {
            okClick.invoke()
        }
    }





}