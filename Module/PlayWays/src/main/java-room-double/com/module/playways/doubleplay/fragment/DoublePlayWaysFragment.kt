package com.module.playways.doubleplay.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.dialog.PersonInfoDialog
import com.component.report.fragment.QuickFeedbackFragment
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.pbLocalModel.LocalCombineRoomMusic
import com.module.playways.doubleplay.pbLocalModel.LocalGameItemInfo
import com.module.playways.doubleplay.pbLocalModel.LocalGamePanelInfo
import com.module.playways.doubleplay.pbLocalModel.LocalGameSenceDataModel
import com.module.playways.doubleplay.presenter.DoubleCorePresenter
import com.module.playways.doubleplay.pushEvent.DoubleEndCombineRoomPushEvent
import com.module.playways.doubleplay.view.DoubleChatSenceView
import com.module.playways.doubleplay.view.DoubleGameSenceView
import com.module.playways.doubleplay.view.DoubleSingSenceView
import com.module.playways.grab.room.invite.fragment.InviteFriendFragment2
import com.module.playways.view.ZanView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.EMsgRoomMediaType
import com.zq.live.proto.Common.ESceneType
import com.zq.mediaengine.kit.ZqEngineKit
import kotlin.properties.Delegates


class DoublePlayWaysFragment : BaseFragment(), IDoublePlayView {
    val mTag = "DoublePlayWaysFragment"
    private var mPaddingView: View? = null
    private var mReportTv: TextView? = null
    private var mExitIv: ImageView? = null
    private var mLeftAvatarSdv: SimpleDraweeView? = null
    private var mLeftLockIcon: ImageView? = null
    private var mLeftNameTv: ExTextView? = null
    private var mUnlockTv: ExTextView? = null
    private var mRightAvatarSdv: SimpleDraweeView? = null   //右边固定是自己
    private var mRightLockIcon: ImageView? = null
    private var mRightNameTv: ExTextView? = null
    private var mCountDownTv: ExTextView? = null
    private var mNoLimitTip: ExTextView? = null
    private var mNoLimitIcon: ExImageView? = null
    private var mZanDisplayView: ZanView? = null
    private var mDialogPlus: DialogPlus? = null
    private var mChangeSenceDialog: DialogPlus? = null
    private var mReceiveChangeSenceDialog: DialogPlus? = null
    private var mViewPager: ViewPager? = null
    private var mChatTagTv: ExTextView? = null
    private var mGameTagTv: ExTextView? = null
    private var mSingTagTv: ExTextView? = null

    private var mDoubleSingSenceView: DoubleSingSenceView? = null
    private var mDoubleChatSenceView: DoubleChatSenceView? = null
    private var mDoubleGameSenceView: DoubleGameSenceView? = null

    lateinit var mDoubleCorePresenter: DoubleCorePresenter

    private var mCountDownScaleAnimators: AnimatorSet? = null

    var mRoomData: DoubleRoomData? = null

    var countDownTimer: HandlerTaskTimer? = null

    var mHasInit: Boolean = false

    val mUiHandler = Handler(Looper.getMainLooper())

    var mPagerPosition: Int by Delegates.observable(1, { _, oldPositon, newPosition ->
        mViewPager?.setCurrentItem(newPosition, false)
        when (oldPositon) {
            0 -> {
                mDoubleChatSenceView?.unselected()
            }
            1 -> {
                mDoubleGameSenceView?.unselected()
            }
            2 -> {
                mDoubleSingSenceView?.unselected()
            }
        }
        when (newPosition) {
            0 -> {
                mDoubleChatSenceView?.selected()
                mChatTagTv?.isSelected = true
                mGameTagTv?.isSelected = false
                mSingTagTv?.isSelected = false
            }
            1 -> {
                mDoubleGameSenceView?.selected()
                mChatTagTv?.isSelected = false
                mGameTagTv?.isSelected = true
                mSingTagTv?.isSelected = false
            }
            2 -> {
                mDoubleSingSenceView?.selected()
                mChatTagTv?.isSelected = false
                mGameTagTv?.isSelected = false
                mSingTagTv?.isSelected = true
            }
        }
    })


