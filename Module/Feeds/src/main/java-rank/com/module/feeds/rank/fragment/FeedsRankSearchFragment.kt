package com.module.feeds.rank.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.base.BaseFragment
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.utils.UserInfoDataUtils
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.NoLeakEditText
import com.module.feeds.R
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.adapter.FeedsRankAdapter
import com.module.feeds.rank.model.FeedRankInfoModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

class FeedsRankSearchFragment : BaseFragment() {
    lateinit var mCancleTv: TextView
    lateinit var mSearchContent: NoLeakEditText
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView

    lateinit var mAdapter: FeedsRankAdapter
    lateinit var mPublishSubject: PublishSubject<String>

    override fun initView(): Int {
        return R.layout.feeds_rank_search_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mCancleTv = rootView.findViewById(R.id.cancle_tv)
        mSearchContent = rootView.findViewById(R.id.search_content)
        mRefreshLayout = rootView.findViewById(R.id.refreshLayout)
        mRecyclerView = rootView.findViewById(R.id.recycler_view)

        mRefreshLayout.setEnableLoadMore(false)
        mRefreshLayout.setEnableRefresh(false)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(true)
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
            }
        })

        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mAdapter = FeedsRankAdapter(object : FeedsRankAdapter.Listener {
            override fun onClickHit(position: Int, model: FeedRankInfoModel?) {
            }

            override fun onClickItem(position: Int, model: FeedRankInfoModel?) {
            }
        })
        mRecyclerView.adapter = mAdapter

        initPublishSubject()
        mSearchContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                mPublishSubject.onNext(editable.toString())
            }
        })

        mSearchContent.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = mSearchContent.text.toString().trim { it <= ' ' }
                if (TextUtils.isEmpty(keyword)) {
                    U.getToastUtil().showShort("搜索内容为空")
                    return@OnEditorActionListener false
                }
                if (mPublishSubject != null) {
                    mPublishSubject.onNext(keyword)
                }
                U.getKeyBoardUtils().hideSoftInput(mSearchContent)
            }
            false
        })

        mCancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                U.getFragmentUtils().popFragment(this@FeedsRankSearchFragment)
            }
        })

        mSearchContent.postDelayed({
            mSearchContent.requestFocus()
            U.getKeyBoardUtils().showSoftInputKeyBoard(activity)
        }, 200)
    }

    private fun initPublishSubject() {
        mPublishSubject = PublishSubject.create()
        ApiMethods.subscribe(mPublishSubject.debounce(200, TimeUnit.MILLISECONDS)
                .filter { s -> s.isNotEmpty() }
                .switchMap { key ->
                    val feedRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)
                    feedRankServerApi.searchChallenge(key)
                }, object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                val list = JSON.parseArray(obj.data.getString("challengeInfos"), FeedRankInfoModel::class.java)
                mAdapter.mDataList.clear()
                if (list != null && list.isNotEmpty()) {
                    mAdapter.mDataList.addAll(list)
                    mAdapter.notifyDataSetChanged()
                }
            }
        }, this)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}