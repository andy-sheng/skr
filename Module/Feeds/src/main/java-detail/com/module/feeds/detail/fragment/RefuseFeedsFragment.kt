package com.module.feeds.detail.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.FeedsDetailServerApi
import com.module.feeds.detail.adapter.FeedClickListener
import com.module.feeds.detail.adapter.FeedRefuseCommentAdapter
import com.module.feeds.detail.model.RefuseCommentModel
import com.module.feeds.statistics.FeedPage
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class RefuseFeedsFragment : BaseFragment() {
    val mTag = "RefuseFeedsFragment"
    val mFeedsDetailServerApi = ApiManager.getInstance().createService(FeedsDetailServerApi::class.java)
    internal var mRefreshLayout: SmartRefreshLayout? = null
    internal var mContentRv: RecyclerView? = null

    internal var mLoadService: LoadService<*>? = null

    var mAdapter: FeedRefuseCommentAdapter? = null

    var feedID: Int? = null

    var mOffset: Int = 0
    var mHasMore: Boolean = true
    val mCount = 30

    var array: ArrayList<RefuseCommentModel> = ArrayList()

    override fun initView(): Int {
        return R.layout.refuse_feeds_fragment_layout
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

        mAdapter = FeedRefuseCommentAdapter(object : FeedClickListener {
            override fun onClickItme(model: RefuseCommentModel?) {
                model?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                            .withInt("feed_ID", it.feedID)
                            .withSerializable("from", FeedPage.DETAIL_FROM_HOMEPAGE)
                            .navigation()
                }
            }
        })
        mContentRv?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mContentRv?.adapter = mAdapter

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.feed_empty_icon, "暂无评论哦", "#FF3B4E79"))
                .build()
        mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
            getList()
        })

        getList()
    }

    private fun getList() {
        launch(Dispatchers.Main) {
            val result = subscribe(RequestControl(mTag + "getList", ControlType.CancelThis)) {
                mFeedsDetailServerApi.getRefuseCommentList(MyUserInfoManager.getInstance().uid.toInt(), mOffset, mCount)
            }

            mRefreshLayout?.finishLoadMore()
            if (result.errno == 0) {
                var list = JSON.parseArray(result.data.getString("details"), RefuseCommentModel::class.java)
                if (!list.isNullOrEmpty()) {
                    array.addAll(list)
                    mAdapter?.dataList = array
                    mAdapter?.notifyDataSetChanged()
                }

                mHasMore = result.data.getBoolean("hasMore")
                if (!mHasMore) {
                    mRefreshLayout?.setEnableLoadMore(false)
                }

                mOffset = result.data.getInteger("offset")
            }

            if (array.size > 0) mLoadService?.showSuccess() else mLoadService?.showCallback(EmptyCallback::class.java)
        }
    }

    override fun onFragmentVisible() {
        super.onFragmentVisible()
    }

    override fun isInViewPager(): Boolean = true

    override fun useEventBus(): Boolean {
        return false
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            feedID = data as Int
        }
    }

    override fun onBackPressed(): Boolean {
        finish()
        return true
    }
}
