package com.module.home.ranked.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.home.R
import com.module.home.ranked.RankedServerApi
import com.module.home.ranked.adapter.RankedHomeAdapter
import com.module.home.ranked.model.RankHomeCardModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener

class RankedHomeFragment : BaseFragment() {

    var mTitlebar: CommonTitleBar? = null
    var mRefreshLayout: SmartRefreshLayout? = null
    var mRecyclerView: RecyclerView? = null

    var mAdapter: RankedHomeAdapter? = null

    override fun initView(): Int {
        return R.layout.ranked_home_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        mTitlebar = rootView.findViewById(R.id.titlebar)
        mRefreshLayout = rootView.findViewById(R.id.refreshLayout)
        mRecyclerView = rootView.findViewById(R.id.recycler_view)

        mRefreshLayout?.setEnableRefresh(false)
        mRefreshLayout?.setEnableLoadMore(false)
        mRefreshLayout?.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout?.setEnableOverScrollDrag(true)

        mRefreshLayout?.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        mRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        mAdapter = RankedHomeAdapter(RecyclerOnItemClickListener { view, position, model ->
            U.getFragmentUtils().addFragment(
                    FragmentUtils.newAddParamsBuilder(activity, RankedDetailFragment::class.java)
                            .addDataBeforeAdd(0, model)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .build())
        })
        mRecyclerView?.adapter = mAdapter

        // TODO: 2019-06-20 为什么把父布局变成
        mTitlebar?.leftTextView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (activity != null) {
                    activity!!.finish()
                }
            }
        })

        initRankCard()
    }

    fun initRankCard() {
        val rankedServerApi = ApiManager.getInstance().createService(RankedServerApi::class.java)
        ApiMethods.subscribe(rankedServerApi.homeRankCards(), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val rankHomeCardModels = JSON.parseArray(result.data!!.getString("cards"), RankHomeCardModel::class.java)

                    // 展示视图
                    mAdapter?.dataList = rankHomeCardModels
                } else {
                    MyLog.w(TAG, "initRankCard result=$result")
                }
            }
        }, this)

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
