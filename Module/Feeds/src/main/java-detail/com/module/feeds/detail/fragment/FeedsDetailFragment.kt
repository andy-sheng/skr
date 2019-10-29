package com.module.feeds.detail.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.base.INVISIBLE_REASON_TO_DESKTOP
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.playcontrol.PlayOrPauseEvent
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.playcontrol.RemoteControlEvent
import com.common.playcontrol.RemoteControlHelper
import com.common.player.SinglePlayerCallbackAdapter
import com.common.statistics.StatisticsAdapter
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.AvatarView
import com.component.person.utils.StringFromatUtils
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.detail.event.AddCommentEvent
import com.module.feeds.detail.event.FeedCommentBoardEvent
import com.module.feeds.detail.inter.IFeedsDetailView
import com.module.feeds.detail.manager.AbsPlayModeManager
import com.module.feeds.detail.manager.FeedSongPlayModeManager
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.detail.presenter.FeedsDetailPresenter
import com.module.feeds.detail.view.FeedCommentMoreDialog
import com.module.feeds.detail.view.FeedsCommentView
import com.module.feeds.detail.view.FeedsCommonLyricView
import com.module.feeds.detail.view.FeedsInputContainerView
import com.module.feeds.event.FeedDetailChangeEvent
import com.module.feeds.event.FeedDetailSwitchEvent
import com.module.feeds.event.FeedLikeChangeEvent
import com.module.feeds.make.make.openFeedsMakeActivityFromChallenge
import com.module.feeds.statistics.FeedPage
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsMoreDialogView
import com.module.feeds.watch.view.FeedsRecordAnimationView
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FeedsDetailFragment : BaseFragment(), IFeedsDetailView {
    val mTag = "FeedsDetailFragment"
    val DELAY_HIDE_CONTROL_AREA = 0
    val HIDE_CONTROL_AREA = 1
    val SHOW_CONTROL_AREA = 2
    val AUTO_CHANGE_SONG = 3
    var mContainer: LinearLayout? = null
    var mAppbar: AppBarLayout? = null
    var mContentLayout: CollapsingToolbarLayout? = null
    var mBlurBg: BaseImageView? = null
    var mXinIv: ExImageView? = null
    var mCollectionIv: ExImageView? = null
    var mCollectionIv2: ExImageView? = null
    var mShareIv: ExImageView? = null
    var mBtnBack: ImageView? = null
    var mPlayTypeIv: ImageView? = null
    var mPlayLastIv: ImageView? = null
    var mPlayNextIv: ImageView? = null
    var mSongNameTv: ExTextView? = null
    var mMoreTv: ExImageView? = null
    var mControlTv: ExImageView? = null
    var mPassTimeTv: ExTextView? = null
    var mLastTimeTv: ExTextView? = null
    var mSeekBar: SeekBar? = null
    var mSingerIv: AvatarView? = null
    var mNameTv: ExTextView? = null
    //    var mCommentTimeTv: ExTextView? = null
    var mFollowTv: ExTextView? = null
    var mMainCommentTv: ExTextView? = null
    var mCommentTv: ExTextView? = null
    var mXinNumTv: ExTextView? = null
    var mShareNumTv: ExTextView? = null
    var mToolbar: Toolbar? = null
    var mToolbarLayout: ConstraintLayout? = null
    var mFeedsCommonLyricView: FeedsCommonLyricView? = null
    var mFeedsCommentView: FeedsCommentView? = null
    var mRadioView: FeedsRecordAnimationView? = null
    var mCommonTitleBar: CommonTitleBar? = null

    var mTagArea: ExConstraintLayout? = null
    var mTagTv: TextView? = null
    var mHitIv: ImageView? = null
    var mShareTag: TextView? = null

    var mFeedsDetailPresenter: FeedsDetailPresenter? = null
    var mMoreDialogPlus: FeedsMoreDialogView? = null
    var mCommentMoreDialogPlus: FeedCommentMoreDialog? = null
    lateinit var mSongControlArea: Group
    var mRefuseModel: FirstLevelCommentModel? = null
    var mSongManager: AbsPlayModeManager? = null

    var mFeedsInputContainerView: FeedsInputContainerView? = null

    var mFeedID: Int = -1   // 外部跳转传入mFeedID
    var mType: Int = -1  // 从外部跳转标记的来源
    var mFrom: FeedPage = FeedPage.UNKNOW
    var mPlayType = FeedSongPlayModeManager.PlayMode.ORDER   // 播放模式，默认顺序播放

    var mFeedsWatchModel: FeedsWatchModel? = null  // 详细的数据model，通过请求去拉

    var mResumeCall: (() -> Unit)? = null

    var sharePanel: SharePanel? = null

    var lastVerticalOffset = Int.MAX_VALUE

    var specialCase: Boolean? = false

    //某一个歌曲被删除了，以防死循环
    var latestAction: (() -> Unit)? = null

    val mUiHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            if (msg?.what == DELAY_HIDE_CONTROL_AREA) {
                showControlArea(false)
                mFeedsCommonLyricView?.showWhole()
            } else if (msg?.what == SHOW_CONTROL_AREA) {
                showControlArea(true)
                mFeedsCommonLyricView?.showHalf()
            } else if (msg?.what == HIDE_CONTROL_AREA) {
                mSongControlArea?.visibility = View.GONE
                if (mType == FeedsDetailActivity.TYPE_SWITCH_MODE) {
                    mPlayTypeIv?.visibility = View.GONE
                }
            } else if (msg?.what == AUTO_CHANGE_SONG) {
                if (latestAction == null) {
                    toNextSongAction(true)
                } else {
                    latestAction?.invoke()
                }
            }
        }
    }

    fun showControlArea(show: Boolean) {
        mUiHandler.removeMessages(HIDE_CONTROL_AREA)
        if (show) {
            mSongControlArea?.visibility = View.VISIBLE
            mUiHandler.removeMessages(DELAY_HIDE_CONTROL_AREA)
            mUiHandler.sendEmptyMessageDelayed(DELAY_HIDE_CONTROL_AREA, 5000)
            val animator1 = ObjectAnimator.ofFloat(mControlTv, "alpha", 0f, 1f)
            val animator2 = ObjectAnimator.ofFloat(mPlayLastIv, "alpha", 0f, 1f)
            val animator3 = ObjectAnimator.ofFloat(mPlayNextIv, "alpha", 0f, 1f)
            val animator5 = ObjectAnimator.ofFloat(mCollectionIv2, "alpha", 0f, 1f)
            val animSet = AnimatorSet()
            animSet.play(animator1).with(animator2).with(animator3).with(animator5)

            if (mType == FeedsDetailActivity.TYPE_SWITCH_MODE) {
                val animator4 = ObjectAnimator.ofFloat(mPlayTypeIv, "alpha", 0f, 1f)
                animSet.play(animator1).with(animator4)
                mPlayTypeIv?.visibility = View.VISIBLE
            }

            animSet.setDuration(300)
            animSet.start()

        } else {
            val animator1 = ObjectAnimator.ofFloat(mControlTv, "alpha", 1f, 0f)
            val animator2 = ObjectAnimator.ofFloat(mPlayLastIv, "alpha", 1f, 0f)
            val animator3 = ObjectAnimator.ofFloat(mPlayNextIv, "alpha", 1f, 0f)
            val animator5 = ObjectAnimator.ofFloat(mCollectionIv2, "alpha", 1f, 0f)
            val animSet = AnimatorSet()
            animSet.play(animator1).with(animator2).with(animator3).with(animator5)
            if (mType == FeedsDetailActivity.TYPE_SWITCH_MODE) {
                val animator4 = ObjectAnimator.ofFloat(mPlayTypeIv, "alpha", 1f, 0f)
                animSet.play(animator1).with(animator4)
            }

            animSet.setDuration(300)
            animSet.start()
            mUiHandler.sendEmptyMessageDelayed(HIDE_CONTROL_AREA, 300)
        }
    }

    var playCallback = object : SinglePlayerCallbackAdapter() {
        override fun onPrepared() {
            MyLog.d(mTag, "onPrepared")
            if (mControlTv!!.isSelected) {
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
            if (mType == FeedsDetailActivity.TYPE_SWITCH_MODE) {
                if (specialCase ?: false) {
                    mFeedsWatchModel?.let {
                        showFeedsWatchModel(it)
                    }
                } else {
                    toNextSongAction(false)
                }
            } else if (mType == FeedsDetailActivity.TYPE_SWITCH) {
                mFeedsWatchModel?.let {
                    showFeedsWatchModel(it)
                }
            } else if (mType == FeedsDetailActivity.TYPE_NO) {
                mFeedsWatchModel?.let {
                    showFeedsWatchModel(it)
                }
            }
        }

        override fun onSeekComplete() {

        }

        override fun onError(what: Int, extra: Int) {
            mFeedsCommonLyricView!!.pause()
        }

        override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
            MyLog.d(mTag, "onBufferingUpdate percent=$percent")
            if (percent == 100) {
                if (SinglePlayer.isPlaying) {
                    mFeedsCommonLyricView!!.resume()
                    mRadioView?.bufferEnd()
                }
            } else {
                mFeedsCommonLyricView!!.pause()
                mRadioView?.buffering()
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

            mPassTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(pos, "mm:ss")
            mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(duration, "mm:ss")
            if (mSeekBar?.max != duration.toInt()) {
                mSeekBar?.max = duration.toInt()
            }
            mSeekBar!!.progress = pos.toInt()
            mFeedsWatchModel?.song?.playDurMsFromPlayerForDebug = duration.toInt()
            mFeedsCommonLyricView?.seekTo(pos.toInt())
            FeedsPlayStatistics.updateCurProgress(pos, duration)
        }
    }

    val playerTag = TAG + hashCode()

    internal var isInitToolbar = false

    override fun initView(): Int {
        return R.layout.feeds_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        StatisticsAdapter.recordCountEvent("music_recommend", "detail_page", null)
        if (mFeedID == -1) {
            activity?.finish()
            return
        }
        mContainer = rootView.findViewById(R.id.container)
        mCommonTitleBar = rootView.findViewById(R.id.titlebar)
        mAppbar = rootView.findViewById(R.id.appbar)
        mContentLayout = rootView.findViewById(R.id.content_layout)
        mBlurBg = rootView.findViewById(R.id.blur_bg)
        mBtnBack = rootView.findViewById(R.id.btn_back) as ImageView
        mSongNameTv = rootView.findViewById(R.id.song_name_tv)
        mMoreTv = rootView.findViewById(R.id.more_iv)
        mPlayTypeIv = rootView.findViewById(R.id.play_type_iv)

        mPassTimeTv = rootView.findViewById(R.id.pass_time_tv)
        mLastTimeTv = rootView.findViewById(R.id.last_time_tv)
        mSeekBar = rootView.findViewById(R.id.seek_bar)
        mSingerIv = rootView.findViewById(R.id.singer_iv)
        mNameTv = rootView.findViewById(R.id.name_tv)
//        mCommentTimeTv = rootView.findViewById(R.id.comment_time_tv)
        mFollowTv = rootView.findViewById(R.id.follow_tv)
        mMainCommentTv = rootView.findViewById(R.id.main_comment_tv)
        mToolbar = rootView.findViewById(R.id.toolbar)
        mToolbarLayout = rootView.findViewById(R.id.toolbar_layout)
        mCommentTv = rootView.findViewById(R.id.comment_tv)
        mXinIv = rootView.findViewById(R.id.xin_iv)
        mXinNumTv = rootView.findViewById(R.id.xin_num_tv)
        mShareIv = rootView.findViewById(R.id.share_iv)
        mShareNumTv = rootView.findViewById(R.id.share_num_tv)
        mFeedsInputContainerView = rootView.findViewById(R.id.feeds_input_container_view)
        mRadioView = rootView.findViewById(R.id.radio_view)
        mFeedsCommonLyricView = FeedsCommonLyricView(rootView, true)
        mFeedsCommentView = rootView.findViewById(R.id.feedsCommentView)
        mCollectionIv = rootView.findViewById(R.id.collection_iv)
        mCollectionIv2 = rootView.findViewById(R.id.collection_iv_2)

        mTagArea = rootView.findViewById(R.id.tag_area)
        mTagTv = rootView.findViewById(R.id.tag_tv)
        mHitIv = rootView.findViewById(R.id.hit_iv)
        mShareTag = rootView.findViewById(R.id.share_tag)

        mSongControlArea = rootView.findViewById(R.id.song_control_arae)
        mPlayLastIv = rootView.findViewById(R.id.play_last_iv)
        mPlayNextIv = rootView.findViewById(R.id.play_next_iv)
        mControlTv = rootView.findViewById(R.id.control_tv)
        mFeedsDetailPresenter = FeedsDetailPresenter(this)
        addPresent(mFeedsDetailPresenter)

        //todo 先去掉打榜和收藏
        mHitIv?.visibility = View.GONE
        mCollectionIv?.visibility = View.GONE
        mCollectionIv2?.visibility = View.GONE

        if (mSongManager != null) {
            launch {
                // 读收藏
                mSongControlArea.visibility = View.VISIBLE
                mPlayLastIv?.setDebounceViewClickListener {
                    mUiHandler.removeMessages(AUTO_CHANGE_SONG)
                    toPreSongAction(true)
                }

                mPlayNextIv?.setDebounceViewClickListener {
                    mUiHandler.removeMessages(AUTO_CHANGE_SONG)
                    toNextSongAction(true)
                }

                mBlurBg?.setOnClickListener {
                    if (mSongControlArea?.visibility == View.VISIBLE) {
                        mUiHandler.sendEmptyMessage(DELAY_HIDE_CONTROL_AREA)
                    } else {
                        mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
                    }
                }

                if (mType == FeedsDetailActivity.TYPE_SWITCH_MODE) {
                    mPlayTypeIv?.setOnClickListener {
                        when (mSongManager?.getCurMode()) {
                            FeedSongPlayModeManager.PlayMode.ORDER -> {
                                mPlayType = FeedSongPlayModeManager.PlayMode.SINGLE
                                mPlayTypeIv?.setImageResource(R.drawable.like_single_repeat_icon)
                                mSongManager?.changeMode(FeedSongPlayModeManager.PlayMode.SINGLE)
                            }
                            FeedSongPlayModeManager.PlayMode.SINGLE -> {
                                mPlayType = FeedSongPlayModeManager.PlayMode.RANDOM
                                mPlayTypeIv?.setImageResource(R.drawable.like_random_icon)
                                mSongManager?.changeMode(FeedSongPlayModeManager.PlayMode.RANDOM)
                            }
                            FeedSongPlayModeManager.PlayMode.RANDOM -> {
                                mPlayType = FeedSongPlayModeManager.PlayMode.ORDER
                                mPlayTypeIv?.setImageResource(R.drawable.like_all_repeat_icon)
                                mSongManager?.changeMode(FeedSongPlayModeManager.PlayMode.ORDER)
                            }
                        }
                    }

                    when (mPlayType) {
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

                mFeedsCommonLyricView?.mAutoScrollLyricView?.lyricTv?.setOnClickListener {
                    if (mSongControlArea?.visibility == View.VISIBLE) {
                        mUiHandler.sendEmptyMessage(DELAY_HIDE_CONTROL_AREA)
                    } else {
                        mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
                    }
                }

                mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
            }
        } else {
            mSongControlArea.visibility = View.GONE
        }

        mControlTv?.setDebounceViewClickListener { view ->
            mFeedsWatchModel?.let {
                if (view!!.isSelected) {
                    pausePlay(true)
                } else {
                    startPlay()
                }
            }
        }

        mFeedsInputContainerView?.mSendCallBack = { s ->
            if (mRefuseModel == null) {
                mFeedsDetailPresenter?.addComment(s, mFeedsWatchModel!!.feedID)
                val behavior = ((mAppbar?.getLayoutParams()) as ((CoordinatorLayout.LayoutParams))).behavior
                if (behavior is AppBarLayout.Behavior) {
                    val topAndBottomOffset = behavior.getTopAndBottomOffset();
                    if (-U.getDisplayUtils().dip2px(460f) > topAndBottomOffset) {
                        behavior.setTopAndBottomOffset(-U.getDisplayUtils().dip2px(460f))
                    }
                    mFeedsCommentView?.mRecyclerView?.scrollToPosition(0)
                }
            } else {
                mFeedsDetailPresenter?.refuseComment(s, mFeedsWatchModel!!.feedID, mRefuseModel!!.comment.commentID, mRefuseModel!!) {
                    EventBus.getDefault().post(AddCommentEvent(mRefuseModel!!.comment.commentID))
                }
            }
        }

        mToolbar?.visibility = View.GONE
        mAppbar?.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (lastVerticalOffset != verticalOffset) {
                lastVerticalOffset = verticalOffset
                var srollLimit = appBarLayout.totalScrollRange - U.getDisplayUtils().dip2px(95f)
                if (U.getDeviceUtils().hasNotch(U.app())) {
                    srollLimit = srollLimit - U.getStatusBarUtil().getStatusBarHeight(U.app())
                }
                if (verticalOffset == 0) {
                    // 展开状态
                    if (mToolbar?.getVisibility() != View.GONE) {
                        mToolbar?.setVisibility(View.GONE)
                    }
                } else if (Math.abs(verticalOffset) >= srollLimit) {
                    // 完全收缩状态
                    if (mToolbar?.getVisibility() != View.VISIBLE) {
                        if (U.getDeviceUtils().hasNotch(U.app()) && !isInitToolbar) {
                            val params = mToolbarLayout?.layoutParams
                            params!!.height = params.height + U.getStatusBarUtil().getStatusBarHeight(U.app())
                            mToolbarLayout?.layoutParams = params
                            isInitToolbar = true
                        }
                        mToolbar?.setVisibility(View.VISIBLE)
                    }
                } else {
                    // TODO: 2019/4/8 过程中，可以加动画，先直接显示
                    if (mToolbar?.getVisibility() != View.GONE) {
                        mToolbar?.setVisibility(View.GONE)
                    }
                }
            }
        }

        mCommonTitleBar?.leftTextView?.setDebounceViewClickListener {
            activity?.finish()
        }

        mBtnBack?.setDebounceViewClickListener {
            activity?.finish()
        }

        mShareIv?.setDebounceViewClickListener {
            if (mFeedsWatchModel == null) {
                return@setDebounceViewClickListener
            }

            sharePanel?.setUMShareListener(null)
            sharePanel = SharePanel(activity)
            sharePanel?.apply {
                mShareImage = mFeedsWatchModel?.user?.avatar
                        ?: ""
                mTitle = mFeedsWatchModel!!.song?.workName
                mDes = mFeedsWatchModel!!.user?.nickname
                mUrl = (String.format("http://www.skrer.mobi/feed/song?songID=%d&userID=%d", mFeedsWatchModel!!.song?.songID, mFeedsWatchModel!!.user?.userId))
                mPlayMusicUrl = mFeedsWatchModel?.song?.playURL
            }

            sharePanel?.show(ShareType.MUSIC)
            sharePanel?.setUMShareListener(object : UMShareListener {
                override fun onResult(p0: SHARE_MEDIA?) {

                }

                override fun onCancel(p0: SHARE_MEDIA?) {

                }

                override fun onError(p0: SHARE_MEDIA?, p1: Throwable?) {

                }

                override fun onStart(p0: SHARE_MEDIA?) {
                    mFeedsWatchModel?.shareCnt = mFeedsWatchModel!!.shareCnt.plus(1)
                    mShareNumTv?.text = StringFromatUtils.formatTenThousand(mFeedsWatchModel!!.shareCnt!!)
                    mFeedsDetailPresenter?.addShareCount(MyUserInfoManager.uid.toInt(), mFeedsWatchModel?.feedID
                            ?: 0)
                }
            })
        }

        mMoreTv?.setDebounceViewClickListener {
            mFeedsWatchModel?.let {
                showMoreOp()
            }
        }

        mCommonTitleBar?.rightImageButton?.setDebounceViewClickListener {
            mFeedsWatchModel?.let {
                showMoreOp()
            }
        }

        mCollectionIv?.setDebounceViewClickListener {
            mFeedsWatchModel?.let {
                mFeedsDetailPresenter?.collection(!it.isCollected, it.feedID)
            }
        }

        mCollectionIv2?.setDebounceViewClickListener {
            mFeedsWatchModel?.let {
                mFeedsDetailPresenter?.collection(!it.isCollected, it.feedID)
            }
        }

        mFeedsCommentView?.mClickContentCallBack = {
            showCommentOp(it)
        }

        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    SinglePlayer.seekTo(playerTag, progress.toLong())
                    mFeedsCommonLyricView?.seekTo(progress)
                    mPassTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(progress.toLong(), "mm:ss")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        mRadioView?.avatarContainer?.setDebounceViewClickListener {
            mFeedsWatchModel?.let {
                mControlTv?.callOnClick()
            }
        }

        mSingerIv?.setDebounceViewClickListener {
            mFeedsWatchModel?.let {
                val bundle = Bundle()
                bundle.putInt("bundle_user_id", mFeedsWatchModel?.user?.userId ?: 0)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                        .with(bundle)
                        .navigation()
            }
        }

        SinglePlayer.addCallback(playerTag, playCallback)
        mFeedsDetailPresenter?.getFeedsWatchModel(MyUserInfoManager.uid.toInt(), mFeedID)
        if (needContinueByFrom()) {
            RemoteControlHelper.registerHeadsetControl(playerTag)
        }
    }

    private fun needContinueByFrom(): Boolean {
        if (mFrom == FeedPage.DETAIL_FROM_RECOMMEND
                || mFrom == FeedPage.DETAIL_FROM_COLLECT
                || mFrom == FeedPage.DETAIL_FROM_SONG_ALBUM_OP
                || mFrom == FeedPage.DETAIL_FROM_SONG_ALBUM_RANK) {
            return true
        }
        return false
    }

    private fun toNextSongAction(userAction: Boolean) {
        if (!userAction) {
            if (mType == FeedsDetailActivity.TYPE_SWITCH_MODE) {
                mSongManager?.getNextSong(userAction) { newModel ->
                    if (newModel == null) {
                        latestAction = null
                        U.getToastUtil().showShort("这已经是最后一首歌了")
                    } else {
                        newModel?.feedID?.let {
                            tryLoadNewFeed(it)
                            mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
                        }
                        latestAction = {
                            toNextSongAction(true)
                        }
                    }
                }
            } else {
                tryLoadNewFeed(mFeedsWatchModel!!.feedID)
            }
        } else {
            mSongManager?.getNextSong(userAction) { newModel ->
                if (newModel == null) {
                    latestAction = null
                    U.getToastUtil().showShort("这已经是最后一首歌了")
                } else {
                    newModel?.feedID?.let {
                        tryLoadNewFeed(it)
                        mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
                    }
                    latestAction = {
                        toNextSongAction(true)
                    }
                }
            }
        }
    }

    private fun toPreSongAction(userAction: Boolean) {
        mSongManager?.getPreSong(userAction) { newModel ->
            if (newModel == null) {
                U.getToastUtil().showShort("这已经是第一首歌了")
                latestAction = null
            } else {
                newModel?.feedID?.let {
                    tryLoadNewFeed(it)
                    mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
                }

                latestAction = {
                    toPreSongAction(true)
                }
            }
        }
    }

    private fun tryLoadNewFeed(newFeedId: Int) {
        if (newFeedId != mFeedID) {
            mFeedID = newFeedId
            mFeedsDetailPresenter?.getFeedsWatchModel(MyUserInfoManager.uid.toInt(), mFeedID)
        } else {
            mSeekBar?.progress = 0
            mPassTimeTv?.text = "00:00"
            mFeedsWatchModel?.song?.playDurMs?.let {
                mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(it.toLong(), "mm:ss")
            }
            startPlay()
        }
    }

    private fun setModelData() {
        mMoreDialogPlus?.dismiss()
        mSeekBar?.max = mFeedsWatchModel?.song?.playDurMs ?: 60 * 1000
        mSeekBar?.progress = 0
        mPassTimeTv?.text = "00:00"
        mFeedsCommentView?.setFeedsID(mFeedsWatchModel!!)
        mFeedsWatchModel?.song?.workName?.let {
            mSongNameTv?.text = it
            mCommonTitleBar?.centerTextView?.text = "正在播放《${it}》"
        }

        mFeedsCommonLyricView?.setSongModel(mFeedsWatchModel!!.song!!, -1)
        AvatarUtils.loadAvatarByUrl(mBlurBg, AvatarUtils.newParamsBuilder(mFeedsWatchModel?.user?.avatar)
                .setCircle(false)
                .setBlur(true)
                .build())

        if (mSongControlArea?.visibility == View.GONE) {
            mFeedsCommonLyricView?.showWhole()
        } else {
            mFeedsCommonLyricView?.showHalf()
        }

        mFeedsWatchModel?.song?.playDurMs?.let {
            mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(it.toLong(), "mm:ss")
        }


        mSingerIv?.bindData(mFeedsWatchModel?.user)

        mNameTv?.text = UserInfoManager.getInstance().getRemarkName(mFeedsWatchModel?.user?.userId
                ?: 0, mFeedsWatchModel?.user?.nickname)
//        mFeedsWatchModel?.song?.createdAt?.let {
//            mCommentTimeTv?.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(it, System.currentTimeMillis())
//        }

        showHitArea()
        showMainComment()

        mCommentTv?.setDebounceViewClickListener {
            mRefuseModel = null
            mFeedsInputContainerView?.showSoftInput()
            mFeedsInputContainerView?.setETHint("回复 ${mFeedsWatchModel?.user?.nickname}")
        }

        mFollowTv?.visibility = if (mFeedsWatchModel?.user?.userId != MyUserInfoManager.uid.toInt()) View.VISIBLE else View.GONE

        mFeedsWatchModel?.user?.avatar?.let {
            mRadioView?.setAvatar(it, mFeedsWatchModel?.song?.needShareTag == true)
        }
        mShareNumTv?.text = StringFromatUtils.formatTenThousand(mFeedsWatchModel!!.shareCnt)
        mXinNumTv?.text = StringFromatUtils.formatTenThousand(mFeedsWatchModel!!.starCnt)
        mXinIv?.isSelected = mFeedsWatchModel!!.isLiked!!
        mFeedsCommentView?.feedsCommendAdapter?.mCommentNum = mFeedsWatchModel?.commentCnt!!

        mCollectionIv?.isSelected = mFeedsWatchModel?.isCollected == true
        mCollectionIv2?.isSelected = mFeedsWatchModel?.isCollected == true

        mFeedsDetailPresenter?.getRelation(mFeedsWatchModel!!.user!!.userId)

    }

    private fun showHitArea() {
        mHitIv?.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                SinglePlayer.reset(playerTag)
                openFeedsMakeActivityFromChallenge(mFeedsWatchModel?.song?.challengeID)
            }
        })

        mTagArea?.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                // 排行榜详情
                mFeedsWatchModel?.let {
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_FEEDS_RANK_DETAIL)
                            .withString("rankTitle", it.rank?.rankTitle)
                            .withLong("challengeID", it.song?.challengeID ?: 0L)
                            .withLong("challengeCnt", it.challengeCnt.toLong())
                            .navigation()
                }
            }
        })

        // 神曲分享为第一优先级
        if (mFeedsWatchModel?.song?.needShareTag == true) {
            mTagArea?.visibility = View.GONE
            mHitIv?.visibility = View.GONE
            var singler = ""
            if (!TextUtils.isEmpty(mFeedsWatchModel?.song?.songTpl?.singer)) {
                singler = " 演唱/${mFeedsWatchModel?.song?.songTpl?.singer}"
            }
            mShareTag?.visibility = View.VISIBLE
            mShareTag?.text = "#神曲分享#$singler"
        } else {
            mShareTag?.visibility = View.GONE
            if (mFeedsWatchModel?.song?.needChallenge == true) {
                //打榜歌曲
                mHitIv?.visibility = View.VISIBLE
                if (mFeedsWatchModel?.rank != null) {
                    if (TextUtils.isEmpty(mFeedsWatchModel?.rank?.rankDesc)) {
                        mTagArea?.visibility = View.GONE
                    } else {
                        mTagTv?.text = mFeedsWatchModel?.rank?.rankDesc
                        mTagArea?.visibility = View.VISIBLE
                    }
                } else {
                    mTagArea?.visibility = View.GONE
                }
            } else {
                //非打榜歌曲
                mHitIv?.visibility = View.GONE
                mTagArea?.visibility = View.GONE
            }
        }
    }

    private fun showMainComment() {
        var recomendTag = ""
        if (mFeedsWatchModel?.song?.needRecommentTag == true) {
            recomendTag = "#小编推荐# "
        }
        var songTag = ""
        mFeedsWatchModel?.song?.tags?.let {
            for (model in it) {
                model?.tagDesc.let { tagDesc ->
                    songTag = "$songTag#$tagDesc# "
                }
            }
        }
        val title = mFeedsWatchModel?.song?.title ?: ""
        if (TextUtils.isEmpty(recomendTag) && TextUtils.isEmpty(songTag) && TextUtils.isEmpty(title)) {
            mMainCommentTv?.visibility = View.GONE
        } else {
            val stringBuilder = SpanUtils()
                    .append(recomendTag).setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append(songTag).setForegroundColor(U.getColor(R.color.black_trans_50))
                    .append(title).setForegroundColor(U.getColor(R.color.black_trans_80))
                    .create()
            mMainCommentTv?.visibility = View.VISIBLE
            mMainCommentTv?.text = stringBuilder
        }
    }

    private fun showMoreOp() {
        dismissDialog()
        mFeedsWatchModel?.let { model ->
            activity?.let {
                mMoreDialogPlus = FeedsMoreDialogView(it, FeedsMoreDialogView.FROM_FEED_DETAIL, model, null)
                mMoreDialogPlus?.showByDialog()
            }
        }
    }

    private fun showCommentOp(model: FirstLevelCommentModel) {
        dismissDialog()
        activity?.let {
            mCommentMoreDialogPlus = FeedCommentMoreDialog(it, model)
                    .apply {
                        // 重新下回复
                        mReplyTv.setOnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                dismiss()
                                mRefuseModel = model
                                mFeedsInputContainerView?.showSoftInput()
                                mFeedsInputContainerView?.setETHint("回复 ${model.commentUser.nickname}")
                            }
                        })
                    }
            mCommentMoreDialogPlus?.showByDialog()
        }
    }

    override fun addCommentSuccess(model: FirstLevelCommentModel) {
        mFeedsCommentView?.feedsCommendAdapter?.mCommentNum = mFeedsCommentView?.feedsCommendAdapter?.mCommentNum!!.plus(1)
        mFeedsWatchModel?.commentCnt = mFeedsCommentView?.feedsCommendAdapter?.mCommentNum!!
        mFeedsCommentView?.addSelfComment(model)
    }

    override fun likeFeed(like: Boolean) {
        mXinIv!!.isSelected = like
        if (like) {
            mFeedsWatchModel!!.starCnt++
        } else {
            mFeedsWatchModel!!.starCnt--
        }
        EventBus.getDefault().post(FeedLikeChangeEvent(mFeedsWatchModel!!.feedID, like))

        mFeedsWatchModel?.isLiked = like
        mXinNumTv?.text = StringFromatUtils.formatTenThousand(mFeedsWatchModel!!.starCnt)
    }

    override fun onResume() {
        super.onResume()
        mResumeCall?.invoke()
        mResumeCall = null
    }

    override fun collectFinish(c: Boolean) {
        mCollectionIv?.isSelected = c
        mCollectionIv2?.isSelected = c
        mFeedsWatchModel?.isCollected = c
    }

    override fun showFeedsWatchModel(model: FeedsWatchModel) {
        mFeedsWatchModel = model
        EventBus.getDefault().post(FeedDetailSwitchEvent(mFeedsWatchModel))

        mXinIv?.setDebounceViewClickListener {
            mFeedsDetailPresenter?.likeFeeds(!mXinIv!!.isSelected, mFeedsWatchModel!!.feedID)
        }

        setModelData()
        startPlay()

        //todo 先去掉打榜和收藏
        mHitIv?.visibility = View.GONE
        mCollectionIv?.visibility = View.GONE
        mCollectionIv2?.visibility = View.GONE
    }

    override fun finishWithModelError() {
        mUiHandler.removeMessages(AUTO_CHANGE_SONG)
        if (mSongManager == null) {
//            activity?.finish()
            MyLog.d(TAG, "finishWithModelError mSongManager == null")
        } else {
            mUiHandler.sendEmptyMessageDelayed(AUTO_CHANGE_SONG, 1000);
        }
    }

    override fun showRelation(isBlacked: Boolean, isFollow: Boolean, isFriend: Boolean) {
        if (isFriend) {
            isFriendState()
            mFollowTv?.setOnClickListener(null)
        } else if (isFollow) {
            isFollowState()
            mFollowTv?.setOnClickListener(null)
        } else {
            isStrangerState()
        }
    }

    private fun startPlay() {
        mControlTv?.isSelected = true
        mRadioView?.play(SinglePlayer.isBufferingOk)
        mFeedsWatchModel?.song?.playURL?.let {
            FeedsPlayStatistics.setCurPlayMode(mFeedsWatchModel?.feedID ?: 0, mFrom, 0)
            SinglePlayer.startPlay(playerTag, it)
        }

        if (SinglePlayer.isBufferingOk) {
            if (!mFeedsCommonLyricView!!.isStart()) {
                mFeedsCommonLyricView?.playLyric()
            } else {
                mFeedsCommonLyricView?.resume()
            }
        }
        mSongManager?.playState(true)
    }

    private fun pausePlay(userAction: Boolean) {
        MyLog.d(mTag, "pausePlay")
        mControlTv!!.isSelected = false
        mRadioView?.pause()
        SinglePlayer.pause(playerTag)
        mFeedsCommonLyricView?.pause()
        if (userAction) {
            mSongManager?.playState(false)
        }
    }

