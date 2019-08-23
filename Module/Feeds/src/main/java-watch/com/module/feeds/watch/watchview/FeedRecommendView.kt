package com.module.feeds.watch.watchview

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.playcontrol.PlayOrPauseEvent
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.sensor.SensorManagerHelper
import com.common.playcontrol.RemoteControlEvent
import com.common.playcontrol.RemoteControlHelper
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.component.person.utils.StringFromatUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.detail.manager.AbsPlayModeManager
import com.module.feeds.detail.manager.FeedSongPlayModeManager
import com.module.feeds.detail.manager.add2SongPlayModeManager
import com.module.feeds.detail.view.FeedsCommonLyricView
import com.module.feeds.event.FeedDetailChangeEvent
import com.module.feeds.event.FeedDetailSwitchEvent
import com.module.feeds.event.FeedsCollectChangeEvent
import com.module.feeds.statistics.FeedPage
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.*
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsMoreDialogView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.set
import kotlin.properties.Delegates

class FeedRecommendView(val fragment: BaseFragment) : ConstraintLayout(fragment.context), CoroutineScope by MainScope() {

    val TAG = "FeedRecommendView"

    val LYRIC_TYPE = 0
    val AVATAR_TYPE = 1

    val mFeedServerApi = ApiManager.getInstance().createService(FeedsWatchServerApi::class.java)

    val background: SimpleDraweeView
    val recommendFilm: ImageView
    val recordCover: SimpleDraweeView
    val songNameTv: TextView
    val songDescTv: TextView
    val lyricTypesongNameTv: TextView
    val lyricTypesongDescTv: TextView
    val collectIv: ImageView
    val likeNumTv: TextView
    val playLastIv: ImageView
    var recordPlayIv: ImageView? = null
    val playNextIv: ImageView
    val playTimeTv: TextView
    val totalTimeTv: TextView
    val seekBar: SeekBar
    val bottomArea: ExConstraintLayout
    val avatarIv: SimpleDraweeView
    val playNumTv: TextView
    val nameTv: TextView
    val contentTv: TextView
    val swichModeView: View
    val moreIv: ImageView
    val avatarTypeViews: Group
    val lyricTypeViews: Group
    var mFeedsCommonLyricView: FeedsCommonLyricView? = null

    private var mOffset = 0   //偏移量
    private val mCNT = 20  // 默认拉去的个数
    private var hasMore = true // 是否可以加载更多
    private var isSeleted = false  // 是否选中
    private var mHasInitData = false  //关注和推荐是否初始化过数据

    private var mCurModel: FeedsWatchModel? = null

    var mFeedsMoreDialogView: FeedsMoreDialogView? = null

    var mDataList = ArrayList<FeedsWatchModel>()  // list列表

    var mSongPlayModeManager: FeedSongPlayModeManager? = null

    val animatorSet: AnimatorSet

    val playerTag = TAG + hashCode()

    var playCallback = object : PlayerCallbackAdapter() {
        override fun onPrepared() {
            MyLog.d(TAG, "onPrepared")
            if (recordPlayIv!!.isSelected) {
                if (!mFeedsCommonLyricView!!.isStart()) {
                    mFeedsCommonLyricView!!.playLyric()
                } else {
                    mFeedsCommonLyricView!!.resume()
                }
            } else {
                SinglePlayer.pause(playerTag)
            }
        }

        override fun onCompletion() {
            onSongComplete()
        }

        override fun onSeekComplete() {

        }

        override fun onError(what: Int, extra: Int) {
            mFeedsCommonLyricView!!.pause()
        }

        override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
            MyLog.d(TAG, "onBufferingUpdate percent=$percent")
            if (percent == 100) {
                if (SinglePlayer.isPlaying) {
                    mFeedsCommonLyricView!!.resume()
                    playAnimation()
                }
            } else {
                mFeedsCommonLyricView!!.pause()
                pauseAnimation()
            }
        }

        override fun openTimeFlyMonitor(): Boolean {
            return true
        }

