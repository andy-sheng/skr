package com.module.playways.doubleplay.fragment

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.common.base.BaseFragment
import com.common.image.fresco.BaseImageView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R


class DoubleGameMatchFragment : BaseFragment() {
    var mReportTv: ExTextView? = null
    var mCloseIv: ImageView? = null
    var mWhiteBg: ExImageView? = null
    var mAvatarIv: BaseImageView? = null
    var mEndTv: ExTextView? = null
    var mChatTimeTv: ExTextView? = null
    var mEndTipTv: ExTextView? = null
    var mFollowTv: ExTextView? = null
    var mMatchAgain: ExTextView? = null
    var mLastNumTv: ExTextView? = null

    override fun initView(): Int {
        return R.layout.double_game_end_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mReportTv = mRootView.findViewById<View>(R.id.report_tv) as ExTextView
        mCloseIv = mRootView.findViewById<View>(R.id.close_iv) as ImageView
        mWhiteBg = mRootView.findViewById<View>(R.id.white_bg) as ExImageView
        mAvatarIv = mRootView.findViewById<View>(R.id.avatar_iv) as BaseImageView
        mEndTv = mRootView.findViewById<View>(R.id.end_tv) as ExTextView
        mChatTimeTv = mRootView.findViewById<View>(R.id.chat_time_tv) as ExTextView
        mEndTipTv = mRootView.findViewById<View>(R.id.end_tip_tv) as ExTextView
        mFollowTv = mRootView.findViewById<View>(R.id.follow_tv) as ExTextView
        mMatchAgain = mRootView.findViewById<View>(R.id.match_again) as ExTextView
        mLastNumTv = mRootView.findViewById<View>(R.id.last_num_tv) as ExTextView


    }
}