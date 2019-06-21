package com.module.playways.doubleplay.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.Group
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.FragmentUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.dialog.view.TipsDialogView
import com.engine.agora.AgoraEngineAdapter
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.presenter.DoubleCorePresenter
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent
import com.module.playways.doubleplay.view.DoubleSingCardView
import com.module.playways.room.song.model.SongModel
import com.module.playways.view.ZanView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.report.fragment.QuickFeedbackFragment


class DoublePlayWaysFragment : BaseFragment(), IDoublePlayView {

    lateinit var mReportTv: TextView
    private var mExitIv: ImageView? = null
    private var mLeftAvatarSdv: SimpleDraweeView? = null
    private var mLeftLockIcon: ImageView? = null
    private var mLeftNameTv: ExTextView? = null
    private var mUnlockTv: ExTextView? = null
    private var mRightAvatarSdv: SimpleDraweeView? = null   //右边固定是自己
    private var mRightLockIcon: ImageView? = null
    private var mRightNameTv: ExTextView? = null
    private var mCountDownTv: ExTextView? = null
    private var mMicIv: ExImageView? = null
    private var mNoLimitIcon: ExImageView? = null
    private var mPickIv: ImageView? = null
    private var mSelectIv: ImageView? = null
    private var mWordGroup: Group? = null
    private var mRightZanView: ZanView? = null
    private var mLeftZanView: ZanView? = null
    private var mDialogPlus: DialogPlus? = null

    lateinit var mDoubleCorePresenter: DoubleCorePresenter
    lateinit var mRoomData: DoubleRoomData

    var countDownTimer: HandlerTaskTimer? = null

    private val mDoubleSingCardView: DoubleSingCardView by lazy {
        DoubleSingCardView(mRootView.findViewById<View>(R.id.double_sing_card_view_layout_stub) as ViewStub)
    }

    init {
        mRoomData = DoubleRoomData()
    }

