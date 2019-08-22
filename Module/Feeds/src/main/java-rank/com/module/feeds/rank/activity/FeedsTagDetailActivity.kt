package com.module.feeds.rank.activity

import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.bigkoo.pickerview.view.TimePickerView
import com.common.base.BaseActivity
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
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.detail.activity.FeedsDetailActivity.Companion.TYPE_SWITCH_MODE
import com.module.feeds.detail.manager.AbsPlayModeManager
import com.module.feeds.detail.manager.FeedSongPlayModeManager
import com.module.feeds.detail.manager.add2SongPlayModeManager
import com.module.feeds.event.FeedDetailChangeEvent
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.rank.adapter.FeedTagDetailAdapter
import com.module.feeds.rank.adapter.FeedTagListener
import com.module.feeds.rank.event.FeedTagFollowStateEvent
import com.module.feeds.statistics.FeedPage
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.FeedsWatchServerApi
import com.module.feeds.watch.model.FeedRecommendTagModel
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

@Route(path = RouterConstants.ACTIVITY_FEEDS_TAG_DETAIL)
class FeedsTagDetailActivity : BaseActivity() {

    lateinit var imageBg: SimpleDraweeView
    lateinit var timeTv: TextView
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView
    lateinit var appbar: AppBarLayout

    lateinit var toolbar: Toolbar
    lateinit var topAreaBg: SimpleDraweeView
    lateinit var topDesc: TextView
    lateinit var mCollectTv: ExTextView

    lateinit var ivBack: ExImageView

    var model: FeedRecommendTagModel? = null
    lateinit var mAdapter: FeedTagDetailAdapter

    var lastVerticalOffset = Int.MAX_VALUE
    lateinit var maxDate: Date   // 最大的时间戳
    lateinit var curDate: Date   // 当前选择的时间
    var queryDate: String = ""

    var mOffset = 0
    val mCNT = 30
    var hasMore = true

    var pvCustomTime: TimePickerView? = null
    private val playerTag = TAG + hashCode()

    private val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    var mSongPlayModeManager: FeedSongPlayModeManager? = null

    private val playCallback = object : PlayerCallbackAdapter() {
        override fun onCompletion() {
            super.onCompletion()
            mSongPlayModeManager?.getNextSong(false) {
                it?.let { sm ->
                    startPlay(sm)
                }
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
        return R.layout.feeds_tag_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        model = intent.getSerializableExtra("model") as FeedRecommendTagModel?
        if (model == null) {
            finish()
        }

        imageBg = findViewById(R.id.image_bg)
        timeTv = findViewById(R.id.time_tv)
        smartRefresh = findViewById(R.id.smart_refresh)
        recyclerView = findViewById(R.id.recycler_view)
        appbar = findViewById(R.id.appbar)
        toolbar = findViewById(R.id.toolbar)
        topAreaBg = findViewById(R.id.top_area_bg)
        topDesc = findViewById(R.id.top_desc)
        ivBack = findViewById(R.id.iv_back)
        mCollectTv = findViewById(R.id.collect_tv)

        if (model?.isSupportCollected ?: false) {
            mCollectTv.visibility = View.VISIBLE
            mCollectTv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    launch {
                        val obj = subscribe(RequestControl("FeedsTagDetailActivity", ControlType.CancelThis)) {
                            val map = mutableMapOf("albumID" to model!!.rankID, "isCollected" to !model!!.isCollected, "userID" to MyUserInfoManager.getInstance().uid)
                            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                            mFeedServerApi.albumCollect(body)
                        }

                        if (obj.errno == 0) {
                            model!!.isCollected = !model!!.isCollected
                            mCollectTv.text = if (model!!.isCollected) "取消收藏" else "收藏歌单"
                            EventBus.getDefault().post(FeedTagFollowStateEvent(model))
                        } else {
                            if (obj.errno == -2) {
                                U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                            }
                        }
                    }
                }
            })

            launch {
                val obj = subscribe(RequestControl("FeedsTagDetailActivity", ControlType.CancelThis)) {
                    mFeedServerApi.checkAlbumCollect(model!!.rankID, MyUserInfoManager.getInstance().uid)
                }

                if (obj.errno == 0) {
                    val isCollected = obj.data.getBooleanValue("isCollected")
                    model!!.isCollected = isCollected
                    mCollectTv.text = if (model!!.isCollected) "取消收藏" else "收藏歌单"
                }
            }
        }

