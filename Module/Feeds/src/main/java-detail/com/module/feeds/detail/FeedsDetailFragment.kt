package com.module.feeds.detail

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import com.common.base.AbsCoroutineFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.share.SharePanel
import com.common.core.share.ShareType
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.feeds.detail.view.FeedsCommonLyricView
import com.module.feeds.detail.view.FeedsInputContainerView
import com.module.feeds.detail.view.RadioView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class FeedsDetailFragment : AbsCoroutineFragment() {
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
    var mRadioView: RadioView? = null

    var mFeedsInputContainerView: FeedsInputContainerView? = null

    internal var isInitToolbar = false
    internal var mIsPlaying = false

    override fun initView(): Int {
        return com.module.feeds.R.layout.feeds_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mContainer = mRootView.findViewById(com.module.feeds.R.id.container)
        mAppbar = mRootView.findViewById(com.module.feeds.R.id.appbar)
        mContentLayout = mRootView.findViewById(com.module.feeds.R.id.content_layout)
        mBlurBg = mRootView.findViewById(com.module.feeds.R.id.blur_bg)
        mBtnBack = mRootView.findViewById(com.module.feeds.R.id.btn_back) as ImageView
        mSongNameTv = mRootView.findViewById(com.module.feeds.R.id.song_name_tv)
        mMoreTv = mRootView.findViewById(com.module.feeds.R.id.more_iv)
        mControlTv = mRootView.findViewById(com.module.feeds.R.id.control_tv)
        mPassTimeTv = mRootView.findViewById(com.module.feeds.R.id.pass_time_tv)
        mLastTimeTv = mRootView.findViewById(com.module.feeds.R.id.last_time_tv)
        mSeekBar = mRootView.findViewById(com.module.feeds.R.id.seek_bar)
        mSingerIv = mRootView.findViewById(com.module.feeds.R.id.singer_iv)
        mNameTv = mRootView.findViewById(com.module.feeds.R.id.name_tv)
        mCommentTimeTv = mRootView.findViewById(com.module.feeds.R.id.comment_time_tv)
        mFollowTv = mRootView.findViewById(com.module.feeds.R.id.follow_tv)
        mMainCommentTv = mRootView.findViewById(com.module.feeds.R.id.main_comment_tv)
        mToolbar = mRootView.findViewById(com.module.feeds.R.id.toolbar)
        mToolbarLayout = mRootView.findViewById(com.module.feeds.R.id.toolbar_layout)
        mCommentTv = mRootView.findViewById(com.module.feeds.R.id.comment_tv)
        mXinIv = mRootView.findViewById(com.module.feeds.R.id.xin_iv)
        mXinNumTv = mRootView.findViewById(com.module.feeds.R.id.xin_num_tv)
        mShareIv = mRootView.findViewById(com.module.feeds.R.id.share_iv)
        mShareNumTv = mRootView.findViewById(com.module.feeds.R.id.share_num_tv)
        mFeedsInputContainerView = mRootView.findViewById(com.module.feeds.R.id.feeds_input_container_view)
        mRadioView = mRootView.findViewById(com.module.feeds.R.id.radio_view)
        mFeedsCommonLyricView = FeedsCommonLyricView(mRootView)

        AvatarUtils.loadAvatarByUrl(mBlurBg, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCircle(false)
                .setBlur(true)
                .build())

        mAppbar?.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            var srollLimit = appBarLayout.totalScrollRange - U.getDisplayUtils().dip2px(55f)
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

        mCommentTv?.setDebounceViewClickListener {
            mFeedsInputContainerView?.showSoftInput()
        }

        mXinIv?.setDebounceViewClickListener {

        }

        mFollowTv?.setDebounceViewClickListener {

        }

        launch(Dispatchers.Main) {
            repeat(100) {
                delay(1000)
                mPassTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate(it.toLong() * 1000, "mm:ss")
                mLastTimeTv?.text = U.getDateTimeUtils().formatTimeStringForDate((100 - it.toLong()) * 1000, "mm:ss")
            }
        }

        launch {
            delay(2000)
            mRadioView?.play()
            delay(2000)
            mRadioView?.pause()
            delay(2000)
            mRadioView?.play()
        }
    }

    private fun playSong() {

    }

    private fun pauseSong() {

    }

    fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                click(v)
            }
        })
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun destroy() {
        super.destroy()
        mRadioView?.destroy()
    }
}