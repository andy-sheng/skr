package com.module.playways.relay.room.bottom

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
import com.component.busilib.constans.GrabRoomType
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.dynamicmsg.DynamicMsgView
import com.module.playways.grab.room.event.GrabRoundChangeEvent
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent
import com.module.playways.grab.room.voicemsg.VoiceRecordTextView
import com.module.playways.relay.room.RelayRoomData
import com.module.playways.room.room.view.BottomContainerView
import com.zq.mediaengine.kit.ZqEngineKit
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class RelayBottomContainerView : BottomContainerView {
//    var mBottomMuteListener: ((mute:Boolean)->Unit)? = null
    var roomData:RelayRoomData? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun getLayout(): Int {
        return R.layout.relay_bottom_container_view_layout
    }

    override fun init() {
        super.init()
        mInputBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if(roomData?.isMute == true){
                    roomData?.isMute = false
                    mInputBtn?.setBackgroundResource(R.drawable.relay_unmute)
                    ZqEngineKit.getInstance().adjustRecordingSignalVolume(ZqEngineKit.getInstance().params.recordingSignalVolume,false)
                }else{
                    roomData?.isMute = true
                    mInputBtn?.setBackgroundResource(R.drawable.relay_mute)
                    ZqEngineKit.getInstance().adjustRecordingSignalVolume(0,false)
                }
            }
        })

    }

    override fun setRoomData(roomData: BaseRoomData<*>) {
        super.setRoomData(roomData)
        this.roomData = roomData as RelayRoomData

    }
    override fun dismissPopWindow() {
        super.dismissPopWindow()
    }


}
