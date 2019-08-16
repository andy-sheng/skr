package com.module.feeds.songmanage.activity

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
import com.module.feeds.make.FROM_CHANGE_SING
import com.module.feeds.make.FROM_QUICK_SING
import com.module.feeds.make.make.openFeedsMakeActivityFromChangeSong
import com.module.feeds.make.make.openFeedsMakeActivityFromQuickSong
import com.module.feeds.songmanage.FeedSongManageServerApi
import com.module.feeds.songmanage.adapter.FeedSongManageAdapter
import com.module.feeds.songmanage.adapter.FeedSongManageListener
import com.module.feeds.songmanage.model.FeedSongInfoModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

/**
 * 歌曲搜索页面
 */
@Route(path = RouterConstants.ACTIVITY_FEEDS_SONG_SEARCH)
class FeedSongSearchActivity : BaseActivity() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mCancleTv: TextView
    lateinit var mSearchContent: NoLeakEditText
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView

    lateinit var mAdapter: FeedSongManageAdapter
    lateinit var mPublishSubject: PublishSubject<SearchModel>

    private var isAutoSearch = false       // 标记是否是自动搜索
    private var lastSearchContent = ""     // 记录最近搜索内容
    private var mLoadService: LoadService<*>? = null
    private var from = FROM_QUICK_SING
    private val feedSongManageServerApi = ApiManager.getInstance().createService(FeedSongManageServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feed_song_search_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        from = intent.getIntExtra("from",FROM_QUICK_SING)
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

        mAdapter = FeedSongManageAdapter(object : FeedSongManageListener {
            override fun onClickSing(position: Int, model: FeedSongInfoModel?) {
                model?.let {
                    if(from== FROM_QUICK_SING){
                        openFeedsMakeActivityFromQuickSong(it.song)
                    }else if (from== FROM_CHANGE_SING){
                        openFeedsMakeActivityFromChangeSong(it.song)
                    }
                }
            }
        })
        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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
                .addCallback(EmptyCallback(R.drawable.feed_search_empty_icon, "暂无相对应的歌曲", "#802F2F30"))
                .build()
        mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
            mPublishSubject.onNext(SearchModel(lastSearchContent, false))
        })

        mSearchContent.requestFocus()
        U.getKeyBoardUtils().showSoftInputKeyBoard(this)
    }

    private fun initPublishSubject() {
        mPublishSubject = PublishSubject.create()
        ApiMethods.subscribe(mPublishSubject.debounce(200, TimeUnit.MILLISECONDS)
                .filter { s -> s.searchContent.isNotEmpty() }
                .switchMap { key ->
                    isAutoSearch = key.isAutoSearch
                    lastSearchContent = key.searchContent
                    feedSongManageServerApi.searchFeedSong(key.searchContent)
                }, object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    val list = JSON.parseArray(obj.data.getString("songs"), FeedSongInfoModel::class.java)
                    mAdapter.mDataList.clear()
                    if (!list.isNullOrEmpty()) {
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

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
