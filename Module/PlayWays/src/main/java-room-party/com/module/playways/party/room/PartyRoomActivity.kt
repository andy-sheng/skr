package com.module.playways.party.room

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.player.SinglePlayer
import com.common.player.SinglePlayerCallbackAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.component.busilib.constans.GameModeType
import com.component.busilib.view.GameEffectBgView
import com.component.dialog.ClubCardDialogView
import com.component.dialog.ConfirmDialog
import com.component.dialog.PersonInfoDialog
import com.component.person.event.ShowPersonCardEvent
import com.component.report.fragment.QuickFeedbackFragment
import com.component.toast.CommonToastView
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.IHomeService
import com.module.playways.BaseRoomData
import com.module.playways.IPlaywaysModeService
import com.module.playways.R
import com.module.playways.grab.room.inter.IGrabVipView
import com.module.playways.grab.room.presenter.VipEnterPresenter
import com.module.playways.grab.room.view.VIPEnterView
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.party.bgmusic.getLocalMusicInfo
import com.module.playways.party.home.PartyHomeActivity
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.actor.PartyApplyPanelView
import com.module.playways.party.room.actor.PartyMemberPanelView
import com.module.playways.party.room.bottom.PartyBottomContainerView
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.event.PartySelectSongEvent
import com.module.playways.party.room.fragment.PartyRoomSettingFragment
import com.module.playways.party.room.model.PartyActorInfoModel
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.party.room.presenter.PartyCorePresenter
import com.module.playways.party.room.seat.PartySeatView
import com.module.playways.party.room.top.PartyTopContentView
import com.module.playways.party.room.top.PartyTopOpView
import com.module.playways.party.room.ui.IPartyRoomView
import com.module.playways.party.room.ui.PartyBottomWidgetAnimationController
import com.module.playways.party.room.ui.PartyWidgetAnimationController
import com.module.playways.party.room.view.*
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
import com.module.playways.room.room.view.InputContainerView
import com.module.playways.songmanager.SongManagerActivity
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.PartyRoom.EPGameType
import com.zq.live.proto.PartyRoom.PBeginVote
import com.zq.live.proto.PartyRoom.PKickoutUserMsg
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RouterConstants.ACTIVITY_PARTY_ROOM)
class PartyRoomActivity : BaseActivity(), IPartyRoomView, IGrabVipView {
    companion object {
        val playerTag = "PartyRoomActivity"
    }

