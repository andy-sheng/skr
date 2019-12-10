package com.module.playways.party.room.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.adapter.PartyHasSelectedGameListRecyclerAdapter
import com.module.playways.party.room.event.PartyAddGameEvent
import com.module.playways.party.room.model.PartySelectedGameModel
import com.module.playways.room.data.H
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PartyHasSelectedGameListView : ExConstraintLayout {
    val mTag = "PartyHasSelectedGameListView"
    var recyclerView: RecyclerView

    var partyHasSelectedGameListRecyclerAdapter: PartyHasSelectedGameListRecyclerAdapter? = null
    var smartRefresh: SmartRefreshLayout

    var offset = 0
    val cnt = 30
    var hasMore = true

    var needLoad = true

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        View.inflate(context, R.layout.party_game_list_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        partyHasSelectedGameListRecyclerAdapter = PartyHasSelectedGameListRecyclerAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = partyHasSelectedGameListRecyclerAdapter

        partyHasSelectedGameListRecyclerAdapter?.mDelMethod = { pos, model ->
            delGame(pos, model)
        }

        partyHasSelectedGameListRecyclerAdapter?.mUpMethod = { pos, model ->
            upGame(pos, model)
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
                    getSelectedGameList()
                }
            })
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    fun selected() {
        if (needLoad) {
            getSelectedGameList()
            needLoad = false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyAddGameEvent) {
        needLoad = true
        offset = 0
        partyHasSelectedGameListRecyclerAdapter?.dataList?.clear()
    }

    fun getSelectedGameList() {
        launch {
            val result = subscribe(RequestControl("${mTag} getGameList", ControlType.CancelThis)) {
                roomServerApi.getListGame(H.partyRoomData?.gameId ?: 0, offset, cnt)
            }

            if (result.errno == 0) {
                hasMore = result.data.getBooleanValue("hasMore")
                offset = result.data.getIntValue("offset")

                val list = JSON.parseArray(result.data.getString("details"), PartySelectedGameModel::class.java)
                list?.let {
                    partyHasSelectedGameListRecyclerAdapter?.addData(list)
                }

                smartRefresh.setEnableLoadMore(hasMore)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }

            smartRefresh.finishLoadMore()
        }
    }

    fun delGame(pos: Int, model: PartySelectedGameModel) {
        launch {
            val map = mutableMapOf(
                    "roomID" to H.partyRoomData?.gameId,
                    "sceneTag" to model.sceneTag
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("${mTag} op", ControlType.CancelThis)) {
                roomServerApi.delGame(body)
            }

            if (result.errno == 0) {
                partyHasSelectedGameListRecyclerAdapter?.deleteAt(pos)

                if (offset > 0) {
                    offset--
                }

                U.getToastUtil().showShort("删除成功")
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    fun upGame(pos: Int, model: PartySelectedGameModel) {
        launch {
            val map = mutableMapOf(
                    "roomID" to H.partyRoomData?.gameId,
                    "sceneTag" to model.sceneTag
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("${mTag} op", ControlType.CancelThis)) {
                roomServerApi.upGame(body)
            }

            if (result.errno == 0) {
                partyHasSelectedGameListRecyclerAdapter?.upModel(pos)
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    interface Listener {
        fun onClickApplyList()
    }
}