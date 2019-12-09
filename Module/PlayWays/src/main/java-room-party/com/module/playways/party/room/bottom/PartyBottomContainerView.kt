package com.module.playways.party.room.bottom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.*
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.relay.room.RelayRoomServerApi
import com.module.playways.room.room.view.BottomContainerView
import com.zq.mediaengine.kit.ZqEngineKit
import okhttp3.MediaType
import okhttp3.RequestBody


class PartyBottomContainerView : BottomContainerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var moreBtn: ExImageView? = null

    var roomData: PartyRoomData? = null
    var settingOpen = false
    var emojiOpen = false
    var listener: Listener? = null

    internal var mRoomServerApi = ApiManager.getInstance().createService(RelayRoomServerApi::class.java)

//    private val drawable = DrawableCreator.Builder()
//            .setShape(DrawableCreator.Shape.Rectangle)
//            .setCornersRadius(0f, 0f, 16.dp().toFloat(), 16.dp().toFloat())
//            .setSolidColor(Color.parseColor("#1F1C48"))
//            .setStrokeColor(U.getColor(R.color.white_trans_20))
//            .setStrokeWidth(1.dp().toFloat())
//            .build()

    override fun getLayout(): Int {
        return R.layout.party_bottom_container_view_layout
    }

    fun showBackground(isShow: Boolean) {
        if (isShow) {
            background = U.getDrawable(R.drawable.party_common_top_bg)
        } else {
            background = null
        }
    }

    override fun init() {
        super.init()
        moreBtn = this.findViewById(R.id.more_btn)

        mInputBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (roomData?.isMute == true) {
                    roomData?.isMute = false
                    mInputBtn?.setBackgroundResource(R.drawable.relay_unmute)
                    ZqEngineKit.getInstance().adjustRecordingSignalVolume(ZqEngineKit.getInstance().params.recordingSignalVolume, false)
                } else {
                    roomData?.isMute = true
                    mInputBtn?.setBackgroundResource(R.drawable.relay_mute)
                    ZqEngineKit.getInstance().adjustRecordingSignalVolume(0, false)
                }

                val map = mutableMapOf(
                        "roomID" to roomData?.gameId,
                        "userID" to MyUserInfoManager.uid,
                        "isMute" to (roomData?.isMute == true)
                )
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                ApiMethods.subscribe(mRoomServerApi.mute(body), object : ApiObserver<ApiResult>() {
                    override fun process(obj: ApiResult?) {
                        if (obj?.errno == 0) {

                        }
                    }
                })
            }
        })

        moreBtn?.setDebounceViewClickListener { listener?.onClickMore(!settingOpen) }
        mEmojiBtn?.setDebounceViewClickListener { listener?.onClickEmoji(!emojiOpen) }

    }

    override fun setRoomData(roomData: BaseRoomData<*>) {
        super.setRoomData(roomData)
        this.roomData = roomData as PartyRoomData

    }

    override fun dismissPopWindow() {
        super.dismissPopWindow()
    }

    interface Listener {
        fun onClickMore(open: Boolean)
        fun onClickEmoji(open: Boolean)
    }

}
