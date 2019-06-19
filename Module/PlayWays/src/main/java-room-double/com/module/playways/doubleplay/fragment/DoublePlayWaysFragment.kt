package com.module.playways.doubleplay.fragment

import android.os.Bundle
import android.support.constraint.Group
import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.base.BaseFragment
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.doubleplay.DoubleCorePresenter
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent
import com.module.playways.doubleplay.view.DoubleSingCardView
import com.module.playways.room.song.model.SongModel


class DoublePlayWaysFragment : BaseFragment(), IDoublePlayView {

    private var mReportTv: TextView? = null
    private var mExitIv: ImageView? = null
    private var mLeftAvatarSdv: SimpleDraweeView? = null
    private var mLeftLockIcon: ImageView? = null
    private var mLeftNameTv: ExTextView? = null
    private var mRightAvatarSdv: SimpleDraweeView? = null   //右边固定是自己
    private var mRightLockIcon: ImageView? = null
    private var mRightNameTv: ExTextView? = null
    private var mMicIv: ExImageView? = null
    private var mPickIv: ImageView? = null
    private var mSelectIv: ImageView? = null
    private var mWordGroup: Group? = null
    private var mDoubleCorePresenter: DoubleCorePresenter? = null
    internal var mRoomData: DoubleRoomData? = null

    private val mDoubleSingCardView: DoubleSingCardView by lazy {
        DoubleSingCardView(mRootView.findViewById<View>(com.module.playways.R.id.double_sing_card_view_layout_stub) as ViewStub)
    }

    override fun initView(): Int {
        return com.module.playways.R.layout.double_play_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mReportTv = mRootView.findViewById<View>(com.module.playways.R.id.report_tv) as TextView
        mExitIv = mRootView.findViewById<View>(com.module.playways.R.id.exit_iv) as ImageView
        mLeftAvatarSdv = mRootView.findViewById<View>(com.module.playways.R.id.left_avatar_sdv) as SimpleDraweeView
        mLeftLockIcon = mRootView.findViewById<View>(com.module.playways.R.id.left_lock_icon) as ImageView
        mLeftNameTv = mRootView.findViewById<View>(com.module.playways.R.id.left_name_tv) as ExTextView
        mRightAvatarSdv = mRootView.findViewById<View>(com.module.playways.R.id.right_avatar_sdv) as SimpleDraweeView
        mRightLockIcon = mRootView.findViewById<View>(com.module.playways.R.id.right_lock_icon) as ImageView
        mRightNameTv = mRootView.findViewById<View>(com.module.playways.R.id.right_name_tv) as ExTextView
        mMicIv = mRootView.findViewById<View>(com.module.playways.R.id.mic_iv) as ExImageView
        mPickIv = mRootView.findViewById<View>(com.module.playways.R.id.pick_iv) as ImageView
        mSelectIv = mRootView.findViewById<View>(com.module.playways.R.id.select_iv) as ImageView
        mWordGroup = mRootView.findViewById<View>(com.module.playways.R.id.word_group) as Group

        mReportTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 举报
            }
        })

        mExitIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 退出
                mDoubleCorePresenter?.exit()
            }
        })

        mRightNameTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {

            }
        })

        mMicIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 开关麦克
            }
        })

        mPickIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // picked
                mDoubleCorePresenter?.pickOther()
            }
        })

        mSelectIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 点歌
                if (TextUtils.isEmpty(mRoomData?.nextMusicDesc)) {
                    //点歌
                } else {
                    mDoubleCorePresenter?.nextSong()
                }
            }
        })

        mDoubleCorePresenter = DoubleCorePresenter(this)
        addPresent(mDoubleCorePresenter)
    }

    override fun startGame(mCur: SongModel, mNext: String) {
        mWordGroup?.visibility = GONE
        mDoubleSingCardView.playLyric("", mCur, mNext)
    }

    override fun changeRound(mCur: SongModel, mNext: String) {
        mDoubleSingCardView.playLyric("", mCur, mNext)
    }

    override fun picked() {

    }

    override fun gameEnd(doubleEndCombineRoomPushEvent: DoubleEndCombineRoomPushEvent) {

    }

    override fun showLockState(userID: Int, lockState: Boolean) {

    }

    override fun showNoLimitDurationState(noLimit: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
