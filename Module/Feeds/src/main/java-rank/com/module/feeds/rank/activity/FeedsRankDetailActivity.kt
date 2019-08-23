package com.module.feeds.rank.activity

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.ActivityUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.detail.manager.AbsPlayModeManager
import com.module.feeds.detail.manager.FeedSongPlayModeManager
import com.module.feeds.detail.manager.add2SongPlayModeManager
import com.module.feeds.event.FeedDetailChangeEvent
import com.module.feeds.event.FeedDetailSwitchEvent
import com.module.feeds.make.make.openFeedsMakeActivityFromChallenge
import com.module.feeds.rank.FeedsRankServerApi
import com.module.feeds.rank.adapter.FeedRankDetailAdapter
import com.module.feeds.statistics.FeedPage
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.model.FeedSongModel
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
class FeedsRankDetailActivity : BaseActivity() {
    val MOUTH_RANK = 0
    val YEAR_RANK = 1

    var mRankType = YEAR_RANK

    lateinit var mTitlebar: CommonTitleBar
    lateinit var mRefreshLayout: SmartRefreshLayout
    lateinit var mRecyclerView: RecyclerView
    lateinit var mHitIv: ImageView
    lateinit var mSelectTv: ExTextView
    internal var mQuickMsgPopWindow: PopupWindow? = null  //快捷词弹出面板

    private val mAdapter: FeedRankDetailAdapter = FeedRankDetailAdapter()

    var title = ""
    var challengeID = 0L
    var challengeCnt = 0L   //挑战人数

    var offset = 0
    val mCNT = 30
    var hasMore = true

    private val mFeedRankServerApi: FeedsRankServerApi = ApiManager.getInstance().createService(FeedsRankServerApi::class.java)

    var isDetailPlaying = false  // 详情页面是否在播放

    var mSongPlayModeManager: FeedSongPlayModeManager? = null

    private val playerTag = TAG + hashCode()

    private val playCallback = object : PlayerCallbackAdapter() {
        override fun onCompletion() {
            super.onCompletion()
            // 重复播放一次
            mAdapter.mCurrentPlayModel?.let {
                play(it)
            }
        }

        override fun openTimeFlyMonitor(): Boolean {
            return true
        }

        override fun onTimeFlyMonitor(pos: Long, duration: Long) {
            FeedsPlayStatistics.updateCurProgress(pos, duration)
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_rank_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        title = intent.getStringExtra("rankTitle")
        challengeID = intent.getLongExtra("challengeID", 0L)
        challengeCnt = intent.getLongExtra("challengeCnt", 0L)

        mTitlebar = findViewById(R.id.titlebar)
        mSelectTv = findViewById(R.id.select_tv)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mRecyclerView = findViewById(R.id.recycler_view)
        mHitIv = findViewById(R.id.hit_iv)

        mTitlebar.centerTextView.text = title
//        mTitlebar.rightTextView.text = "${StringFromatUtils.formatTenThousand(challengeCnt.toInt())}人参与"
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

        mSelectTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                showRankWindow()
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
                pause()
                openFeedsMakeActivityFromChallenge(challengeID)
            }
        })

        mAdapter.onClickPlayListener = { model, _ ->
            model?.let {
                if (mAdapter.mCurrentPlayModel == it) {
                    // 暂停播放
                    pause()
                } else {
                    // 开始播放
                    play(it)
                }
            }
        }
        mAdapter.onClickItemListener = { model, _ ->
            model?.let {
                //                pause()
                mSongPlayModeManager?.setCurrentPlayModel(it.song)
                FeedsDetailActivity.openActivity(FeedPage.DETAIL_FROM_SONG_ALBUM_CHANLLENGE, this@FeedsRankDetailActivity, it.feedID, FeedsDetailActivity.TYPE_SWITCH,
                        FeedSongPlayModeManager.PlayMode.ORDER, object : AbsPlayModeManager() {
                    override fun getNextSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                        mSongPlayModeManager?.getNextSong(userAction) { sm ->
                            if (mSongPlayModeManager?.getCurPostionInOrigin() in 0 until mAdapter.mDataList.size) {
                                mAdapter.mCurrentPlayModel = mAdapter.mDataList[mSongPlayModeManager?.getCurPostionInOrigin()!!]
                            }
                            callback.invoke(sm)
                        }
                    }

                    override fun playState(isPlaying: Boolean) {
                        isDetailPlaying = isPlaying
                    }

                    override fun getPreSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                        mSongPlayModeManager?.getPreSong(userAction) { sm ->
                            if (mSongPlayModeManager?.getCurPostionInOrigin() in 0 until mAdapter.mDataList.size) {
                                mAdapter.mCurrentPlayModel = mAdapter.mDataList[mSongPlayModeManager?.getCurPostionInOrigin()!!]
                            }
                            callback.invoke(sm)
                        }
                    }

                    override fun changeMode(mode: FeedSongPlayModeManager.PlayMode) {
                        mSongPlayModeManager?.changeMode(mode)
                    }

                    override fun getCurMode(): FeedSongPlayModeManager.PlayMode {
                        return mSongPlayModeManager?.getCurMode()
                                ?: FeedSongPlayModeManager.PlayMode.ORDER
                    }
                })
            }
        }
        initLoadData()

        mSongPlayModeManager = FeedSongPlayModeManager(FeedSongPlayModeManager.PlayMode.ORDER, null, null)
        mSongPlayModeManager?.supportCycle = false
        mSongPlayModeManager?.loadMoreCallback = { size, callback ->
            if (hasMore) {
                loadData(offset, false) {
                    callback.invoke()
                }
            } else {
                callback.invoke()
            }
        }
        SinglePlayer.reset(playerTag)
        SinglePlayer.addCallback(playerTag, playCallback)
    }

    private fun showRankWindow() {
        if (mQuickMsgPopWindow == null) {
            val mLayoutInflater = LayoutInflater.from(this)
            val view = mLayoutInflater.inflate(R.layout.feeds_rank_select_layout, null)
            mQuickMsgPopWindow = PopupWindow(view, U.getDisplayUtils().dip2px(80f), U.getDisplayUtils().dip2px(84f))
            mQuickMsgPopWindow?.setFocusable(false)
            view.findViewById<View>(R.id.month_tv).setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    if (mRankType == YEAR_RANK) {
                        mRankType = MOUTH_RANK
                        mSelectTv?.text = "月榜"
                        offset = 0
                        loadData(offset, true)
                    }
                    mQuickMsgPopWindow?.dismiss()
                }
            })

            view.findViewById<View>(R.id.year_tv).setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    if (mRankType == MOUTH_RANK) {
                        mRankType = YEAR_RANK
                        mSelectTv?.text = "年榜"
                        offset = 0
                        loadData(offset, true)
                    }
                    mQuickMsgPopWindow?.dismiss()
                }
            })

            // 去除动画