    override fun initView(): Int {
        return R.layout.double_play_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mReportTv = mRootView.findViewById<View>(R.id.report_tv) as TextView
        mExitIv = mRootView.findViewById<View>(R.id.exit_iv) as ImageView
        mLeftAvatarSdv = mRootView.findViewById<View>(R.id.left_avatar_sdv) as SimpleDraweeView
        mLeftLockIcon = mRootView.findViewById<View>(R.id.left_lock_icon) as ImageView
        mLeftNameTv = mRootView.findViewById<View>(R.id.left_name_tv) as ExTextView
        mRightAvatarSdv = mRootView.findViewById<View>(R.id.right_avatar_sdv) as SimpleDraweeView
        mRightLockIcon = mRootView.findViewById<View>(R.id.right_lock_icon) as ImageView
        mRightNameTv = mRootView.findViewById<View>(R.id.right_name_tv) as ExTextView
        mMicIv = mRootView.findViewById<View>(R.id.mic_iv) as ExImageView
        mPickIv = mRootView.findViewById<View>(R.id.pick_iv) as ImageView
        mSelectIv = mRootView.findViewById<View>(R.id.select_iv) as ImageView
        mUnlockTv = mRootView.findViewById<View>(R.id.unlock_tv) as ExTextView
        mWordGroup = mRootView.findViewById<View>(R.id.word_group) as Group
        mCountDownTv = mRootView.findViewById<View>(R.id.count_down_tv) as ExTextView
        mRightZanView = mRootView.findViewById<View>(R.id.right_zanView) as ZanView
        mLeftZanView = mRootView.findViewById<View>(R.id.left_zanView) as ZanView
        mNoLimitIcon = mRootView.findViewById(R.id.no_limit_icon) as ExImageView

        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 举报
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(activity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, 1)
                                .addDataBeforeAdd(1, mRoomData.getAntherUser()?.avatar ?: 0)
                                .setEnterAnim(com.component.busilib.R.anim.slide_in_bottom)
                                .setExitAnim(com.component.busilib.R.anim.slide_out_bottom)
                                .build())
            }
        })

        mExitIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 退出
                exitLogin()
            }
        })

        mRightNameTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {

            }
        })

        mUnlockTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mDoubleCorePresenter?.unLockInfo()
            }
        })

        mMicIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 开关麦克
                val isSelected = mMicIv?.isSelected ?: false
                AgoraEngineAdapter.getInstance().muteLocalAudioStream(!isSelected)
                mMicIv?.setSelected(!isSelected)
            }
        })

        mPickIv?.setOnClickListener {
            mDoubleCorePresenter?.pickOther()
            mLeftZanView?.addZanXin(mLeftZanView)
        }

        mSelectIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 点歌
                if (TextUtils.isEmpty(mRoomData?.nextMusicDesc)) {
//                    OwnerManagerActivity.open(activity, mRoomData)
                } else {
                    mDoubleCorePresenter?.nextSong()
                }
            }
        })

        mDoubleCorePresenter = DoubleCorePresenter(mRoomData, this)
        addPresent(mDoubleCorePresenter)

        if (!mRoomData.enableNoLimitDuration) {
            selfLockState()
            guestLockState()
            mNoLimitIcon?.visibility = GONE
            mCountDownTv?.visibility = VISIBLE
            startCountDown()
        } else {
            unLockOther()
            unLockSelf()
            mNoLimitIcon?.visibility = VISIBLE
            mCountDownTv?.visibility = GONE
        }
    }

    private fun startCountDown() {
        countDownTimer = HandlerTaskTimer.newBuilder().interval(1000).take(mRoomData.config?.durationTimeMs
                ?: 0 / 1000).start(object : HandlerTaskTimer.ObserverW() {
            override fun onNext(t: Int) {
                mCountDownTv?.text = t.toString()
            }

            override fun onComplete() {
                mDoubleCorePresenter.closeByTimeOver()
            }
        })
    }

    private fun exitLogin() {
        val tipsDialogView = TipsDialogView.Builder(context)
                .setMessageTip("确定退出唱聊房吗？")
                .setConfirmTip("确定")
                .setCancelTip("取消")
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mDialogPlus?.dismiss()
                        mDoubleCorePresenter.exit()
                    }
                })
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        mDialogPlus?.dismiss()
                    }
                })
                .build()

        mDialogPlus = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create()
        mDialogPlus?.show()
    }

    /**
     * 自己锁定状态
     */
    fun selfLockState() {
        AvatarUtils.loadAvatarByUrl(mRightAvatarSdv, AvatarUtils.newParamsBuilder(mRoomData.getMaskAvatar(MyUserInfoManager.getInstance().sex))
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        mRightLockIcon?.visibility = VISIBLE
        mUnlockTv?.visibility = VISIBLE
        mRightNameTv?.text = ""
        mRightNameTv?.visibility = GONE
    }

    /**
     * 别人锁定状态
     */
    fun guestLockState() {
        AvatarUtils.loadAvatarByUrl(mLeftAvatarSdv, AvatarUtils.newParamsBuilder(mRoomData.getMaskAvatar(mRoomData.getAntherUser()?.sex
                ?: 0))
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        mLeftLockIcon?.visibility = VISIBLE
        mLeftNameTv?.text = ""
        mLeftNameTv?.visibility = GONE
    }

    override fun finishActivityWithError() {
        activity?.finish()
    }

    override fun startGame(mCur: SongModel, mNext: String) {
        mWordGroup?.visibility = GONE
        mDoubleSingCardView.playLyric("", mCur, mNext)
    }

    override fun changeRound(mCur: SongModel, mNext: String) {
        mDoubleSingCardView.playLyric("", mCur, mNext)
    }

    override fun picked() {
        mRightZanView?.addZanXin(mRightZanView)
    }

    override fun gameEnd(doubleEndCombineRoomPushEvent: DoubleEndCombineRoomPushEvent) {

    }

    override fun showLockState(userID: Int, lockState: Boolean) {
        if (lockState) {
            if (userID == MyUserInfoManager.getInstance().uid as Int) {
                unLockSelf()
            } else {
                unLockOther()
            }
        }
    }

    /**
     * 别人解锁状态
     */
    fun unLockOther() {
        AvatarUtils.loadAvatarByUrl(mLeftAvatarSdv, AvatarUtils.newParamsBuilder(mRoomData?.getAntherUser()?.avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        mLeftLockIcon?.visibility = GONE
        mLeftNameTv?.text = mRoomData?.getAntherUser()?.nickname
        mLeftNameTv?.visibility = VISIBLE
    }

    /**
     * 自己解锁状态
     */
    fun unLockSelf() {
        AvatarUtils.loadAvatarByUrl(mRightAvatarSdv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                .setCircle(true)
                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderColor(Color.WHITE)
                .build())

        mRightLockIcon?.visibility = GONE
        mUnlockTv?.visibility = GONE
        mRightNameTv?.text = MyUserInfoManager.getInstance().nickName
        mRightNameTv?.visibility = VISIBLE
    }

    override fun showNoLimitDurationState(noLimit: Boolean) {
        if (noLimit) {
            unLockSelf()
            unLockOther()
            mNoLimitIcon?.visibility = VISIBLE
            mCountDownTv?.visibility = GONE
            countDownTimer?.dispose()
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
