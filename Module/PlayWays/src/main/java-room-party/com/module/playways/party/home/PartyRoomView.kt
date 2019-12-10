package com.module.playways.party.home

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.module.RouterConstants
import com.module.playways.IPartyRoomView
import com.module.playways.IPlaywaysModeService
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.room.data.H
import com.module.playways.room.room.RoomServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody

// 首页的剧场
class PartyRoomView(context: Context) : ConstraintLayout(context), IPartyRoomView, CoroutineScope by MainScope() {

    private val refreshLayout: SmartRefreshLayout
    private val recyclerView: RecyclerView

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)
    private val adapter = PartyRoomAdapter()
    private var offset = 0
    private val cnt = 15

    private var roomJob: Job? = null
    private var lastLoadDateTime: Long = 0    //记录上次获取接口的时间
    private var recommendInterval: Long = 15 * 1000   // 自动更新的时间间隔

    init {
        View.inflate(context, R.layout.party_room_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        adapter.listener = { position, model ->
            model?.roomID?.let {
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService.tryGoPartyRoom(it, 1)
            }
        }

        refreshLayout.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    stopTimer()
                    loadData(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    initData(true)
                }

            })
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    starTimer(recommendInterval)
                } else {
                    stopTimer()
                }
            }
        })
    }

    fun loadData(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getPartyRoomList", ControlType.CancelThis)) {
                roomServerApi.getPartyRoomList(off, cnt)
            }
            if (result.errno == 0) {
                lastLoadDateTime = System.currentTimeMillis()
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("roomInfo"), PartyRoomInfoModel::class.java)
                addRoomList(list, isClean)
            }
            if (!isClean) {
                // 是加载更多
                starTimer(recommendInterval)
            }
        }
    }

    private fun addRoomList(list: List<PartyRoomInfoModel>?, isClean: Boolean) {
        if (isClean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                // todo 可能需要补一个去重
                val size = adapter.mDataList.size
                adapter.mDataList.addAll(list)
                val newSize = adapter.mDataList.size
                adapter.notifyItemRangeInserted(size, newSize - size)
            }
        }
    }

    private fun starTimer(delayTime: Long) {
        roomJob?.cancel()
        roomJob = launch {
            delay(delayTime)
            repeat(Int.MAX_VALUE) {
                loadData(0, true)
                delay(recommendInterval)
            }
        }
    }

    override fun stopTimer() {
        roomJob?.cancel()
    }

    override fun initData(flag: Boolean) {
        if (!flag) {
            var now = System.currentTimeMillis();
            if ((now - lastLoadDateTime) > recommendInterval) {
                starTimer(0)
            } else {
                var delayTime = recommendInterval - (now - lastLoadDateTime)
                starTimer(delayTime)
            }
        } else {
            starTimer(0)
        }
    }

    override fun destory() {
        cancel()
        roomJob?.cancel()
    }

}