    fun ensureActivtyTop() {
        // 销毁其他的除排麦房页面所有界面
        for (i in U.getActivityUtils().activityList.size - 1 downTo 0) {
            val activity = U.getActivityUtils().activityList[i]
            if (activity === this) {
                continue
            }
//            if (activity is RelayHomeActivity) {
//                continue
//            }
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue
            }

            if (activity is PartyHomeActivity) {
                continue
            }
            activity.finish()
        }
    }

    /**
     * 存起该房间一些状态信息
     */
    internal var mRoomData = PartyRoomData()
    private lateinit var mCorePresenter: PartyCorePresenter
    //基础ui组件
    internal lateinit var mInputContainerView: InputContainerView
    internal lateinit var mBottomContainerView: PartyBottomContainerView
    internal lateinit var mVoiceRecordTipsView: VoiceRecordTipsView
    internal lateinit var mCommentView: CommentView
    internal lateinit var mGiftPanelView: GiftPanelView
    internal lateinit var mContinueSendView: ContinueSendView
    internal lateinit var mTopOpView: PartyTopOpView
    internal lateinit var mTopContentView: PartyTopContentView

    internal lateinit var mGameEffectBgView: GameEffectBgView

    var mRightOpView: PartyRightOpView? = null
    var mRightQuickAnswerView: PartyRightQuickAnswerView? = null
    var mPartyGameMainView: PartyGameMainView? = null
    var mSeatView: PartySeatView? = null
    var mPartySettingView: PartySettingView? = null
    var mPartyEmojiView: PartyEmojiView? = null

    lateinit var mAddSongIv: ImageView
    lateinit var mChangeSongIv: ImageView

    private var mVIPEnterView: VIPEnterView? = null
    // 都是dialogplus
    private var mPersonInfoDialog: PersonInfoDialog? = null
    private var mGameRuleDialog: DialogPlus? = null
    private var mTipsDialogView: TipsDialogView? = null
    private var mVoiceControlPanelView: PartyVoiceControlPanelView? = null
    private var mPartyManageDialogView: PartyManageDialogView? = null
    private var mPartyManageHostDialogView: PartyManageHostDialogView? = null
    private var mPartyApplyPanelView: PartyApplyPanelView? = null
    private var mPartyMemberPanelView: PartyMemberPanelView? = null
    private var mConfirmDialog: ConfirmDialog? = null
    private var mClubCardDialogView: ClubCardDialogView? = null

    private var mVipEnterPresenter: VipEnterPresenter? = null

    lateinit var mVoiceRecordUiController: VoiceRecordUiController

    val mWidgetAnimationController = PartyWidgetAnimationController(this)
    val mBottomWidgetAnimationController = PartyBottomWidgetAnimationController(this)

    internal var mSkrAudioPermission = SkrAudioPermission()

    val mUiHanlder = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.party_room_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        ensureActivtyTop()
        var rspModel = intent.getSerializableExtra("JoinPartyRoomRspModel") as JoinPartyRoomRspModel?
        rspModel?.let {
            mRoomData.loadFromRsp(it)
            MyLog.d(TAG, "initData mRoomData=$mRoomData")
        }
        H.partyRoomData = mRoomData
        H.setType(GameModeType.GAME_MODE_PARTY, "PartyRoomActivity")

        mCorePresenter = PartyCorePresenter(mRoomData, this)
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

        SinglePlayer.addCallback(playerTag, object : SinglePlayerCallbackAdapter() {
            override fun onCompletion() {
                super.onCompletion()
                mPartyGameMainView?.partyGameTabView?.setAudioPlay(false)
            }

            override fun onError(what: Int, extra: Int) {
                super.onError(what, extra)
                mPartyGameMainView?.partyGameTabView?.setAudioPlay(false)
            }
        })

        initBgEffectView()
        initTopView()
        initInputView()
        initBottomView()
        initCommentView()
        initGiftPanelView()
        initGiftDisplayView()

        initGameMainView()
        initMicSeatView()
        initRightOpView()
        initVipEnterView()

        mCorePresenter.onOpeningAnimationOver()

        mUiHanlder.postDelayed(Runnable {
            var openOpBarTimes = U.getPreferenceUtils().getSettingInt("key_open_op_bar_times", 0)
            if (openOpBarTimes < 2) {
                mWidgetAnimationController.open()
                openOpBarTimes++
                U.getPreferenceUtils().setSettingInt("key_open_op_bar_times", openOpBarTimes)
            } else {
                mWidgetAnimationController.close()
            }

        }, 500)
        if (MyLog.isDebugLogOpen()) {
            val viewStub = findViewById<ViewStub>(R.id.debug_log_view_stub)
            val debugLogView = DebugLogView(viewStub)
            debugLogView.tryInflate()
        }

        if (U.getPreferenceUtils().getSettingBoolean("is_first_enter_party_room", true)) {
            U.getPreferenceUtils().setSettingBoolean("is_first_enter_party_room", false)
            showGameRuleDialog()
        }

        MyUserInfoManager.myUserInfo?.let {
            if (it.ranking != null) {
                mVipEnterPresenter?.addNotice(MyUserInfo.toUserInfoModel(it))
            }
        }

        U.getStatusBarUtil().setTransparentBar(this, false)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(this)
    }

    override fun destroy() {
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        super.destroy()
        dismissDialog()
        mPartyApplyPanelView?.destory()
        mPartyMemberPanelView?.destory()
        mWidgetAnimationController.destroy()
        mBottomWidgetAnimationController.destroy()
        mGiftPanelView?.destroy()
        SinglePlayer.stop(playerTag)
        SinglePlayer.removeCallback(playerTag)
        H.reset("PartyRoomActivity")
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
//        if (mSelfSingCardView != exclude) {
//            mSelfSingCardView.setVisibility(View.GONE)
//        }
//        if (mOthersSingCardView != exclude) {
//            mOthersSingCardView.setVisibility(View.GONE)
//        }
//        if (mTurnInfoCardView != exclude) {
//            mTurnInfoCardView.visibility = View.GONE
//        }
//        if (mGiveUpView != exclude) {
//            mGiveUpView.hideWithAnimation(false)
//        }
//        if (mRoundOverCardView != exclude) {
//            mRoundOverCardView.setVisibility(View.GONE)
//        }
//        if (mSingBeginTipsCardView != exclude) {
//            mSingBeginTipsCardView.setVisibility(View.GONE)
//        }
    }

    private fun initMicSeatView() {
        mSeatView = findViewById(R.id.seat_view)
        mSeatView?.bindData(mRoomData)
        mSeatView?.listener = object : PartySeatView.Listener {
            override fun onClickAvatar(position: Int, model: PartyActorInfoModel?) {
                if (mRoomData.getMyUserInfoInParty().isAdmin() || mRoomData.getMyUserInfoInParty().isHost()) {
                    if (model?.player?.userID == MyUserInfoManager.uid.toInt()) {
                        // 点开的是自己
                        showPersonInfoView(model?.player?.userID ?: 0, null)
                    } else {
                        showPartyManageView(model)
                    }
                } else {
                    // 非管理人员
                    if (model?.player?.userID != null) {
                        showPersonInfoView(model?.player?.userID ?: 0, null)
                    } else {
                        if (mRoomData.myUserInfo?.isGuest() == true) {
                            // 嘉宾 点了个空座位 没反应
                        } else {
                            // 观众
                            dismissDialog()
                            mTipsDialogView = TipsDialogView.Builder(this@PartyRoomActivity)
                                    .setMessageTip("是否申请上麦")
                                    .setConfirmTip("是")
                                    .setCancelTip("取消")
                                    .setConfirmBtnClickListener {
                                        mTipsDialogView?.dismiss(false)
                                        mRightOpView?.applyForGuest(false)
                                    }
                                    .setCancelBtnClickListener {
                                        mTipsDialogView?.dismiss()
                                    }
                                    .build()
                            mTipsDialogView?.showByDialog()
                        }
                    }
                }
            }
        }
    }

    private fun initRightOpView() {
        mRightOpView = findViewById(R.id.right_op_view)
        mRightOpView?.bindData(true, null)
        mRightOpView?.listener = object : PartyRightOpView.Listener {
            override fun onClickApplyList() {
                dismissDialog()
                mPartyApplyPanelView = PartyApplyPanelView(this@PartyRoomActivity)
                mPartyApplyPanelView?.showByDialog()
            }
        }
        mRightQuickAnswerView = PartyRightQuickAnswerView(findViewById(R.id.party_right_quick_answer_view))
    }

    private fun initGameMainView() {
        mPartyGameMainView = PartyGameMainView(findViewById(R.id.party_game_main_view_layout_viewStub), mRoomData)
        mPartyGameMainView?.iAudioGameListener = object : PartyGameTabView.IAudioGameListener {
            override fun stopPlay() {
                SinglePlayer.stop(playerTag)
            }

            override fun startPlay(url: String) {
                SinglePlayer.startPlay(playerTag, url)
            }
        }
        mPartyGameMainView?.tryInflate()
        mPartyGameMainView?.toEmptyState()
    }

    private fun initVipEnterView() {
        mVIPEnterView = VIPEnterView(findViewById(R.id.vip_enter_view_stub))
    }

    private fun initBgEffectView() {
        mGameEffectBgView = GameEffectBgView(findViewById(R.id.game_effect_bg_view_layout_viewStub))
    }

    private fun initInputView() {
        mInputContainerView = findViewById(R.id.input_container_view)
        mInputContainerView.setRoomData(mRoomData)
    }


    private fun initBottomView() {
        mChangeSongIv = findViewById(R.id.change_song_tv)
        mChangeSongIv.setAnimateDebounceViewClickListener {
            mCorePresenter.giveUpSing { }
        }

        mAddSongIv = findViewById(R.id.select_song_tv)
        mAddSongIv.setAnimateDebounceViewClickListener {
            mSkrAudioPermission.ensurePermission({
                //                SongManagerActivity.open(this, mRoomData)
            }, true)
        }

        run {
            val voiceStub = findViewById<ViewStub>(R.id.voice_record_tip_view_stub)
            mVoiceRecordTipsView = VoiceRecordTipsView(voiceStub)
        }

        mBottomContainerView = findViewById(R.id.bottom_container_view)
        mBottomContainerView.setRoomData(mRoomData)
        mBottomContainerView.listener = object : PartyBottomContainerView.Listener {
            override fun onClickEmoji(open: Boolean) {
                if (open) {
                    mBottomWidgetAnimationController.open(PartyBottomWidgetAnimationController.OPEN_TYPE_EMOJI)
                } else {
                    mBottomWidgetAnimationController.close(PartyBottomWidgetAnimationController.OPEN_TYPE_EMOJI)
                }
            }

            override fun onClickMore(open: Boolean) {
                if (open) {
                    mBottomWidgetAnimationController.open(PartyBottomWidgetAnimationController.OPEN_TYPE_SETTING)
                } else {
                    mBottomWidgetAnimationController.close(PartyBottomWidgetAnimationController.OPEN_TYPE_SETTING)
                }
            }
        }
        mBottomContainerView.setListener(object : BottomContainerView.Listener() {
            override fun showInputBtnClick() {
                dismissDialog()
                mInputContainerView.showSoftInput()
            }

            override fun clickRoomManagerBtn() {

            }

            override fun showGiftPanel() {
                MyLog.d(TAG, "${getLocalMusicInfo()}")
                mContinueSendView.visibility = View.GONE
                showPanelView()
            }

            override fun onClickFlower() {
                buyFlowerFromOuter()
            }
        })

        mPartySettingView = PartySettingView(findViewById(R.id.party_bottom_setting_viewStub))
        mPartySettingView?.listener = object : PartySettingView.Listener {
            override fun onClickQuickAnswer() {
                mCorePresenter.beginQuickAnswer()
//                mRightQuickAnswerView?.playCountDown(System.currentTimeMillis()+3000,System.currentTimeMillis()+10000)
            }

            override fun onClickGameSetting() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PARTY_SELECT_GAME)
                        .navigation()
            }

            override fun onClickGameSound() {

            }

            override fun onClickVote() {
                val partySendVoteDialogView = PartySendVoteDialogView(this@PartyRoomActivity)
                partySendVoteDialogView.showByDialog()
            }
        }

        mPartyEmojiView = PartyEmojiView(findViewById(R.id.party_bottom_emoji_viewStub))

    }

    private fun showPanelView() {
        val partyGameInfoModel = mRoomData?.realRoundInfo?.sceneInfo

        if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
            if (partyGameInfoModel?.ktv?.userID ?: 0 > 0) {
                if (partyGameInfoModel?.ktv?.userID == MyUserInfoManager.uid.toInt()) {
                    //自己唱
                    mGiftPanelView?.show(null)
                } else {
                    //别人在唱
                    mGiftPanelView?.show(mRoomData.getPlayerInfoById(partyGameInfoModel?.ktv?.userID
                            ?: 0))
                }
            } else {
                //还没开始
                mGiftPanelView?.show(null)
            }
        } else {
            //别的模式
            mGiftPanelView?.show(null)
        }
    }

    private fun buyFlowerFromOuter() {
//        EventBus.getDefault().post(BuyGiftEvent(NormalGift.getFlower(), mRoomData.peerUser?.userInfo))
    }

    private fun initTopView() {
        mTopOpView = findViewById(R.id.top_op_view)
        mTopOpView.setListener(object : PartyTopOpView.Listener {
            override fun onClickGameRule() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PartyRoomActivity)
                showGameRuleDialog()
            }

            override fun onClickFeedBack() {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(this@PartyRoomActivity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_PARTY_ROOM)
                                .addDataBeforeAdd(1, QuickFeedbackFragment.FEED_BACK)
                                .addDataBeforeAdd(3, mRoomData.gameId)
                                .setEnterAnim(R.anim.slide_in_bottom)
                                .setExitAnim(R.anim.slide_out_bottom)
                                .build())
            }

            override fun onClickVoiceAudition() {
                // 调音面板
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@PartyRoomActivity)
                dismissDialog()
                if (mVoiceControlPanelView == null) {
                    mVoiceControlPanelView = PartyVoiceControlPanelView(this@PartyRoomActivity)
                    mVoiceControlPanelView?.setRoomData(mRoomData)
                }
                mVoiceControlPanelView?.showByDialog()
            }

            override fun onClickSetting() {
                if (mRoomData.getPlayerInfoById(MyUserInfoManager.uid.toInt()) == null
                        || mRoomData.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isHost() == false) {
                    U.getToastUtil().showShort("只有主持人才能进行房间设置哦～")
                    return
                }

                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(this@PartyRoomActivity, PartyRoomSettingFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build())
            }

            override fun closeBtnClick() {
                quitGame()
            }
        })

        mTopContentView = findViewById(R.id.top_content_view)
        mTopContentView.roomData = mRoomData
        mTopContentView.bindData()
        mTopContentView.listener = object : PartyTopContentView.Listener {
            override fun showRoomMember() {
                // 查看房间所有人
                dismissDialog()
                mPartyMemberPanelView = PartyMemberPanelView(this@PartyRoomActivity)
                mPartyMemberPanelView?.showByDialog()
            }

            override fun clickArrow(open: Boolean) {
                if (open) {
                    mWidgetAnimationController.open()
                } else {
                    mWidgetAnimationController.close()
                }
            }

            override fun showPartyBeHostConfirm() {
                getPartyManageHostDialogView().apply {
                    function1.text = "上麦"
                    function1.setDebounceViewClickListener {
                        mCorePresenter.becomeClubHost()
                        dismiss(false)
                    }

                    function2.visibility = View.GONE
                }.showByDialog()
            }

            override fun showPartyOpHost() {
                getPartyManageHostDialogView().apply {
                    function1.text = "上麦"
                    function1.setDebounceViewClickListener {
                        mCorePresenter.takeClubHost()
                        dismiss(false)
                    }

                    function2.visibility = View.VISIBLE
                    function2.setDebounceViewClickListener {
                        dismiss(false) // 2个对话框
                        EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.hostId))
                    }
                }.showByDialog()
            }

            override fun showPartySelfOpHost() {
                getPartyManageHostDialogView().apply {
                    function1.text = "下麦"
                    function1.setDebounceViewClickListener {
                        mCorePresenter.giveClubHost()
                        dismiss(false)
                    }

                    function2.visibility = View.VISIBLE
                    function2.setDebounceViewClickListener {
                        dismiss(false) // 2个对话框
                        EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.hostId))
                    }
                }.showByDialog()
            }

            override fun showClubInfoCard() {
                dismissDialog()
                mClubCardDialogView = ClubCardDialogView(this@PartyRoomActivity, mRoomData.clubInfo?.clubID
                        ?: 0)
                mClubCardDialogView?.showByDialog()
            }
        }
    }

    private fun showGameRuleDialog() {
        dismissDialog()
        mGameRuleDialog = DialogPlus.newDialog(this)
                .setContentHolder(ViewHolder(R.layout.party_game_rule_view_layout))
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
            showPersonInfoView(userId, null)
        })
        mCommentView.roomData = mRoomData
    }

    private fun initGiftPanelView() {
        mGiftPanelView = findViewById<View>(R.id.gift_panel_view) as GiftPanelView
        mGiftPanelView.setRoomData(mRoomData)
        mContinueSendView = findViewById<View>(R.id.continue_send_view) as ContinueSendView
        mContinueSendView.mScene = ContinueSendView.EGameScene.GS_Party
        mContinueSendView.setRoomData(mRoomData)
        mContinueSendView.setObserver(object : ContinueSendView.OnVisibleStateListener {
            override fun onVisible(isVisible: Boolean) {
                mBottomContainerView.setOpVisible(!isVisible)
            }
        })
        mGiftPanelView.setIGetGiftCountDownListener(GiftDisplayView.IGetGiftCountDownListener {
            //                return mGiftTimerPresenter.getCountDownSecond();
            0
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

    private fun showPersonInfoView(userID: Int, isShowKick: Boolean?) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!")
            return
        }
        dismissDialog()
        mInputContainerView.hideSoftInput()
        var showKick = false
        if (isShowKick != null) {
            showKick = isShowKick
        } else if (mRoomData.myUserInfo?.isHost() == true) {
            // 主持人 能踢所有人
            showKick = mRoomData.getPlayerInfoById(userID)?.isHost() != true
        } else if (mRoomData.myUserInfo?.isAdmin() == true) {
            // 管理员能踢 嘉宾 观众
            showKick = !(mRoomData.getPlayerInfoById(userID)?.isAdmin() == true || mRoomData.getPlayerInfoById(userID)?.isHost() == true)
        }

        mPersonInfoDialog = PersonInfoDialog.Builder(this, QuickFeedbackFragment.FROM_PARTY_ROOM, userID, showKick, true, true)
                .setRoomID(mRoomData.gameId)
                .setInviteReplyListener { userInfoModel ->
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService.tryInviteToRelay(userInfoModel.userId, userInfoModel.isFriend)
                }
                .setKickListener {
                    showKickConfirmDialog(it)
                }
                .build()
        mPersonInfoDialog?.show()
    }

    private fun showKickConfirmDialog(model: UserInfoModel) {
        dismissDialog()
        mConfirmDialog = ConfirmDialog(U.getActivityUtils().topActivity, model, ConfirmDialog.TYPE_OWNER_KICK_CONFIRM, 0)
        mConfirmDialog?.setListener { userInfoModel ->
            // 发起踢人请求
            mCorePresenter.kickOut(userInfoModel.userId)
        }
        mConfirmDialog?.show()
    }

    private fun showPartyManageView(model: PartyActorInfoModel?) {
        dismissDialog()
        mInputContainerView.hideSoftInput()
        mPartyManageDialogView = PartyManageDialogView(this, model)
        mPartyManageDialogView?.showByDialog()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowPersonCardEvent) {
        showPersonInfoView(event.uid, event.showKick)
    }

    private fun getPartyManageHostDialogView(): PartyManageHostDialogView {
        if (mPartyManageHostDialogView == null) {
            mPartyManageHostDialogView = PartyManageHostDialogView(this)
        }

        return mPartyManageHostDialogView!!
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartySelectSongEvent) {
        SongManagerActivity.open(this@PartyRoomActivity, mRoomData)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BuyGiftEvent) {
        if (event.receiver.userId != MyUserInfoManager.uid.toInt()) {
            mContinueSendView.startBuy(event.baseGift, event.receiver)
        } else {
            U.getToastUtil().showShort("只能给正在演唱的其他选手送礼哦～")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyHostChangeEvent) {
        if (mRoomData.isClubHome()) {
            if (mRoomData.hostId > 0) {
                val model = mRoomData.getPlayerInfoById(mRoomData.hostId)
                mCorePresenter?.pretendSystemMsg("${UserInfoManager.getInstance().getRemarkName(model?.userInfo?.userId
                        ?: 0, model?.userInfo?.nickname)} 已成为新的主持人")
                if ((model?.userInfo?.userId ?: 0) == MyUserInfoManager.uid.toInt()) {
                    finishTopActivity("您已成为主持人")
                }
            } else {
                mCorePresenter?.pretendSystemMsg("主持人已下麦，已自动结束所有游戏")
            }
        }
    }

    private fun finishTopActivity(str: String) {
        var hasActivity = false
        for (i in U.getActivityUtils().activityList.size - 1 downTo 0) {
            val activity = U.getActivityUtils().activityList[i]
            if (activity is PartyRoomActivity) {
                break
            }
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue
            }
            activity.finish()
            hasActivity = true
        }

        if (!TextUtils.isEmpty(str) && hasActivity) {
            U.getToastUtil().showShort(str)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PKickoutUserMsg) {
        //todo 需不需要让被踢人游戏直接结束
        MyLog.d(TAG, "onEvent event = $event")
        if (event.kickUser.userInfo.userID == MyUserInfoManager.uid.toInt()) {
            // 我被踢出去了
            U.getToastUtil().showSkrCustomLong(CommonToastView.Builder(U.app())
                    .setImage(R.drawable.touxiangshezhishibai_icon)
                    .setText("管理员已将你踢出房间")
                    .build())
            finish()
        } else {
            val opUser = PartyPlayerInfoModel.parseFromPb(event.opUser)
            val stringBuilder = StringBuilder()
            if (opUser.isHost()) {
                stringBuilder.append("主持人")
            } else if (opUser.isAdmin()) {
                stringBuilder.append("管理员")
            }

            val kickUser = PartyPlayerInfoModel.parseFromPb(event.kickUser)
            stringBuilder.append("将${kickUser.userInfo?.nicknameRemark}踢出了房间")
            mCorePresenter.pretendSystemMsg(stringBuilder.toString())
        }
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
                                    showPanelView()
                                }
                            }
                        })
                        .setHasAnimation(true)
                        .build())
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun onBackPressedForActivity(): Boolean {
        if (mInputContainerView.onBackPressed()) {
            return true
        }

        if (mGiftPanelView.onBackPressed()) {
            return true
        }

//        if (mMicSeatView.onBackPressed()) {
//            return true
//        }

        quitGame()
        return true
    }


    private fun quitGame() {
        dismissDialog()
        mTipsDialogView = TipsDialogView.Builder(this)
                .setMessageTip("确定要退出主题房吗")
                .setConfirmTip("确定")
                .setCancelTip("取消")
                .setConfirmBtnClickListener {
                    mTipsDialogView?.dismiss(false)
                    finish()
                }
                .setCancelBtnClickListener {
                    mTipsDialogView?.dismiss()
                }
                .build()
        mTipsDialogView?.showByDialog()
    }

    private fun dismissDialog() {
        mPersonInfoDialog?.dismiss(false)
        mGameRuleDialog?.dismiss(false)
        mTipsDialogView?.dismiss(false)
        mVoiceControlPanelView?.dismiss(false)
        mPartyManageDialogView?.dismiss(false)
        mPartyManageHostDialogView?.dismiss(false)
        mPartyApplyPanelView?.dismiss(false)
        mPartyMemberPanelView?.dismiss(false)
        mConfirmDialog?.dismiss(false)
        mClubCardDialogView?.dismiss(false)
    }

    /**
     * 某个轮次结束了
     */
    override fun showRoundOver(lastRoundInfo: PartyRoundInfoModel?, continueOp: (() -> Unit)?) {
        continueOp?.invoke()
    }

    /**
     * 某个游戏开始了 信息在 realRoundInfo里取
     */
    override fun gameBegin(thisRound: PartyRoundInfoModel?) {
        mPartyGameMainView?.updateRound(thisRound)
    }

    override fun showVoteView(event: PBeginVote) {
        if ((System.currentTimeMillis() - BaseRoomData.shiftTsForRelay) - event.beginTimeMs >= event.endTimeMs - event.beginTimeMs) {
            MyLog.w(TAG, "已经过了投票时间，PBeginVote event.voteTag is ${event.voteTag}")
        } else {
            val partyVoteDialogView = PartyVoteDialogView(this@PartyRoomActivity, event)
            partyVoteDialogView.showByDialog()
        }
    }

    /**
     * 没有游戏了
     */
    override fun showWaiting() {
        mPartyGameMainView?.toEmptyState()

    }

    override fun joinNotice(model: PartyPlayerInfoModel?) {
        model?.let {
            if (it.userID != MyUserInfoManager.myUserInfo?.userId?.toInt()) {
                mVipEnterPresenter?.addNotice(it.userInfo)
            }
        }
    }

    override fun gameOver() {
        finish()
        U.getToastUtil().showShort("房间已解散")
    }

    override fun showWarningDialog(warningMsg: String) {
        dismissDialog()
        mTipsDialogView = TipsDialogView.Builder(this)
                .setMessageTip(warningMsg).build()
        mTipsDialogView?.showByDialog()
    }

    override fun beginQuickAnswer(beginTs: Long, endTs: Long) {
        mRightQuickAnswerView?.playCountDown(beginTs, endTs)
    }
}
