package com.module.playways.party.room.fragment

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.titlebar.CommonTitleBar
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.room.data.H
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class AdministratorSelectFragment : BaseFragment() {
    val mTag = "AdministratorSelectFragment"
    lateinit var titlebar: CommonTitleBar
    lateinit var bgIv: ExImageView
    lateinit var recyclerView: RecyclerView

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    override fun initView(): Int {
        return R.layout.administrator_select_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        bgIv = rootView.findViewById(R.id.bg_iv)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }
    }

    private fun setAdmin(userID: Int, setType: Int) {
        launch {
            val map = mutableMapOf(
                    "adminUserID" to userID,
                    "roomID" to H.partyRoomData?.gameId,
                    "setType" to setType
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("${mTag} setAdmin", ControlType.CancelThis)) {
                roomServerApi.setAdmin(body)
            }

            if (result.errno == 0) {

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
