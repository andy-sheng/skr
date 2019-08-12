package com.module.feeds.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.kingja.loadsir.core.LoadService
import com.module.feeds.R
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.adapter.LikeWorkAdapter
import com.module.feeds.detail.model.FeedLikeModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LikeWorksFragment : BaseFragment() {
    val mTag = "LikeWorksFragment"
    val mFeedsDetailServerApi = ApiManager.getInstance().createService(FeedsDetailServerApi::class.java)
    internal var mRefreshLayout: SmartRefreshLayout? = null
    internal var mContentRv: RecyclerView? = null

    internal var mLoadService: LoadService<*>? = null

    var mAdapter: LikeWorkAdapter? = null

    var mOffset: Int = 0
    var mHasMore: Boolean = true
    val mCount = 30

    var array: ArrayList<FeedLikeModel> = ArrayList()

    override fun initView(): Int {
        return R.layout.like_work_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mRefreshLayout = rootView.findViewById<View>(R.id.refreshLayout) as SmartRefreshLayout
        mContentRv = rootView.findViewById<View>(R.id.content_rv) as RecyclerView

        mRefreshLayout?.setEnableRefresh(false)
        mRefreshLayout?.setEnableLoadMore(true)
        mRefreshLayout?.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout?.setEnableOverScrollDrag(false)
        mRefreshLayout?.setRefreshHeader(ClassicsHeader(context!!))
        mRefreshLayout?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                getList()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        mContentRv?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mAdapter = LikeWorkAdapter()
        mContentRv?.adapter = mAdapter

//        val mLoadSir = LoadSir.Builder()
//                .build()
//        mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
//
//        })

        getList()
    }

    private fun getList() {
        launch(Dispatchers.Main) {
            val result = subscribe(RequestControl(mTag + "getList", ControlType.CancelThis)) {
                mFeedsDetailServerApi.getLikeWorkList(MyUserInfoManager.getInstance().uid.toInt(), mOffset, mCount)
            }

            mRefreshLayout?.finishLoadMore()
            if (result.errno == 0) {
                var list = JSON.parseArray(result.data.getString("details"), FeedLikeModel::class.java)
                if (!list.isNullOrEmpty()) {
                    array.addAll(list)
                    mAdapter?.dataList = array
                }

                mHasMore = result.data.getBoolean("hasMore")
                if (!mHasMore) {
                    mRefreshLayout?.setEnableLoadMore(false)
                }

                mOffset = result.data.getInteger("offset")
            }
        }
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
    }

    override fun isInViewPager(): Boolean = true

    override fun useEventBus(): Boolean {
        return false
    }
}
