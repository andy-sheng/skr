package com.module.playways.mic.room

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import android.view.animation.Animation
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.constans.GameModeType
import com.component.dialog.PersonInfoDialog
import com.component.person.event.ShowPersonCardEvent
import com.component.report.fragment.QuickFeedbackFragment
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.IHomeService
import com.module.playways.R
import com.module.playways.grab.room.inter.IGrabVipView
import com.module.playways.grab.room.invite.fragment.InviteFriendFragment2
import com.module.playways.grab.room.presenter.VipEnterPresenter
import com.module.playways.grab.room.view.VIPEnterView
import com.module.playways.grab.room.view.control.OthersSingCardView
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.grab.room.view.control.SingBeginTipsCardView
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.listener.AnimationListener
import com.module.playways.listener.SVGAListener
import com.module.playways.mic.match.model.JoinMicRoomRspModel
import com.module.playways.mic.room.bottom.MicBottomContainerView
import com.module.playways.mic.room.event.MicWantInviteEvent
import com.module.playways.mic.room.model.MicPlayerInfoModel
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.module.playways.mic.room.presenter.MicCorePresenter
import com.module.playways.mic.room.top.MicTopContentView
import com.module.playways.mic.room.top.MicTopOpView
import com.module.playways.mic.room.ui.IMicRoomView
import com.module.playways.mic.room.ui.MicWidgetAnimationController
import com.module.playways.mic.room.view.*
import com.module.playways.race.match.activity.RaceHomeActivity
import com.module.playways.room.data.H
import com.module.playways.room.gift.event.BuyGiftEvent
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent
import com.module.playways.room.gift.view.ContinueSendView
import com.module.playways.room.gift.view.GiftDisplayView
import com.module.playways.room.gift.view.GiftPanelView
import com.module.playways.room.room.comment.CommentView
import com.module.playways.room.room.comment.listener.CommentViewItemListener
import com.module.playways.room.room.gift.GiftBigAnimationViewGroup
import com.module.playways.room.room.gift.GiftBigContinuousView
import com.module.playways.room.room.gift.GiftContinueViewGroup
import com.module.playways.room.room.gift.GiftOverlayAnimationViewGroup
import com.module.playways.room.room.view.BottomContainerView
import com.module.playways.songmanager.SongManagerActivity
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.MicRoom.EMRoundStatus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_MIC_ROOM)
class MicRoomActivity : BaseActivity(), IMicRoomView, IGrabVipView {

    /**
     * 存起该房间一些状态信息
     */
    internal var mRoomData = MicRoomData()

    internal lateinit var mCorePresenter: MicCorePresenter

    //基础ui组件
    internal lateinit var mInputContainerView: MicInputContainerView
    internal lateinit var mBottomContainerView: MicBottomContainerView
    internal lateinit var mVoiceRecordTipsView: VoiceRecordTipsView
    internal lateinit var mCommentView: CommentView
    internal lateinit var mGiftPanelView: GiftPanelView
    internal lateinit var mContinueSendView: ContinueSendView
    internal lateinit var mTopOpView: MicTopOpView
    internal lateinit var mTopContentView: MicTopContentView

    // 专场ui组件
    lateinit var mTurnInfoCardView: MicTurnInfoCardView  // 下一局
    lateinit var mOthersSingCardView: OthersSingCardView// 他人演唱卡片
    lateinit var mSelfSingCardView: SelfSingCardView // 自己演唱卡片
    lateinit var mSingBeginTipsCardView: SingBeginTipsCardView

    private lateinit var mAddSongIv: ImageView
    internal lateinit var mRightOpView: MicRightOpView

    internal var mVIPEnterView: VIPEnterView? = null
    private lateinit var mHasSelectSongNumTv: ExTextView
    private lateinit var mMicSeatView: MicSeatView

    // 都是dialogplus
    private var mPersonInfoDialog: PersonInfoDialog? = null
    private var mVoiceControlPanelView: MicVoiceControlPanelView? = null
    private var mMicSettingView: MicSettingView? = null
    private var mGameRuleDialog: DialogPlus? = null
    private var mTipsDialogView: TipsDialogView? = null