        override fun onTimeFlyMonitor(pos: Long, duration: Long) {
            //歌曲还没加载到的时候这个会返回1毫秒，无意义，do not care
            if (pos < 1000) {
                return
            }

            onTimeFly(pos, duration)
        }
    }

    // 保持 init Postion 一致
    var showType: Int by Delegates.observable(AVATAR_TYPE, { _, oldPositon, newPosition ->
        if (newPosition != oldPositon) {
            if (newPosition == LYRIC_TYPE) {
                toLyricType()
            } else {
                toAvatarType()
            }
        }
    })

    fun onTimeFly(pos: Long, duration: Long) {
        playTimeTv.text = U.getDateTimeUtils().formatTimeStringForDate(pos, "mm:ss")
        totalTimeTv.text = U.getDateTimeUtils().formatTimeStringForDate(duration, "mm:ss")
        if (seekBar.max != duration.toInt()) {
            seekBar.max = duration.toInt()
        }
        seekBar.progress = pos.toInt()
        mCurModel?.song?.playDurMsFromPlayerForDebug = duration.toInt()
        mFeedsCommonLyricView?.seekTo(pos.toInt())
        FeedsPlayStatistics.updateCurProgress(pos, duration)
    }

    fun toLyricType() {
        avatarTypeViews.visibility = View.GONE
        lyricTypeViews.visibility = View.VISIBLE
        mFeedsCommonLyricView?.setShowState(View.VISIBLE)
    }

    fun toAvatarType() {
        avatarTypeViews.visibility = View.VISIBLE
        lyricTypeViews.visibility = View.GONE
        mFeedsCommonLyricView?.setShowState(View.GONE)
    }

    fun onSongComplete() {
        if (U.getActivityUtils().isAppForeground) {
            seekBar?.progress = 0
            playTimeTv?.text = "00:00"
            mFeedsCommonLyricView?.seekTo(0)
            mCurModel?.song?.playDurMs?.let {
                totalTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(it.toLong(), "mm:ss")
            }

            startPlay()
        } else {
            goNextSong()
        }
    }

    init {
        View.inflate(context, R.layout.feed_recomend_view_layout, this)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        background = this.findViewById(R.id.background)
        recommendFilm = this.findViewById(R.id.recommend_film)
        recordCover = this.findViewById(R.id.record_cover)
        songNameTv = this.findViewById(R.id.song_name_tv)
        songDescTv = this.findViewById(R.id.song_desc_tv)
        collectIv = this.findViewById(R.id.collect_iv)
        likeNumTv = this.findViewById(R.id.like_num_tv)
        playLastIv = this.findViewById(R.id.play_last_iv)
        recordPlayIv = this.findViewById(R.id.record_play_iv)
        playNextIv = this.findViewById(R.id.play_next_iv)
        playTimeTv = this.findViewById(R.id.play_time_tv)
        totalTimeTv = this.findViewById(R.id.total_time_tv)
        seekBar = this.findViewById(R.id.seek_bar)
        bottomArea = this.findViewById(R.id.bottom_area)
        avatarIv = this.findViewById(R.id.avatar_iv)
        playNumTv = this.findViewById(R.id.play_num_tv)
        nameTv = this.findViewById(R.id.name_tv)
        contentTv = this.findViewById(R.id.content_tv)
        moreIv = this.findViewById(R.id.more_iv)
        swichModeView = this.findViewById(R.id.swich_mode_view)
        avatarTypeViews = this.findViewById(R.id.avatar_type_views)
        lyricTypeViews = this.findViewById(R.id.lyric_type_views)
        lyricTypesongNameTv = this.findViewById(R.id.lyric_type_song_name_tv)
        lyricTypesongDescTv = this.findViewById(R.id.lyric_type_desc_tv)
        mFeedsCommonLyricView = FeedsCommonLyricView(rootView, false)
        mFeedsCommonLyricView?.setShowState(View.GONE)

        mSongPlayModeManager = FeedSongPlayModeManager(FeedSongPlayModeManager.PlayMode.ORDER, null, null)
        mSongPlayModeManager?.supportCycle = false
        mSongPlayModeManager?.loadMoreCallback = { size, callback ->
            if (hasMore) {
                getMoreFeeds {
                    callback.invoke()
                }
            } else {
                callback.invoke()
            }
        }

        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    SinglePlayer.seekTo(playerTag, progress.toLong())
                    mFeedsCommonLyricView?.seekTo(progress)
                    playTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(progress.toLong(), "mm:ss")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        collectIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                mCurModel?.let {
                    collectOrUnCollectFeed(it)
                }
            }
        })

        recordPlayIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                if (recordPlayIv?.isSelected == true) {
                    pausePlay()
                    recordPlayIv?.isSelected = false
                } else {
                    startPlay()
                    recordPlayIv?.isSelected = true
                }
            }
        })

        likeNumTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                mCurModel?.let {
                    feedLike(it)
                }
            }
        })

        swichModeView.setOnClickListener {
            showType = if (showType == LYRIC_TYPE) AVATAR_TYPE else LYRIC_TYPE
        }

        playNextIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                goNextSong()
            }
        })

        playLastIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                mSongPlayModeManager?.getPreSong(true) { song ->
                    if (song == null) {
                        U.getToastUtil().showShort("这已经是第一首歌了")
                    } else {
                        mSongPlayModeManager?.getCurPostionInOrigin()?.let { position ->
                            seekBar.progress = 0
                            playTimeTv?.text = "00:00"
                            song?.let {
                                totalTimeTv.text = U.getDateTimeUtils().formatTimeStringForDate(it.playDurMs.toLong(), "mm:ss")
                                if (seekBar.max != it.playDurMs) {
                                    seekBar.max = it.playDurMs
                                }
                            }
                            if (position in 0 until this@FeedRecommendView.mDataList.size) {
                                bindCurFeedWatchModel(mDataList[position])
                            } else {
                                MyLog.d(TAG, "clickValidposition = $position mDataList.size = ${mDataList.size}")
                            }
                        }
                    }
                }
            }
        })

        avatarIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                openPersonCenter()
            }
        })

        nameTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                openPersonCenter()
            }
        })

        moreIv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                mCurModel?.let {
                    mFeedsMoreDialogView?.dismiss(false)
                    if (it.user?.userID == MyUserInfoManager.getInstance().uid.toInt()) {
                        mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED_HOME, it, true)
                        mFeedsMoreDialogView?.showByDialog()
                    } else {
                        mFeedsMoreDialogView = FeedsMoreDialogView(fragment.activity!!, FeedsMoreDialogView.FROM_FEED_HOME, it, null)
                        mFeedsMoreDialogView?.showByDialog()
                    }
                }
            }
        })

        bottomArea.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                //todo 补充跳到详情的逻辑
                mCurModel?.let {
                    mSongPlayModeManager?.setCurrentPlayModel(mCurModel?.song)
                    FeedsDetailActivity.openActivity(FeedPage.DETAIL_FROM_RECOMMEND, context as Activity, it.feedID, FeedsDetailActivity.TYPE_SWITCH, FeedSongPlayModeManager.PlayMode.ORDER, object : AbsPlayModeManager() {
                        override fun getNextSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                            mSongPlayModeManager?.getNextSong(true) { song ->
                                mSongPlayModeManager?.getCurPostionInOrigin()?.let { pos ->
                                    if (pos in 0 until mDataList.size) {
                                        mCurModel = mDataList[pos]
                                    } else {
                                        MyLog.e(TAG, "getNextSongpos = $pos mDataList.size = ${mDataList.size}")
                                    }
                                }
                                callback(song)
                            }
                        }

                        override fun getPreSong(userAction: Boolean, callback: (songMode: FeedSongModel?) -> Unit) {
                            mSongPlayModeManager?.getPreSong(true) { song ->
                                mSongPlayModeManager?.getCurPostionInOrigin()?.let { pos ->
                                    if (pos in 0 until mDataList.size) {
                                        mCurModel = mDataList[pos]
                                    } else {
                                        MyLog.e(TAG, "getPreSong = $pos mDataList.size = ${mDataList.size}")
                                    }
                                }
                                callback(song)
                            }
                        }

                        override fun playState(isPlaying: Boolean) {

                        }
                    })
                }
            }
        })

        // 初始化动画
        animatorSet = AnimatorSet()
        val recordAnimator = ObjectAnimator.ofFloat(recommendFilm, View.ROTATION, 0f, 360f)
        recordAnimator.duration = 10000
        recordAnimator.interpolator = LinearInterpolator()
        recordAnimator.repeatCount = Animation.INFINITE
        val coverAnimator = ObjectAnimator.ofFloat(recordCover, View.ROTATION, 0f, 360f)
        coverAnimator.duration = 10000
        coverAnimator.interpolator = LinearInterpolator()
        coverAnimator.repeatCount = Animation.INFINITE
        animatorSet.playTogether(recordAnimator, coverAnimator)

        SinglePlayer.addCallback(playerTag, playCallback)
    }

    private fun goNextSong() {
        mSongPlayModeManager?.getNextSong(true) { song ->
            if (song == null) {
                U.getToastUtil().showShort("这已经是最后一首歌了")
            } else {
                mSongPlayModeManager?.getCurPostionInOrigin()?.let { position ->
                    seekBar.progress = 0
                    playTimeTv?.text = "00:00"
                    song.let {
                        totalTimeTv.text = U.getDateTimeUtils().formatTimeStringForDate(it.playDurMs.toLong(), "mm:ss")
                        if (seekBar.max != it.playDurMs) {
                            seekBar.max = it.playDurMs
                        }
                    }
                    if (position in 0 until mDataList.size) {
                        bindCurFeedWatchModel(mDataList[position])
                    } else {
                        MyLog.e(TAG, "clickValidposition = $position  mDataList.size=${mDataList.size}")
                    }
                }
            }
        }
    }

    private fun openPersonCenter() {
        mCurModel?.let {
            val bundle = Bundle()
            bundle.putInt("bundle_user_id", it.user?.userID ?: 0)
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                    .with(bundle)
                    .navigation()
        }
    }


    open fun selected() {
        MyLog.d(TAG, "selected")
        isSeleted = true
        // 该页面选中以及从详情页返回都会回调这个方法
        if (!initFeedList(false)) {
            // 恢复播放
            bindCurFeedWatchModel(mCurModel)
        }
        RemoteControlHelper.register(playerTag)
    }

    open fun unselected(reason: Int) {
        MyLog.d(TAG, "unselected")
        when (reason) {
            UNSELECT_REASON_SLIDE_OUT,
            UNSELECT_REASON_TO_OTHER_ACTIVITY,
            UNSELECT_REASON_TO_OTHER_TAB -> {
                isSeleted = false
                pausePlay()
                RemoteControlHelper.unregister(playerTag)
            }
            UNSELECT_REASON_TO_DESKTOP -> {

            }
        }

    }

    private fun getMoreFeeds(dataOkCallback: (() -> Unit)?) {
        getRecommendFeedList(mOffset, false, dataOkCallback)
    }

    private fun initFeedList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }
        getRecommendFeedList(0, true)
        return true
    }

    private fun getRecommendFeedList(offset: Int, isClear: Boolean, dataOkCallback: (() -> Unit)? = null) {
        launch {
            val obj = subscribe(RequestControl("getRecommendFeedList", ControlType.CancelThis)) {
                mFeedServerApi.getFeedRecommendList(offset, mCNT, MyUserInfoManager.getInstance().uid.toInt())
            }
            if (obj.errno == 0) {
                mHasInitData = true
                val list = JSON.parseArray(obj.data.getString("recommends"), FeedsWatchModel::class.java)
                mOffset = obj.data.getIntValue("offset")
                hasMore = obj.data.getBoolean("hasMore")
                addRecommendWatchList(list, isClear)
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
            dataOkCallback?.invoke()
        }
    }

    private fun addRecommendWatchList(list: List<FeedsWatchModel>?, isClear: Boolean) {
        if (isClear) {
            mDataList.clear()
            if (!list.isNullOrEmpty()) {
                mDataList.addAll(list)
            }
            add2SongPlayModeManager(mSongPlayModeManager, mDataList, isClear)
            if (!mDataList.isNullOrEmpty()) {
                bindCurFeedWatchModel(mDataList[0])
            }
        } else {
            if (!list.isNullOrEmpty()) {
                mDataList.addAll(list)
                add2SongPlayModeManager(mSongPlayModeManager, list, isClear)
            }
        }
    }

    private fun bindCurFeedWatchModel(model: FeedsWatchModel?) {
        this.mCurModel = model

        mCurModel?.let {
            AvatarUtils.loadAvatarByUrl(background, AvatarUtils.newParamsBuilder(it.user?.avatar)
                    .setCornerRadius(16.dp().toFloat())
                    .setBlur(true)
                    .build())
            if (it.song?.needShareTag == true) {
                FrescoWorker.loadImage(recordCover, ImageFactory.newResImage(R.drawable.feed_share_cover_icon)
                        .setCircle(true)
                        .build())
                if (!TextUtils.isEmpty(it.song?.songTpl?.singer)) {
                    songDescTv.visibility = View.VISIBLE
                    songDescTv.text = "演唱/${it.song?.songTpl?.singer}"
                } else {
                    songDescTv.visibility = View.GONE
                }
            } else {
                AvatarUtils.loadAvatarByUrl(recordCover, AvatarUtils.newParamsBuilder(it.user?.avatar)
                        .setCircle(true)
                        .build())
                songDescTv.visibility = View.VISIBLE
                songDescTv.text = "演唱/${UserInfoManager.getInstance().getRemarkName(it.user?.userID
                        ?: 0, it.user?.nickname)}"
            }

            lyricTypesongDescTv.text = songDescTv.text
            lyricTypesongDescTv.visibility = songDescTv.visibility

            //歌名
            if (!TextUtils.isEmpty(it.song?.workName)) {
                songNameTv.visibility = View.VISIBLE
                songNameTv.text = "《${it.song?.workName}》"
            } else {
                songNameTv.visibility = View.GONE
            }

            lyricTypesongNameTv.text = songNameTv.text
            lyricTypesongNameTv.visibility = songNameTv.visibility

            //头像和昵称、评论
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(it.user?.avatar)
                    .setCircle(true)
                    .setBorderWidth(1.dp().toFloat())
                    .setBorderColor(Color.WHITE)
                    .build())
            nameTv.text = UserInfoManager.getInstance().getRemarkName(it.user?.userID
                    ?: 0, it.user?.nickname)
            playNumTv.text = "${StringFromatUtils.formatTenThousand(it.exposure)} 收听"
            // 内容
            var recomendTag = ""
            if (it.song?.needRecommentTag == true) {
                recomendTag = "#小编推荐# "
            }
            var shareTag = ""
            if (it.song?.needShareTag == true) {
                shareTag = "#神曲分享# "
            }
            var songTag = ""
            it.song?.tags?.let { list ->
                for (model in list) {
                    model?.tagDesc.let { tagDesc ->
                        songTag = "$songTag#$tagDesc# "
                    }
                }
            }
            val title = it.song?.title ?: ""
            if (TextUtils.isEmpty(recomendTag) && TextUtils.isEmpty(songTag) && TextUtils.isEmpty(title) && TextUtils.isEmpty(shareTag)) {
                contentTv.visibility = View.GONE
            } else {
                contentTv.visibility = View.VISIBLE
                val stringBuilder = SpanUtils()
                        .append(recomendTag).setForegroundColor(U.getColor(R.color.white_trans_50))
                        .append(shareTag).setForegroundColor(U.getColor(R.color.white_trans_50))
                        .append(songTag).setForegroundColor(U.getColor(R.color.white_trans_50))
                        .append(title).setForegroundColor(U.getColor(R.color.white_trans_80))
                        .create()
                contentTv.text = stringBuilder
            }

            if (mFeedsCommonLyricView?.mFeedSongModel != mCurModel!!.song!!) {
                mFeedsCommonLyricView?.setSongModel(mCurModel!!.song!!, -1)
            }

            // 收藏和喜欢
            refreshCollect()
            refreshLike()
        }

        if (isSeleted) {
            startPlay()
        }
    }

    private fun refreshCollect() {
        mCurModel?.let {
            if (it.isCollected) {
                collectIv.setImageResource(R.drawable.feed_collect_selected_icon)
            } else {
                collectIv.setImageResource(R.drawable.feed_collect_normal_icon)
            }
        }
    }

    private fun refreshLike() {
        mCurModel?.let {
            var drawble = U.getDrawable(R.drawable.feed_like_white_icon)
            if (it.isLiked) {
                drawble = U.getDrawable(R.drawable.feed_like_selected_icon)
            }
            drawble.setBounds(0, 0, 20.dp(), 18.dp())
            likeNumTv.setCompoundDrawables(null, drawble, null, null)
            likeNumTv.text = StringFromatUtils.formatTenThousand(it.starCnt)
        }
    }

    fun autoRefresh() {

    }

    open fun destroy() {
        cancel()
        mFeedsMoreDialogView?.dismiss(false)
        animatorSet.removeAllListeners()
        animatorSet.cancel()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    // 收藏和取消收藏
    private fun collectOrUnCollectFeed(model: FeedsWatchModel) {
        launch {
            val map = HashMap<String, Any>()
            map["feedID"] = model.feedID
            map["like"] = !model.isCollected

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("collectFeed", ControlType.CancelThis)) { mFeedServerApi.collectFeed(body) }
            if (result.errno == 0) {
                model.isCollected = !model.isCollected
                EventBus.getDefault().post(FeedsCollectChangeEvent(model.feedID, model.isCollected))
                if (model.isCollected) {
                    U.getToastUtil().showShort("收藏成功")
                } else {
                    U.getToastUtil().showShort("取消收藏成功")
                }
                refreshCollect()
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

    private fun feedLike(model: FeedsWatchModel) {
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
                refreshLike()
            } else {
                if (obj.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }
        }
    }

    private fun startPlay() {
        mCurModel?.let {
            recordPlayIv?.isSelected = true
//        mRadioView?.play(SinglePlayer.isBufferingOk)
            mCurModel?.song?.playURL?.let {
                FeedsPlayStatistics.setCurPlayMode(mCurModel?.feedID
                        ?: 0, FeedPage.RECOMMEND, 0)
                SinglePlayer.startPlay(playerTag, it)
            }

            if (SinglePlayer.isBufferingOk) {
                if (!mFeedsCommonLyricView!!.isStart()) {
                    mFeedsCommonLyricView?.playLyric()
                } else {
                    mFeedsCommonLyricView?.resume()
                }
            }

            if (showType == LYRIC_TYPE) {
                toLyricType()
            } else {
                toAvatarType()
            }
            playAnimation()
            mSongPlayModeManager?.setCurrentPlayModel(it.song)
        }
    }

    private fun playAnimation() {
        if (animatorSet.isStarted) {
            animatorSet.resume()
        } else {
            animatorSet.start()
        }
    }

    private fun pauseAnimation() {
        animatorSet.pause()
    }

    private fun pausePlay() {
        MyLog.d(TAG, "pausePlay")
        recordPlayIv!!.isSelected = false
//        mRadioView?.pause()
        SinglePlayer.pause(playerTag)
        mFeedsCommonLyricView?.pause()
        pauseAnimation()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedDetailChangeEvent) {
        // 数据要更新了
        event.model?.let {
            updateDetailModel(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RemoteControlEvent) {
        if (SinglePlayer.startFrom == playerTag) {
            goNextSong()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PlayOrPauseEvent) {
        if (SinglePlayer.startFrom == playerTag) {
            if(SinglePlayer.isPlaying){
                pausePlay()
            }else{
                startPlay()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedDetailSwitchEvent) {
        // 数据要更新了
        event.model?.let {
            updateDetailModel(it)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedsCollectChangeEvent) {
        if (mCurModel?.feedID == event.feedID) {
            mCurModel?.isCollected = event.isCollected
            refreshCollect()
        }
    }

    private fun updateDetailModel(it: FeedsWatchModel) {
        for (model in mDataList) {
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

}