//            mQuickMsgPopWindow?.setAnimationStyle(R.style.MyPopupWindow_anim_style)
            mQuickMsgPopWindow?.setOutsideTouchable(true)
            mQuickMsgPopWindow?.setFocusable(true)
            mQuickMsgPopWindow?.setOnDismissListener {
                val drawable = U.getDrawable(R.drawable.bangdan_top)
                drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                mSelectTv.setCompoundDrawables(null, null, drawable, null)
            }
        }

        mQuickMsgPopWindow?.getContentView()?.findViewById<View>(R.id.month_tv).apply {
            this?.isSelected = (mRankType == MOUTH_RANK)
        }

        mQuickMsgPopWindow?.getContentView()?.findViewById<View>(R.id.year_tv).apply {
            this?.isSelected = (mRankType == YEAR_RANK)
        }

        if (!(mQuickMsgPopWindow?.isShowing() ?: false)) {
            val drawable = U.getDrawable(R.drawable.bangdan_down)
            drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            mSelectTv.setCompoundDrawables(null, null, drawable, null)
            val l = IntArray(2)
            mSelectTv.getLocationInWindow(l)
            mQuickMsgPopWindow?.showAtLocation(mSelectTv, Gravity.START or Gravity.TOP, U.getDisplayUtils().phoneWidth - mSelectTv.measuredWidth - U.getDisplayUtils().dip2px(45f), l[1] + mSelectTv.measuredHeight + U.getDisplayUtils().dip2px(15f))
        } else {
            mQuickMsgPopWindow?.dismiss()
        }
    }

    private fun play(model: FeedsWatchModel) {
        mAdapter.mCurrentPlayModel = model
        mAdapter.notifyDataSetChanged()
        mSongPlayModeManager?.setCurrentPlayModel(model.song)
        model.song?.playURL?.let {
            FeedsPlayStatistics.setCurPlayMode(model?.feedID, FeedPage.SONG_ALBUM_CHANLLENGE, 0)
            SinglePlayer.startPlay(playerTag, it)
        }
    }

    private fun pause() {
        mAdapter.mCurrentPlayModel = null
        mAdapter.notifyDataSetChanged()
        SinglePlayer.pause(playerTag)
    }


    private fun initLoadData() {
        loadData(0, true)
    }

    private fun loadMoreData() {
        loadData(offset, false)
    }

    private fun loadData(off: Int, isClean: Boolean, dataOkListener: (() -> Unit)? = null) {
        launch {
            val result = subscribe { mFeedRankServerApi.getFeedRankDetailList(off, mCNT, MyUserInfoManager.getInstance().uid.toInt(), challengeID, if (mRankType == MOUTH_RANK) 1 else 2) }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("rankInfos"), FeedsWatchModel::class.java)
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")
                showDetailInfo(list, isClean)
            } else {
                mRefreshLayout.finishLoadMore()
                mRefreshLayout.finishRefresh()
            }
            dataOkListener?.invoke()
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
            var feedList: ArrayList<FeedSongModel> = ArrayList()
            list.forEach {
                feedList.add(it.song!!)
            }
            mSongPlayModeManager?.setOriginList(feedList, false)
        }
        mAdapter.notifyDataSetChanged()
        add2SongPlayModeManager(mSongPlayModeManager, list, isClean)
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        title = intent?.getStringExtra("rankTitle") ?: ""
        challengeID = intent?.getLongExtra("challengeID", 0L) ?: 0
        mTitlebar.centerTextView.text = title
        initLoadData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
        if (!event.foreground) {
            pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isDetailPlaying) {
            mAdapter.mCurrentPlayModel?.let {
                play(it)
            }
        } else {
            pause()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedDetailChangeEvent) {
        event.model?.let {
            updateDetailModel(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedDetailSwitchEvent) {
        // 数据要更新了
        event.model?.let {
            updateDetailModel(it)
        }
    }

    private fun updateDetailModel(it: FeedsWatchModel) {
        for (model in mAdapter.mDataList) {
            if (it.feedID == model.feedID && it.song?.songID == model.song?.songID) {
                // 更新数据
                model.isLiked = it.isLiked
                model.starCnt = it.starCnt
                model.shareCnt = it.shareCnt
                model.exposure = it.exposure
                model.challengeCnt = it.challengeCnt
                model.isCollected = it.isCollected
            }
        }
    }

    override fun destroy() {
        super.destroy()
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
    }
}
