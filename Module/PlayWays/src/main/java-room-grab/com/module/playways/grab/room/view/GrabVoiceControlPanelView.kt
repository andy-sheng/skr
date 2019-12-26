package com.module.playways.grab.room.view

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import android.widget.LinearLayout

import com.common.utils.U
import com.kyleduo.switchbutton.SwitchButton
import com.module.playways.grab.room.GrabRoomData
import com.component.voice.control.VoiceControlPanelView
import com.module.playways.R

class GrabVoiceControlPanelView : VoiceControlPanelView {
    override fun getLayout(): Int {
        return R.layout.grab_voice_control_panel_layout
    }

    // 清唱与伴奏
    internal var mAccSb: SwitchButton?=null

    internal var roomData: GrabRoomData? = null

    protected override fun getMarginLeft(): Int {
        return U.getDisplayUtils().screenWidth - U.getDisplayUtils().dip2px((30 + 24).toFloat()) - U.getDisplayUtils().dip2px((44 * 5).toFloat())
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun init(context: Context?) {
        super.init(context)
        mAccSb = findViewById(R.id.acc_sb)
        mAccSb?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            if(roomData?.realRoundInfo?.singBySelf() == true){
                U.getToastUtil().showShort("你的演唱阶段无法修改演唱模式")
                mAccSb?.setCheckedNoEvent(!isChecked)
                return@OnCheckedChangeListener
            }
            if (roomData != null) {
                roomData!!.isAccEnable = isChecked
            }
        })
    }

    override fun bindData() {
        super.bindData()
        mAccSb?.setCheckedNoEvent(roomData!!.isAccEnable)
    }

    fun setRoomData(modelBaseRoomData: GrabRoomData) {
        roomData = modelBaseRoomData
    }
}
