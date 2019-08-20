package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.component.busilib.callback.EmptyCallback
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.kingja.loadsir.callback.Callback
import com.kingja.loadsir.core.LoadService
import com.kingja.loadsir.core.LoadSir
import com.module.feeds.R
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.detail.activity.FeedsDetailActivity.Companion.FROM_HOME_COLLECT
import com.module.feeds.detail.manager.AbsPlayModeManager
import com.module.feeds.detail.manager.FeedSongPlayModeManager
import com.module.feeds.event.FeedDetailChangeEvent
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.rank.event.FeedTagFollowStateEvent
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.adapter.FeedCollectListener
import com.module.feeds.watch.adapter.FeedsCollectViewAdapter
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsCollectModel
import com.module.feeds.watch.presenter.FeedCollectViewPresenter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * 收藏view
 */
class FeedsCollectView(var fragment: BaseFragment) : ExConstraintLayout(fragment.context!!), IFeedCollectView {

    val TAG = "FeedsCollectView"

    var mCurrentType = FeedSongPlayModeManager.PlayMode.ORDER //默认顺序播放
    var isPlaying = false

    var mTopModel: FeedsCollectModel? = null
    var mTopPosition: Int = 0      // 顶部在播放队列中的位置
    var mIsNeedResumePlay = false    // 标记是否需要恢复播放

    var mSongPlayModeManager: FeedSongPlayModeManager? = null

    private val mContainer: ConstraintLayout
    private val mTopAreaBg: SimpleDraweeView
    private val mPlayDescTv: TextView

    private val mRefreshLayout: SmartRefreshLayout
    private val mClassicsHeader: ClassicsHeader
    private val mRecyclerView: RecyclerView

    private val mPlayTypeIv: ImageView
    private val mPlayLikeIv: ExImageView
    private val mRecordGroup: Group
    private val mRecordFilm: ImageView
    private val mRecordCover: SimpleDraweeView
    private val mRecordPlayIv: ImageView
    private val mPlayLastIv: ImageView
    private val mPlayNextIv: ImageView

    private val mPersenter: FeedCollectViewPresenter
    private val mAdapter: FeedsCollectViewAdapter

    private var mCDRotateAnimation: RotateAnimation? = null
    private var mCoverRotateAnimation: RotateAnimation? = null