    override fun initView(): Int {
        return R.layout.double_play_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mHasInit = true
        if (mRoomData == null) {
            MyLog.w(mTag, "initData mRoomData is null")
            return
        }

        ZqEngineKit.getInstance().muteLocalAudioStream(false)
        U.getSoundUtils().preLoad(mTag, R.raw.double_chat_cd)
        MyLog.w(mTag, "initData mRoomData='${mRoomData}'")
        mReportTv = rootView.findViewById<View>(R.id.report_tv) as TextView
        mExitIv = rootView.findViewById<View>(R.id.exit_iv) as ImageView
        mLeftAvatarSdv = rootView.findViewById<View>(R.id.left_avatar_sdv) as SimpleDraweeView
        mLeftLockIcon = rootView.findViewById<View>(R.id.left_lock_icon) as ImageView
        mLeftNameTv = rootView.findViewById<View>(R.id.left_name_tv) as ExTextView
        mRightAvatarSdv = rootView.findViewById<View>(R.id.right_avatar_sdv) as SimpleDraweeView
        mRightLockIcon = rootView.findViewById<View>(R.id.right_lock_icon) as ImageView
        mRightNameTv = rootView.findViewById<View>(R.id.right_name_tv) as ExTextView
        mViewPager = rootView.findViewById<View>(R.id.viewpager) as ViewPager
        mUnlockTv = rootView.findViewById<View>(R.id.unlock_tv) as ExTextView
        mCountDownTv = rootView.findViewById<View>(R.id.count_down_tv) as ExTextView
        mNoLimitIcon = rootView.findViewById(R.id.no_limit_icon) as ExImageView
        mNoLimitTip = rootView.findViewById(R.id.no_limit_tip) as ExTextView
        mChatTagTv = rootView.findViewById(R.id.chat_tag_tv) as ExTextView
        mGameTagTv = rootView.findViewById(R.id.game_tag_tv) as ExTextView
        mSingTagTv = rootView.findViewById(R.id.sing_tag_tv) as ExTextView
        mZanDisplayView = rootView.findViewById(R.id.zan_display_view) as ZanView
        mPaddingView = rootView.findViewById(R.id.padding_view)

        (mPaddingView?.layoutParams as ConstraintLayout.LayoutParams).topMargin = U.getStatusBarUtil().getStatusBarHeight(activity)

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

        mRightAvatarSdv?.setDebounceViewClickListener {
            val info = mRoomData!!.userLockInfoMap[MyUserInfoManager.getInstance().uid.toInt()]
            if (info != null && !info.isHasLock) {
                showPersonInfoView(MyUserInfoManager.getInstance().uid.toInt())
            }
        }

        mChatTagTv?.setDebounceViewClickListener {
            if (mRoomData!!.sceneType != ESceneType.ST_Chat.value)
                confirmChangeSenceDialog(mChatTagTv?.text.toString(), ESceneType.ST_Chat.value)
        }

        mGameTagTv?.setDebounceViewClickListener {
            if (mRoomData!!.sceneType != ESceneType.ST_Game.value)
                confirmChangeSenceDialog(mGameTagTv?.text.toString(), ESceneType.ST_Game.value)
        }

        mSingTagTv?.setDebounceViewClickListener {
            if (mRoomData!!.sceneType != ESceneType.ST_Sing.value)
                confirmChangeSenceDialog(mSingTagTv?.text.toString(), ESceneType.ST_Sing.value)
        }

        mExitIv?.setDebounceViewClickListener {
            confirmExitRoom()
        }

        mRightNameTv?.setDebounceViewClickListener {}

        mUnlockTv?.setDebounceViewClickListener {
            mDoubleCorePresenter.unLockInfo()
        }

        initPager()

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
                mLeftNameTv?.setDebounceViewClickListener {
                    U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(activity, InviteFriendFragment2::class.java)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .addDataBeforeAdd(0, InviteFriendFragment2.FROM_DOUBLE_ROOM)
                            .addDataBeforeAdd(1, mRoomData!!.gameId)
                            .addDataBeforeAdd(2, EMsgRoomMediaType.EMR_AUDIO.value)
                            .build())
                }
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

