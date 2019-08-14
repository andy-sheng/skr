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
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.player.PlayerCallbackAdapter
import com.common.player.SinglePlayer
import com.common.statistics.StatisticsAdapter
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.titlebar.CommonTitleBar
import com.component.person.utils.StringFromatUtils
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.FeedSongPlayModeManager
import com.module.feeds.detail.activity.FeedsDetailActivity
import com.module.feeds.detail.event.AddCommentEvent
import com.module.feeds.detail.event.FeedCommentBoardEvent
import com.module.feeds.detail.inter.IFeedsDetailView
import com.module.feeds.detail.model.FirstLevelCommentModel
import com.module.feeds.detail.presenter.FeedsDetailPresenter
import com.module.feeds.detail.view.FeedCommentMoreDialog
import com.module.feeds.detail.view.FeedsCommentView
import com.module.feeds.detail.view.FeedsCommonLyricView
import com.module.feeds.detail.view.FeedsInputContainerView
import com.module.feeds.event.FeedDetailChangeEvent
import com.module.feeds.statistics.FeedsPlayStatistics
import com.module.feeds.watch.manager.FeedCollectManager
import com.module.feeds.watch.model.FeedSongModel
import com.module.feeds.watch.model.FeedsWatchModel
import com.module.feeds.watch.view.FeedsMoreDialogView
import com.module.feeds.watch.view.FeedsRecordAnimationView
import com.umeng.socialize.UMShareListener
import com.umeng.socialize.bean.SHARE_MEDIA
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FeedsDetailFragment : BaseFragment(), IFeedsDetailView {
    val mTag = "FeedsDetailFragment"
    val DELAY_HIDE_CONTROL_AREA = 0
    val HIDE_CONTROL_AREA = 1
    val SHOW_CONTROL_AREA = 2
    var mContainer: LinearLayout? = null
    var mAppbar: AppBarLayout? = null
    var mContentLayout: CollapsingToolbarLayout? = null
    var mBlurBg: BaseImageView? = null
    var mXinIv: ExImageView? = null
    var mCollectionIv: ExImageView? = null
    var mShareIv: ExImageView? = null
    var mBtnBack: ImageView? = null
    var mPlayLastIv: ImageView? = null
    var mPlayNextIv: ImageView? = null
    var mSongNameTv: ExTextView? = null
    var mMoreTv: ExImageView? = null
    var mControlTv: ExImageView? = null
    var mPassTimeTv: ExTextView? = null
    var mLastTimeTv: ExTextView? = null
    var mSeekBar: SeekBar? = null
    var mSingerIv: BaseImageView? = null
    var mNameTv: ExTextView? = null
    var mCommentTimeTv: ExTextView? = null
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
    var mFeedsDetailPresenter: FeedsDetailPresenter? = null
    var mMoreDialogPlus: FeedsMoreDialogView? = null
    var mCommentMoreDialogPlus: FeedCommentMoreDialog? = null
    lateinit var mSongControlArea: Group
    var mRefuseModel: FirstLevelCommentModel? = null
    var mSongManager: FeedSongPlayModeManager? = null

    var mFeedsInputContainerView: FeedsInputContainerView? = null

    var mFeedID: Int = -1   // 外部跳转传入mFeedID
    var mFrom: Int = -1  // 从外部跳转标记的来源
    var mPlayType = FeedSongPlayModeManager.PlayMode.ORDER   // 播放模式，默认顺序播放

    var mFeedsWatchModel: FeedsWatchModel? = null  // 详细的数据model，通过请求去拉

    var mResumeCall: (() -> Unit)? = null

    var sharePanel: SharePanel? = null

    var lastVerticalOffset = Int.MAX_VALUE

    var specialCase: Boolean? = false

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
            val animSet = AnimatorSet()
            animSet.play(animator1).with(animator2).with(animator3)
            animSet.setDuration(300)
            animSet.start()

        } else {
            val animator1 = ObjectAnimator.ofFloat(mControlTv, "alpha", 1f, 0f)
            val animator2 = ObjectAnimator.ofFloat(mPlayLastIv, "alpha", 1f, 0f)
            val animator3 = ObjectAnimator.ofFloat(mPlayNextIv, "alpha", 1f, 0f)
            val animSet = AnimatorSet()
            animSet.play(animator1).with(animator2).with(animator3)
            animSet.setDuration(300)
            animSet.start()
            mUiHandler.sendEmptyMessageDelayed(HIDE_CONTROL_AREA, 300)
        }
    }

    var playCallback = object : PlayerCallbackAdapter() {
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
            if (mFrom == FeedsDetailActivity.FROM_HOME_COLLECT) {
                if (specialCase ?: false) {
                    mFeedsWatchModel?.let {
                        showFeedsWatchModel(it)
                    }
                } else {
                    val newModel = mSongManager?.getNextSong(true)
                    newModel?.feedID?.let {
                        tryLoadNewFeed(it)
                    }
                }
            } else {
                stopSong()
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
            mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(duration - pos, "mm:ss")
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

        mPassTimeTv = rootView.findViewById(R.id.pass_time_tv)
        mLastTimeTv = rootView.findViewById(R.id.last_time_tv)
        mSeekBar = rootView.findViewById(R.id.seek_bar)
        mSingerIv = rootView.findViewById(R.id.singer_iv)
        mNameTv = rootView.findViewById(R.id.name_tv)
        mCommentTimeTv = rootView.findViewById(R.id.comment_time_tv)
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
        mFeedsCommonLyricView = FeedsCommonLyricView(rootView)
        mFeedsCommentView = rootView.findViewById(R.id.feedsCommentView)
        mCollectionIv = rootView.findViewById(R.id.collection_iv)

        mSongControlArea = rootView.findViewById(R.id.song_control_arae)
        mPlayLastIv = rootView.findViewById(R.id.play_last_iv)
        mPlayNextIv = rootView.findViewById(R.id.play_next_iv)
        mControlTv = rootView.findViewById(R.id.control_tv)
        mFeedsDetailPresenter = FeedsDetailPresenter(this)
        addPresent(mFeedsDetailPresenter)

        if (mFrom == FeedsDetailActivity.FROM_HOME_COLLECT) {
            launch {
                // 读收藏
                val collectList = async(Dispatchers.IO) {
                    FeedCollectManager.getMyCollect()
                }
                mSongControlArea.visibility = View.VISIBLE
                mPlayLastIv?.setDebounceViewClickListener {
                    val newModel = mSongManager?.getPreSong(true)
                    newModel?.feedID?.let {
                        tryLoadNewFeed(it)
                        mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
                    }
                }

                mPlayNextIv?.setDebounceViewClickListener {
                    val newModel = mSongManager?.getNextSong(true)
                    newModel?.feedID?.let {
                        tryLoadNewFeed(it)
                        mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
                    }
                }

                val feedSongModels = ArrayList<FeedSongModel>()
                var cur: FeedSongModel? = null
                collectList.await()?.forEach {
                    it.song?.let {
                        feedSongModels.add(it)
                        if (it.feedID == mFeedID) {
                            cur = it
                        }
                    }
                }

                mBlurBg?.setOnClickListener {
                    if (mSongControlArea?.visibility == View.VISIBLE) {
                        mUiHandler.sendEmptyMessage(DELAY_HIDE_CONTROL_AREA)
                    } else {
                        mUiHandler.sendEmptyMessage(SHOW_CONTROL_AREA)
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

                mSongManager = FeedSongPlayModeManager(mPlayType, cur, feedSongModels)
            }
        } else {
            mSongControlArea.visibility = View.GONE
        }

        mControlTv?.setDebounceViewClickListener {
            if (it!!.isSelected) {
                pausePlay()
            } else {
                startPlay()
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
            sharePanel?.setUMShareListener(null)
            sharePanel = SharePanel(activity)
            sharePanel?.apply {
                mShareImage = mFeedsWatchModel?.user?.avatar
                        ?: ""
                mTitle = mFeedsWatchModel!!.song?.workName
                mDes = mFeedsWatchModel!!.user?.nickname
                mUrl = (String.format("http://www.skrer.mobi/feed/song?songID=%d&userID=%d", mFeedsWatchModel!!.song?.songID, mFeedsWatchModel!!.user?.userID))
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
                    mFeedsDetailPresenter?.addShareCount(MyUserInfoManager.getInstance().uid.toInt(), mFeedsWatchModel?.feedID
                            ?: 0)
                }
            })
        }

        mMoreTv?.setDebounceViewClickListener {
            showMoreOp()
        }

        mCommonTitleBar?.rightImageButton?.setDebounceViewClickListener {
            showMoreOp()
        }

        mCollectionIv?.setDebounceViewClickListener {
            mFeedsDetailPresenter?.collection(mFeedsWatchModel?.isCollected != true, mFeedsWatchModel!!.feedID)
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
            mControlTv?.callOnClick()
        }

        mSingerIv?.setDebounceViewClickListener {
            val bundle = Bundle()
            bundle.putInt("bundle_user_id", mFeedsWatchModel?.user?.userID ?: 0)
            ARouter.getInstance()
                    .build(RouterConstants.ACTIVITY_OTHER_PERSON)
                    .with(bundle)
                    .navigation()
        }

        SinglePlayer.addCallback(playerTag, playCallback)
        mFeedsDetailPresenter?.getFeedsWatchModel(MyUserInfoManager.getInstance().uid.toInt(), mFeedID)
    }

    private fun tryLoadNewFeed(newFeedId: Int) {
        if (newFeedId != mFeedID) {
            mFeedID = newFeedId
            mFeedsDetailPresenter?.getFeedsWatchModel(MyUserInfoManager.getInstance().uid.toInt(), mFeedID)
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
        mSeekBar?.max = mFeedsWatchModel?.song?.playDurMs ?: 60*1000
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

        AvatarUtils.loadAvatarByUrl(mSingerIv, AvatarUtils.newParamsBuilder(mFeedsWatchModel?.user?.avatar)
                .setCircle(true)
                .build())


        mNameTv?.text = UserInfoManager.getInstance().getRemarkName(mFeedsWatchModel?.user?.userID
                ?: 0, mFeedsWatchModel?.user?.nickname)
        mFeedsWatchModel?.song?.createdAt?.let {
            mCommentTimeTv?.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(it, System.currentTimeMillis())
        }

        showMainComment()

        mCommentTv?.setDebounceViewClickListener {
            mRefuseModel = null
            mFeedsInputContainerView?.showSoftInput()
            mFeedsInputContainerView?.setETHint("回复 ${mFeedsWatchModel?.user?.nickname}")
        }

        mFollowTv?.visibility = if (mFeedsWatchModel?.user?.userID != MyUserInfoManager.getInstance().uid.toInt()) View.VISIBLE else View.GONE

        mFeedsWatchModel?.user?.avatar?.let {
            mRadioView?.setAvatar(it)
        }
        mShareNumTv?.text = StringFromatUtils.formatTenThousand(mFeedsWatchModel!!.shareCnt)
        mXinNumTv?.text = StringFromatUtils.formatTenThousand(mFeedsWatchModel!!.starCnt)
        mXinIv?.isSelected = mFeedsWatchModel!!.isLiked!!
        mFeedsCommentView?.feedsCommendAdapter?.mCommentNum = mFeedsWatchModel?.commentCnt!!

        mCollectionIv?.isSelected = mFeedsWatchModel?.isCollected == true

        mFeedsDetailPresenter?.getRelation(mFeedsWatchModel!!.user!!.userID)
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

        mFeedsWatchModel?.isLiked = like
        mXinNumTv?.text = StringFromatUtils.formatTenThousand(mFeedsWatchModel!!.starCnt)
    }

    override fun onResume() {
        super.onResume()
        mResumeCall?.invoke()
        mResumeCall = null
    }

    override fun onPause() {
        super.onPause()
        if (SinglePlayer.isPlaying) {
            mResumeCall = {
                startPlay()
            }
        } else {
            mResumeCall = null
        }

        pausePlay()
    }

    override fun collectFinish(c: Boolean) {
        mCollectionIv?.isSelected = c
        mFeedsWatchModel?.isCollected = c
    }

    override fun showFeedsWatchModel(model: FeedsWatchModel) {
        mFeedsWatchModel = model

        mXinIv?.setDebounceViewClickListener {
            mFeedsDetailPresenter?.likeFeeds(!mXinIv!!.isSelected, mFeedsWatchModel!!.feedID)
        }

        setModelData()
        startPlay()
    }

    override fun finishWithModelError() {
        activity?.finish()
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
            FeedsPlayStatistics.setCurPlayMode(mFeedsWatchModel?.feedID ?: 0)
            SinglePlayer.startPlay(playerTag, it)
        }

        if (SinglePlayer.isBufferingOk) {
            mFeedsCommonLyricView?.playLyric()
        }
    }

    private fun pausePlay() {
        MyLog.d(mTag, "pausePlay")
        mControlTv!!.isSelected = false
        mRadioView?.pause()
        SinglePlayer.pause(playerTag)
        mFeedsCommonLyricView?.pause()
    }

    private fun stopSong() {
        MyLog.d(mTag, "stopSong()")
        SinglePlayer.stop(playerTag)
        mControlTv!!.isSelected = false
        mRadioView?.pause()
        mSeekBar!!.progress = 0
        mPassTimeTv?.text = "00:00"
        mFeedsWatchModel?.song?.playDurMs?.let {
            mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(it.toLong(), "mm:ss")
        }
        mFeedsCommonLyricView?.stop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RelationChangeEvent) {
        if (mFeedsWatchModel?.user?.userID == event.useId) {
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: FeedCommentBoardEvent) {
        specialCase = event.showing
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
            UserInfoManager.getInstance().mateRelation(mFeedsWatchModel!!.user!!.userID, UserInfoManager.RA_BUILD, false, 0, null)
        }
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mFeedID = (data as Int?) ?: -1
        } else if (type == 1) {
            mFrom = (data as Int?) ?: -1
        } else if (type == 2) {
            mPlayType = (data as FeedSongPlayModeManager.PlayMode?)
                    ?: FeedSongPlayModeManager.PlayMode.ORDER
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

        EventBus.getDefault().post(FeedDetailChangeEvent(mFeedsWatchModel?.apply {
            commentCnt = mFeedsCommentView?.feedsCommendAdapter?.mCommentNum ?: 0
        }))

    }
}