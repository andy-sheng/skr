package com.module.playways.mic.room.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.CompoundButton
import com.common.rxretrofit.ApiManager
import com.kyleduo.switchbutton.SwitchButton
import com.module.playways.R
import com.module.playways.mic.room.MicRoomServerApi
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

// 右边操作区域，投票
class MicSettingView : ConstraintLayout {

    val mTag = "MicSettingView"

    private var mDialogPlus: DialogPlus? = null

    // 清唱与伴奏
    private var mSbAcc: SwitchButton? = null

    internal var mRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}


    init {
        View.inflate(context, R.layout.mic_setting_view_layout, this)
        mSbAcc = findViewById(R.id.sb_acc)

        mSbAcc?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

        })

        setOnClickListener {
            //拦截
        }
    }

    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.transparent)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }
}
