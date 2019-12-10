package com.module.playways.party.room.fragment

import android.os.Bundle
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.room.data.H
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class GonggaoSettingFragment : BaseFragment() {
    val mTag = "GonggaoSettingFragment"

    lateinit var titlebar: CommonTitleBar
    lateinit var editText: NoLeakEditText
    lateinit var saveTv: ExTextView

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    override fun initView(): Int {
        return R.layout.gonggao_setting_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        editText = rootView.findViewById(R.id.edit_text)
        saveTv = rootView.findViewById(R.id.save_tv)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        saveTv.setDebounceViewClickListener {
            setNotice(editText.text.toString())
        }
    }

    private fun setNotice(notice: String) {
        launch {
            val map = mutableMapOf(
                    "notice" to notice,
                    "roomID" to H.partyRoomData?.gameId
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("${mTag} setNotice", ControlType.CancelThis)) {
                roomServerApi.setNotice(body)
            }

            if (result.errno == 0) {
                U.getToastUtil().showShort("公告设置成功")
                finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun destroy() {
        super.destroy()
        U.getSoundUtils().release(TAG)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
