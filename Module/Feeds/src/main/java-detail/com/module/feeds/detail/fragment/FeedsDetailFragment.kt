package com.module.feeds.detail.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.image.fresco.BaseImageView
import com.common.player.IPlayerCallback
import com.common.player.MyMediaPlayer
import com.common.player.event.PlayerEvent
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.feeds.model.FeedsWatchModel
import com.module.feeds.detail.view.FeedsCommentView
import com.module.feeds.detail.view.FeedsCommonLyricView
import com.module.feeds.detail.view.FeedsInputContainerView
import com.module.feeds.detail.view.RadioView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class FeedsDetailFragment : BaseFragment() {
    val mTag = "FeedsDetailFragment"
    var mContainer: LinearLayout? = null
    var mAppbar: AppBarLayout? = null
    var mContentLayout: CollapsingToolbarLayout? = null
    var mBlurBg: BaseImageView? = null
    var mXinIv: ExImageView? = null
    var mShareIv: ExImageView? = null
    var mBtnBack: ImageView? = null
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
    var mRadioView: RadioView? = null
    var mCommonTitleBar: CommonTitleBar? = null

    var mIsSongStart = false

    var mFeedsInputContainerView: FeedsInputContainerView? = null

    var mFeedsWatchModel: FeedsWatchModel? = null

    val mMyMediaPlayer: MyMediaPlayer by lazy {
        MyMediaPlayer().also {
            it.setMonitorProgress(true)
            it.setCallback(object : IPlayerCallback {
                override fun onPrepared() {
                    if (!mFeedsCommonLyricView!!.isStart()) {
                        mFeedsCommonLyricView!!.playLyric()
                    } else {
                        mFeedsCommonLyricView!!.resume()
                    }
                }

                override fun onCompletion() {
                    stopSong()
                }

                override fun onSeekComplete() {

                }

                override fun onVideoSizeChanged(width: Int, height: Int) {

                }

                override fun onError(what: Int, extra: Int) {
                    mFeedsCommonLyricView!!.pause()
                }

                override fun onInfo(what: Int, extra: Int) {

                }

                override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
                    if (percent == 100) {
                        mMyMediaPlayer.resume()
                    } else {
                        mMyMediaPlayer.pause()
                    }
                }
            })
        }
    }

    internal var isInitToolbar = false
    internal var mIsPlaying = false

    override fun initView(): Int {
        return com.module.feeds.R.layout.feeds_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mFeedsWatchModel == null) {
            activity?.finish()
            return
        }

        mContainer = rootView.findViewById(com.module.feeds.R.id.container)
        mCommonTitleBar = rootView.findViewById(com.module.feeds.R.id.titlebar)
        mAppbar = rootView.findViewById(com.module.feeds.R.id.appbar)
        mContentLayout = rootView.findViewById(com.module.feeds.R.id.content_layout)
        mBlurBg = rootView.findViewById(com.module.feeds.R.id.blur_bg)
        mBtnBack = rootView.findViewById(com.module.feeds.R.id.btn_back) as ImageView
        mSongNameTv = rootView.findViewById(com.module.feeds.R.id.song_name_tv)
        mMoreTv = rootView.findViewById(com.module.feeds.R.id.more_iv)
        mControlTv = rootView.findViewById(com.module.feeds.R.id.control_tv)
        mPassTimeTv = rootView.findViewById(com.module.feeds.R.id.pass_time_tv)
        mLastTimeTv = rootView.findViewById(com.module.feeds.R.id.last_time_tv)
        mSeekBar = rootView.findViewById(com.module.feeds.R.id.seek_bar)
        mSingerIv = rootView.findViewById(com.module.feeds.R.id.singer_iv)
        mNameTv = rootView.findViewById(com.module.feeds.R.id.name_tv)
        mCommentTimeTv = rootView.findViewById(com.module.feeds.R.id.comment_time_tv)
        mFollowTv = rootView.findViewById(com.module.feeds.R.id.follow_tv)
        mMainCommentTv = rootView.findViewById(com.module.feeds.R.id.main_comment_tv)
        mToolbar = rootView.findViewById(com.module.feeds.R.id.toolbar)
        mToolbarLayout = rootView.findViewById(com.module.feeds.R.id.toolbar_layout)
        mCommentTv = rootView.findViewById(com.module.feeds.R.id.comment_tv)
        mXinIv = rootView.findViewById(com.module.feeds.R.id.xin_iv)
        mXinNumTv = rootView.findViewById(com.module.feeds.R.id.xin_num_tv)
        mShareIv = rootView.findViewById(com.module.feeds.R.id.share_iv)
        mShareNumTv = rootView.findViewById(com.module.feeds.R.id.share_num_tv)
        mFeedsInputContainerView = rootView.findViewById(com.module.feeds.R.id.feeds_input_container_view)
        mRadioView = rootView.findViewById(com.module.feeds.R.id.radio_view)
        mFeedsCommonLyricView = FeedsCommonLyricView(rootView)
        mFeedsCommentView = rootView.findViewById(com.module.feeds.R.id.feedsCommentView)

        mFeedsCommentView?.setFeedsID(mFeedsWatchModel!!.feedID!!)
        mFeedsWatchModel?.song?.songTpl?.songName?.let {
            mSongNameTv?.text = it
            mCommonTitleBar?.centerTextView?.text = "正在播放《${it}》"
        }

        AvatarUtils.loadAvatarByUrl(mBlurBg, AvatarUtils.newParamsBuilder(mFeedsWatchModel?.user?.avatar)
                .setCircle(false)
                .setBlur(true)
                .build())

        mAppbar?.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
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
                        val params = mToolbarLayout?.getLayoutParams()
                        params!!.height = params!!.height + U.getStatusBarUtil().getStatusBarHeight(U.app())
                        mToolbarLayout?.setLayoutParams(params)
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

        mBtnBack?.setDebounceViewClickListener {
            activity?.finish()
        }

        mShareIv?.setDebounceViewClickListener {
            val sharePanel = SharePanel(activity)
            sharePanel.setShareContent("http://res-static.inframe.mobi/common/skr-share.png")
            sharePanel.show(ShareType.IMAGE_RUL)
        }

        mControlTv?.setDebounceViewClickListener {
            if (mIsPlaying) {
                pauseSong()
                mIsPlaying = false
                mControlTv?.isSelected = false
            } else {
                playSong()
                mIsPlaying = true
                mControlTv?.isSelected = true
            }
        }

        AvatarUtils.loadAvatarByUrl(mSingerIv, AvatarUtils.newParamsBuilder(mFeedsWatchModel?.user?.avatar)
                .setCircle(true)
                .build())

        mNameTv?.text = mFeedsWatchModel?.user?.nickname
        mFeedsWatchModel?.song?.createdAt?.let {
            mCommentTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(it, "MM-dd HH:mm")
        }
        mMainCommentTv?.text = mFeedsWatchModel?.song?.title

        mCommentTv?.setDebounceViewClickListener {
            mFeedsInputContainerView?.showSoftInput()
        }

        mXinIv?.setDebounceViewClickListener {

        }

        mFollowTv?.setDebounceViewClickListener {

        }

        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    mMyMediaPlayer.seekTo(progress.toLong())
                    mFeedsCommonLyricView?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        mFeedsWatchModel?.user?.avatar?.let {
            mRadioView?.setAvator(it)
        }

        mRadioView?.avatarContainer?.setDebounceViewClickListener {
            mControlTv?.callOnClick()
        }

        mFeedsCommonLyricView?.setSongModel(mFeedsWatchModel!!.song!!)
        playSong()

        mControlTv?.setDebounceViewClickListener {
            if (it!!.isSelected) {
                pauseSong()
            } else {
                resumeSong()
            }
        }
    }

    private fun playSong() {
        mMyMediaPlayer.reset()
        mControlTv?.isSelected = true
        mIsSongStart = true
        mRadioView?.play()
        mMyMediaPlayer.startPlay(mFeedsWatchModel?.song?.playURL)
    }

    private fun resumeSong() {
        mControlTv!!.isSelected = true
        mRadioView?.play()
        if (mIsSongStart) {
            mMyMediaPlayer.resume()
            mFeedsCommonLyricView?.resume()
        } else {
            playSong()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PlayerEvent.TimeFly) {
        mPassTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(event.curPostion, "mm:ss")
        mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(event.totalDuration - event.curPostion, "mm:ss")
        mSeekBar!!.max = event.totalDuration.toInt()
        mSeekBar!!.progress = event.curPostion.toInt()
        mFeedsCommonLyricView?.seekTo(event.curPostion.toInt())
    }

    private fun pauseSong() {
        mControlTv!!.isSelected = false
        mRadioView?.pause()
        mMyMediaPlayer.pause()
        mFeedsCommonLyricView?.pause()
    }

    private fun stopSong() {
        mIsSongStart = false
        mMyMediaPlayer.stop()
        mControlTv!!.isSelected = false
        mRadioView?.pause()
        mSeekBar!!.progress = 0
        mPassTimeTv?.text = "00:00"
        mLastTimeTv?.text = "00:00"
        mFeedsCommonLyricView?.stop()
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mFeedsWatchModel = data as FeedsWatchModel
        }
    }

    fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                click(v)
            }
        })
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun destroy() {
        super.destroy()
        mRadioView?.destroy()
        mMyMediaPlayer.release()
        mFeedsCommonLyricView?.destroy()
    }
}