        smartRefresh.apply {
            setEnableRefresh(true)
            setEnableLoadMore(true)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)
            setHeaderMaxDragRate(1.5f)
            setOnMultiPurposeListener(object : SimpleMultiPurposeListener() {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    super.onRefresh(refreshLayout)
                    loadInitData()
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    super.onLoadMore(refreshLayout)
                    loadMoreData()
                }

                var lastScale = 0f
                override fun onHeaderMoving(header: RefreshHeader?, isDragging: Boolean, percent: Float, offset: Int, headerHeight: Int, maxDragHeight: Int) {
                    super.onHeaderMoving(header, isDragging, percent, offset, headerHeight, maxDragHeight)
                    // 背景图片的高度是400
                    val scale = offset.toFloat() / 400.dp().toFloat() + 1
                    if (Math.abs(scale - lastScale) >= 0.01) {
                        lastScale = scale
                        imageBg.scaleX = scale
                        imageBg.scaleY = scale
                    }
                }
            })
        }

        appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            // TODO: 2019-06-23 也可以加效果，看产品怎么说
            imageBg.translationY = verticalOffset.toFloat()
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                if (verticalOffset == 0) {
                    // 展开状态
                    if (toolbar.visibility != View.GONE) {
                        toolbar.visibility = View.GONE
                    }
                } else if (Math.abs(verticalOffset) >= appBarLayout.totalScrollRange) {
                    // 完全收缩状态
                    if (toolbar.visibility != View.VISIBLE) {
                        toolbar.visibility = View.VISIBLE
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (toolbar.visibility != View.GONE) {
                        toolbar.visibility = View.GONE
                    }
                }
            }
        }

        ivBack.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                finish()
            }
        })

        timeTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                showDatePicker()
            }
        })

        mSongPlayModeManager = FeedSongPlayModeManager(FeedSongPlayModeManager.PlayMode.ORDER, null, null)
        mSongPlayModeManager?.supportCycle = false
        mSongPlayModeManager?.loadMoreCallback = { size, callback ->
            if (hasMore) {
                getRecomendTagDetailList(mOffset, queryDate, curDate, false) {
                    callback.invoke()
                }
            } else {
                callback.invoke()
            }
        }
        mAdapter = FeedTagDetailAdapter(object : FeedTagListener {
            override fun onClickItem(position: Int, model: FeedsWatchModel?) {
                // 默认顺序就只是列表循环
                mAdapter.mCurrentPlayPosition = position
                mAdapter.mCurrentPlayModel = model
                model?.let {
                    mSongPlayModeManager?.setCurrentPlayModel(it.song)
                    FeedsDetailActivity.openActivity(this@FeedsTagDetailActivity, it.feedID, TYPE_SWITCH_MODE, FeedSongPlayModeManager.PlayMode.ORDER, object : AbsPlayModeManager() {
                        override fun getNextSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                            mSongPlayModeManager?.getNextSong(userAction) { sm ->
                                if (sm != null) {
                                    val curPos = mSongPlayModeManager?.getCurPostionInOrigin()
                                    curPos?.let {
                                        mAdapter.startPlayModel(curPos)
                                    }
                                } else {

                                }
                                callback.invoke(sm)
                            }
                        }

                        override fun getPreSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                            mSongPlayModeManager?.getPreSong(userAction) { sm ->
                                if (sm != null) {
                                    val curPos = mSongPlayModeManager?.getCurPostionInOrigin()
                                    curPos?.let {
                                        mAdapter.startPlayModel(curPos)
                                    }
                                } else {

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

            override fun onClickCollect(position: Int, model: FeedsWatchModel?) {
                model?.let {
                    collectOrUnCollectFeed(position, it)
                }
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = mAdapter
        FrescoWorker.loadImage(imageBg, ImageFactory.newPathImage(model?.bigImgURL)
                .setScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_640)
                .build<BaseImage>())
        topDesc.text = model?.rankDesc
        AvatarUtils.loadAvatarByUrl(topAreaBg, AvatarUtils.newParamsBuilder(model?.bigImgURL)
                .setSizeType(ImageUtils.SIZE.SIZE_160)
                .setBlur(true)
                .build())
        maxDate = Date(model?.timeMs ?: System.currentTimeMillis())
        curDate = Date(model?.timeMs ?: System.currentTimeMillis())
        queryDate = U.getDateTimeUtils().formatDateString(curDate)
        if ((model?.timeMs ?: 0) > 0) {
            timeTv.visibility = View.VISIBLE
            timeTv.text = queryDate
        } else {
            timeTv.visibility = View.GONE
        }


        loadInitData()

        SinglePlayer.reset(playerTag)
        SinglePlayer.addCallback(playerTag, playCallback)
    }

    private fun showDatePicker() {
        var cur = Calendar.getInstance()
        cur.time = curDate
        var max = Calendar.getInstance()
        max.time = maxDate
        var star = Calendar.getInstance()
        //todo 月份需要减去一
        star.set(2019, 7, 1)
        if (star <= max && cur >= star && cur <= max) {
            pvCustomTime = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
                changeDate(date)
            })
                    .setType(booleanArrayOf(true, true, true, false, false, false))
                    .setDividerColor(Color.parseColor("#4c979797"))
                    .setBgColor(Color.parseColor("#EBEDF2"))
                    .setContentTextSize(16)
                    .setTextColorCenter(Color.BLACK)
                    .setLineSpacingMultiplier(2f)
                    .setDate(cur)
                    .setRangDate(star, max)
                    .setLayoutRes(R.layout.feed_time_picker_layout) {
                        val cancleTv: TextView = it.findViewById(R.id.cancle_tv)
                        val confirmTv: TextView = it.findViewById(R.id.confirm_tv)

                        cancleTv.setOnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                pvCustomTime?.dismiss()
                            }
                        })

                        confirmTv.setOnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                pvCustomTime?.returnData()
                                pvCustomTime?.dismiss()
                            }
                        })

                    }
                    .isDialog(true) //默认设置false ，内部实现将DecorView 作为它的父控件。
                    .build()

            val mDialog = pvCustomTime?.dialog
            if (mDialog != null) {

                val params = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM)

                params.leftMargin = 0
                params.rightMargin = 0
                pvCustomTime?.dialogContainerLayout?.layoutParams = params

                val dialogWindow = mDialog.window
                if (dialogWindow != null) {
                    dialogWindow.setWindowAnimations(R.style.picker_view_slide_anim)//修改动画样式
                    dialogWindow.setGravity(Gravity.BOTTOM)//改成Bottom,底部显示
                }
            }
            pvCustomTime?.show()
        } else {
            MyLog.e(TAG, "showDatePicker time error")
        }
    }