    internal var mVipEnterPresenter: VipEnterPresenter? = null

    lateinit var mVoiceRecordUiController: VoiceRecordUiController
    val mWidgetAnimationController = MicWidgetAnimationController(this)

    val mUiHanlder = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.mic_room_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        // 销毁其他的除一唱到底页面所有界面
        for (activity in U.getActivityUtils().activityList) {
            if (activity === this) {
                continue
            }
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue
            }
            if (activity is RaceHomeActivity) {
                continue
            }
            activity.finish()
        }

        val joinRaceRoomRspModel = intent.getSerializableExtra("JoinMicRoomRspModel") as JoinMicRoomRspModel?
        joinRaceRoomRspModel?.let {
            mRoomData.loadFromRsp(it)
        }
        H.micRoomData = mRoomData
        H.curType = GameModeType.GAME_MODE_MIC

        mCorePresenter = MicCorePresenter(mRoomData, this)
        addPresent(mCorePresenter)
        mVipEnterPresenter = VipEnterPresenter(this, mRoomData)
        addPresent(mVipEnterPresenter)
        // 请保证从下面的view往上面的view开始初始化
        findViewById<View>(R.id.main_act_container).setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mInputContainerView.hideSoftInput()
            }
            false
        }
        initInputView()
        initBottomView()
        initCommentView()
        initGiftPanelView()
        initGiftDisplayView()
        initTopView()
        initTurnSenceView()
        initSingStageView()

        initRightView()
        initVipEnterView()
        initMicSeatView()

        mCorePresenter.onOpeningAnimationOver()

        mUiHanlder.postDelayed(Runnable {
            mWidgetAnimationController.close()
        }, 500)
        if (MyLog.isDebugLogOpen()) {
            val viewStub = findViewById<ViewStub>(R.id.debug_log_view_stub)
            val debugLogView = DebugLogView(viewStub)
            debugLogView.tryInflate()
        }

        if (U.getPreferenceUtils().getSettingBoolean("is_first_enter_microom", true)) {
            U.getPreferenceUtils().setSettingBoolean("is_first_enter_microom", false)
            showGameRuleDialog()
        }

        MyUserInfoManager.getInstance().myUserInfo?.let {
            mVipEnterPresenter?.addNotice(MyUserInfo.toUserInfoModel(it))
        }


        U.getStatusBarUtil().setTransparentBar(this, false)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun destroy() {
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        super.destroy()
        dismissDialog()
        mGiftPanelView?.destroy()
        mSelfSingCardView?.destroy()
        H.reset()
    }

    override fun finish() {
        super.finish()
        MyLog.w(TAG, "finish")
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }


    private fun hideAllSceneView(exclude: Any?) {
        if (mSelfSingCardView != exclude) {
            mSelfSingCardView.setVisibility(View.GONE)
        }
        if (mOthersSingCardView != exclude) {
            mOthersSingCardView.setVisibility(View.GONE)
        }
        if (mTurnInfoCardView != exclude) {
            mTurnInfoCardView.visibility = View.GONE
        }
    }

    private fun initSingStageView() {
        var rootView = findViewById<View>(R.id.main_act_container)
        mSelfSingCardView = SelfSingCardView(rootView)
        mSelfSingCardView?.setListener {
            //            removeNoAccSrollTipsView()
//            removeGrabSelfSingTipView()
            mCorePresenter?.sendRoundOverInfo()
        }
//        mSelfSingCardView?.setListener4FreeMic { mCorePresenter?.sendMyGrabOver("onSelfSingOver") }
        mOthersSingCardView = OthersSingCardView(rootView)
    }

    private fun initRightView() {
        mRightOpView = findViewById(R.id.right_op_view)
    }

    private fun initMicSeatView() {
        mHasSelectSongNumTv = findViewById(R.id.has_select_song_num_tv)
        mMicSeatView = findViewById(R.id.mic_seat_view)
        mMicSeatView.mRoomData = mRoomData
        mHasSelectSongNumTv.setDebounceViewClickListener {
            mMicSeatView.show()
        }
    }

    private fun initVipEnterView() {
        mVIPEnterView = VIPEnterView(findViewById(R.id.vip_enter_view_stub))
    }

    private fun initInputView() {
        mInputContainerView = findViewById(R.id.input_container_view)
        mInputContainerView.setRoomData(mRoomData)
    }

    private fun initTurnSenceView() {
//        mWaitingCardView = findViewById(R.id.wait_card_view)
//        mWaitingCardView.visibility = View.GONE
        mTurnInfoCardView = findViewById(R.id.turn_card_view)
        mTurnInfoCardView.visibility = View.GONE

        mSingBeginTipsCardView = SingBeginTipsCardView(findViewById<ViewStub>(R.id.mic_sing_begin_tips_card_stub))
    }

    private fun initBottomView() {
        mAddSongIv = findViewById(R.id.add_song_iv)
        mAddSongIv.setAnimateDebounceViewClickListener { SongManagerActivity.open(this, mRoomData) }

        run {
            val voiceStub = findViewById<ViewStub>(R.id.voice_record_tip_view_stub)
            mVoiceRecordTipsView = VoiceRecordTipsView(voiceStub)
        }
//        mBottomBgVp = findViewById<ViewGroup>(R.id.bottom_bg_vp)
//        val lp = mBottomBgVp.getLayoutParams() as RelativeLayout.LayoutParams
//        /**
//         * 按比例适配手机
//         */
//        lp.height = U.getDisplayUtils().screenHeight * 284 / 667

        mBottomContainerView = findViewById(R.id.bottom_container_view)
        mBottomContainerView.setRoomData(mRoomData)
        mBottomContainerView.setListener(object : BottomContainerView.Listener() {
            override fun showInputBtnClick() {
                dismissDialog()
                mInputContainerView.showSoftInput()
            }

            override fun clickRoomManagerBtn() {
                //                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(GrabRoomFragment.this.getActivity(), OwnerManageFragment.class)
                //                        .setAddToBackStack(true)
                //                        .setHasAnimation(true)
                //                        .setEnterAnim(R.anim.slide_right_in)
                //                        .setExitAnim(R.anim.slide_right_out)
                //                        .addDataBeforeAdd(0, mRoomData)
                //                        .build());
//                SongManagerActivity.open(activity, mRoomData)
//                removeManageSongTipView()
            }

            override fun showGiftPanel() {
                mContinueSendView.setVisibility(View.GONE)
//                if (mRoomData.realRoundInfo?.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
//                    mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData!!, mRoomData!!.realRoundInfo!!.subRoundInfo[mRoomData!!.realRoundInfo!!.subRoundSeq - 1].userID))
//                } else {
                mGiftPanelView.show(null)
//                }
            }
        })
    }

    private fun initTopView() {
        mTopOpView = findViewById(R.id.top_op_view)
        mTopOpView.setRoomData(mRoomData)
        mTopOpView.setListener(object : MicTopOpView.Listener {
            override fun onClickVoiceAudition() {
                // 调音面板
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@MicRoomActivity)
                dismissDialog()
                if (mVoiceControlPanelView == null) {
                    mVoiceControlPanelView = MicVoiceControlPanelView(this@MicRoomActivity)
                    mVoiceControlPanelView?.setRoomData(mRoomData)
                }
                mVoiceControlPanelView?.showByDialog()
            }

            override fun onClickFeedBack() {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(this@MicRoomActivity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_MIC_ROOM)
                                .addDataBeforeAdd(1, QuickFeedbackFragment.FEED_BACK)
                                .addDataBeforeAdd(3, mRoomData.gameId)
                                .setEnterAnim(R.anim.slide_in_bottom)
                                .setExitAnim(R.anim.slide_out_bottom)
                                .build())
            }

            override fun closeBtnClick() {
                quitGame()
            }

            override fun onClickGameRule() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@MicRoomActivity)
                showGameRuleDialog()
            }

            override fun onClickSetting() {
                //设置界面
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@MicRoomActivity)
                dismissDialog()
                if (mMicSettingView == null) {
                    mMicSettingView = MicSettingView(this@MicRoomActivity)
                }
                mMicSettingView?.showByDialog()
            }
        })


        mTopContentView = findViewById(R.id.top_content_view)
        mTopContentView.setRoomData(mRoomData)

        mTopContentView.setListener(object : MicTopContentView.Listener {
            override fun clickArrow(open: Boolean) {
                if (open) {
                    mWidgetAnimationController.open()
                } else {
                    mWidgetAnimationController.close()
                }
            }
        })
    }

    private fun showGameRuleDialog() {
        dismissDialog()
        mGameRuleDialog = DialogPlus.newDialog(this)
                .setContentHolder(ViewHolder(R.layout.race_game_rule_view_layout))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setMargin(U.getDisplayUtils().dip2px(16f), -1, U.getDisplayUtils().dip2px(16f), -1)
                .setExpanded(false)
                .setGravity(Gravity.CENTER)
                .create()
        mGameRuleDialog?.show()
    }

    private fun initCommentView() {
        mCommentView = findViewById(R.id.comment_view)
        mCommentView.setListener(CommentViewItemListener { userId ->
            showPersonInfoView(userId)
        })
        mCommentView.roomData = mRoomData
        mVoiceRecordUiController = VoiceRecordUiController(mBottomContainerView.mVoiceRecordBtn, mVoiceRecordTipsView, mCommentView)
    }

    private fun initGiftPanelView() {
        mGiftPanelView = findViewById<View>(R.id.gift_panel_view) as GiftPanelView
        mGiftPanelView.setRoomData(mRoomData)
        mContinueSendView = findViewById<View>(R.id.continue_send_view) as ContinueSendView
        mContinueSendView.mScene = ContinueSendView.EGameScene.GS_Race
        mContinueSendView.setRoomData(mRoomData)
        mContinueSendView.setObserver(object : ContinueSendView.OnVisibleStateListener {
            override fun onVisible(isVisible: Boolean) {
                mBottomContainerView.setOpVisible(!isVisible)
            }
        })
        mGiftPanelView.setIGetGiftCountDownListener(object : GiftDisplayView.IGetGiftCountDownListener {
            override fun getCountDownTs(): Long {
                //                return mGiftTimerPresenter.getCountDownSecond();
                return 0
            }
        })
    }

    private fun initGiftDisplayView() {
        val giftContinueViewGroup = findViewById<GiftContinueViewGroup>(R.id.gift_continue_vg)
        giftContinueViewGroup.setRoomData(mRoomData)
        val giftOverlayAnimationViewGroup = findViewById<GiftOverlayAnimationViewGroup>(R.id.gift_overlay_animation_vg)
        giftOverlayAnimationViewGroup.setRoomData(mRoomData)
        val giftBigAnimationViewGroup = findViewById<GiftBigAnimationViewGroup>(R.id.gift_big_animation_vg)
        giftBigAnimationViewGroup.setRoomData(mRoomData)
        val giftBigContinueView = findViewById<GiftBigContinuousView>(R.id.gift_big_continue_view)
        giftBigAnimationViewGroup.setGiftBigContinuousView(giftBigContinueView)
        //mDengBigAnimation = findViewById<View>(R.id.deng_big_animation) as GrabDengBigAnimationView
    }

    override fun startEnterAnimation(playerInfoModel: UserInfoModel, finishCall: () -> Unit) {
        mVIPEnterView?.enter(playerInfoModel, finishCall)
    }

    private fun showPersonInfoView(userID: Int) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!")
            return
        }
        dismissDialog()
        mInputContainerView.hideSoftInput()
        mPersonInfoDialog = PersonInfoDialog.Builder(this, QuickFeedbackFragment.FROM_MIC_ROOM, userID, false, false)
                .setRoomID(mRoomData.gameId)
                .build()
        mPersonInfoDialog?.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicWantInviteEvent) {
        // 房主想要邀请别人加入游戏
        // 打开邀请面板
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, InviteFriendFragment2::class.java)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, InviteFriendFragment2.FROM_MIC_ROOM)
                .addDataBeforeAdd(1, mRoomData!!.gameId)
                .build()
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowPersonCardEvent) {
        showPersonInfoView(event.uid)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BuyGiftEvent) {
        mContinueSendView.startBuy(event.baseGift, event.receiver)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowHalfRechargeFragmentEvent) {
        val channelService = ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation() as IHomeService
        val baseFragmentClass = channelService.getData(2, null) as Class<android.support.v4.app.Fragment>
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, baseFragmentClass)
                        .setEnterAnim(R.anim.slide_in_bottom)
                        .setExitAnim(R.anim.slide_out_bottom)
                        .setAddToBackStack(true)
                        .setFragmentDataListener(object : FragmentDataListener {
                            override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                                //充值成功
                                if (requestCode == 100 && resultCode == 0) {
                                    mGiftPanelView.updateZS()
//                                    if (mRoomData.realRoundInfo?.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
//                                        mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData!!, mRoomData!!.realRoundInfo!!.subRoundInfo[mRoomData!!.realRoundInfo!!.subRoundSeq - 1].userID))
//                                    } else {
                                    mGiftPanelView.show(null)
//                                    }
                                }
                            }
                        })
                        .setHasAnimation(true)
                        .build())
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
            return
        }
        if (mGiftPanelView.onBackPressed()) {
            return
        }
        dismissDialog()
        mTipsDialogView = TipsDialogView.Builder(this)
                .setMessageTip("确定要退出排位赛吗")
                .setConfirmTip("确定")
                .setCancelTip("取消")
                .setConfirmBtnClickListener {
                    mTipsDialogView?.dismiss(false)
                    quitGame()
                }
                .setCancelBtnClickListener {
                    mTipsDialogView?.dismiss()
                }
                .build()
        mTipsDialogView?.showByDialog()
    }


    fun quitGame() {
//        mCorePresenter.exitRoom("quitGame")
        finish()
    }

    private fun dismissDialog() {
//        mRaceActorPanelView?.dismiss(false)
        mPersonInfoDialog?.dismiss(false)
//        mRaceVoiceControlPanelView?.dismiss(false)
        mGameRuleDialog?.dismiss(false)
        mTipsDialogView?.dismiss(false)
    }

    override fun showWaiting() {
        hideAllSceneView(null)
    }

    override fun singBySelf(hasLastRound: Boolean) {
        hideAllSceneView(null)
        var step2 = {
            mSelfSingCardView.playLyric()
        }

        var step1 = {
            //不是pk 第二轮 都显示 卡片
            if (mRoomData?.realRoundInfo?.status != EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
                mSingBeginTipsCardView.bindData(SVGAListener {
                    step2.invoke()
                })
            } else {
                step2.invoke()
            }
        }

        if (hasLastRound) {
            // 有上一局 肯定要显示下一首
            mTurnInfoCardView.showAnimation(object : AnimationListener {
                override fun onFinish() {
                    step1.invoke()
                }
            })
        } else {
            step1.invoke()
        }
    }

    override fun singByOthers(hasLastRound: Boolean) {
        hideAllSceneView(null)
        var step2 = {
            mOthersSingCardView.bindData()
        }

        var step1 = {
            //不是pk 第二轮 都显示 卡片
            var b1 = mRoomData?.realRoundInfo?.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value
            var b2 = (mRoomData?.realRoundInfo?.isParticipant == false) && (mRoomData?.realRoundInfo?.isEnterInSingStatus == true)
            if (b2 || b1) {
                step2.invoke()
            } else {
                mSingBeginTipsCardView.bindData(SVGAListener {
                    step2.invoke()
                })
            }
        }

        if (hasLastRound) {
            // 有上一局 肯定要显示下一首
            mTurnInfoCardView.showAnimation(object : AnimationListener {
                override fun onFinish() {
                    step1.invoke()
                }
            })
        } else {
            step1.invoke()
        }
    }

    override fun joinNotice(model: MicPlayerInfoModel?) {
        model?.let {
            mVipEnterPresenter?.addNotice(it.userInfo)
        }
    }

    override fun showRoundOver(lastRoundInfo: MicRoundInfoModel?, continueOp: (() -> Unit)?) {
        continueOp?.invoke()
    }

    override fun kickBySomeOne(b: Boolean) {
    }

    override fun dismissKickDialog() {
    }

    override fun gameOver() {
        mCorePresenter.exitRoom("gameOver")
        finish()
    }


}
