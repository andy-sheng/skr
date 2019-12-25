package com.component.person.guard

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.R
import com.kingja.loadsir.core.LoadService
import com.module.RouterConstants
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

// 守护列表
class GuardListFragment : BaseFragment() {

    lateinit var titlebar: CommonTitleBar
    lateinit var refreshLayout: SmartRefreshLayout
    lateinit var contentRv: RecyclerView

    lateinit var mLoadService: LoadService<*>
    lateinit var adapter: GuardListAdapter

    private val userServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
    private var offset = 0
    private var hasMore = true
    private val limit = 15

    private var userID = 0

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            userID = (data as Int?) ?: 0
        }
    }

    override fun initView(): Int {
        return R.layout.guard_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (userID == 0) {
            finish()
            return
        }

        titlebar = rootView.findViewById(R.id.titlebar)
        refreshLayout = rootView.findViewById(R.id.refreshLayout)
        contentRv = rootView.findViewById(R.id.content_rv)

        if (userID == MyUserInfoManager.uid.toInt()) {
            titlebar.centerTextView.text = "我的守护"
            titlebar.rightTextView.visibility = View.GONE
        } else {
            titlebar.centerTextView.text = "Ta的守护"
            titlebar.rightTextView.text = "开通守护"
            titlebar.rightTextView.visibility = View.VISIBLE
        }

        titlebar.rightTextView.setDebounceViewClickListener {

        }

        refreshLayout.apply {
            setEnableRefresh(false)
            setEnableLoadMore(hasMore)
            setEnableLoadMoreWhenContentNotFull(true)
            setEnableOverScrollDrag(false)
            setRefreshHeader(ClassicsHeader(context))
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getGuardList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        contentRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = GuardListAdapter(userID == MyUserInfoManager.uid.toInt(), object : GuardListAdapter.Listener {
            override fun onClickAvatar(position: Int, model: GuardInfoModel?) {
                model?.userInfoModel?.userId?.let {
                    val bundle = Bundle()
                    bundle.putInt("bundle_user_id", it)
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation()
                }

            }
        })
        contentRv.adapter = adapter


        getGuardList(0, true)
    }

    fun getGuardList(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe(RequestControl("getGuardList", ControlType.CancelThis)) {
                userServerApi.getGuardList(userID, off, limit)
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("list"), GuardInfoModel::class.java)
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")

                addGuardList(list, isClean)
            } else {

            }
            refreshLayout.finishRefresh()
            refreshLayout.finishLoadMore()
            refreshLayout.setEnableLoadMore(hasMore)
        }
    }

    private fun addGuardList(list: List<GuardInfoModel>?, isClean: Boolean) {
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
                var oldSize = adapter.mDataList.size
                adapter.notifyItemRangeInserted(size, oldSize - size)
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}