    private val mLoadService: LoadService<*>
    private val playerTag = TAG + hashCode()
    private val playCallback: PlayerCallbackAdapter

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    init {
        View.inflate(context, R.layout.feed_like_view_layout, this)

        mContainer = findViewById(R.id.container)

        mRefreshLayout = findViewById(R.id.refreshLayout)
        mClassicsHeader = findViewById(R.id.classics_header)
        mRecyclerView = findViewById(R.id.recycler_view)

        mTopAreaBg = findViewById(R.id.top_area_bg)
        mPlayDescTv = findViewById(R.id.play_desc_tv)

        mPlayTypeIv = findViewById(R.id.play_type_iv)
        mPlayLikeIv = findViewById(R.id.play_like_iv)
        mRecordGroup = findViewById(R.id.record_group)
        mRecordFilm = findViewById(R.id.record_film)
        mRecordCover = findViewById(R.id.record_cover)
        mRecordPlayIv = findViewById(R.id.record_play_iv)
        mPlayLastIv = findViewById(R.id.play_last_iv)
        mPlayNextIv = findViewById(R.id.play_next_iv)

        mPersenter = FeedCollectViewPresenter(this)
        mAdapter = FeedsCollectViewAdapter(object : FeedCollectListener {
            override fun onClickItemListener(model: FeedsCollectModel?, position: Int) {
                // 跳到详情页面
                model?.let {
                    mIsNeedResumePlay = true
                    fragment?.activity?.let { fragmentActivity ->
                        FeedsDetailActivity.openActivity(fragmentActivity, it.feedID, FROM_HOME_COLLECT, mCurrentType, object : AbsPlayModeManager() {
                            override fun getNextSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                                mSongPlayModeManager?.getNextSong(userAction,callback)
                            }

                            override fun getPreSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                                mSongPlayModeManager?.getPreSong(userAction,callback)
                            }

                            override fun changeMode(mode: FeedSongPlayModeManager.PlayMode) {
                                mSongPlayModeManager?.changeMode(mode)
                                mCurrentType = mode
                                when (mode) {
                                    FeedSongPlayModeManager.PlayMode.ORDER -> {
                                        mPlayTypeIv?.setImageResource(R.drawable.like_all_repeat_icon)
                                    }
                                    FeedSongPlayModeManager.PlayMode.SINGLE -> {
                                        mPlayTypeIv?.setImageResource(R.drawable.like_single_repeat_icon)
                                    }
                                    FeedSongPlayModeManager.PlayMode.RANDOM -> {
                                        mPlayTypeIv?.setImageResource(R.drawable.like_random_icon)
                                    }
                                }
                            }

                            override fun getCurMode(): FeedSongPlayModeManager.PlayMode {
                                return mSongPlayModeManager?.getCurMode()?:FeedSongPlayModeManager.PlayMode.ORDER
                            }
                        })
                    }

                    mSongPlayModeManager?.setCurrentPlayModel(it.song)
                }
            }

            override fun onClickPlayListener(model: FeedsCollectModel?, position: Int) {
                model?.let {
                    mSongPlayModeManager?.setCurrentPlayModel(it.song)
                    playOrPause(it, position, false)
                }
            }
        })

        mRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mRecyclerView.adapter = mAdapter

        mRefreshLayout.setEnableRefresh(true)
        mRefreshLayout.setEnableLoadMore(false)
        mRefreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        mRefreshLayout.setEnableOverScrollDrag(false)
        mRefreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                mPersenter.initFeedLikeList(true)
                getRecommendTagList()
            }
        })

        mPlayTypeIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 更新游戏类别 全部循环，单曲循环，随机播放(需要重新校准播放的位置)
                // 为了保证随机播放（确定是否要重新随机）
                when (mCurrentType) {
                    FeedSongPlayModeManager.PlayMode.ORDER -> {
                        mCurrentType = FeedSongPlayModeManager.PlayMode.SINGLE
                        mPlayTypeIv.setImageResource(R.drawable.like_single_repeat_icon)
                        U.getToastUtil().showShort("单曲循环")
                        mSongPlayModeManager?.changeMode(mCurrentType)
                    }
                    FeedSongPlayModeManager.PlayMode.SINGLE -> {
                        mCurrentType = FeedSongPlayModeManager.PlayMode.RANDOM
                        mPlayTypeIv.setImageResource(R.drawable.like_random_icon)
                        U.getToastUtil().showShort("随机播放")
                        mSongPlayModeManager?.changeMode(mCurrentType)
                    }
                    FeedSongPlayModeManager.PlayMode.RANDOM -> {
                        mCurrentType = FeedSongPlayModeManager.PlayMode.ORDER
                        mPlayTypeIv.setImageResource(R.drawable.like_all_repeat_icon)
                        U.getToastUtil().showShort("列表循环")
                        mSongPlayModeManager?.changeMode(mCurrentType)
                    }
                }
            }
        })


        mPlayLikeIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 不需要对界面进行任何更新
                mTopModel?.let { mPersenter.likeOrUnLikeFeed(it) }
            }
        })

        mRecordFilm.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mTopModel?.let {
                    mSongPlayModeManager?.setCurrentPlayModel(it.song)
                    playOrPause(it, mTopPosition, false)
                }
            }

        })

        mPlayLastIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 上一首
                playWithType(false, true)
            }
        })

        mPlayNextIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                // 下一首
                playWithType(true, true)
            }
        })

        val mLoadSir = LoadSir.Builder()
                .addCallback(EmptyCallback(R.drawable.feed_home_list_empty_icon, "暂无收藏的神曲", "#802F2F30"))
                .build()

        mLoadService = mLoadSir.register(mContainer, Callback.OnReloadListener {
            initData(true)
        })

        playCallback = object : PlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                // 自动播放下一首
                playWithType(true, false)
            }

            override fun openTimeFlyMonitor(): Boolean {
                return true
            }

            override fun onTimeFlyMonitor(pos: Long, duration: Long) {
                FeedsPlayStatistics.updateCurProgress(pos, duration)
            }
        }

        SinglePlayer.addCallback(playerTag, playCallback)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        getRecommendTagList()
    }

    private fun getRecommendTagList() {
        launch {
            val obj = subscribe(RequestControl("getRecomendTagList", ControlType.CancelThis)) {
                mFeedServerApi.getAlbumCollectList(0, 10, MyUserInfoManager.getInstance().uid)
            }
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("tags"), FeedRecommendTagModel::class.java)
                mAdapter.mRankTagList.clear()
                if (list != null && list.size > 0) {
                    mAdapter.mRankTagList.addAll(list)
                }

                mAdapter.notifyDataSetChanged()
                if ((mAdapter.mDataList == null || mAdapter.mDataList.isEmpty()) && mAdapter.mRankTagList.isEmpty()) {
                    mLoadService.showCallback(EmptyCallback::class.java)
                } else {
                    mLoadService.showSuccess()
                }
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    // 初始化数据
    fun initData(flag: Boolean) {
        if (mAdapter.mDataList.isNullOrEmpty()) {
            mPersenter.initFeedLikeList(true)
        } else {
            mPersenter.initFeedLikeList(flag)
        }
    }

    // 停止播放
    fun stopPlay() {
        if (isPlaying) {
            isPlaying = false
            bindPlayAreaData(mTopPosition, mTopModel, false)
            mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_play_icon)
            mAdapter.mCurrentPlayModel = null
            mAdapter.notifyDataSetChanged()
            SinglePlayer.reset(playerTag)
        }
    }

    private fun playWithType(isNext: Boolean, fromUser: Boolean) {
        val f: (songModel: FeedSongModel?) -> Unit = {
            mAdapter.mDataList.forEachIndexed { index, feedsCollectModel ->
                if (feedsCollectModel.feedID == it?.feedID) {
                    playOrPause(feedsCollectModel, index, true)
                    return@forEachIndexed
                }
            }
        }
        if (isNext) {
            mSongPlayModeManager?.getNextSong(fromUser, f)
        } else {
            mSongPlayModeManager?.getPreSong(fromUser, f)
        }
    }


    fun playOrPause(model: FeedsCollectModel, position: Int, isMust: Boolean) {
        if (isMust) {
            // 开始播放
            play(model, position)
        } else {
            if (mAdapter.mCurrentPlayModel == model) {
                // 暂停播放
                pause(model, position)
            } else {
                // 开始播放
                play(model, position)
            }
        }
    }

    private fun play(model: FeedsCollectModel, position: Int) {
        isPlaying = true
        bindPlayAreaData(position, model, true)
        mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_pause_icon)
        mAdapter.mCurrentPlayModel = mTopModel
        mAdapter.notifyDataSetChanged()
        model?.song?.playURL?.let {
            FeedsPlayStatistics.setCurPlayMode(model?.song?.feedID ?: 0)
            SinglePlayer.startPlay(playerTag, it)
        }
    }

    private fun pause(model: FeedsCollectModel, position: Int) {
        isPlaying = false
        bindPlayAreaData(position, model, false)
        mRecordPlayIv.background = U.getDrawable(R.drawable.like_record_play_icon)
        mAdapter.mCurrentPlayModel = null
        mAdapter.notifyDataSetChanged()
        SinglePlayer.reset(playerTag)
    }

    override fun showCollectList(list: List<FeedsCollectModel>?) {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()

        list?.let {
            // 初始化数据
            val feedSongModels = ArrayList<FeedSongModel>()
            var cur: FeedSongModel? = null
            it.forEach { feedCollectModel ->
                feedCollectModel.song?.let { feedSongModel ->
                    feedSongModels.add(feedSongModel)
                }
            }
            mSongPlayModeManager = FeedSongPlayModeManager(mCurrentType, cur, feedSongModels)


            stopPlay()
            mAdapter.mDataList.clear()
            if (list != null) {
                // 数据改变了，重新去生成随机队列去
                mAdapter.mDataList.addAll(list)
            }
            if (mAdapter.mDataList.isNotEmpty()) {
                bindPlayAreaData(0, mAdapter.mDataList[0], false)
            }
            mAdapter.notifyDataSetChanged()
        }

        if ((mAdapter.mDataList == null || mAdapter.mDataList.isEmpty())) {
            mTopModel = null
            if (mAdapter.mRankTagList.isEmpty()) {
                mLoadService.showCallback(EmptyCallback::class.java)
            } else {
                mLoadService.showSuccess()
                mRecordCover.setImageDrawable(null)
                mTopAreaBg.setImageDrawable(null)
            }
        } else {
            mLoadService.showSuccess()
        }
    }

    override fun showCollect(model: FeedsCollectModel) {
        this.mTopModel = model
        mPlayLikeIv.isSelected = model.isLiked
        if (model.isLiked) {
            mPlayLikeIv.setImageResource(R.drawable.feed_collect_selected_icon)
        } else {
            mPlayLikeIv.setImageResource(R.drawable.feed_collect_normal_icon)
        }
        // 更新数据
        mAdapter.update(mTopPosition, model)
    }

    override fun requestError() {
        mRefreshLayout.finishRefresh()
        mRefreshLayout.finishLoadMore()
    }

    /**
     * 绑定顶部播放区域数据
     */
    private fun bindPlayAreaData(position: Int, model: FeedsCollectModel?, isPlay: Boolean) {
        this.mTopPosition = position
        if (this.mTopModel != model) {
            this.mTopModel = model
            AvatarUtils.loadAvatarByUrl(mTopAreaBg, AvatarUtils.newParamsBuilder(mTopModel?.user?.avatar)
                    .setBlur(true)
                    .build())

            AvatarUtils.loadAvatarByUrl(mRecordCover, AvatarUtils.newParamsBuilder(mTopModel?.user?.avatar)
                    .setCircle(true)
                    .build())

            var songTag = mTopModel?.song?.tags?.get(0)?.tagDesc ?: ""
            if (!TextUtils.isEmpty(songTag)) {
                songTag = " #$songTag# "
            }
            mPlayDescTv.text = "《${mTopModel?.song?.workName ?: ""}》$songTag"
            mPlayLikeIv.isSelected = mTopModel?.isLiked == true
            if (mTopModel?.isLiked == true) {
                mPlayLikeIv.setImageResource(R.drawable.feed_collect_selected_icon)
            } else {
                mPlayLikeIv.setImageResource(R.drawable.feed_collect_normal_icon)
            }
        }
        // 开启和关闭动画
        if (isPlay) {
            if (mCDRotateAnimation == null) {
                mCDRotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                mCDRotateAnimation?.duration = 3000
                mCDRotateAnimation?.repeatCount = Animation.INFINITE
                mCDRotateAnimation?.fillAfter = true
                mCDRotateAnimation?.interpolator = LinearInterpolator()
            }
            if (mCoverRotateAnimation == null) {
                mCoverRotateAnimation = RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                mCoverRotateAnimation?.duration = 3000
                mCoverRotateAnimation?.repeatCount = Animation.INFINITE
                mCoverRotateAnimation?.fillAfter = true
                mCoverRotateAnimation?.interpolator = LinearInterpolator()
            }
            mRecordFilm.startAnimation(mCDRotateAnimation)
            mRecordCover.startAnimation(mCoverRotateAnimation)
        } else {
            mCDRotateAnimation?.setAnimationListener(null)
            mCDRotateAnimation?.cancel()
            mCoverRotateAnimation?.setAnimationListener(null)
            mCoverRotateAnimation?.cancel()
        }
    }

    @Subscribe
    fun onEvent(event: FeedsCollectChangeEvent) {
        // 有喜欢事件发生促使刷新
        mPersenter.hasInitData = false
    }

    @Subscribe
    fun onEvent(event: FeedTagFollowStateEvent) {
        if (event.feedRecommendTagModel.isCollected) {
            mAdapter.mRankTagList.add(event.feedRecommendTagModel)
        } else {
            var model: FeedRecommendTagModel? = null
            mAdapter.mRankTagList.forEach {
                if (it.rankID == event.feedRecommendTagModel.rankID) {
                    model = it
                    return@forEach
                }
            }

            model?.let {
                mAdapter.mRankTagList.remove(it)
            }
        }

        mAdapter.notifyDataSetChanged()
    }

    @Subscribe
    fun onEvent(event: FeedDetailChangeEvent) {
        // 播放的歌曲更新了,更新mTopModel 和 mTopPosition
        MyLog.d(TAG, "onEventevent FeedSongPlayEvent = $event")
        event.model?.song?.let {
            mAdapter.mDataList.forEachIndexed { index, feedsCollectModel ->
                if (it.feedID == feedsCollectModel.song?.feedID && it.songID == feedsCollectModel.song?.songID) {
                    bindPlayAreaData(index, feedsCollectModel, false)
                    mTopPosition = index
                    mTopModel = feedsCollectModel
                    return@forEachIndexed
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun destory() {
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
        mCDRotateAnimation?.setAnimationListener(null)
        mCDRotateAnimation?.cancel()
        mCoverRotateAnimation?.setAnimationListener(null)
        mCoverRotateAnimation?.cancel()
        mPersenter.destroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    fun unselected() {
        MyLog.d(TAG, "unselected")
        SinglePlayer.reset(playerTag)
        stopPlay()
    }

    fun selected() {
        MyLog.d(TAG, "selected isPlaying=$isPlaying")
        if (!isPlaying) {
            if (mIsNeedResumePlay) {
                // 恢复播放
                mIsNeedResumePlay = false
                mTopModel?.let {
                    playOrPause(it, mTopPosition, true)
                }
            } else {
                // 停止播放
                MyLog.d(TAG, "selected 停止播放吧")
                SinglePlayer.reset(playerTag)
                initData(false)
            }

        }
    }
}