package com.module.feeds.rank.activity

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.ActivityUtils
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.make.openFeedsMakeActivity
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.adapter.FeedDetailAdapter
import com.module.feeds.watch.model.FeedsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 具体神曲的榜单
 */
@Route(path = RouterConstants.ACTIVITY_FEEDS_RANK_DETAIL)
class FeedsDetailRankActivity : BaseActivity() {

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView
    lateinit var mHitIv: ImageView

    private val mAdapter: FeedDetailAdapter = FeedDetailAdapter()

    var title = ""
    var challengeID = 0L

    var offset = 0
    val mCNT = 30
    var hasMore = true

    private val mFeedRankServerApi: FeedsRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)

    private val playerTag = TAG + hashCode()

    private val playCallback = object : PlayerCallbackAdapter() {
        override fun onCompletion() {
            super.onCompletion()
            // 重复播放一次
            mAdapter.mCurrentPlayModel?.let {
                play(it)
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_rank_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        title = intent.getStringExtra("rankTitle")
        challengeID = intent.getLongExtra("challengeID", 0L)

        mTitlebar = findViewById(R.id.titlebar)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.recycler_view)
        mHitIv = findViewById(R.id.hit_iv)

        mTitlebar.centerTextView.text = title
        mRecyclerView.layoutManager = GridLayoutManager(this, 3)
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        mRefreshLayout.setEnableRefresh(true)
        mRefreshLayout.setEnableLoadMore(true)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(true)

        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
                loadMoreData()
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                initLoadData()
            }
        })

        mTitlebar.leftTextView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        mHitIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 打榜去
                stop()
                openFeedsMakeActivity(challengeID)
            }
        })

        mAdapter.onClickPlayListener = { model, _ ->
            model?.let {
                if (mAdapter.mCurrentPlayModel == it) {
                    // 暂停播放
                    stop()
                } else {
                    // 开始播放
                    play(it)
                }
            }
        }
        mAdapter.onClickItemListener = { model, _ ->
            stop()
            ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                    .withSerializable("feed_model", model)
                    .navigation()
        }
        initLoadData()
        SinglePlayer.addCallback(playerTag, playCallback)
    }

    private fun play(model: FeedsWatchModel) {
        mAdapter.mCurrentPlayModel = model
        mAdapter.notifyDataSetChanged()
        model.song?.playURL?.let {
            SinglePlayer.startPlay(playerTag, it)
        }
    }

    private fun stop() {
        mAdapter.mCurrentPlayModel = null
        mAdapter.notifyDataSetChanged()
        SinglePlayer.stop(playerTag)
    }


    private fun initLoadData() {
        loadData(0, true)
    }

    private fun loadMoreData() {
        loadData(offset, false)
    }

    private fun loadData(off: Int, isClean: Boolean) {
        launch {
            val result = subscribe { mFeedRankServerApi.getFeedRankDetailList(off, mCNT, MyUserInfoManager.getInstance().uid.toInt(), challengeID) }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("rankInfos"), FeedsWatchModel::class.java)
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                showDetailInfo(list, isClean)
            } else {
                mRefreshLayout.finishLoadMore()
                mRefreshLayout.finishRefresh()
            }
        }
    }

    private fun showDetailInfo(list: List<FeedsWatchModel>?, isClean: Boolean) {
        mRefreshLayout.finishLoadMore()
        mRefreshLayout.finishRefresh()
        mRefreshLayout.setEnableLoadMore(hasMore)

        if (isClean) {
            mAdapter.mDataList.clear()
            mRefreshLayout.setEnableLoadMore(true)
        }

        if (list.isNullOrEmpty()) {
            // 后面都没有数据了
            mRefreshLayout.setEnableLoadMore(false)
        }

        list?.let {
            mAdapter.mDataList.addAll(it)
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun useEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
        if (!event.foreground) {
            stop()
        }
    }

    override fun destroy() {
        super.destroy()
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
    }
}
