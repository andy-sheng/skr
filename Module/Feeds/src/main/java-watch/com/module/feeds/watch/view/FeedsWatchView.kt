package com.module.feeds.watch.view

import android.media.MediaPlayer
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.AbsListView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.utils.U
import com.common.utils.dp
import com.common.videocache.MediaCacheManager
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.component.busilib.callback.EmptyCallback
import com.component.person.view.RequestCallBack
import com.dialog.view.TipsDialogView
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.RouterConstants
import com.module.feeds.IPersonFeedsWall
import com.module.feeds.R
import com.module.feeds.event.FeedWatchChangeEvent
import com.module.feeds.make.openFeedsMakeActivity
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.adapter.FeedsWatchViewAdapter
import com.module.feeds.watch.listener.FeedsListener
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.presenter.FeedWatchViewPresenter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FeedsWatchView(val fragment: BaseFragment, val type: Int) : ConstraintLayout(fragment.context), IFeedsWatchView, IPersonFeedsWall {
    val TAG = "FeedsWatchView"

    constructor(fragment: BaseFragment, type: Int, userInfoModel: UserInfoModel, callBack: RequestCallBack?) : this(fragment, type) {
        this.mUserInfo = userInfoModel
        this.mCallBack = callBack
        mPersenter.mUserInfo = userInfoModel
    }

    companion object {
        const val TYPE_RECOMMEND = 1  // 推荐
        const val TYPE_FOLLOW = 2   // 关注
        const val TYPE_PERSON = 3   // 个人中心
    }

    private val mRefreshLayout: SmartRefreshLayout
    private val mClassicsHeader: ClassicsHeader
    private val mLayoutManager: LinearLayoutManager
    private val mRecyclerView: RecyclerView

    private var mLoadService: LoadService<*>? = null

    private var mAdapter: FeedsWatchViewAdapter? = null
    private val mPersenter: FeedWatchViewPresenter = FeedWatchViewPresenter(this, type)

    var mFeedsMoreDialogView: FeedsMoreDialogView? = null
    val playerTag = TAG + hashCode()

    val playCallback: PlayerCallbackAdapter

    var hasMore = true // 是否可以加载更多

    private var mUserInfo: UserInfoModel? = null
    private var mCallBack: RequestCallBack? = null
    private var mTipsDialogView: TipsDialogView? = null

    fun isHomePage(): Boolean {
        if (type == TYPE_RECOMMEND || type == TYPE_FOLLOW) {
            return true
        }
        return false
    }

    override fun isHasMore(): Boolean {
        return hasMore
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
                            .navigation()
                }
            }

            override fun onClickLikeListener(position: Int, watchModel: FeedsWatchModel?) {
                // 喜欢
                watchModel?.let { mPersenter.feedLike(position, it) }
            }

            override fun onClickCommentListener(watchModel: FeedsWatchModel?) {
                // 必须不为空，且审核通过
                if (watchModel != null && watchModel.status == 2) {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                            .withSerializable("feed_model", watchModel)
                            .navigation()
                }

            }

            override fun onClickHitListener(watchModel: FeedsWatchModel?) {
                SinglePlayer.reset(playerTag)
                openFeedsMakeActivity(watchModel?.song?.challengeID)
            }

            override fun onClickDetailListener(position: Int, watchModel: FeedsWatchModel?) {
                // 详情  声音要连贯
                // 这样返回时能 resume 上
                if (watchModel != null && watchModel.status == 2) {
                    mAdapter?.mCurrentPlayModel = watchModel
                    mAdapter?.mCurrentPlayPosition = position
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_DETAIL)
                            .withSerializable("feed_model", watchModel)
                            .navigation()
                }

            }

            override fun onClickCDListener(position: Int, watchModel: FeedsWatchModel?) {
                // 播放
                watchModel?.let { model -> controlPlay(position, model, false) }
            }

            override fun onClickMoreListener(position: Int, watchModel: FeedsWatchModel?) {
                // 更多
                watchModel?.let {
                    if (fragment.activity != null) {
                        if (isHomePage()) {
                            homePagerMore(it)
                        } else {
                            personPageMore(position, it)
                        }
                    }

                }
            }

        }, isHomePage())

        mRefreshLayout.apply {
            setEnableRefresh(isHomePage())
            setEnableLoadMore(isHomePage())
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(isHomePage())
            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    getMoreFeeds()
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {
                    getFeeds(true)
                }
            })
        }

        mLayoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.layoutManager = mLayoutManager
        mRecyclerView.adapter = mAdapter
        mAdapter?.notifyDataSetChanged()

        if (isHomePage()) {
            addOnScrollListenerToRv()
            val mLoadSir = LoadSir.Builder()
                    .addCallback(EmptyCallback(R.drawable.feed_home_list_empty_icon, "暂无小音乐发布", "#802F2F30"))
                    .build()
            mLoadService = mLoadSir.register(mRefreshLayout, Callback.OnReloadListener {
                getFeeds(true)
            })
        }
        playCallback = object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                mAdapter?.pausePlayModel()
                SinglePlayer.reset(playerTag)
                // 循环播放
                mAdapter?.mCurrentPlayModel?.let {
                    mAdapter?.mCurrentPlayPosition?.let { it1 ->
                        startPlay(it1, it)
                    }
                }
            }

            override fun onPrepared() {
                super.onPrepared()
                mAdapter?.mCurrentPlayPosition?.let {
                    if (it + 1 < mAdapter!!.mDataList.size) {
                        mAdapter?.mDataList?.get(it + 1)?.song?.playURL?.let { it2 ->
                            MediaCacheManager.preCache(it2)
                        }
                    }
                }
            }

            override fun openTimeFlyMonitor(): Boolean {
                return true
            }

            override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
                MyLog.d(TAG, "onBufferingUpdate percent=$percent")
                if (percent == 100) {
                    if (mp!!.isPlaying) {
                        mAdapter!!.resumeLyricWhenBufferingEnd()
                    }
                } else {
                    mAdapter!!.pauseLyricWhenBuffering()
                }
            }

            override fun onTimeFlyMonitor(pos: Long, duration: Long) {
                if (mAdapter?.playing == true) {
                    mAdapter?.updatePlayProgress(pos, duration)
                    if ((pos * 100 / duration) > 80) {
                        FeedsPlayStatistics.addComplete(mAdapter?.mCurrentPlayModel?.feedID)
                    }
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

    private fun addOnScrollListenerToRv() {
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var maxPercent = 0f
            var model: FeedsWatchModel? = null
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
                        var postion = 0
                        // 以光盘为界限，找光盘显示百分比最多的
                        var firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition()
                        var lastVisibleItem = mLayoutManager.findLastVisibleItemPosition()
                        if (firstVisibleItem != RecyclerView.NO_POSITION && lastVisibleItem != RecyclerView.NO_POSITION) {
                            val percents = FloatArray(lastVisibleItem - firstVisibleItem + 1)
                            var i = firstVisibleItem
                            isFound = false
                            maxPercent = 0f
                            model = null
                            while (i <= lastVisibleItem && !isFound) {
                                val itemView = mRecyclerView.findViewHolderForAdapterPosition(i).itemView
                                val location1 = IntArray(2)
                                val location2 = IntArray(2)
                                itemView.getLocationOnScreen(location1)
                                mRecyclerView.getLocationOnScreen(location2)
                                val top = location1[1] - location2[1]
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
                                    model = mAdapter?.mDataList?.get(i)
                                    postion = i
                                } else {
                                    if (percents[i - firstVisibleItem] > maxPercent) {
                                        maxPercent = percents[i - firstVisibleItem]
                                        model = mAdapter?.mDataList?.get(i)
                                        postion = i
                                    }
                                }
                                i++
                            }
                        }


                        model?.let {
                            isFound = true
                            controlPlay(postion, it, true)
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

    private fun controlPlay(pos: Int, model: FeedsWatchModel?, isMustPlay: Boolean) {
        MyLog.d(TAG, "controlPlay fragment.fragmentVisible = ${fragment.fragmentVisible}")
        if (model != null &&
                !(model.feedID == mAdapter?.mCurrentPlayModel?.feedID && model.song?.songID == mAdapter?.mCurrentPlayModel?.song?.songID)) {
            SinglePlayer.reset(playerTag)
        }
        if (isMustPlay) {
            // 播放
            startPlay(pos, model)
        } else {
            if (mAdapter?.mCurrentPlayModel?.feedID == model?.feedID
                    && mAdapter?.mCurrentPlayModel?.song?.songID == model?.song?.songID
                    && mAdapter?.playing == true) {
                // 停止播放
                pausePlay()
            } else {
                // 播放
                startPlay(pos, model)
            }
        }
    }

    private fun startPlay(pos: Int, model: FeedsWatchModel?) {
        MyLog.d(TAG, "startPlayModel fragment.fragmentVisible = ${fragment.fragmentVisible}")
        mAdapter?.startPlayModel(pos, model)
        // 数据还是要更新，只是不播放，为恢复播放做准备
        if (!fragment.fragmentVisible) {
            mAdapter?.pausePlayModel()
            return
        }
        model?.song?.playURL?.let {
            FeedsPlayStatistics.addExpose(model?.feedID)
            SinglePlayer.startPlay(playerTag, it)
        }
    }

    /**
     * 继续播放
     */
    private fun resumePlay() {
        MyLog.d(TAG, "resumePlay fragment.fragmentVisible = ${fragment.fragmentVisible}")
        if (!fragment.fragmentVisible) {
            mAdapter?.pausePlayModel()
            return
        }
        mAdapter?.resumePlayModel()
        mAdapter?.mCurrentPlayModel?.song?.playURL?.let {
            FeedsPlayStatistics.addExpose(mAdapter?.mCurrentPlayModel?.feedID)
            SinglePlayer.startPlay(playerTag, it)
        }
    }

    private fun pausePlay() {
        MyLog.d(TAG, "pausePlay")
        mAdapter?.pausePlayModel()
        SinglePlayer.pause(playerTag)
    }

    override fun getFeeds(flag: Boolean) {
        if (mAdapter?.mDataList.isNullOrEmpty()) {
            // 列表都为空了，还不去拉一次
            mPersenter.initWatchList(true)
        } else {
            mPersenter.initWatchList(flag)
        }
    }

    override fun getMoreFeeds() {
        mPersenter.loadMoreWatchList()
    }

    override fun setUserInfoModel(userInfoModel: Any?) {
        mUserInfo = userInfoModel as UserInfoModel
        mPersenter.mUserInfo = mUserInfo
    }

    override fun addWatchList(list: List<FeedsWatchModel>?, isClear: Boolean, hasMore: Boolean) {
        this.hasMore = hasMore
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
        mRefreshLayout.setEnableLoadMore(hasMore)

        mCallBack?.onRequestSucess(hasMore)
        if (isClear) {
            mAdapter?.mDataList?.clear()
            if (list != null && list.isNotEmpty()) {
                mAdapter?.mDataList?.addAll(list)
            }
            if (isHomePage()) {
                if (mAdapter?.mDataList?.isNotEmpty() == true) {
                    controlPlay(0, mAdapter?.mDataList?.get(0), true)
                } else {
                    // 拉回来的列表为空
                    pausePlay()
                    mAdapter?.mCurrentPlayModel = null
                }
            }
            mAdapter?.notifyDataSetChanged()
        } else {
            if (list != null && list.isNotEmpty()) {
                mAdapter?.mDataList?.addAll(list)
                mAdapter?.notifyDataSetChanged()
            }
        }

        if (mAdapter?.mDataList != null && mAdapter?.mDataList?.isNotEmpty() == true) {
            mLoadService?.showSuccess()
        } else {
            mLoadService?.showCallback(EmptyCallback::class.java)
        }
    }

    override fun requestError() {
        mCallBack?.onRequestSucess(true)
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
    }

    override fun feedLikeResult(position: Int, model: FeedsWatchModel, isLike: Boolean) {
        model.isLiked = isLike
        if (isLike) {
            model.starCnt = model.starCnt?.plus(1)
        } else {
            model.starCnt = model.starCnt?.minus(1)
        }
        mAdapter?.update(position, model, FeedsWatchViewAdapter.REFRESH_TYPE_LIKE)
    }

    override fun feedDeleteResult(position: Int, model: FeedsWatchModel) {
        mAdapter?.delete(model)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        SinglePlayer.addCallback(playerTag, playCallback)
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFeedsMoreDialogView?.dismiss(false)
    }

    override fun destroy() {
        mPersenter.destroy()
        mFeedsMoreDialogView?.dismiss(false)
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun unselected() {
        MyLog.d("unselected")
        pausePlay()
    }

    fun selected() {
        MyLog.d(TAG, "selected")
        // 该页面选中以及从详情页返回都会回调这个方法
        if (!mPersenter.initWatchList(false)) {
            // 如果因为时间短没请求，继续往前播放
            resumePlay()
        }
    }

    private fun homePagerMore(it: FeedsWatchModel) {
        mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED_HOME,
                it.user?.userID ?: 0,
                it.feedID)
                .apply {
                    songID = it.song?.songID ?: 0
                    if (it.status != 2) {
                        hideCollect()
                    }
                    mFuncationTv.setOnClickListener(object : DebounceViewClickListener() {
                        override fun clickValid(v: View?) {
                            dismiss()
                            UserInfoManager.getInstance().mateRelation(it.user?.userID
                                    ?: 0, UserInfoManager.RA_BUILD
                                    , false, object : UserInfoManager.ResponseCallBack<Boolean>() {
                                override fun onServerFailed() {
                                    U.getToastUtil().showShort("关注失败了")
                                }

                                override fun onServerSucess(isFriend: Boolean?) {
                                    U.getToastUtil().showShort("关注成功")
                                }
                            })
                        }
                    })
                }
        mFeedsMoreDialogView?.showByDialog()
    }

    private fun personPageMore(position: Int, it: FeedsWatchModel) {
        if ((mUserInfo?.userId
                        ?: 0).toLong() == MyUserInfoManager.getInstance().uid) {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_PERSON,
                    it.user?.userID ?: 0,
                    it.feedID)
                    .apply {
                        songID = it.song?.songID ?: 0
                        if (it.status != 2) {
                            // 去掉分享和收藏
                            hideCollect()
                            hideFuncation()
                        } else {
                            showFuncation("分享")
                            mFuncationTv.setOnClickListener(object : DebounceViewClickListener() {
                                override fun clickValid(v: View?) {
                                    dismiss(false)
                                    share(it)
                                }
                            })
                        }
                        mReportTv.text = "删除"
                        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                dismiss(false)
                                mTipsDialogView = TipsDialogView.Builder(fragment.activity)
                                        .setMessageTip("是否确定删除该页面")
                                        .setConfirmTip("确认删除")
                                        .setCancelTip("取消")
                                        .setCancelBtnClickListener(object : AnimateClickListener() {
                                            override fun click(view: View?) {
                                                mTipsDialogView?.dismiss()
                                            }
                                        })
                                        .setConfirmBtnClickListener(object : AnimateClickListener() {
                                            override fun click(view: View?) {
                                                mTipsDialogView?.dismiss(false)
                                                mPersenter.deleteFeed(position, it)
                                            }
                                        })
                                        .build()
                                mTipsDialogView?.showByDialog()
                            }
                        })
                    }
        } else {
            mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_OTHER_PERSON,
                    it.user?.userID ?: 0,
                    it.feedID)
                    .apply {
                        songID = it.song?.songID ?: 0
                        if (it.status != 2) {
                            hideCollect()
                            hideFuncation()
                        } else {
                            showFuncation("分享")
                            mFuncationTv.setOnClickListener(object : DebounceViewClickListener() {
                                override fun clickValid(v: View?) {
                                    dismiss(false)
                                    share(it)
                                }
                            })
                        }
                    }
        }
        mFeedsMoreDialogView?.showByDialog()
    }

    private fun share(model: FeedsWatchModel) {
        //TODO 需要产品确认跳到哪
        val sharePanel = SharePanel(fragment.activity)
        sharePanel.setShareContent("", model.song?.workName, model.user?.nickname,
                ApiManager.getInstance().findRealUrlByChannel(String.format("http://app.inframe.mobi/feed/song?songID=%d&userID=%d",
                        model.song?.songID, model.user?.userID)))
        sharePanel.show(ShareType.URL)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedWatchChangeEvent) {
        // 数据要更新了
        val model = mAdapter?.getModelFromDetail(event.model)
        model?.let {
            mAdapter?.update(it, FeedsWatchViewAdapter.REFRESH_TYPE_LIKE)
            mAdapter?.update(it, FeedsWatchViewAdapter.REFRESH_TYPE_COMMENT)
        }

    }
}