package com.module.playways.battle.songlist.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.module.playways.R
import com.module.playways.battle.BattleServerApi
import com.module.playways.battle.songlist.adapter.BattleRankAdapter
import com.module.playways.battle.songlist.model.BattleRankInfoModel
import com.module.playways.battle.songlist.model.BattleRankTagModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class BattleRankView(context: Context, val tag: BattleRankTagModel, val tagID: Int) : ConstraintLayout(context), CoroutineScope by MainScope() {

    private val refreshLayout: SmartRefreshLayout
    private val recyclerView: RecyclerView
    private val adapter = BattleRankAdapter()

    private val battleServerApi = ApiManager.getInstance().createService(BattleServerApi::class.java)

    var offset = 0
    val mCNT = 20
    var hasMore = true

    init {
        View.inflate(context, R.layout.battle_rank_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        refreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    loadMoreData()
                }
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        adapter.onClickListener = { model, _ ->
            model?.user?.let {
                // todo 跳到个人中心
            }
        }
    }

    fun tryLoadData() {
        getBattleRankList(0, true)
    }

    fun loadMoreData() {
        getBattleRankList(offset, false)
    }

    private fun getBattleRankList(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getBattleRankList", ControlType.CancelThis)) {
                battleServerApi.getStandRankList(MyUserInfoManager.getInstance().uid, tagID, off, mCNT, tag.tabType)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("details"), BattleRankInfoModel::class.java)
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                addRankList(list, isClean)
            } else {
                //todo 失败怎么处理
            }
        }
    }

    private fun addRankList(list: List<BattleRankInfoModel>?, clean: Boolean) {
        refreshLayout.finishLoadMore()
        refreshLayout.finishRefresh()
        refreshLayout.setEnableLoadMore(hasMore)

        if (clean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }
    }


    fun destory() {
        cancel()
    }
}