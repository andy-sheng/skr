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
import com.common.view.titlebar.CommonTitleBar
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.adapter.PartyGamePlayListRecyclerAdapter
import com.module.playways.party.room.model.PartyPlayRule
import com.module.playways.party.room.model.PartyRule
import com.module.playways.room.data.H
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch

class PartyGamePlayListFragment : BaseFragment() {
    val mTag = "PartyGamePlayListFragment"

    lateinit var titlebar: CommonTitleBar
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView
    var partyRule: PartyRule? = null

    var partyGamePlayListRecyclerAdapter: PartyGamePlayListRecyclerAdapter? = null

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    var offset = 0
    val cnt = 30
    var hasMore = true

    override fun initView(): Int {
        return R.layout.party_game_play_list_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = rootView.findViewById(R.id.titlebar)
        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        partyGamePlayListRecyclerAdapter = PartyGamePlayListRecyclerAdapter()
        recyclerView.adapter = partyGamePlayListRecyclerAdapter

        partyGamePlayListRecyclerAdapter?.mAddMethod = {

        }

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        smartRefresh.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(true)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)
            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    super.onLoadMore(refreshLayout)
                    getGamePlayList()
                }
            })
        }

        getGamePlayList()
    }

    fun getGamePlayList() {
        launch {
            val result = subscribe(RequestControl("${mTag} getGameList", ControlType.CancelThis)) {
                roomServerApi.getPartyGamePlayList(H.partyRoomData?.gameId ?: 0, partyRule?.ruleID
                        ?: 0, offset, cnt)
            }

            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")

                val list = JSON.parseArray(result.data.getString("plays"), PartyPlayRule::class.java)
                list?.let {
                    partyGamePlayListRecyclerAdapter?.addData(list)
                }

                smartRefresh.setEnableLoadMore(hasMore)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }

            smartRefresh.finishLoadMore()
        }
    }

    override fun setData(type: Int, data: Any?) {
        data?.let {
            if (type == 1) {
                partyRule = it as PartyRule
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
