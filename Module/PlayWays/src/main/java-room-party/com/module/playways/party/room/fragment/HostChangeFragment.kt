package com.module.playways.party.room.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import com.module.playways.party.room.adapter.ChangeHostRecyclerAdapter
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.room.data.H
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class HostChangeFragment : BaseFragment() {
    val mTag = "HostChangeFragment"
    lateinit var titlebar: CommonTitleBar
    lateinit var bgIv: ExImageView
    lateinit var recyclerView: RecyclerView

    var administratorSelectRecyclerAdapter: ChangeHostRecyclerAdapter? = null

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    override fun initView(): Int {
        return R.layout.host_change_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        bgIv = rootView.findViewById(R.id.bg_iv)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        administratorSelectRecyclerAdapter = ChangeHostRecyclerAdapter()
        recyclerView.adapter = administratorSelectRecyclerAdapter

        administratorSelectRecyclerAdapter?.mOpMethod = { pos, model ->
            giveUpClubHost(model)
        }

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        getCouldBeHostList()
    }

    fun giveUpClubHost(model: PartyPlayerInfoModel) {
        val map = HashMap<String, Any?>()
        map["roomID"] = H.partyRoomData?.gameId
        map["getHostUserID"] = model.userID

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("giveClubHost", ControlType.CancelThis)) {
                roomServerApi.giveClubHost(body)
            }

            if (result.errno == 0) {
                finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun getCouldBeHostList() {
        launch {
            val result = subscribe(RequestControl("${mTag} getCouldBeHostList", ControlType.CancelThis)) {
                roomServerApi.getCouldBeHostList(H.partyRoomData?.gameId ?: 0, 0, 30)
            }

            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("users"), PartyPlayerInfoModel::class.java)
                list?.let {
                    if (it.size > 0) {
                        val iterator = list.iterator()
                        while (iterator.hasNext()) {
                            if (iterator.next().userID == (H.partyRoomData?.hostId ?: 0)) {
                                iterator.remove()
                            }
                        }

                        administratorSelectRecyclerAdapter?.addData(it)
                        administratorSelectRecyclerAdapter?.notifyDataSetChanged()
                    }
                }
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
