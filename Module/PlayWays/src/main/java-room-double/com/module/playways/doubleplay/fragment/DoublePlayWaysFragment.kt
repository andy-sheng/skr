package com.module.playways.doubleplay.fragment

import android.graphics.Color
import android.os.Bundle
import android.support.constraint.Group
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.FragmentUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.event.EnterDoubleRoomEvent
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.doubleplay.presenter.DoubleCorePresenter
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent
import com.module.playways.doubleplay.view.DoubleSingCardView
import com.module.playways.grab.room.songmanager.OwnerManagerActivity
import com.module.playways.grab.room.songmanager.SongManageData
import com.module.playways.view.ZanView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.dialog.PersonInfoDialog
import com.zq.mediaengine.kit.ZqEngineKit
import com.zq.report.fragment.QuickFeedbackFragment
import org.greenrobot.eventbus.EventBus


class DoublePlayWaysFragment : BaseFragment(), IDoublePlayView {
    val mTag = "DoublePlayWaysFragment"
    lateinit var mReportTv: TextView
    private var mExitIv: ImageView? = null
    private var mLeftAvatarSdv: SimpleDraweeView? = null
    private var mLeftLockIcon: ImageView? = null
    private var mLeftNameTv: ExTextView? = null
    private var mUnlockTv: ExTextView? = null
    private var mWordTv: ExTextView? = null
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

    lateinit var mDoubleSingCardView1: DoubleSingCardView
    lateinit var mDoubleSingCardView2: DoubleSingCardView

    var mCurrentCardView: DoubleSingCardView? = null

