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
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

class AdministratorSelectFragment : BaseFragment() {
    val mTag = "AdministratorSelectFragment"
    lateinit var titlebar: CommonTitleBar
    lateinit var bgIv: ExImageView
    lateinit var recyclerView: RecyclerView
    lateinit var smartRefresh: SmartRefreshLayout
    var changeHostRecyclerAdapter: ChangeHostRecyclerAdapter? = null

    var offset = 0
    val cnt = 30
    var hasMore = true

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    override fun initView(): Int {
        return R.layout.administrator_select_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        bgIv = rootView.findViewById(R.id.bg_iv)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        changeHostRecyclerAdapter = ChangeHostRecyclerAdapter()
        recyclerView.adapter = changeHostRecyclerAdapter

        changeHostRecyclerAdapter?.mOpMethod = { pos, model ->
            setAdmin(pos, model)
        }

        smartRefresh.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(true)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)
            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    super.onLoadMore(refreshLayout)
                    getOnlineUserList()
                }
            })
        }

        getOnlineUserList()
    }

    private fun getOnlineUserList() {
        launch {
            val result = subscribe(RequestControl("${mTag} setAdmin", ControlType.CancelThis)) {
                roomServerApi.getOnlineUserList(H.partyRoomData?.gameId ?: 0, offset, cnt)
            }

            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")

                val list = JSON.parseArray(result.data.getString("users"), PartyPlayerInfoModel::class.java)

                val iterator = list.iterator()
                while (iterator.hasNext()) {
                    if (iterator.next().isHost()) {
                        iterator.remove()
                        break
                    }
                }

                changeHostRecyclerAdapter?.addData(list)
                smartRefresh.setEnableLoadMore(hasMore)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }

            smartRefresh.finishLoadMore()
        }
    }

    private fun setAdmin(pos: Int, model: PartyPlayerInfoModel) {
        launch {
            val map = mutableMapOf(
                    "adminUserID" to model.userID,
                    "roomID" to H.partyRoomData?.gameId,
                    "setType" to if (model.isAdmin()) 2 else 1
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("${mTag} setAdmin", ControlType.CancelThis)) {
                roomServerApi.setAdmin(body)
            }

            if (result.errno == 0) {
                if (model.isAdmin()) {
                    model.role.remove(2)
                } else {
                    if (!model.role.contains(2)) {
                        model.role.add(2)
                    }
                }

                changeHostRecyclerAdapter?.notifyItemChanged(pos, 1)

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
