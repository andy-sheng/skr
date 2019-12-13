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
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.event.PartyMySeatInfoChangeEvent
import com.module.playways.party.room.event.PartyMyUserInfoChangeEvent
import com.module.playways.relay.room.RelayRoomServerApi
import com.module.playways.room.data.H
import com.module.playways.room.room.view.BottomContainerView
import com.zq.live.proto.PartyRoom.EMicStatus
import com.zq.mediaengine.kit.ZqEngineKit
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PartyBottomContainerView : BottomContainerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var moreBtn: ExImageView? = null
    private var partyEmojiTv: ExImageView? = null

    var roomData: PartyRoomData? = null
    var settingOpen = false
    var emojiOpen = false
    var listener: Listener? = null

    internal var mRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

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
        partyEmojiTv = this.findViewById(R.id.party_emoji_tv)

        mInputBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 嘉宾和主持人
                when {
                    roomData?.myUserInfo?.isHost() == true -> {
                        var micStatus = EMicStatus.MS_CLOSE.value
                        if (roomData?.isMute == true) {  // 已经被禁麦了
                            micStatus = EMicStatus.MS_OPEN.value
                        }
                        val map = mutableMapOf(
                                "roomID" to roomData?.gameId,
                                "micStatus" to micStatus
                        )
                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                        ApiMethods.subscribe(mRoomServerApi.setHostMicStatus(body), object : ApiObserver<ApiResult>() {
                            override fun process(obj: ApiResult?) {
                                if (obj?.errno == 0) {
                                    roomData?.isMute = (roomData?.isMute == false)
                                    refreshInputMic()
                                }
                            }
                        })
                    }
                    roomData?.myUserInfo?.isGuest() == true -> {
                        val mySeatInfo = roomData?.mySeatInfo
                        var micStatus = EMicStatus.MS_CLOSE.value
                        if (mySeatInfo?.micStatus == EMicStatus.MS_CLOSE.value) {
                            micStatus = EMicStatus.MS_OPEN.value
                        }
                        val map = mutableMapOf(
                                "roomID" to roomData?.gameId,
                                "seatSeq" to mySeatInfo?.seatSeq,
                                "seatUserID" to MyUserInfoManager.uid,
                                "micStatus" to micStatus
                        )

                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                        ApiMethods.subscribe(mRoomServerApi.setUserMicStatus2(body), object : ApiObserver<ApiResult>() {
                            override fun process(obj: ApiResult?) {
                                if (obj?.errno == 0) {

                                }
                            }
                        })
                    }
                    else -> {

                    }
                }
            }
        })

        moreBtn?.setDebounceViewClickListener { listener?.onClickMore(!settingOpen) }
        partyEmojiTv?.setDebounceViewClickListener { listener?.onClickEmoji(!emojiOpen) }

    }


    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    fun onEvent(event: PartyMySeatInfoChangeEvent) {
        bindData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    fun onEvent(event: PartyMyUserInfoChangeEvent) {
        bindData()
        // 身份变化了，需要判断底部是否展开，直接收起
        // 不是嘉宾，并且是展开状态 收起来
        val myInfo = roomData?.getMyUserInfoInParty()
        if (myInfo?.isGuest() != true && (settingOpen || emojiOpen)) {
            listener?.onClickEmoji(false)
            listener?.onClickMore(false)
        }

    }

    override fun setRoomData(roomData: BaseRoomData<*>) {
        super.setRoomData(roomData)
        this.roomData = roomData as PartyRoomData
        bindData()
    }

    private fun bindData() {
        val myInfo = roomData?.getMyUserInfoInParty()
        when {
            myInfo?.isHost() == true -> {
                // 主持人
                mInputBtn?.visibility = View.VISIBLE
                moreBtn?.visibility = View.VISIBLE
                partyEmojiTv?.visibility = View.GONE
            }
            myInfo?.isGuest() == true -> {
                // 嘉宾
                mInputBtn?.visibility = View.VISIBLE
                moreBtn?.visibility = View.GONE
                partyEmojiTv?.visibility = View.VISIBLE
            }
            else -> {
                // 观众
                mInputBtn?.visibility = View.GONE
                moreBtn?.visibility = View.GONE
                partyEmojiTv?.visibility = View.GONE
            }
        }

        refreshInputMic()
    }

    private fun refreshInputMic() {
        if (roomData?.isMute == true) {
            mInputBtn?.setBackgroundResource(R.drawable.relay_mute)
            ZqEngineKit.getInstance().adjustRecordingSignalVolume(0, false)
        } else {
            mInputBtn?.setBackgroundResource(R.drawable.relay_unmute)
            ZqEngineKit.getInstance().adjustRecordingSignalVolume(ZqEngineKit.getInstance().params.recordingSignalVolume, false)
        }
    }

    override fun dismissPopWindow() {
        super.dismissPopWindow()
    }

    interface Listener {
        fun onClickMore(open: Boolean)
        fun onClickEmoji(open: Boolean)
    }

}
