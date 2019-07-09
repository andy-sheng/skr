package com.module.playways.doubleplay.fragment

import android.graphics.Color
import android.os.Bundle
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
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExFrameLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.doubleplay.presenter.DoubleCorePresenter
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent
import com.module.playways.doubleplay.view.DoubleSingCardView
import com.module.playways.grab.room.invite.fragment.InviteFriendFragment2
import com.module.playways.songmanager.OwnerManagerActivity
import com.module.playways.songmanager.SongManageData
import com.module.playways.view.ZanView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.dialog.PersonInfoDialog
import com.zq.live.proto.Common.EMsgRoomMediaType
import com.zq.mediaengine.kit.ZqEngineKit
import com.zq.report.fragment.QuickFeedbackFragment


class DoublePlayWaysFragment : BaseFragment(), IDoublePlayView {
    val mTag = "DoublePlayWaysFragment"
    private var mReportTv: TextView? = null
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
    private var mNoLimitTip: ExTextView? = null
    private var mMicTv: TextView? = null
    private var mMicIv: ExImageView? = null
    private var mNoLimitIcon: ExImageView? = null
    private var mWordArea: ExFrameLayout? = null
    private var mPickIv: ImageView? = null
    private var mSelectIv: ImageView? = null
    private var mRightZanView: ZanView? = null
    private var mLeftZanView: ZanView? = null
    private var mDialogPlus: DialogPlus? = null

    lateinit var mDoubleCorePresenter: DoubleCorePresenter
    var mRoomData: DoubleRoomData? = null

    var countDownTimer: HandlerTaskTimer? = null

    lateinit var mDoubleSingCardView1: DoubleSingCardView
    lateinit var mDoubleSingCardView2: DoubleSingCardView

    var mCurrentCardView: DoubleSingCardView? = null

    var mHasInit: Boolean = false

    override fun initView(): Int {
        return R.layout.double_play_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mHasInit = true
        if (mRoomData == null) {
            MyLog.w(mTag, "initData mRoomData is null")
            return
        }

        MyLog.w(mTag, "initData mRoomData='${mRoomData}'")
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
        mCountDownTv = mRootView.findViewById<View>(R.id.count_down_tv) as ExTextView
        mRightZanView = mRootView.findViewById<View>(R.id.right_zanView) as ZanView
        mLeftZanView = mRootView.findViewById<View>(R.id.left_zanView) as ZanView
        mNoLimitIcon = mRootView.findViewById(R.id.no_limit_icon) as ExImageView
        mNoLimitTip = mRootView.findViewById(R.id.no_limit_tip) as ExTextView
        mMicTv = mRootView.findViewById(R.id.mic_tv) as TextView
        mWordArea = mRootView.findViewById(R.id.word_area) as ExFrameLayout
        mDoubleSingCardView1 = mRootView.findViewById(R.id.show_card1) as DoubleSingCardView
        mDoubleSingCardView2 = mRootView.findViewById(R.id.show_card2) as DoubleSingCardView

        mWordTv?.text = mRoomData!!.config?.roomSignature

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
                if (!mRoomData!!.isRoomPrepared()) {
                    U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(activity, InviteFriendFragment2::class.java)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .addDataBeforeAdd(0, InviteFriendFragment2.FROM_DOUBLE_ROOM)
                            .addDataBeforeAdd(1, mRoomData!!.gameId)
                            .addDataBeforeAdd(2, EMsgRoomMediaType.EMR_AUDIO.value)
                            .build()
                    )
                    return
                }