    override fun initView(): Int {
        return R.layout.double_play_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        MyLog.w(mTag, "initData mRoomData='${mRoomData}'")
        EventBus.getDefault().post(EnterDoubleRoomEvent())
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
        mWordTv = mRootView.findViewById(R.id.word_tv) as ExTextView
        mSelectIv = mRootView.findViewById<View>(R.id.select_iv) as ImageView
        mUnlockTv = mRootView.findViewById<View>(R.id.unlock_tv) as ExTextView
        mWordGroup = mRootView.findViewById<View>(R.id.word_group) as Group
        mCountDownTv = mRootView.findViewById<View>(R.id.count_down_tv) as ExTextView
        mRightZanView = mRootView.findViewById<View>(R.id.right_zanView) as ZanView
        mLeftZanView = mRootView.findViewById<View>(R.id.left_zanView) as ZanView
        mNoLimitIcon = mRootView.findViewById(R.id.no_limit_icon) as ExImageView
        mDoubleSingCardView1 = mRootView.findViewById(R.id.show_card1) as DoubleSingCardView
        mDoubleSingCardView2 = mRootView.findViewById(R.id.show_card2) as DoubleSingCardView

        mWordTv?.text = mRoomData.config?.roomSignature

        var mListener = object : DoubleSingCardView.Listener() {
            override fun clickChangeSong() {
                mDoubleCorePresenter?.nextSong()
            }

            override fun clickToAddMusic() {
                OwnerManagerActivity.open(activity, SongManageData(mRoomData))
            }
        }
        mDoubleSingCardView1.mListener = mListener
        mDoubleSingCardView2.mListener = mListener


        mLeftAvatarSdv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                val info = mRoomData.userLockInfoMap[mRoomData.getAntherUser()?.userId]
                if (info != null && !info.isHasLock) {
                    showPersonInfoView(info.userID)
                }
            }
        })

        mRightAvatarSdv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                val info = mRoomData.userLockInfoMap[MyUserInfoManager.getInstance().uid.toInt()]
                if (info != null && !info.isHasLock) {
                    showPersonInfoView(MyUserInfoManager.getInstance().uid.toInt())
                }
            }
        })

        mReportTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 举报
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(activity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, 1)
                                .addDataBeforeAdd(1, mRoomData.getAntherUser()?.userId ?: 0)
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
                mDoubleCorePresenter.unLockInfo()
            }
        })

        mMicIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 开关麦克
                val isSelected = mMicIv?.isSelected ?: false
                ZqEngineKit.getInstance().muteLocalAudioStream(!isSelected)
                mMicIv?.setSelected(!isSelected)
            }
        })

        mPickIv?.setOnClickListener {
            mDoubleCorePresenter.pickOther()
            mLeftZanView?.addZanXin(mLeftZanView)
        }

        mSelectIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 点歌
                OwnerManagerActivity.open(activity, SongManageData(mRoomData))
            }
        })

        mDoubleCorePresenter = DoubleCorePresenter(mRoomData, this)
        addPresent(mDoubleCorePresenter)

        if (mRoomData.needMaskUserInfo) {
            selfLockState()
            guestLockState()
        } else {
            unLockOther()
            unLockSelf()
        }

        MyLog.d(mTag, "mRoomData.enableNoLimitDuration " + mRoomData.enableNoLimitDuration)
        if (mRoomData.enableNoLimitDuration) {
            mNoLimitIcon?.visibility = VISIBLE
            mCountDownTv?.visibility = GONE
            mUnlockTv?.visibility = GONE
        } else {
            mNoLimitIcon?.visibility = GONE
            mCountDownTv?.visibility = VISIBLE
            mUnlockTv?.visibility = VISIBLE
            startCountDown()
        }
    }

    private fun startCountDown() {
        val take = (mRoomData.config?.durationTimeMs ?: 0) / 1000
        MyLog.d(mTag, "startCountDown take is " + take)
        countDownTimer = HandlerTaskTimer.newBuilder().interval(1000).take(take).start(object : HandlerTaskTimer.ObserverW() {
            override fun onNext(t: Int) {
                mCountDownTv?.text = U.getDateTimeUtils().formatTimeStringForDate((take - t).toLong() * 1000, "mm:ss")
            }

            override fun onComplete() {
                mDoubleCorePresenter.closeByTimeOver()
            }
        })
    }

    var mPersonInfoDialog: PersonInfoDialog? = null

    private fun showPersonInfoView(userID: Int) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!")
            return
        }

        mPersonInfoDialog = PersonInfoDialog(activity, userID, true, false, mRoomData.gameId)
        mPersonInfoDialog?.setListener(object : PersonInfoDialog.KickListener {

            override fun onClickKick(userInfoModel: UserInfoModel) {

            }

            override fun onClickDoubleInvite(userInfoModel: UserInfoModel) {

            }
        })
        mPersonInfoDialog?.show()
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

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mRoomData = data as DoubleRoomData
        }
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

    override fun updateNextSongDec(mNext: String, hasNext: Boolean) {
        mCurrentCardView?.updateNextSongDec(mNext, hasNext)
    }

    override fun startGame(mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean) {
        mWordGroup?.visibility = GONE
        toNextSongCardView()
        mCurrentCardView?.visibility = VISIBLE
        mCurrentCardView?.playLyric(mRoomData, mCur, mNext, hasNext)
    }

    override fun changeRound(mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean) {
        toNextSongCardView()
        mCurrentCardView?.playLyric(mRoomData, mCur, mNext, hasNext)
    }

    override fun finishActivity() {
        activity?.finish()
    }

    override fun unLockSelfSuccess() {
        unLockSelf()
    }

    override fun picked() {
        mRightZanView?.addZanXin(mRightZanView)
    }

    fun toNextSongCardView() {
        if (mDoubleSingCardView1.visibility == VISIBLE) {
            mDoubleSingCardView1.goOut()
            mDoubleSingCardView2.centerScale()
            mCurrentCardView = mDoubleSingCardView2
        } else if (mDoubleSingCardView2.visibility == VISIBLE) {
            mDoubleSingCardView2.goOut()
            mDoubleSingCardView1.centerScale()
            mCurrentCardView = mDoubleSingCardView1
        } else {
            mDoubleSingCardView1.centerScale()
            mCurrentCardView = mDoubleSingCardView1
        }
    }

    override fun gameEnd(doubleEndCombineRoomPushEvent: DoubleEndCombineRoomPushEvent) {
        activity?.finish()
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_DOUBLE_END)
                .withSerializable("roomData", mRoomData)
                .navigation()
    }

    override fun showLockState(userID: Int, lockState: Boolean) {
        if (!lockState) {
            if (userID.toLong() == MyUserInfoManager.getInstance().uid) {
                unLockSelf()
            } else {
                unLockOther()
            }
            mCurrentCardView?.updateLockState()
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

    override fun onBackPressed(): Boolean {
        exitLogin()
        return true
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

        mUnlockTv?.visibility = GONE
        mRightLockIcon?.visibility = GONE
        mRightNameTv?.text = MyUserInfoManager.getInstance().nickName
        mRightNameTv?.visibility = VISIBLE
    }

    override fun destroy() {
        super.destroy()
        countDownTimer?.dispose()
    }

    override fun showNoLimitDurationState(noLimit: Boolean) {
        MyLog.d(mTag, "showNoLimitDurationState noLimit is $noLimit")
        if (noLimit) {
            mNoLimitIcon?.visibility = VISIBLE
            mCountDownTv?.visibility = GONE
            countDownTimer?.dispose()
        } else {
            mNoLimitIcon?.visibility = GONE
            mCountDownTv?.visibility = VISIBLE
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
