package com.module.feeds.watch.watchview

import android.media.MediaPlayer
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AbsListView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.log.MyLog
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.component.busilib.callback.EmptyCallback
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.detail.manager.AbsPlayModeManager
import com.module.feeds.detail.manager.FeedSongPlayModeManager
import com.module.feeds.event.FeedDetailChangeEvent
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.make.make.openFeedsMakeActivityFromChallenge
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.adapter.FeedsWatchViewAdapter
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.viewholder.FeedViewHolder
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

abstract class BaseWatchView(val fragment: BaseFragment, val type: Int) : ConstraintLayout(fragment.context!!), CoroutineScope by MainScope() {
    val TAG = "FeedsWatchView"
    val playerTag = TAG + hashCode()

    val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    private val mRefreshLayout: SmartRefreshLayout
    private val mClassicsHeader: ClassicsHeader
    private val mLayoutManager: LinearLayoutManager
    private val mRecyclerView: RecyclerView

    val mAdapter: FeedsWatchViewAdapter
    val playCallback: PlayerCallbackAdapter

    var hasMore = true // 是否可以加载更多
    var isSeleted = false  // 是否选中
    var mSharePanel: SharePanel? = null

    var mHasInitData = false  //关注和推荐是否初始化过数据

    var mLoadService: LoadService<*>? = null

    companion object {
        const val TYPE_RECOMMEND = 1  // 推荐
        const val TYPE_FOLLOW = 2   // 关注
        const val TYPE_PERSON = 3   // 个人中心
    }

