package com.module.club.manage.list

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.ClubInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_LIST_CLUB)
class ClubListActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView

    var adapter: ClubListAdapter? = null

    var from: String = "create"

    private var clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var offset = 0
    private val cnt = 15
    private var hasMore = true

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_list_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        contentRv = findViewById(R.id.content_rv)

        adapter = ClubListAdapter(object : ClubListAdapter.Listener {
            override fun onClickItem(position: Int, model: ClubInfo?) {
                model?.let {
                    if (MyUserInfoManager.myUserInfo?.clubInfo?.club?.clubID == it.clubID) {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_HOMEPAGE_CLUB)
                                .withInt("clubID", it.clubID)
                                .navigation()
                    } else {
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_OTHER_HOMEPAGE_CLUB)
                                .withInt("clubID", it.clubID)
                                .navigation()
                    }
                }
            }
        })

        contentRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        contentRv.adapter = adapter

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        titlebar.rightTextView.setDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_CREATE_CLUB)
                    .withString("from", "create")
                    .navigation()
        }

        refreshLayout.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    loadClubListData(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }

            })
        }

        loadClubListData(0, true)
    }

    private fun loadClubListData(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("loadClubListData", ControlType.CancelThis)) {
                clubServerApi.getInnerRecommendClubList(0, cnt)
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("items"), ClubInfo::class.java)
                addClubList(list, isClean)
            }
            finishLoadMoreOrRefresh()
        }
    }

    private fun addClubList(list: List<ClubInfo>?, isClean: Boolean) {
        if (isClean) {
            adapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
            }
            adapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                val size = adapter?.mDataList?.size ?: 0
                adapter?.mDataList?.addAll(list)
                val newSize = adapter?.mDataList?.size ?: 0
                adapter?.notifyItemRangeInserted(size, newSize - size)
            }
        }
    }

    private fun finishLoadMoreOrRefresh() {
        refreshLayout.finishRefresh()
        refreshLayout.finishLoadMore()
        refreshLayout.setEnableLoadMore(hasMore)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}