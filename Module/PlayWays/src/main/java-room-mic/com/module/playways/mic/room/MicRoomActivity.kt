package com.module.playways.mic.room

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.component.dialog.PersonInfoDialog
import com.component.person.event.ShowPersonCardEvent
import com.component.report.fragment.QuickFeedbackFragment
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.IHomeService
import com.module.playways.R
import com.module.playways.grab.room.inter.IGrabVipView
import com.module.playways.grab.room.presenter.VipEnterPresenter
import com.module.playways.grab.room.view.VIPEnterView
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.mic.match.model.JoinMicRoomRspModel
import com.module.playways.mic.room.bottom.MicBottomContainerView
import com.module.playways.mic.room.presenter.MicCorePresenter
import com.module.playways.mic.room.top.MicTopContentView
import com.module.playways.mic.room.top.MicTopOpView
import com.module.playways.mic.room.ui.IMicRoomView
import com.module.playways.mic.room.ui.MicWidgetAnimationController
import com.module.playways.mic.room.view.*
import com.module.playways.race.match.activity.RaceHomeActivity
import com.module.playways.race.room.event.RaceWantSingChanceEvent
import com.module.playways.race.room.model.RaceRoundInfoModel
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
import com.module.playways.room.song.model.SongModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConstants.ACTIVITY_MIC_ROOM)
class MicRoomActivity : BaseActivity(), IMicRoomView,IGrabVipView {

    /**
     * 存起该房间一些状态信息
     */
    internal var mRoomData = MicRoomData()

    internal lateinit var mCorePresenter: MicCorePresenter

    internal lateinit var mInputContainerView: MicInputContainerView
    internal lateinit var mBottomContainerView: MicBottomContainerView
    internal lateinit var mVoiceRecordTipsView: VoiceRecordTipsView
    internal lateinit var mCommentView: CommentView
    internal lateinit var mGiftPanelView: GiftPanelView
    internal lateinit var mContinueSendView: ContinueSendView
    internal lateinit var mTopOpView: MicTopOpView
    internal lateinit var mTopContentView: MicTopContentView
//    internal lateinit var mRaceTopVsView: RaceTopVsView

    internal lateinit var mRightOpView: MicRightOpView

    //    private lateinit var mRaceSelectSongView: RaceSelectSongView   // 选歌
//    private lateinit var mWaitingCardView: MicWaitingCardView   // 等待中
//    private lateinit var mRaceWantingSignUpCardView: RaceWantingSignUpCardView   // 正在报名卡片
    private lateinit var mTurnInfoCardView: MicTurnInfoCardView  // 下一局
//    private lateinit var mRaceMatchView: RaceMatchView
    private lateinit var mSelfSingLyricView: MicSelfSingLyricView  // 自己唱
    private lateinit var mOtherSingCardView: MicOtherSingCardView   // 别人唱
//    private lateinit var mRaceNoSingCardView: RaceNoSingerCardView    // 无人响应
//    private lateinit var mRaceMiddleResultView: RaceMiddleResultView   // 比赛结果
//    private lateinit var mRacePagerSelectSongView: RacePagerSelectSongView
//    private lateinit var mSignUpView: RaceSignUpBtnView

    internal var mVIPEnterView: VIPEnterView? = null

    // 都是dialogplus
//    private var mRaceActorPanelView: MicActorPanelView? = null  //参与的人
    private var mPersonInfoDialog: PersonInfoDialog? = null
    private var mVoiceControlPanelView: MicVoiceControlPanelView? = null
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
        val joinRaceRoomRspModel = intent.getSerializableExtra("JoinMicRoomRspModel") as JoinMicRoomRspModel?
        joinRaceRoomRspModel?.let {
            mRoomData.loadFromRsp(it)
        }

