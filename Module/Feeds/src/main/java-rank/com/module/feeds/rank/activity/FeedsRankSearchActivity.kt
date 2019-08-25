package com.module.feeds.rank.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.callback.EmptyCallback
import com.component.busilib.model.SearchModel
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.make.openFeedsMakeActivityFromChallenge
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.adapter.FeedRankAdapter
import com.module.feeds.rank.model.FeedRankInfoModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

@Route(path = RouterConstants.ACTIVITY_FEEDS_RANK_SEARCH)
class FeedsRankSearchActivity : BaseActivity() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mCancleTv: TextView
    lateinit var mSearchContent: NoLeakEditText
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView

    lateinit var mAdapter: FeedRankAdapter
    lateinit var mPublishSubject: PublishSubject<SearchModel>

    private var mLoadService: LoadService<*>? = null
    private var isAutoSearch = false       // 标记是否是自动搜索
    private var lastSearchContent = ""     // 记录最近搜索内容

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_rank_search_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = findViewById(R.id.titlebar)
        mCancleTv = findViewById(R.id.cancle_tv)
        mSearchContent = findViewById(R.id.search_content)
        mRefreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.recycler_view)

        if (U.getDeviceUtils().hasNotch(this)) {
            mTitlebar.visibility = View.VISIBLE
        } else {
            mTitlebar.visibility = View.GONE
        }

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

        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = FeedRankAdapter(object : FeedRankAdapter.Listener {
            override fun onClickHit(position: Int, model: FeedRankInfoModel?) {
                // 直接去打榜
                model?.let {
                    openFeedsMakeActivityFromChallenge(it.challengeID)
                }
            }

            override fun onClickItem(position: Int, model: FeedRankInfoModel?) {
                model?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK_DETAIL)
                            .withString("rankTitle", it.rankTitle)
                            .withLong("challengeID", it.challengeID ?: 0L)
                            .withLong("challengeCnt", it.userCnt?.toLong() ?: 0L)
                            .navigation()
                }
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
                mPublishSubject.onNext(SearchModel(editable.toString(), true))
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
                    mPublishSubject.onNext(SearchModel(keyword, false))
                }
                U.getKeyBoardUtils().hideSoftInput(mSearchContent)
            }
            false
        })

        mCancleTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.feed_search_empty_icon, "没搜到任何结果", "#802F2F30"))
                .build()
        mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
            mPublishSubject.onNext(SearchModel(lastSearchContent, true))
        })

        mSearchContent.requestFocus()
        U.getKeyBoardUtils().showSoftInputKeyBoard(this)
    }

    private fun initPublishSubject() {
        mPublishSubject = PublishSubject.create()
        ApiMethods.subscribe(mPublishSubject.debounce(200, TimeUnit.MILLISECONDS)
                .filter { s -> s.searchContent.isNotEmpty() }
                .switchMap { key ->
                    val feedRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)
                    isAutoSearch = key.isAutoSearch
                    lastSearchContent = key.searchContent
                    feedRankServerApi.searchChallenge(key.searchContent)
                }, object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    val list = JSON.parseArray(obj.data.getString("challengeInfos"), FeedRankInfoModel::class.java)
                    mAdapter.mDataList.clear()
                    if (list != null && list.isNotEmpty()) {
                        mAdapter.mDataList.addAll(list)
                        mAdapter.notifyDataSetChanged()
                    }
                    if (!isAutoSearch && mAdapter.mDataList.isEmpty()) {
                        mLoadService?.showCallback(EmptyCallback::class.java)
                    }
                    if (mAdapter.mDataList.isNotEmpty()) {
                        mLoadService?.showSuccess()
                    }
                } else {
                    if (obj.errno == -2) {
                        U.getToastUtil().showShort("网络异常，请检查网络后重试")
                    }
                }
            }
        }, this)
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun destroy() {
        super.destroy()
    }
}