//    private fun stopSong() {
//        MyLog.d(mTag, "stopSong()")
//        SinglePlayer.stop(playerTag)
//        mControlTv!!.isSelected = false
//        mRadioView?.pause()
//        mSeekBar!!.progress = 0
//        mPassTimeTv?.text = "00:00"
//        mFeedsWatchModel?.song?.playDurMs?.let {
//            mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(it.toLong(), "mm:ss")
//        }
//        mFeedsCommonLyricView?.stop()
//        mSongManager?.playState(false)
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (mFeedsWatchModel?.user?.userId == event.useId) {
            if (event.isFriend) {
                isFriendState()
                mFollowTv?.setOnClickListener(null)
            } else if (event.isFollow) {
                isFollowState()
                mFollowTv?.setOnClickListener(null)
            } else {
                isStrangerState()
            }
        }
    }

    override fun onFragmentInvisible(reason: Int) {
        super.onFragmentInvisible(reason)

        MyLog.d(TAG, "onFragmentInvisiblereason = $reason, mFrom is $mFrom")
        if (needContinueByFrom()) {
            //如果是从mFrom == FeedPage.DETAIL_FROM_COLLECT || mFrom == FeedPage.DETAIL_FROM_RECOMMEND  这两个渠道进来的，特殊处理
            if (reason == INVISIBLE_REASON_TO_DESKTOP) {
                //如果是退到后台，不需要做什么
            } else {
                //如果是跳转别的界面，保持之前的逻辑
                pauseWhenInvisible()
            }
        } else {
            //如果是别的渠道进来的
            pauseWhenInvisible()
        }
    }

    private fun pauseWhenInvisible() {
        if (SinglePlayer.isPlaying) {
            mResumeCall = {
                startPlay()
            }
        } else {
            mResumeCall = null
        }

        pausePlay(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedCommentBoardEvent) {
        specialCase = event.showing
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RemoteControlEvent) {
        // 不在前台 或者 详情页在前台
        if (SinglePlayer.startFrom == playerTag && (!U.getActivityUtils().isAppForeground || U.getActivityUtils().topActivity == activity)) {
            mFeedsInputContainerView?.hideSoftInput()
            toNextSongAction(true)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PlayOrPauseEvent) {
        // 不在前台 或者 详情页在前台
        if (SinglePlayer.startFrom == playerTag && (!U.getActivityUtils().isAppForeground || U.getActivityUtils().topActivity == activity)) {
            if (SinglePlayer.isPlaying) {
                pausePlay(true)
            } else {
                startPlay()
            }
        }
    }

    fun isFriendState() {
        val followState = DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                .setSolidColor(U.getColor(R.color.white))
                .setStrokeColor(Color.parseColor("#AD6C00"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .build()

        mFollowTv?.text = "已互关"
        mFollowTv?.background = followState
        mFollowTv?.setTextColor(Color.parseColor("#AD6C00"))
    }

    fun isFollowState() {
        val followState = DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                .setSolidColor(U.getColor(R.color.white))
                .setStrokeColor(Color.parseColor("#AD6C00"))
                .setStrokeWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                .build()

        mFollowTv?.text = "已关注"
        mFollowTv?.background = followState
        mFollowTv?.setTextColor(Color.parseColor("#AD6C00"))
    }

    fun isStrangerState() {
        val followState = DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                .setSolidColor(Color.parseColor("#FFC15B"))
                .build()

        mFollowTv?.text = "+关注"
        mFollowTv?.background = followState
        mFollowTv?.setTextColor(Color.parseColor("#AD6C00"))
        mFollowTv?.setDebounceViewClickListener {
            UserInfoManager.getInstance().mateRelation(mFeedsWatchModel!!.user!!.userId, UserInfoManager.RA_BUILD, false, 0, null)
        }
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mFeedID = (data as Int?) ?: -1
        } else if (type == 1) {
            mType = (data as Int?) ?: -1
        } else if (type == 2) {
            mPlayType = (data as FeedSongPlayModeManager.PlayMode?)
                    ?: FeedSongPlayModeManager.PlayMode.ORDER
        } else if (type == 3) {
            data?.let {
                mSongManager = data as AbsPlayModeManager
            }
        } else if (type == 4) {
            data?.let {
                mFrom = data as FeedPage
            }
        }
    }

    fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                click(v)
            }
        })
    }

    override fun isBlackStatusBarText(): Boolean = true

    override fun useEventBus(): Boolean {
        return true
    }
    private fun dismissDialog() {
        mMoreDialogPlus?.dismiss(false)
        mCommentMoreDialogPlus?.dismiss(false)
    }

    override fun destroy() {
        super.destroy()
        dismissDialog()
        SinglePlayer.removeCallback(playerTag)
        mFeedsCommonLyricView?.destroy()
        mFeedsCommentView?.destroy()
        sharePanel?.setUMShareListener(null)
        mSongManager = null
        RemoteControlHelper.unregisterHeadsetControl(playerTag)
        EventBus.getDefault().post(FeedDetailChangeEvent(mFeedsWatchModel?.apply {
            commentCnt = mFeedsCommentView?.feedsCommendAdapter?.mCommentNum ?: 0
        }))
    }
}