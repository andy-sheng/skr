package com.module.playways.mic.room.bottom

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.grab.room.dynamicmsg.DynamicMsgView
import com.module.playways.grab.room.voicemsg.VoiceRecordTextView
import com.module.playways.room.room.view.BottomContainerView

class MicBottomContainerView : BottomContainerView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

//    override fun getLayout(): Int {
//        return R.layout.mic_bottom_container_view_layout
//    }

}