        mCorePresenter = MicCorePresenter(mRoomData, this)
        addPresent(mCorePresenter)
        mVipEnterPresenter = VipEnterPresenter(this, mRoomData)
        addPresent(mVipEnterPresenter)
        // 请保证从下面的view往上面的view开始初始化
        findViewById<View>(R.id.mic_root_view).setOnTouchListener { v, event ->
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
//        initSelectSongView()
        initResuleView()

        initSingSenceView()
        initRightView()
        initVipEnterView()
        initSelectPagerView()
        initRaceMatchView()
        initSignUpView()

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



    private fun hideAllSceneView() {
        mSelfSingLyricView?.setVisibility(View.GONE)
        mOtherSingCardView.setVisibility(View.GONE)
//        mRaceNoSingCardView?.visibility = View.GONE
//        mRaceMiddleResultView?.visibility = View.GONE
//        mRaceSelectSongView?.visibility = View.GONE
//        mWaitingCardView?.visibility = View.GONE
        mTurnInfoCardView?.visibility = View.GONE
//        mRaceMatchView?.visibility = View.GONE
//        mRaceWantingSignUpCardView?.visibility = View.GONE
    }

    private fun initSignUpView() {
//        mSignUpView = findViewById(R.id.sign_up_view)
//        mSignUpView.roomData = mRoomData
//        mSignUpView.clickSignUpBtn = {
//            mRacePagerSelectSongView.showView()
//        }
    }

    private fun initSelectPagerView() {
//        mRacePagerSelectSongView = findViewById(R.id.select_pager_view)
//        mRacePagerSelectSongView.mRoomData = mRoomData
//
//        mRacePagerSelectSongView.mSignUpMethed = { itemID, model ->
//            mCorePresenter.wantSingChance(itemID, model?.commonMusic)
//        }
//
//        mRaceWantingSignUpCardView = findViewById(R.id.race_wanting_signup_view)
//        mRacePagerSelectSongView.showView()
    }

    private fun initRaceMatchView() {
//        mRaceMatchView = findViewById(R.id.race_match_view)
//        mRaceMatchView.roomData = mRoomData
    }

    private fun initResuleView() {
//        mRaceNoSingCardView = findViewById(R.id.race_nosinger_result_view)
//        mRaceMiddleResultView = findViewById(R.id.race_middle_result_view)
//        mRaceMiddleResultView.setRaceRoomData(mRoomData)
    }


//    private fun initSelectSongView() {
//        mRaceSelectSongView = findViewById(R.id.race_select_song_view)
//        mRaceSelectSongView.setRoomData(mRoomData) { choiceID, seq ->
//            mCorePresenter.wantSingChance(choiceID, seq)
//        }
//        mRaceSelectSongView.visibility = View.GONE
//    }

    private fun initRightView() {
        mRightOpView = findViewById(R.id.right_op_view)
//        mRightOpView.setListener(object : RightOpListener {
//            override fun onClickGiveUp() {
//                // 放弃演唱
//                mCorePresenter.giveupSing {
//                    if (it) {
//                        mRaceRightOpView.showGiveUp(true)
//                    } else {
//                        MyLog.e(TAG, "onClickGiveUp 请求失败了")
//                    }
//                }
//            }
//
//            override fun onClickVote() {
//                // 投票
//                mCorePresenter.sendBLight {
//                    if (it) {
//                        mRaceRightOpView.showVote(true)
//                    } else {
//                        MyLog.e(TAG, "onClickVote 请求失败了")
//                    }
//                }
//            }
//        })
    }

    private fun initVipEnterView() {
        mVIPEnterView = VIPEnterView(findViewById(R.id.vip_enter_view_stub))
    }

    private fun initSingSenceView() {
        mSelfSingLyricView = MicSelfSingLyricView(findViewById(R.id.self_sing_lyric_view_stub) as ViewStub, mRoomData)
        mOtherSingCardView = MicOtherSingCardView(findViewById(R.id.other_sing_lyric_view_stub) as ViewStub, mRoomData)
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
    }

    private fun initBottomView() {
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
        })


        mTopContentView = findViewById(R.id.top_content_view)
        mTopContentView.setRoomData(mRoomData)