//    private fun findNextSong(userAction: Boolean): FeedSongModel? {
//        if (adapter.mCurrentPlayPosition == adapter.mDataList.size - 2) {
//            // 已经到最后一个，需要去更新数据
//            loadMoreData()
//        }
//
//        if (!adapter.mDataList.isNullOrEmpty()) {
//            // 在合理范围内
//            if (adapter.mCurrentPlayPosition in -1..(adapter.mDataList.size - 2)) {
//                adapter.mCurrentPlayPosition = adapter.mCurrentPlayPosition + 1
//                adapter.mCurrentPlayModel = adapter.mDataList[adapter.mCurrentPlayPosition]
//                return adapter.mCurrentPlayModel?.song
//            }
//        }
//
//        return null
//    }
//
//    private fun findPresong(userAction: Boolean): FeedSongModel? {
//        if (adapter.mCurrentPlayPosition == 0) {
//            return null
//        }
//
//        if (!adapter.mDataList.isNullOrEmpty()) {
//            if (adapter.mCurrentPlayPosition in 1..adapter.mDataList.size) {
//                adapter.mCurrentPlayPosition = adapter.mCurrentPlayPosition - 1
//                adapter.mCurrentPlayModel = adapter.mDataList[adapter.mCurrentPlayPosition]
//                return adapter.mCurrentPlayModel?.song
//            }
//        }
//        return null
//    }


    private fun loadInitData() {
        getRecomendTagDetailList(0, queryDate, curDate, true)
    }

    private fun loadMoreData() {
        if (hasMore) {
            getRecomendTagDetailList(mOffset, queryDate, curDate, false)
        } else {
            U.getToastUtil().showShort("没有更多数据了")
        }
    }

    private fun getRecomendTagDetailList(offset: Int, queryTime: String, date: Date, isClean: Boolean, dataOkListener: (() -> Unit)? = null) {
        launch {
            val obj = subscribe(RequestControl("getRecomendTagDetailList", ControlType.CancelThis)) {
                mFeedServerApi.getRecomendTagDetailList(offset, mCNT, model?.rankID!!, queryTime, MyUserInfoManager.getInstance().uid)
            }
            if (obj.errno == 0) {
                val list = JSON.parseArray(obj.data.getString("rankInfos"), FeedsWatchModel::class.java)
                mOffset = obj.data.getIntValue("offset")
                hasMore = obj.data.getBooleanValue("hasMore")
                if (date != curDate) {
                    queryDate = queryTime
                    curDate = date
                    timeTv.text = queryDate
                }
                smartRefresh.finishRefresh()
                smartRefresh.finishLoadMore()
                smartRefresh.setEnableLoadMore(hasMore)

                if (isClean) {
                    mAdapter.mDataList.clear()
                }

                if (!list.isNullOrEmpty()) {
                    mAdapter.mDataList.addAll(list)
                }

                mAdapter.notifyDataSetChanged()

                if (isClean) {
                    add2SongPlayModeManager(mSongPlayModeManager, mAdapter.mDataList, true)
                } else {
                    add2SongPlayModeManager(mSongPlayModeManager, list, false)
                }
                // 一定要放在 mSongPlayModeManager 后面
                dataOkListener?.invoke()
            } else {
                dataOkListener?.invoke()
                smartRefresh.finishRefresh()
                smartRefresh.finishLoadMore()
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun changeDate(date: Date) {
        if (date != curDate) {
            getRecomendTagDetailList(0, U.getDateTimeUtils().formatDateString(date), date, true)
        } else {

        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun canSlide(): Boolean {
        return false
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
                mAdapter?.update(position, model, FeedTagDetailAdapter.REFRESH_TYPE_COLLECT)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedsCollectChangeEvent) {
        mAdapter.mDataList.forEachIndexed { index, feedsWatchModel ->
            if (feedsWatchModel.feedID == event.feedID) {
                feedsWatchModel.isCollected = event.isCollected
                mAdapter.update(index, feedsWatchModel, FeedTagDetailAdapter.REFRESH_TYPE_COLLECT)
            }
        }
    }

    @Subscribe
    fun onEvent(event: FeedDetailChangeEvent) {
        // 播放的歌曲更新了,更新mTopModel 和 mTopPosition
        MyLog.d(TAG, "onEventevent FeedSongPlayEvent = $event")
        event.model?.song?.let {
            startPlay(it)
        }
    }

    fun startPlay(it: FeedSongModel) {
        mAdapter.mDataList.forEachIndexed { index, feed ->
            if (it.feedID == feed.song?.feedID && it.songID == feed.song?.songID) {
                //todo 从详情页面返回，直接播放吧(继续播放)
                mAdapter.startPlayModel(index, feed)
                mSongPlayModeManager?.setCurrentPlayModel(feed.song)
                feed.song?.playURL?.let {
                    if(model?.isSupportCollected == true){
                        // 运营歌单
                        FeedsPlayStatistics.setCurPlayMode(feed.feedID,FeedPage.SONG_ALBUM_OP,model?.rankID?:0)
                    }else {
                        FeedsPlayStatistics.setCurPlayMode(feed.feedID,FeedPage.SONG_ALBUM_RANK,model?.rankID?:0)
                    }

                    SinglePlayer.startPlay(playerTag, it)
                }
                return@forEachIndexed
            }
        }
    }

    override fun destroy() {
        super.destroy()
        SinglePlayer.reset(playerTag)
        SinglePlayer.removeCallback(playerTag)
    }
}