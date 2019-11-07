package com.module.msg.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.msg.follow.SPFollowAdapter
import com.module.msg.follow.SPFollowRecordModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.rong.imkit.R
import kotlinx.coroutines.launch

//特别关注页面
@Route(path = RouterConstants.ACTIVITY_SPECIAL_FOLLOW)
class SpecialFollowActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView

    val adapter: SPFollowAdapter = SPFollowAdapter()

    var offset: Int = 0
    var hasMore = true
    val mCnt = 20

    val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.special_follow_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        refreshLayout = findViewById(R.id.refreshLayout)
        contentRv = findViewById(R.id.content_rv)

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        refreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(false)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getSPFollowRecordList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        contentRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        contentRv.adapter = adapter

        adapter.onClickItemListener = { model, _ ->
            model?.let {
                //todo 跳转
            }
        }

        getSPFollowRecordList(0, true)
    }

    private fun getSPFollowRecordList(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getSPFollowRecordList", ControlType.CancelThis)) {
                userInfoServerApi.getSPFollowRecordList(MyUserInfoManager.uid.toInt(), off, mCnt)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("details"), SPFollowRecordModel::class.java)
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                addRecordList(list, isClean)
            } else {
                refreshLayout.finishLoadMore()
                refreshLayout.finishRefresh()
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun addRecordList(list: List<SPFollowRecordModel>?, clean: Boolean) {
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

    override fun useEventBus(): Boolean {
        return false
    }
}