        mTopContentView.setListener(object : MicTopContentView.Listener {
            override fun clickMore() {
                dismissDialog()
//                mRaceActorPanelView = MicActorPanelView(this@MicRoomActivity, mRoomData)
//                mRaceActorPanelView?.showByDialog()
            }

            override fun clickArrow(open: Boolean) {
                if (open) {
                    mWidgetAnimationController.open()
                } else {
                    mWidgetAnimationController.close()
                }
            }
        })
//        mPracticeFlagIv = findViewById<ExImageView>(R.id.practice_flag_iv)
        // 加上状态栏的高度
        val statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(this)

//        run {
//            val topLayoutParams = mTopOpView.getLayoutParams() as ConstraintLayout.LayoutParams
//            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin
//        }
//        run {
//            val topLayoutParams = mTopContentView.getLayoutParams() as ConstraintLayout.LayoutParams
//            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin
//        }

//        mRaceTopVsView = findViewById(R.id.race_top_vs_view)
//        mRaceTopVsView.setRaceRoomData(mRoomData)
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
        mPersonInfoDialog = PersonInfoDialog.Builder(this, QuickFeedbackFragment.FROM_MIC_ROOM , userID, false, false)
                .setRoomID(mRoomData.gameId)
                .build()
        mPersonInfoDialog?.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWantSingChanceEvent) {
//        mSignUpView.setType(RaceSignUpBtnView.SignUpType.SIGN_UP_FINISH)
//        mRacePagerSelectSongView.hideView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowPersonCardEvent) {
        showPersonInfoView(event.uid)
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: RaceScoreChangeEvent) {
//        mRaceTopVsView.updateData()
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BuyGiftEvent) {
        mContinueSendView.startBuy(event.baseGift, event.receiver)
    }

    override fun singBySelfFirstRound(songModel: SongModel?) {
        MyLog.d(TAG, "singBySelfFirstRound songModel = ${songModel?.toSimpleString()}")
        hideAllSceneView()
//        mRaceTopVsView.visibility = View.VISIBLE
//        mRaceTopVsView.startVs()
//        mRaceTopVsView.startSingBySelf {
//            hideAllSceneView()
//            mRaceRightOpView.showGiveUp(false)
//            //            mRaceTopVsView.visibility = View.GONE
//            mRaceSelfSingLyricView.startFly {
//                mCorePresenter.sendSingComplete("singBySelfFirstRound")
//            }
//            mRaceSelfSingLyricView.setVisibility(View.VISIBLE)
//        }

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

    override fun singByOtherFirstRound(songModel: SongModel?, userModel: UserInfoModel?) {
        MyLog.d(TAG, "singByOtherFirstRound songModel = ${songModel?.toSimpleString()}, userModel = ${userModel?.toSimpleString()}")
        hideAllSceneView()
//        mRaceTopVsView.visibility = View.VISIBLE
//        mRoomData.realRoundInfo?.let {
//            if (it.enterStatus == ERaceRoundStatus.ERRS_ONGOINE.value && it.enterSubRoundSeq == it.subRoundSeq) {
//                MyLog.d(TAG, "singByOtherFirstRound 中途进来的, $it")
                //中途进来的
//                mRaceTopVsView.bindData()
//                mRaceTopVsView?.raceTopVsIv.visibility = View.VISIBLE
//                showRightVote()
//                mRaceOtherSingCardView.bindData()
//                hideAllSceneView()
//                mRaceOtherSingCardView.setVisibility(View.VISIBLE)
//            } else {
//                mRaceTopVsView.startVs()
//                mRaceTopVsView.startSingByOther(1) {
//                    showRightVote()
//                    mRaceOtherSingCardView.bindData()
//                    hideAllSceneView()
//                    mRaceOtherSingCardView.setVisibility(View.VISIBLE)
//                }
//            }
//        }

    }


    override fun singBySelfSecondRound(songModel: SongModel?) {
        MyLog.d(TAG, "singBySelfSecondRound songModel = ${songModel?.toSimpleString()}")
//        hideAllSceneView()
//        mRaceRightOpView.visibility = View.GONE
//        mRaceTopVsView.visibility = View.VISIBLE
//        mRaceTopVsView.bindData()
//        mRaceTopVsView.startSingBySelf {
//            hideAllSceneView()
//            mRaceRightOpView.showGiveUp(false)
//            mRaceSelfSingLyricView.startFly {
//                mCorePresenter.sendSingComplete("singBySelfSecondRound")
//            }
//            mRaceSelfSingLyricView.setVisibility(View.VISIBLE)
//        }

//        hideSignUpUI(true)
    }

    override fun singByOtherSecondRound(songModel: SongModel?, userModel: UserInfoModel?) {
        MyLog.d(TAG, "singByOtherSecondRound songModel = ${songModel?.toSimpleString()}, userModel = ${userModel?.toSimpleString()}")
//        hideAllSceneView()
//        mRaceRightOpView.visibility = View.GONE
//        mRaceTopVsView.visibility = View.VISIBLE
//        mRoomData.realRoundInfo?.let {
//            if (it.enterStatus == ERaceRoundStatus.ERRS_ONGOINE.value && it.enterSubRoundSeq == it.subRoundSeq) {
//                showRightVote()
//                mRaceTopVsView?.raceTopVsIv.visibility = View.VISIBLE
//                mRaceTopVsView.bindData()
//                mRaceOtherSingCardView.bindData()
//                hideAllSceneView()
//                mRaceOtherSingCardView.setVisibility(View.VISIBLE)
//            } else {
//                mRaceTopVsView.startSingByOther(2) {
//                    showRightVote()
//                    mRaceOtherSingCardView.bindData()
//                    hideAllSceneView()
//                    mRaceOtherSingCardView.setVisibility(View.VISIBLE)
//                }
//            }
//        }
    }

    override fun showRoundOver(lastRoundInfo: RaceRoundInfoModel, continueOp: (() -> Unit)?) {
        MyLog.d(TAG, "showRoundOver lastRoundInfo = $lastRoundInfo, continueOp = $continueOp")
//        mRaceRightOpView.visibility = View.GONE
//        mRaceTopVsView.visibility = View.GONE

//        if (lastRoundInfo.overReason == ERaceRoundOverReason.ERROR_NO_ONE_SING.value ||
//                lastRoundInfo.overReason == ERaceRoundOverReason.ERROR_NOT_ENOUTH_PLAYER.value) {
//            // 无人应战
//            hideAllSceneView()
//            mRaceNoSingCardView.visibility = View.VISIBLE
//            mRaceNoSingCardView.showAnimation(object : AnimationListener {
//                override fun onFinish() {
//                    continueOp?.invoke()
//                }
//            })
//        } else if (lastRoundInfo.overReason == ERaceRoundOverReason.ERROR_UNKNOWN.value) {
//            hideAllSceneView()
//            continueOp?.invoke()
//        } else {
//            hideAllSceneView()
//            mRaceMiddleResultView.visibility = View.VISIBLE
//            mRaceMiddleResultView.showResult(lastRoundInfo) {
//                continueOp?.invoke()
//            }
//        }
    }

    override fun showWaiting(showAnimation: Boolean) {
        MyLog.d(TAG, "showWaiting showAnimation = $showAnimation")
//        mRaceRightOpView.visibility = View.GONE
//        hideAllSceneView()
//        mRaceWaitingCardView.visibility = View.VISIBLE
//        if (showAnimation) {
//            mRaceWaitingCardView.animationEnter()
//        } else {
//            mRaceWaitingCardView.visibility = View.VISIBLE
//        }
    }

    override fun showChoiceView(showNextRound: Boolean) {
        MyLog.d(TAG, "showChoiceView showNextRound = $showNextRound")
//        mRaceRightOpView.visibility = View.GONE
//        if (showNextRound) {
//            hideAllSceneView()
//            mRaceTurnInfoCardView.visibility = View.VISIBLE
//            // 下一首动画
//            mRaceTurnInfoCardView.showAnimation(object : AnimationListener {
//                override fun onFinish() {
//                    showSelectSongView()
//                }
//            })
//        } else {
//            showSelectSongView()
//        }
    }

    override fun showMatchAnimationView(overListener: () -> Unit) {
        MyLog.d(TAG, "showMatchAnimationView")
        hideAllSceneView()
//        mRaceMatchView.visibility = View.VISIBLE
//        mRaceMatchView.bindData {
//            overListener.invoke()
//        }
    }

    /**
     * 直接弹出选歌面板
     */
    private fun showSelectSongView() {
//        mRaceWantingSignUpCardView.showAnimation(object : AnimationListener {
//            override fun onFinish() {
//
//            }
//        })
    }

    override fun joinNotice(playerInfoModel: UserInfoModel?) {
        playerInfoModel?.let {
            mVipEnterPresenter?.addNotice(it)
        }
    }

    override fun goResultPage(lastRound: RaceRoundInfoModel) {
        finish()
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_RESULT)
                .withInt("roomID", mRoomData.gameId)
                .withInt("roundSeq", lastRound.roundSeq)
                .navigation()
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

    override fun gameOver(lastRound: RaceRoundInfoModel?) {
        quitGame()
    }

    fun quitGame() {
        mCorePresenter.exitRoom("quitGame")
        finish()
    }

    private fun dismissDialog() {
//        mRaceActorPanelView?.dismiss(false)
        mPersonInfoDialog?.dismiss(false)
//        mRaceVoiceControlPanelView?.dismiss(false)
        mGameRuleDialog?.dismiss(false)
        mTipsDialogView?.dismiss(false)
    }




}