        val objectAnimator1 = ObjectAnimator.ofFloat<View>(mCountDownTv, View.SCALE_X, 1.5f, 1.0f)
        val objectAnimator2 = ObjectAnimator.ofFloat<View>(mCountDownTv, View.SCALE_Y, 1.5f, 1.0f)
        mCountDownScaleAnimators = AnimatorSet();
        mCountDownScaleAnimators?.setDuration(1000)
        mCountDownScaleAnimators?.playTogether(objectAnimator1, objectAnimator2)
        mDoubleGameSenceView?.setFirstGamePanelInfo(mRoomData!!.localGamePanelInfo)
    }


    private fun initPager() {
        mDoubleChatSenceView = DoubleChatSenceView(context)
        mDoubleGameSenceView = DoubleGameSenceView(context)
        mDoubleGameSenceView?.mRoomData = mRoomData

        var mPickFun: () -> Unit = {
            if (mRoomData!!.isRoomPrepared()) {
                mDoubleCorePresenter.pickOther()
            }
            mZanDisplayView?.addZanXin(1)
        }

        mDoubleGameSenceView?.mPickFun = mPickFun

        mDoubleSingSenceView = DoubleSingSenceView(context)
        mDoubleSingSenceView?.mRoomData = mRoomData
        mDoubleSingSenceView?.mPickFun = mPickFun

        val mTabPagerAdapter = object : PagerAdapter() {
            override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
                container.removeView(any as View)
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                var view: View? = when (position) {
                    0 -> mDoubleChatSenceView
                    1 -> mDoubleGameSenceView
                    2 -> mDoubleSingSenceView
                    else -> null
                }

                if (container.indexOfChild(view) == -1) {
                    container.addView(view)
                }
                return view!!
            }

            override fun getCount(): Int {
                return 3
            }

            override fun isViewFromObject(view: View, any: Any): Boolean {
                return view === any
            }
        }

        mViewPager?.adapter = mTabPagerAdapter
        mViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                mPagerPosition = position
            }
        })
        mViewPager?.offscreenPageLimit = 3

        mPagerPosition = getPagerPositionByScene(mRoomData!!.sceneType)
    }

    private fun getPagerPositionByScene(scene: Int): Int {
        return when (scene) {
            ESceneType.ST_Chat.value -> 0
            ESceneType.ST_Game.value -> 1
            ESceneType.ST_Sing.value -> 2
            else -> 0
        }
    }

    private fun startCountDown() {
        val take = (mRoomData!!.config?.durationTimeMs ?: 0) / 1000
        MyLog.d(mTag, "startCountDown take is " + take)
        countDownTimer = HandlerTaskTimer.newBuilder().interval(1000)
                .take(take)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        var leftSecond = take - t
                        if (leftSecond > 10) {
                            mCountDownTv?.text = U.getDateTimeUtils().formatTimeStringForDate((leftSecond * 1000).toLong(), "mm:ss")
                        } else {
                            U.getSoundUtils().play(mTag, R.raw.double_chat_cd)
                            mCountDownTv?.text = "${leftSecond}s"
                            mCountDownTv?.setTextColor(Color.parseColor("#FFC15B"))
                            mCountDownScaleAnimators?.cancel()
                            mCountDownScaleAnimators?.start()
                        }
                    }

                    override fun onComplete() {
                        mDoubleCorePresenter.closeByTimeOver()
                    }
                })
    }

    fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                click(v)
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

    private fun confirmChangeSenceDialog(text: String, sceneType: Int) {
        if (!mRoomData!!.isRoomPrepared()) {
            U.getToastUtil().showShort("人齐了才可以开始玩哦～")
        } else {
            val tipsDialogView = TipsDialogView.Builder(context)
                    .setMessageTip("邀请对方一起${text}？")
                    .setConfirmTip("是的")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View) {
                            mChangeSenceDialog?.dismiss()
                            mDoubleCorePresenter?.sendChangeScene(sceneType)
                        }
                    })
                    .setCancelBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View) {
                            mChangeSenceDialog?.dismiss()
                        }
                    })
                    .build()

            mChangeSenceDialog = DialogPlus.newDialog(context!!)
                    .setContentHolder(ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create()
            mChangeSenceDialog?.show()
        }
    }

    private fun receiveChangeSenceDialog(text: String, sceneType: Int) {
        mChangeSenceDialog?.dismiss()
        mDialogPlus?.dismiss()
        mPersonInfoDialog?.dismiss()
        mReceiveChangeSenceDialog?.dismiss()
        mUiHandler.postDelayed({
            val tipsDialogView = TipsDialogView.Builder(context)
                    .setMessageTip(text)
                    .setConfirmTip("同意")
                    .setCancelTip("不同意")
                    .setConfirmBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View) {
                            mReceiveChangeSenceDialog?.dismiss()
                            mDoubleCorePresenter?.agreeChangeScene(sceneType)
                        }
                    })
                    .setCancelBtnClickListener(object : AnimateClickListener() {
                        override fun click(view: View) {
                            mReceiveChangeSenceDialog?.dismiss()
                            mDoubleCorePresenter?.refuseChangeScene(sceneType)
                        }
                    })
                    .build()

            mReceiveChangeSenceDialog = DialogPlus.newDialog(context!!)
                    .setContentHolder(ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create()
            mReceiveChangeSenceDialog?.show()
        }, 500)
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
//                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                .setBorderWidth(2f.dp().toFloat())
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
        mDoubleSingSenceView?.updateNextSongDec(mNext, hasNext)
    }

    override fun startSing(mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean) {
        mDoubleSingSenceView?.startSing(mRoomData!!, mCur, mNext, hasNext)
    }

    override fun changeRound(mCur: LocalCombineRoomMusic, mNext: String, hasNext: Boolean) {
        mDoubleSingSenceView?.changeRound(mRoomData!!, mCur, mNext, hasNext)
    }

    override fun finishActivity() {
        activity?.finish()
    }

    override fun unLockSelfSuccess() {
        unLockSelf()
    }

    override fun updateGameSenceData(localGameSenceDataModel: LocalGameSenceDataModel) {
        mDoubleGameSenceView?.setData(localGameSenceDataModel)
    }

    override fun showGameSceneGamePanel(localGamePanelInfo: LocalGamePanelInfo) {
        mDoubleGameSenceView?.showGamePanel(localGamePanelInfo)
    }

    override fun showGameSceneGameCard(localGameItemInfo: LocalGameItemInfo) {
        mDoubleGameSenceView?.playGame(localGameItemInfo)
    }

    override fun updateGameSceneSelectState(userInfoModel: UserInfoModel, panelSeq: Int, itemID: Int, isChoiced: Boolean) {
        mDoubleGameSenceView?.changeChoiceGameState(userInfoModel, panelSeq, itemID, isChoiced)
    }

    override fun updateGameScene(sceneType: Int) {
        mPagerPosition = getPagerPositionByScene(sceneType)
    }

    override fun askSceneChange(sceneType: Int, str: String) {
        receiveChangeSenceDialog(str, sceneType)
    }

    /**
     * 进入agora
     */
    override fun joinAgora() {
        mDoubleSingSenceView?.joinAgora()
        mDoubleChatSenceView?.joinAgora()
        mDoubleGameSenceView?.joinAgora()

        mLeftNameTv?.setOnClickListener(null)

        mReportTv?.visibility = VISIBLE
        mReportTv?.setDebounceViewClickListener {
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

        //只有主动邀请的人打点
        if (mRoomData!!.inviterId != null && mRoomData!!.inviterId == MyUserInfoManager.getInstance().uid) {
            if (mRoomData!!.isGrabInviteRoom()) {
                StatisticsAdapter.recordCountEvent("cp", "invite2_success", null)
            } else if (mRoomData!!.isCreateRoom()) {
                StatisticsAdapter.recordCountEvent("game_cp", "invite3_success", null)
            }
        }
    }

    override fun noMusic() {
        mDoubleSingSenceView?.noMusic()
    }

    override fun picked(count: Int) {
        mZanDisplayView?.addZanXin(count)
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
        mDoubleGameSenceView?.destroy()
        mDoubleChatSenceView?.destroy()
        mDoubleSingSenceView?.destroy()
        mCountDownScaleAnimators?.cancel()
        mUiHandler.removeCallbacksAndMessages(null)
        U.getSoundUtils().release(mTag)
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
            mDoubleSingSenceView?.updateLockState()
            mDoubleGameSenceView?.updateLockState()
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