    init {
        View.inflate(context, R.layout.feed_watch_view_layout, this)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mClassicsHeader = findViewById(R.id.classics_header)
        mRecyclerView = findViewById(R.id.recycler_view)

        mAdapter = FeedsWatchViewAdapter(object : FeedsListener {
            override fun onClickRecommendTagMore() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_TAG)
                        .withInt("from", 1)
                        .navigation()
            }

            override fun onClickRecommendTag(model: FeedRecommendTagModel) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_TAG_DETAIL)
                        .withSerializable("model", model)
                        .navigation()
            }

            override fun onClickCollectListener(position: Int, watchModel: FeedsWatchModel?) {
                // 收藏
                watchModel?.let {
                    collectOrUnCollectFeed(position, it)
                }
            }

            override fun onClickShareListener(position: Int, watchModel: FeedsWatchModel?) {
                // 分享
                watchModel?.let {
                    mSharePanel = SharePanel(fragment.activity)
                            .apply {
                                mTitle = it.song?.workName
                                mDes = it.user?.nickname
                                mPlayMusicUrl = it.song?.playURL
                                mUrl = String.format("http://www.skrer.mobi/feed/song?songID=%d&userID=%d",
                                        it.song?.songID, it.user?.userID)
                            }
                    mSharePanel?.show(ShareType.MUSIC)
                    mSharePanel?.setUMShareListener(object : UMShareListener {
                        override fun onResult(p0: SHARE_MEDIA?) {

                        }

                        override fun onCancel(p0: SHARE_MEDIA?) {

                        }

                        override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {

                        }

                        override fun onStart(p0: SHARE_MEDIA?) {
                            addShareCount(it)
                        }
                    })
                }
            }

            override fun onClickAvatarListener(watchModel: FeedsWatchModel?) {
                watchModel?.user?.let {
                    val bundle = Bundle()
                    bundle.putInt("bundle_user_id", it.userID)
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                            .with(bundle)
                            .navigation()
                }
            }

            override fun onclickRankListener(watchModel: FeedsWatchModel?) {
                // 排行榜详情
                watchModel?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK_DETAIL)
                            .withString("rankTitle", it.rank?.rankTitle)
                            .withLong("challengeID", it.song?.challengeID ?: 0L)
                            .withLong("challengeCnt", it.challengeCnt.toLong())
                            .navigation()
                }
            }

            override fun onClickLikeListener(position: Int, watchModel: FeedsWatchModel?) {
                // 喜欢
                watchModel?.let { feedLike(position, it) }
            }

            override fun onClickHitListener(watchModel: FeedsWatchModel?) {
                SinglePlayer.reset(playerTag)
                openFeedsMakeActivityFromChallenge(watchModel?.song?.challengeID)
            }

            override fun onClickDetailListener(position: Int, watchModel: FeedsWatchModel?) {
                // 详情  声音要连贯
                // 这样返回时能 resume 上
                if (watchModel != null && watchModel.status == 2) {
                    startPlay(position, watchModel)
                    fragment.activity?.let { fragmentActivity ->
                        FeedsDetailActivity.openActivity(fragmentActivity, watchModel.feedID, 1, FeedSongPlayModeManager.PlayMode.ORDER, object : AbsPlayModeManager() {
                            override fun getNextSong(userAction: Boolean): FeedSongModel? {
                                return findNextSong(userAction)
                            }

                            override fun getPreSong(userAction: Boolean): FeedSongModel? {
                                return findPreSong(userAction)
                            }
                        })
                    }
                }

            }

            override fun onClickCDListener(position: Int, watchModel: FeedsWatchModel?) {
                // 播放
                watchModel?.let { model -> controlPlay(position, model, false) }
            }

            override fun onClickMoreListener(position: Int, watchModel: FeedsWatchModel?) {
                // 更多
                watchModel?.let {
                    clickMore(position, it)
                }
            }

        }, type)

        mRefreshLayout.apply {
            setEnableRefresh(type != TYPE_PERSON)
            setEnableLoadMore(type != TYPE_PERSON)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(type != TYPE_PERSON)
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getMoreFeeds()
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    initFeedList(true)
                }
            })
        }

        mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        if (type != TYPE_PERSON) {
            addOnScrollListenerToRv()
            val mLoadSir = LoadSir.Builder()
                    .addCallback(EmptyCallback(R.drawable.feed_home_list_empty_icon, "暂无神曲发布", "#802F2F30"))
                    .build()
            mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
                initFeedList(true)
            })
        }

        playCallback = object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                MyLog.w(TAG, "PlayerCallbackAdapter onCompletion")
                // 播放完成
                SinglePlayer.reset(playerTag)
                // 显示分享，收藏和播放的按钮
                mAdapter.mCurrentPlayModel?.let {
                    mAdapter.playComplete()
                }
            }

            override fun onPrepared() {
                super.onPrepared()
                mAdapter.resumeWhenBufferingEnd()
                /**
                 * 预加载
                 */
                onPreparedMusic()
            }

            override fun openTimeFlyMonitor(): Boolean {
                return true
            }

            override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
                MyLog.d(TAG, "onBufferingUpdate percent=$percent")
                if (percent == 100) {
                    if (SinglePlayer.isPlaying) {
                        mAdapter.resumeWhenBufferingEnd()
                    }
                } else {
                    mAdapter.pauseWhenBuffering()
                }
            }

            override fun onTimeFlyMonitor(pos: Long, duration: Long) {
                if (mAdapter.playing) {
                    mAdapter.updatePlayProgress(pos, duration)
                    if (mAdapter.mCurrentPlayPosition >= 0) {
                        try {
                            val holder = mRecyclerView.findViewHolderForAdapterPosition(mAdapter.mCurrentPlayPosition)
                            if (holder is FeedViewHolder?) {
                                mAdapter.mCurrentPlayModel?.let { _ ->
                                    holder?.playLyric()
                                }
                            }
                        } catch (e: Exception) {
                            MyLog.e(e)
                        }
                    }
                    FeedsPlayStatistics.updateCurProgress(pos, duration)
                } else {
                    if (MyLog.isDebugLogOpen()) {
                        U.getToastUtil().showShort("FeedsWatchView有bug,暂停失败了？")
                    }
                    pausePlay()
                }

            }
        }
        SinglePlayer.addCallback(playerTag, playCallback)
    }

    fun controlPlay(pos: Int, model: FeedsWatchModel?, isMustPlay: Boolean) {
        MyLog.d(TAG, "controlPlay isSeleted = $isSeleted")
        if (model != null && model != mAdapter.mCurrentPlayModel) {
            SinglePlayer.reset(playerTag)
        }
        if (isMustPlay) {
            // 播放
            startPlay(pos, model)
        } else {
            if (mAdapter.mCurrentPlayModel == model && mAdapter.playing) {
                // 停止播放
                pausePlay()
            } else {
                // 播放
                startPlay(pos, model)
            }
        }
    }

    private fun startPlay(pos: Int, model: FeedsWatchModel?) {
        MyLog.d(TAG, "startPlayModel isSeleted = $isSeleted pos=$pos mCurrentPlayPosition=${mAdapter.mCurrentPlayPosition}")
        mAdapter.startPlayModel(pos, model)
        // 数据还是要更新，只是不播放，为恢复播放做准备
        if (!isSeleted) {
            mAdapter.pausePlayModel()
            return
        }
        model?.song?.playURL?.let {
            FeedsPlayStatistics.setCurPlayMode(model.feedID)
            SinglePlayer.startPlay(playerTag, it)
        }
    }

    /**
     * 继续播放
     */
    open fun resumePlay() {
        MyLog.d(TAG, "resumePlay isSeleted= $isSeleted")
        if (!isSeleted) {
            mAdapter.pausePlayModel()
            return
        }
        mAdapter.resumePlayModel()
        mAdapter.mCurrentPlayModel?.song?.playURL?.let {
            FeedsPlayStatistics.setCurPlayMode(mAdapter.mCurrentPlayModel?.feedID ?: 0)
            SinglePlayer.startPlay(playerTag, it)
        }
    }

    fun srollPositionToTop(position: Int) {
        // 置顶显示
        if (position >= 0) {
            val firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
            val lastVisibleItem = mLayoutManager.findLastVisibleItemPosition()
            if (position < firstVisibleItem || position > lastVisibleItem) {
                mLayoutManager.scrollToPositionWithOffset(position, 0)
            }
        }
    }

    fun pausePlay() {
        MyLog.d(TAG, "pausePlay")
        mAdapter.pausePlayModel()
        SinglePlayer.pause(playerTag)
    }

    fun autoRefresh() {
        mRefreshLayout.autoRefresh()
    }

    open fun unselected() {
        MyLog.d(TAG, "unselected")
        isSeleted = false
        pausePlay()
    }

    open fun selected() {
        MyLog.d(TAG, "selected")
        isSeleted = true
    }

    fun finishRefreshOrLoadMore() {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
        mRefreshLayout.setEnableLoadMore(hasMore)
    }

    // 列表空闲选中的逻辑
    abstract fun recyclerIdlePosition(position: Int)

    // 加载数据
    abstract fun initFeedList(flag: Boolean): Boolean

    // 加载更多数据
    abstract fun getMoreFeeds()

    // 点击更多
    abstract fun clickMore(position: Int, it: FeedsWatchModel)

    // 上一首
    abstract fun findPreSong(userAction: Boolean): FeedSongModel?

    // 下一首
    abstract fun findNextSong(userAction: Boolean): FeedSongModel?

    // 预加载
    abstract fun onPreparedMusic()

    open fun destroy() {
        cancel()
        mSharePanel?.dismiss(false)
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun addOnScrollListenerToRv() {
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var maxPercent = 0f
            var isFound = false

            val cdHeight = 168.dp()   // 光盘高度
            val bottomHeight = 50.dp()  // 底部高度

            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    AbsListView.OnScrollListener.SCROLL_STATE_IDLE -> {
                        var postion = -1
                        // 以光盘为界限，找光盘显示百分比最多的
                        val firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
                        val lastVisibleItem = mLayoutManager.findLastVisibleItemPosition()
                        if (firstVisibleItem != RecyclerView.NO_POSITION && lastVisibleItem != RecyclerView.NO_POSITION) {
                            val percents = FloatArray(lastVisibleItem - firstVisibleItem + 1)
                            var i = firstVisibleItem
                            isFound = false
                            maxPercent = 0f
                            while (i <= lastVisibleItem && !isFound) {
                                if (mRecyclerView.findViewHolderForAdapterPosition(i) != null) {
                                    val itemView = mRecyclerView.findViewHolderForAdapterPosition(i).itemView
                                    val location1 = IntArray(2)
                                    val location2 = IntArray(2)
                                    itemView.getLocationOnScreen(location1)
                                    mRecyclerView.getLocationOnScreen(location2)
                                    val top = location1[1] - location2[1]
//                                    MyLog.d(TAG, "onScrollStateChangedrecyclerView i=$i, newState = $newState location1[1]=${location1[1]} location2[1]=${location2[1]}")
                                    when {
                                        top < 0 -> {
                                            // 顶部的
                                            if ((itemView.height + top) >= (cdHeight + bottomHeight)) {
                                                percents[i - firstVisibleItem] = 100f
                                            } else {
                                                percents[i - firstVisibleItem] = (itemView.height + top - bottomHeight).toFloat() / cdHeight.toFloat()
                                            }
                                        }
                                        (top + itemView.height) < mRecyclerView.height -> {
                                            percents[i - firstVisibleItem] = 100f
                                        }
                                        else -> {
                                            // 底部的
                                            if ((mRecyclerView.height - top) >= (itemView.height - bottomHeight)) {
                                                percents[i - firstVisibleItem] = 100f
                                            } else {
                                                percents[i - firstVisibleItem] = (itemView.height - (mRecyclerView.height - top) - bottomHeight).toFloat() / cdHeight.toFloat()
                                            }
                                        }
                                    }
                                    if (percents[i - firstVisibleItem] == 100f) {
                                        isFound = true
                                        maxPercent = 100f
                                        postion = i
                                    } else {
                                        if (percents[i - firstVisibleItem] > maxPercent) {
                                            maxPercent = percents[i - firstVisibleItem]
                                            postion = i
                                        }
                                    }
                                }
                                i++
                            }
                        }
                        if (postion >= 0) {
                            isFound = true
                            recyclerIdlePosition(postion)
                        }
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                    }
                }
            }
        })
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedDetailChangeEvent) {
        // 数据要更新了
        event.model?.let {
            mAdapter.updateModelFromDetail(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedsCollectChangeEvent) {
        mAdapter.mDataList.forEachIndexed { index, feedsWatchModel ->
            if (feedsWatchModel.feedID == event.feedID) {
                feedsWatchModel.isCollected = event.isCollected
                mAdapter.update(index, feedsWatchModel, FeedsWatchViewAdapter.REFRESH_TYPE_COLLECT)
            }
        }
    }

    // 收藏和取消收藏
    private fun collectOrUnCollectFeed(position: Int, model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !model.isCollected

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("collectFeed", ControlType.CancelThis)) { mFeedServerApi.collectFeed(body) }
            if (result.errno == 0) {
                model.isCollected = !model.isCollected
                EventBus.getDefault().post(FeedsCollectChangeEvent(model.feedID, model.isCollected))
                mAdapter.update(position, model, FeedsWatchViewAdapter.REFRESH_TYPE_COLLECT)
                if (model.isCollected) {
                    U.getToastUtil().showShort("收藏成功")
                } else {
                    U.getToastUtil().showShort("取消收藏成功")
                }
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络异常，请检查网络之后重试")
                }
                if (MyLog.isDebugLogOpen()) {
                    U.getToastUtil().showShort("${result?.errmsg}")
                } else {
                    MyLog.e(TAG, "${result?.errmsg}")
                }
            }
        }
    }

    private fun feedLike(position: Int, model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !model.isLiked

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe(RequestControl("feedLike", ControlType.CancelThis)) {
                mFeedServerApi.feedLike(body)
            }
            if (obj.errno == 0) {
                model.isLiked = !model.isLiked
                if (model.isLiked) {
                    model.starCnt = model.starCnt.plus(1)
                } else {
                    model.starCnt = model.starCnt.minus(1)
                }
                mAdapter.update(position, model, FeedsWatchViewAdapter.REFRESH_TYPE_LIKE)
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    fun addShareCount(model: FeedsWatchModel) {
        launch {
            val map = mapOf("feedID" to model.feedID, "userID" to MyUserInfoManager.getInstance().uid.toInt())
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { mFeedServerApi.shareAdd(body) }
            if (result.errno == 0) {
                model.shareCnt = model.shareCnt.plus(1)
            } else {

            }
        }
    }

    fun deleteFeed(position: Int, model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["songID"] = model.song?.songID ?: 0

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val obj = subscribe(RequestControl("deleteFeed", ControlType.CancelThis)) { mFeedServerApi.deleteFeed(body) }
            if (obj.errno == 0) {
                if (mAdapter.mCurrentPlayModel == model) {
                    SinglePlayer.pause(playerTag)
                    mAdapter.mCurrentPlayModel = null
                    mAdapter.mCurrentPlayPosition = -1
                }
                mAdapter.delete(model)
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }
}