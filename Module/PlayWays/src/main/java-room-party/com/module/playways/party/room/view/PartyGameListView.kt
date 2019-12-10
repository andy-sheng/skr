package com.module.playways.party.room.view

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.adapter.PartyGameListRecyclerAdapter
import com.module.playways.party.room.event.PartyAddGameEvent
import com.module.playways.party.room.fragment.PartyGamePlayListFragment
import com.module.playways.party.room.model.PartyRule
import com.module.playways.room.data.H
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

class PartyGameListView : ExConstraintLayout {
    val mTag = "PartyGameListView"

    var recyclerView: RecyclerView
    var smartRefresh: SmartRefreshLayout

    var partyGameListRecyclerAdapter: PartyGameListRecyclerAdapter? = null

    var offset = 0
    val cnt = 30
    var hasMore = true

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.party_game_list_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        partyGameListRecyclerAdapter = PartyGameListRecyclerAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = partyGameListRecyclerAdapter

        partyGameListRecyclerAdapter?.mMoreMethod = { model ->
            U.getFragmentUtils().addFragment(
                    FragmentUtils.newAddParamsBuilder(context as FragmentActivity, PartyGamePlayListFragment::class.java)
                            .setAddToBackStack(true)
                            .addDataBeforeAdd(1, model)
                            .setHasAnimation(true)
                            .build())
        }

        partyGameListRecyclerAdapter?.mAddMethod = { model ->
            addGame(model)
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
                    getGameList()
                }
            })
        }

        getGameList()
    }

    fun selected() {

    }

    fun getGameList() {
        launch {
            val result = subscribe(RequestControl("${mTag} getGameList", ControlType.CancelThis)) {
                roomServerApi.getPartyGameRuleList(H.partyRoomData?.gameId ?: 0, offset, cnt)
            }

            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")

                val list = JSON.parseArray(result.data.getString("rules"), PartyRule::class.java)
                list?.let {
                    partyGameListRecyclerAdapter?.addData(list)
                }

                smartRefresh.setEnableLoadMore(hasMore)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }

            smartRefresh.finishLoadMore()
        }
    }

    fun addGame(partyRule: PartyRule) {
        launch {
            val map = mutableMapOf(
                    "roomID" to H.partyRoomData?.gameId,
                    "ruleID" to partyRule.ruleID
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("${mTag} addGame", ControlType.CancelThis)) {
                roomServerApi.addGame(body)
            }

            if (result.errno == 0) {
                EventBus.getDefault().post(PartyAddGameEvent())
                U.getToastUtil().showShort("添加成功")
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }
}