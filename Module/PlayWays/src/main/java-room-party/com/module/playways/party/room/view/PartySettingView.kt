package com.module.playways.party.room.view

import android.graphics.Rect
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.flutter.boost.FlutterBoostController
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.ExViewStub
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.room.data.H
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class PartySettingView(viewStub: ViewStub) : ExViewStub(viewStub) {

    lateinit var gameSettingTv: TextView
    lateinit var soundSettingTv: TextView
    lateinit var muteSettingTv: TextView
    lateinit var bgmTv: TextView
    lateinit var voteTv: TextView
    lateinit var qiangdaTv: TextView
    lateinit var punishmentTv: TextView
    lateinit var roomSettingTv: TextView

    var listener: Listener? = null

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    override fun init(parentView: View) {
        gameSettingTv = parentView.findViewById(R.id.game_setting_tv)
        soundSettingTv = parentView.findViewById(R.id.sound_setting_tv)
        muteSettingTv = parentView.findViewById(R.id.mute_setting_tv)
        bgmTv = parentView.findViewById(R.id.bgm_tv)
        voteTv = parentView.findViewById(R.id.vote_tv)
        qiangdaTv = parentView.findViewById(R.id.qiangda_tv)
        punishmentTv = parentView.findViewById(R.id.punishment_tv)
        roomSettingTv = parentView.findViewById(R.id.room_setting_tv)


        gameSettingTv.setAnimateDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "tool_game_click", null)
            listener?.onClickGameSetting()
        }

        soundSettingTv.setAnimateDebounceViewClickListener {
            listener?.onClickGameSound()
        }

        voteTv.setAnimateDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "tool_vote_click", null)
            listener?.onClickVote()
        }
        qiangdaTv.setAnimateDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "tool_responder_click", null)
            listener?.onClickQuickAnswer()
        }
        muteSettingTv.setAnimateDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "tool_microphone_click", null)
            var b = (H.partyRoomData?.isAllMute == true)
            setAllMicMute(!b)
        }
        punishmentTv.setAnimateDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "tool_punishment_click", null)
            listener?.onClickPunishment()
        }
        roomSettingTv.setAnimateDebounceViewClickListener {
            listener?.onClickRoomSetting()
        }
        bgmTv.setAnimateDebounceViewClickListener {
            StatisticsAdapter.recordCountEvent("party", "tool_background_music_click", null)
            FlutterBoostController.openFlutterPage(realView?.context!!, RouterConstants.FLUTTER_PAGE_PARTY_BGM_PAGE, null)
        }
    }

    private fun setAllMicMute(muteStatus: Boolean) {
        launch {
            // 2闭麦  1开麦
            val micStatus = if (muteStatus) 2 else 1
            val map = mutableMapOf(
                    "micStatus" to micStatus,
                    "roomID" to H.partyRoomData?.gameId
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("setAllMicStatus", ControlType.CancelThis)) {
                roomServerApi.setAllMicStatus(body)
            }
            if (result.errno == 0) {
                H.partyRoomData?.isAllMute = muteStatus
                refreshAllMute()
            } else {

            }
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.party_setting_view_layout
    }

    fun bindData() {
        tryInflate()
        refreshAllMute()
    }

    private fun refreshAllMute() {
        var drawable = U.getDrawable(R.drawable.party_setting_all_unmute)
        if (H.partyRoomData?.isAllMute == true) {
            muteSettingTv.text = "全员开麦"
        } else {
            drawable = U.getDrawable(R.drawable.party_setting_all_mute)
            muteSettingTv.text = "全员闭麦"
        }
        drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        muteSettingTv.setCompoundDrawables(null, drawable, null, null)
    }

    interface Listener {
        fun onClickGameSetting()
        fun onClickGameSound()
        fun onClickVote()
        fun onClickQuickAnswer()
        fun onClickPunishment()
        fun onClickRoomSetting()
    }
}
