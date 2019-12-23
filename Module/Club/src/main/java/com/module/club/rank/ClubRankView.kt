package com.module.club.rank

import android.content.Context
import android.os.Bundle
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
import com.module.club.ClubServerApi
import com.module.club.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ClubRankView(context: Context, val clubID: Int, val model: ClubTagModel) : ConstraintLayout(context), CoroutineScope by MainScope() {

    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private var hasMore = true
    private val cnt = 15

    private val refreshLayout: SmartRefreshLayout
    private val recyclerView: RecyclerView

    private val adapter: ClubRankAdapter

    init {
        View.inflate(context, R.layout.club_rank_list_view_layout, this)

        refreshLayout = this.findViewById(R.id.refreshLayout)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = ClubRankAdapter(object : ClubRankAdapter.Listener {
            override fun onClickAvatar(position: Int, model: ClubRankModel?) {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", model?.userInfoModel?.userId ?: 0)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        })
        recyclerView.adapter = adapter

        refreshLayout.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getRankList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

            })
        }
    }

    fun tryLoad() {
        getRankList(0, true)
    }

    private fun getRankList(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getClubRankTags", ControlType.CancelThis)) {
                clubServerApi.getClubRankList(off, cnt, model.type, clubID)
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                val list = JSON.parseArray(result.data.getString("items"), ClubRankModel::class.java)
                addList(list, isClean)
            }
            finishRefreshAndLoadMore()
        }
    }

    private fun finishRefreshAndLoadMore() {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
        refreshLayout.setEnableLoadMore(hasMore)
    }

    private fun addList(list: List<ClubRankModel>?, isClean: Boolean) {
        if (isClean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                val size = adapter.mDataList.size
                adapter.mDataList.addAll(list)
                val newSize = adapter.mDataList.size
                adapter.notifyItemRangeInserted(size, newSize - size)
            }
        }
    }

    fun destory() {
        cancel()
    }
}