                val info = mRoomData!!.userLockInfoMap[mRoomData!!.getAntherUser()?.userId]
                if (info != null && !info.isHasLock) {
                    showPersonInfoView(info.userID)
                }
            }
        })

        mRightAvatarSdv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                val info = mRoomData!!.userLockInfoMap[MyUserInfoManager.getInstance().uid.toInt()]
                if (info != null && !info.isHasLock) {
                    showPersonInfoView(MyUserInfoManager.getInstance().uid.toInt())
                }
            }
        })

        mExitIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 退出
                confirmExitRoom()
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

        mPickIv?.setOnClickListener {
            if (mRoomData!!.isRoomPrepared()) {
                mDoubleCorePresenter.pickOther()
            }
            mRightZanView?.addZanXin(1)
        }

        mSelectIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 点歌
                if (mRoomData!!.isRoomPrepared()) {
                    OwnerManagerActivity.open(activity, SongManageData(mRoomData))
                } else {
                    U.getToastUtil().showShort("房间里还没有人哦～")
                }
            }
        })

        mDoubleCorePresenter = DoubleCorePresenter(mRoomData!!, this)
        addPresent(mDoubleCorePresenter)

        if (mRoomData!!.isMatchRoom()) {
            mNoLimitTip?.visibility = VISIBLE
        }

        if (mRoomData!!.isCreateRoom()) {
            if (mRoomData!!.isRoomPrepared()) {
                unLockOther()
                unLockSelf()
            } else {
                unLockSelf()
                toInviteUI()
                mLeftNameTv?.setOnClickListener(object : DebounceViewClickListener() {
                    override fun clickValid(v: View?) {
                        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(activity, InviteFriendFragment2::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, InviteFriendFragment2.FROM_DOUBLE_ROOM)
                                .addDataBeforeAdd(1, mRoomData!!.gameId)
                                .addDataBeforeAdd(2, EMsgRoomMediaType.EMR_AUDIO.value)
                                .build())
                    }
                })
            }
        } else {
            if (mRoomData!!.needMaskUserInfo) {
                selfLockState()
                guestLockState()
            } else {
                unLockOther()
                unLockSelf()
            }
        }


        MyLog.d(mTag, "mRoomData.enableNoLimitDuration " + mRoomData!!.enableNoLimitDuration)
        if (mRoomData!!.enableNoLimitDuration) {
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
        val take = (mRoomData!!.config?.durationTimeMs ?: 0) / 1000
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

        mPersonInfoDialog = PersonInfoDialog.Builder(activity, QuickFeedbackFragment.FROM_DOUBLE_ROOM, userID, false, false)
                .setRoomID(mRoomData!!.gameId)
                .build()
        mPersonInfoDialog?.show()
    }

    private fun confirmExitRoom() {
        if (!mRoomData!!.isRoomPrepared()) {
            activity?.finish()
        } else {
            val tipsDialogView = TipsDialogView.Builder(context)
                    .setMessageTip("确定退出唱聊房吗？")
                    .setConfirmTip("确定")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View) {
                            mDialogPlus?.dismiss()
                            activity?.finish()
                            ARouter.getInstance()
                                    .build(RouterConstants.ACTIVITY_DOUBLE_END)
                                    .withSerializable("roomData", mRoomData)
                                    .navigation()
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
    }

    override fun setData(type: Int, data: Any?) {
        if (type == 0) {
            mRoomData = data as DoubleRoomData
            if (mHasInit) initData(null)
        }
    }

    /**
     * 自己锁定状态
     */
    fun selfLockState() {
        AvatarUtils.loadAvatarByUrl(mRightAvatarSdv, AvatarUtils.newParamsBuilder(mRoomData!!.getSelfAvatar())
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
        AvatarUtils.loadAvatarByUrl(mLeftAvatarSdv, AvatarUtils.newParamsBuilder(mRoomData!!.getPartnerAvatar())
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
        mWordArea?.visibility = GONE
        toNextSongCardView()
        mCurrentCardView?.visibility = VISIBLE
        mCurrentCardView?.playLyric(mRoomData!!, mCur, mNext, hasNext)
    }

    override fun changeRound(mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean) {
        toNextSongCardView()
        mCurrentCardView?.playLyric(mRoomData!!, mCur, mNext, hasNext)
    }

    override fun finishActivity() {
        activity?.finish()
    }

    override fun unLockSelfSuccess() {
        unLockSelf()
    }

    /**
     * 进入agora
     */
    override fun joinAgora() {
        val drawable = DrawableCreator.Builder()
                .setSelectedDrawable(U.getDrawable(R.drawable.skr_jingyin_able))
                .setUnSelectedDrawable(U.getDrawable(R.drawable.srf_bimai))
                .build()

        mMicIv?.background = drawable

        mMicIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 开关麦克
                val isSelected = mMicIv?.isSelected ?: false
                ZqEngineKit.getInstance().muteLocalAudioStream(!isSelected)
                mMicIv?.setSelected(!isSelected)
            }
        })

        mLeftNameTv?.setOnClickListener(null)

        mReportTv?.visibility = VISIBLE
        mReportTv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                // 举报
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(activity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_DOUBLE_ROOM)
                                .addDataBeforeAdd(1, QuickFeedbackFragment.REPORT)
                                .addDataBeforeAdd(2, mRoomData!!.getAntherUser()?.userId ?: 0)
                                .setEnterAnim(com.component.busilib.R.anim.slide_in_bottom)
                                .setExitAnim(com.component.busilib.R.anim.slide_out_bottom)
                                .build())
            }
        })

        //只有主动邀请的人打点
        if (mRoomData!!.inviterId != null && mRoomData!!.inviterId == MyUserInfoManager.getInstance().uid) {
            if (mRoomData!!.isGrabInviteRoom()) {
                StatisticsAdapter.recordCountEvent("cp", "invite3_success", null)
            } else if (mRoomData!!.isCreateRoom()) {
                StatisticsAdapter.recordCountEvent("cp", "invite2_success", null)
            }
        }
    }

    override fun noMusic() {
        mDoubleSingCardView1.visibility = GONE
        mDoubleSingCardView2.visibility = GONE
        mWordArea?.visibility = VISIBLE
    }

    override fun picked(count: Int) {
        mLeftZanView?.addZanXin(count)
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
        }
    }

    private fun toInviteUI() {
        FrescoWorker.loadImage(mLeftAvatarSdv, ImageFactory.newResImage(R.drawable.double_invite)
                .build<BaseImage>())

        val drawable = DrawableCreator.Builder()
                .setCornersRadius(U.getDisplayUtils().dip2px(16f).toFloat())
                .setSolidColor(Color.parseColor("#FFC15B"))
                .build()

        mLeftLockIcon?.visibility = GONE
        mLeftNameTv?.background = drawable
        mLeftNameTv?.text = "邀请好友"
        mLeftNameTv?.setTextColor(Color.parseColor("#404A9A"))
    }

    /**
     * 别人解锁状态
     */
    fun unLockOther() {
        if (mRoomData!!.isMatchRoom()) {
            if (mRoomData!!.enableNoLimitDuration) {
                AvatarUtils.loadAvatarByUrl(mLeftAvatarSdv, AvatarUtils.newParamsBuilder(mRoomData?.getAntherUser()?.avatar)
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setBorderColor(Color.WHITE)
                        .build())

                mLeftNameTv?.setTextColor(U.getColor(R.color.white_trans_50))
                mLeftNameTv?.text = mRoomData?.getAntherUser()?.getNicknameRemark()
                mLeftNameTv?.visibility = VISIBLE
                mLeftLockIcon?.visibility = GONE
            } else {
                mLeftLockIcon?.visibility = VISIBLE
                mLeftLockIcon?.background = U.getDrawable(R.drawable.double_light)
            }
        } else {
            AvatarUtils.loadAvatarByUrl(mLeftAvatarSdv, AvatarUtils.newParamsBuilder(mRoomData?.getAntherUser()?.avatar)
                    .setCircle(true)
                    .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                    .setBorderColor(Color.WHITE)
                    .build())

            val drawable = DrawableCreator.Builder()
                    .setCornersRadius(U.getDisplayUtils().dip2px(16f).toFloat())
                    .setSolidColor(U.getColor(R.color.black_trans_20))
                    .build()

            mLeftNameTv?.background = drawable
            mLeftNameTv?.text = mRoomData?.getAntherUser()?.getNicknameRemark()
            mLeftNameTv?.setTextColor(U.getColor(R.color.white_trans_50))
            mLeftNameTv?.visibility = VISIBLE
            mLeftLockIcon?.visibility = GONE
        }
    }

    override fun onBackPressed(): Boolean {
        confirmExitRoom()
        return true
    }

    /**
     * 自己解锁状态
     */
    fun unLockSelf() {
        if (mRoomData!!.isMatchRoom()) {
            if (mRoomData!!.enableNoLimitDuration) {
                AvatarUtils.loadAvatarByUrl(mRightAvatarSdv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setBorderColor(Color.WHITE)
                        .build())

                mUnlockTv?.visibility = GONE
                mRightNameTv?.text = MyUserInfoManager.getInstance().nickName
                mRightNameTv?.visibility = VISIBLE
                mRightLockIcon?.visibility = GONE
            } else {
                mUnlockTv?.visibility = GONE
                mRightLockIcon?.visibility = VISIBLE
                mRightLockIcon?.background = U.getDrawable(R.drawable.double_light)
            }
        } else {
            if (mRoomData!!.enableNoLimitDuration) {
                AvatarUtils.loadAvatarByUrl(mRightAvatarSdv, AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setBorderColor(Color.WHITE)
                        .build())

                mUnlockTv?.visibility = GONE
                mRightNameTv?.text = MyUserInfoManager.getInstance().nickName
                mRightNameTv?.visibility = VISIBLE
                mRightLockIcon?.visibility = GONE
            }
        }
    }

    override fun destroy() {
        super.destroy()
        countDownTimer?.dispose()
        mLeftZanView?.stop()
        mRightZanView?.stop()
    }

    override fun showNoLimitDurationState(noLimit: Boolean) {
        MyLog.d(mTag, "showNoLimitDurationState noLimit is $noLimit")
        if (noLimit) {
            mLeftLockIcon?.visibility = GONE
            mRightLockIcon?.visibility = GONE
            mNoLimitIcon?.visibility = VISIBLE
            mCountDownTv?.visibility = GONE
            mNoLimitTip?.visibility = GONE
            unLockSelf()
            unLockOther()
            countDownTimer?.dispose()
            mCurrentCardView?.updateLockState()
        } else {
            selfLockState()
            guestLockState()
            mNoLimitIcon?.visibility = GONE
            mCountDownTv?.visibility = VISIBLE
            mNoLimitTip?.visibility = VISIBLE
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
