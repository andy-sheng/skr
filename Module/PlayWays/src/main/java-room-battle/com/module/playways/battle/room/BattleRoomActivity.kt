package com.module.playways.battle.room

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.kouling.api.KouLingServerApi
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.component.busilib.constans.GameModeType
import com.component.busilib.view.GameEffectBgView
import com.component.dialog.ClubCardDialogView
import com.component.dialog.ConfirmDialog
import com.component.dialog.PersonInfoDialog
import com.component.person.event.SendGiftByPersonCardEvent
import com.component.person.event.ShowPersonCardEvent
import com.component.report.fragment.QuickFeedbackFragment
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.IHomeService
import com.module.playways.BaseRoomData
import com.module.playways.IPlaywaysModeService
import com.module.playways.R
import com.module.playways.battle.match.model.JoinBattleRoomRspModel
import com.module.playways.battle.room.bottom.BattleBottomContainerView
import com.module.playways.battle.room.presenter.BattleCorePresenter
import com.module.playways.battle.room.top.BattleTopContentView
import com.module.playways.battle.room.top.BattleTopOpView
import com.module.playways.battle.room.ui.BattleWidgetAnimationController
import com.module.playways.battle.room.ui.IBattleRoomView
import com.module.playways.battle.room.view.BattleVoiceControlPanelView
import com.module.playways.grab.room.inter.IGrabVipView
import com.module.playways.grab.room.invite.IInviteCallBack
import com.module.playways.grab.room.invite.InviteFriendActivity
import com.module.playways.grab.room.presenter.VipEnterPresenter
import com.module.playways.grab.room.view.GrabChangeRoomTransitionView
import com.module.playways.grab.room.view.VIPEnterView
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
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
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RouterConstants.ACTIVITY_BATTLE_ROOM)
class BattleRoomActivity : BaseActivity(), IBattleRoomView, IGrabVipView {
    companion object {
        val playerTag = "BattleRoomActivity"
    }

    private fun ensureActivtyTop() {
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

            activity.finish()
        }
    }

    /**
     * 存起该房间一些状态信息
     */
    internal var mRoomData = BattleRoomData()
    private lateinit var mCorePresenter: BattleCorePresenter
    //基础ui组件
    internal lateinit var mInputContainerView: InputContainerView
    internal lateinit var mBottomContainerView: BattleBottomContainerView
    internal lateinit var mVoiceRecordTipsView: VoiceRecordTipsView
    internal lateinit var mCommentView: CommentView
    internal lateinit var mGiftPanelView: GiftPanelView
    internal lateinit var mContinueSendView: ContinueSendView
    internal lateinit var mTopOpView: BattleTopOpView
    internal lateinit var mTopContentView: BattleTopContentView

    internal lateinit var mGameEffectBgView: GameEffectBgView

    var mChangeRoomTransitionView: GrabChangeRoomTransitionView? = null

    lateinit var mAddSongIv: ImageView
    lateinit var mChangeSongIv: ImageView

    private var mVIPEnterView: VIPEnterView? = null
    // 都是dialogplus
    private var mMainActContainer: ExConstraintLayout? = null
    private var mPersonInfoDialog: PersonInfoDialog? = null
    private var mGameRuleDialog: DialogPlus? = null
    private var mTipsDialogView: TipsDialogView? = null
    private var mVoiceControlPanelView: BattleVoiceControlPanelView? = null
    private var mConfirmDialog: ConfirmDialog? = null
    private var mClubCardDialogView: ClubCardDialogView? = null

    private var mVipEnterPresenter: VipEnterPresenter? = null

    lateinit var mVoiceRecordUiController: VoiceRecordUiController

    val mWidgetAnimationController = BattleWidgetAnimationController(this)

    internal var mSkrAudioPermission = SkrAudioPermission()

    val SP_KEY_HOST_TIP_TIMES = "sp_key_host_tips_show_times"
//    val REMOVE_HOST_OP_TIP_MSG = 0x01     // 主持人操作提示
//    val CHECK_GO_MIC_TIP_MSG = 0x02  // 去上麦或者换房间的提示

    val mUiHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
//                REMOVE_HOST_OP_TIP_MSG -> {
//                    removeHostOpTips()
//                }
//                CHECK_GO_MIC_TIP_MSG -> {
//                    showGoMicTips()
//                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.battle_room_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        ensureActivtyTop()
        var rspModel = intent.getSerializableExtra("JoinBattleRoomRspModel") as JoinBattleRoomRspModel?
        rspModel?.let {
            mRoomData.loadFromRsp(it)
            MyLog.d(TAG, "initData mRoomData=$mRoomData")
        }
        H.battleRoomData = mRoomData
        H.setType(GameModeType.GAME_MODE_BATTLE, "BattleRoomActivity")

        mCorePresenter = BattleCorePresenter(mRoomData, this)
        addPresent(mCorePresenter)
        mVipEnterPresenter = VipEnterPresenter(this, mRoomData)
        addPresent(mVipEnterPresenter)
        // 请保证从下面的view往上面的view开始初始化
        mMainActContainer = findViewById(R.id.main_act_container)
        mMainActContainer?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                mInputContainerView.hideSoftInput()
            }
            false
        }

        initBgEffectView()
        initTopView()
        initInputView()
        initBottomView()
        initCommentView()
        initGiftPanelView()
        initGiftDisplayView()

//        initGameMainView()
//        initMicSeatView()
        initRightOpView()
        initVipEnterView()
        initChangeRoomTransitionView()
//        initPunishView()
        mCorePresenter.onOpeningAnimationOver()

        mUiHandler.postDelayed(Runnable {
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

//        if (U.getPreferenceUtils().getSettingBoolean("is_first_enter_battle_room", true)) {
//            U.getPreferenceUtils().setSettingBoolean("is_first_enter_battle_room", false)
//            showGameRuleDialog()
//        }

        MyUserInfoManager.myUserInfo?.let {
            if (it.ranking != null) {
                mVipEnterPresenter?.addNotice(MyUserInfo.toUserInfoModel(it))
            }
        }

        U.getStatusBarUtil().setTransparentBar(this, false)
//        showHostOpTips()
//        checkGoMicTips()
    }

//    private fun checkGoMicTips() {
//        if (mRoomData.joinSrc == JoinBattleRoomRspModel.JRS_QUICK_JOIN || mRoomData.joinSrc == JoinBattleRoomRspModel.JRS_CHANGE_ROOM) {
//            mUiHandler.removeMessages(CHECK_GO_MIC_TIP_MSG)
//            mUiHandler.sendEmptyMessageDelayed(CHECK_GO_MIC_TIP_MSG, 6000L)
//        }
//    }

//    private fun showGoMicTips() {
//        // 用户不在麦上、有空位、房间允许观众自由上麦
//        if ((mRoomData.myUserInfo?.isGuest() != true && mRoomData.myUserInfo?.isHost() != true
//                        && mRoomData.getSeatMode == 1) && mRoomData.hasEmptySeat()) {
//            // 不在麦上, 且不需要申请上麦，且座位还没满
//            MyLog.d(TAG, "need showGoMicTips")
//            var roundInfoModel = mRoomData.realRoundInfo
//            if (roundInfoModel == null) {
//                roundInfoModel = mRoomData.expectRoundInfo
//            }
//            val gameInfoModel = roundInfoModel?.sceneInfo
//            dismissDialog()
//            mTipsDialogView = TipsDialogView.Builder(this)
//                    .setTitleTip(gameInfoModel?.rule?.ruleName)
//                    .setMessageTip("快上麦一起玩吧")
//                    .setCancelTip("换个房间")
//                    .setConfirmTip("立即上麦")
//                    .setConfirmBtnClickListener {
//                        mCorePresenter.selfGetSeat()
//                        mTipsDialogView?.dismiss(false)
//                    }
//                    .setCancelBtnClickListener {
//                        StatisticsAdapter.recordCountEvent("battle", "popup_change_room", null)
//                        mCorePresenter.changeRoom()
//                        mTipsDialogView?.dismiss(false)
//                    }
//                    .build()
//            mTipsDialogView?.showByDialog()
//        }
//    }

//    private fun showHostOpTips() {
//        if (mRoomData.myUserInfo?.isHost() == true) {
//            val times = U.getPreferenceUtils().getSettingInt(SP_KEY_HOST_TIP_TIMES, 0)
//            if (times < 2) {
//                U.getPreferenceUtils().setSettingInt(SP_KEY_HOST_TIP_TIMES, times + 1)
//                mHostOpTipImageView = ImageView(this)
//                mHostOpTipImageView?.setImageResource(R.drawable.battle_host_tips_icon)
//                val layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
//                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
//                layoutParams.rightMargin = 15.dp()
//                layoutParams.bottomMargin = 50.dp()
//                mMainActContainer?.addView(mHostOpTipImageView, layoutParams)
//                mUiHandler.removeMessages(REMOVE_HOST_OP_TIP_MSG)
//                mUiHandler.sendEmptyMessageDelayed(REMOVE_HOST_OP_TIP_MSG, 15000L)
//                mHostOpTipImageView?.setDebounceViewClickListener {
//                    removeHostOpTips()
//                }
//            }
//        }
//    }

//    private fun removeHostOpTips() {
//        mUiHandler.removeMessages(REMOVE_HOST_OP_TIP_MSG)
//        if (mMainActContainer?.indexOfChild(mHostOpTipImageView) != -1) {
//            mMainActContainer?.removeView(mHostOpTipImageView)
//        }
//    }

//    private fun initPunishView() {
//        mBattlePunishView = BattlePunishView(findViewById(R.id.battle_punish_view_layout_viewStub), mRoomData)
//    }

    private fun initChangeRoomTransitionView() {
        mChangeRoomTransitionView = GrabChangeRoomTransitionView(findViewById(R.id.change_room_transition_view))
        mChangeRoomTransitionView?.setVisibility(View.GONE)
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
        mUiHandler.removeCallbacksAndMessages(null)
//        mBattleApplyPanelView?.destory()
//        mBattleMemberPanelView?.destory()
        mWidgetAnimationController.destroy()
//        mBottomWidgetAnimationController.destroy()
        mGiftPanelView?.destroy()
        H.reset("BattleRoomActivity")
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


//    private fun hideAllSceneView(exclude: Any?) {
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
//    }

//    private fun initMicSeatView() {
//        mSeatView = findViewById(R.id.seat_view)
//        mSeatView?.bindData(mRoomData)
//        mSeatView?.listener = object : BattleSeatView.Listener {
//            override fun onClickAvatar(position: Int, model: BattleActorInfoModel?) {
//                if (mRoomData.getMyUserInfoInBattle().isAdmin() || mRoomData.getMyUserInfoInBattle().isHost()) {
//                    if (model?.player?.userID == MyUserInfoManager.uid.toInt()) {
//                        // 点开的是自己
//                        showPersonInfoView(model?.player?.userID ?: 0, null)
//                    } else {
//                        showBattleManageView(model)
//                    }
//                } else {
//                    // 非管理人员
//                    if (model?.player?.userID != null) {
//                        showPersonInfoView(model?.player?.userID ?: 0, null)
//                    } else {
//                        if (mRoomData.myUserInfo?.isGuest() == true) {
//                            // 嘉宾 点了个空座位 没反应
//                        } else {
//                            // 观众
//                            if (mRoomData.getSeatMode == EGetSeatMode.EGSM_NO_APPLY.value) {
//                                // 产品说让直接就上去了
//                                mRightOpView?.selfGetSeat()
//                            } else {
//                                dismissDialog()
//                                mTipsDialogView = TipsDialogView.Builder(this@BattleRoomActivity)
//                                        .setMessageTip("是否申请上麦")
//                                        .setConfirmTip("是")
//                                        .setCancelTip("取消")
//                                        .setConfirmBtnClickListener {
//                                            mTipsDialogView?.dismiss(false)
//                                            mRightOpView?.applyForGuest(false)
//                                        }
//                                        .setCancelBtnClickListener {
//                                            mTipsDialogView?.dismiss()
//                                        }
//                                        .build()
//                                mTipsDialogView?.showByDialog()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun initRightOpView() {
//        mRightOpView = findViewById(R.id.right_op_view)
//        mRightOpView?.bindData(true, null)
//        mRightOpView?.listener = object : BattleRightOpView.Listener {
//            override fun onClickApplyList() {
//                dismissDialog()
//                mBattleApplyPanelView = BattleApplyPanelView(this@BattleRoomActivity)
//                mBattleApplyPanelView?.showByDialog()
//            }
//        }
//        mRightQuickAnswerView = BattleRightQuickAnswerView(findViewById(R.id.battle_right_quick_answer_view))
    }

//    private fun initGameMainView() {
//        mBattleGameMainView = BattleGameMainView(findViewById(R.id.battle_game_main_view_layout_viewStub), mRoomData)
//        mBattleGameMainView?.tryInflate()
//        mBattleGameMainView?.toEmptyState()
//    }

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
//        mBottomContainerView.listener = object : BattleBottomContainerView.Listener {
//            override fun onClickEmoji(open: Boolean) {
//                if (open) {
//                    mBottomWidgetAnimationController.open(BattleBottomWidgetAnimationController.OPEN_TYPE_EMOJI)
//                } else {
//                    mBottomWidgetAnimationController.close(BattleBottomWidgetAnimationController.OPEN_TYPE_EMOJI)
//                }
//            }
//
//            override fun onClickMore(open: Boolean) {
//                removeHostOpTips()
//                if (open) {
//                    mBottomWidgetAnimationController.open(BattleBottomWidgetAnimationController.OPEN_TYPE_SETTING)
//                } else {
//                    mBottomWidgetAnimationController.close(BattleBottomWidgetAnimationController.OPEN_TYPE_SETTING)
//                }
//            }
//        }
        mBottomContainerView.setListener(object : BottomContainerView.Listener() {
            override fun showInputBtnClick() {
                dismissDialog()
                mInputContainerView.showSoftInput()
            }

            override fun clickRoomManagerBtn() {

            }

            override fun showGiftPanel() {
                mContinueSendView.visibility = View.GONE
                showPanelView()
            }

            override fun onClickFlower() {
                buyFlowerFromOuter()
            }
        })

//        mBattleSettingView = BattleSettingView(findViewById(R.id.battle_bottom_setting_viewStub))
//        mBattleSettingView?.listener = object : BattleSettingView.Listener {
//            override fun onClickPunishment() {
//                mBattlePunishView.show()
//                mBottomWidgetAnimationController.close(BattleBottomWidgetAnimationController.OPEN_TYPE_SETTING)
//            }
//
//            override fun onClickRoomSetting() {
//                if (mRoomData.getPlayerInfoById(MyUserInfoManager.uid.toInt()) == null
//                        || mRoomData.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isHost() == false) {
//                    U.getToastUtil().showShort("只有主持人才能进行房间设置哦～")
//                    return
//                }
//
//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newAddParamsBuilder(this@BattleRoomActivity, BattleRoomSettingFragment::class.java)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .build())
//            }
//
//            override fun onClickQuickAnswer() {
//                mCorePresenter.beginQuickAnswer()
////                mRightQuickAnswerView?.playCountDown(System.currentTimeMillis()+3000,System.currentTimeMillis()+10000)
//            }
//
//            override fun onClickGameSetting() {
//                ARouter.getInstance().build(RouterConstants.ACTIVITY_BATTLE_SELECT_GAME)
//                        .navigation()
//            }
//
//            override fun onClickGameSound() {
//
//            }
//
//            override fun onClickVote() {
//                val battleSendVoteDialogView = BattleSendVoteDialogView(this@BattleRoomActivity)
//                battleSendVoteDialogView.showByDialog()
//            }
//        }
//        mBattleEmojiView = BattleEmojiView(findViewById(R.id.battle_bottom_emoji_viewStub))
    }

    private fun showPanelView() {
        val battleGameInfoModel = mRoomData?.realRoundInfo?.sceneInfo

//        if (battleGameInfoModel?.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
//            if (battleGameInfoModel?.ktv?.userID ?: 0 > 0) {
//                if (battleGameInfoModel?.ktv?.userID == MyUserInfoManager.uid.toInt()) {
//                    //自己唱
//                    mGiftPanelView?.show(null)
//                } else {
//                    //别人在唱
//                    mGiftPanelView?.show(mRoomData.getPlayerInfoById(battleGameInfoModel?.ktv?.userID
//                            ?: 0)?.userInfo)
//                }
//            } else {
//                //还没开始
//                mGiftPanelView?.show(null)
//            }
//        } else {
            //别的模式
            mGiftPanelView?.show(null)
//        }
    }

    private fun buyFlowerFromOuter() {
//        EventBus.getDefault().post(BuyGiftEvent(NormalGift.getFlower(), mRoomData.peerUser?.userInfo))
    }

    val mInviteCallBack = object : IInviteCallBack {
        override fun getFrom(): Int {
            return GameModeType.GAME_MODE_BATTLE
        }

        override fun getInviteDialogText(kouling: String?): String {
            return ""
        }

        override fun getShareTitle(): String {
            return "这个房间有点意思，还不戳进来看看！"
        }

        override fun getShareDes(): String {
            return "我在这个房间唱歌玩游戏，邀你一起嗨"
        }

        override fun getInviteObservable(model: UserInfoModel?): Observable<ApiResult> {
            StatisticsAdapter.recordCountEvent("battle", "invite", null)
            MyLog.d(TAG, "inviteMicFriend roomID=${H.battleRoomData?.gameId ?: 0} model=$model")
            val map = mutableMapOf("roomID" to H.battleRoomData?.gameId, "userID" to model?.getUserId())
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            return ApiManager.getInstance().createService(BattleRoomServerApi::class.java).invite(body)
        }

        override fun getRoomID(): Int {
            return H.battleRoomData?.gameId ?: 0
        }

        override fun getKouLingTokenObservable(): Observable<ApiResult> {
            val code = String.format("inframeskr://room/joinbattle?owner=%s&gameId=%s&ask=1&mediaType=%s", MyUserInfoManager.uid, mRoomData.gameId, 0)
            return ApiManager.getInstance().createService(KouLingServerApi::class.java).setTokenByCode(code)
        }

        override fun needShowFans(): Boolean {
            return true
        }
    }

    private fun initTopView() {
        mTopOpView = findViewById(R.id.top_op_view)
        mTopOpView.setListener(object : BattleTopOpView.Listener {
            override fun onClickInviteRoom() {
//                ARouter.getInstance().build(RouterConstants.ACTIVITY_INVITE_FRIEND)
//                        .withInt("from", GameModeType.GAME_MODE_BATTLE)
//                        .withInt("roomId", H.battleRoomData?.gameId ?: 0)
//                        .navigation()

                InviteFriendActivity.open(mInviteCallBack)
            }

            override fun onClickChangeRoom() {
                if (mRoomData?.myUserInfo?.isHost() == true || mRoomData?.myUserInfo?.isGuest() == true) {
                    U.getToastUtil().showShort("在麦上不能切房间哦～")
                    return
                }
                mBeginChangeRoomTs = System.currentTimeMillis()
                mChangeRoomTransitionView?.setVisibility(View.VISIBLE)
                mCorePresenter?.changeRoom()
            }

            override fun onClickRoomReport() {
//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newAddParamsBuilder(this@BattleRoomActivity, QuickFeedbackFragment::class.java)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_BATTLE_ROOM)
//                                .addDataBeforeAdd(1, QuickFeedbackFragment.REPORT)
//                                .addDataBeforeAdd(3, mRoomData.gameId)
//                                .addDataBeforeAdd(4, mRoomData.roomName)
//                                .setEnterAnim(R.anim.slide_in_bottom)
//                                .setExitAnim(R.anim.slide_out_bottom)
//                                .build())
            }

            override fun onClickGameRule() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@BattleRoomActivity)
                showGameRuleDialog()
            }

            override fun onClickFeedBack() {
//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newAddParamsBuilder(this@BattleRoomActivity, QuickFeedbackFragment::class.java)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_BATTLE_ROOM)
//                                .addDataBeforeAdd(1, QuickFeedbackFragment.FEED_BACK)
//                                .addDataBeforeAdd(3, mRoomData.gameId)
//                                .setEnterAnim(R.anim.slide_in_bottom)
//                                .setExitAnim(R.anim.slide_out_bottom)
//                                .build())
            }

            override fun onClickVoiceAudition() {
                // 调音面板
                U.getKeyBoardUtils().hideSoftInputKeyBoard(this@BattleRoomActivity)
                dismissDialog()
                if (mVoiceControlPanelView == null) {
                    mVoiceControlPanelView = BattleVoiceControlPanelView(this@BattleRoomActivity)
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

//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newAddParamsBuilder(this@BattleRoomActivity, BattleRoomSettingFragment::class.java)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .build())
            }

            override fun closeBtnClick() {
                quitGame()
            }
        })
        mTopOpView.bindData()
        mTopContentView = findViewById(R.id.top_content_view)
        mTopContentView.roomData = mRoomData
        mTopContentView.bindData()
        mTopContentView.listener = object : BattleTopContentView.Listener {
            override fun showRoomMember() {
                // 查看房间所有人
                dismissDialog()
            }

            override fun clickArrow(open: Boolean) {
                if (open) {
                    mWidgetAnimationController.open()
                } else {
                    mWidgetAnimationController.close()
                }
            }

            override fun showBattleBeHostConfirm() {
//                getBattleManageHostDialogView().apply {
//                    function1.text = "上麦"
//                    function1.setDebounceViewClickListener {
//                        mCorePresenter.becomeClubHost {
//                            dismissDialog()
//                            mTipsDialogView = TipsDialogView.Builder(this@BattleRoomActivity)
//                                    .setMessageTip("为保障绿色、文明的主题房游戏环境，需要对主持人进行实名认证哦！")
//                                    .setConfirmTip("立即认证")
//                                    .setCancelTip("暂不")
//                                    .setConfirmBtnClickListener {
//                                        dismissDialog()
//                                        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
//                                                .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=room"))
//                                                .greenChannel().navigation();
//                                    }
//                                    .setCancelBtnClickListener {
//                                        dismissDialog()
//                                    }
//                                    .build()
//                            mTipsDialogView?.showByDialog()
//                        }
//                        dismissDialog()
//                    }
//                    function2.visibility = View.GONE
//                }.showByDialog()
            }

            override fun showBattleOpHost() {
//                getBattleManageHostDialogView().apply {
//                    function1.text = "上麦"
//                    function1.setDebounceViewClickListener {
//                        mCorePresenter.takeClubHost()
//                        dismissDialog()
//                    }
//
//                    function2.visibility = View.VISIBLE
//                    function2.setDebounceViewClickListener {
//                        dismissDialog() // 2个对话框
//                        EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.hostId))
//                    }
//                }.showByDialog()
            }

            override fun showBattleSelfOpHost() {
//                getBattleManageHostDialogView().apply {
//                    function1.text = "下麦"
//                    function1.setDebounceViewClickListener {
//                        mCorePresenter.giveClubHost()
//                        dismissDialog()
//                    }
//
//                    function2.visibility = View.VISIBLE
//                    function2.setDebounceViewClickListener {
//                        dismissDialog() // 2个对话框
//                        EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.hostId))
//                    }
//                }.showByDialog()
            }

            override fun showClubInfoCard() {
                dismissDialog()
                mClubCardDialogView = ClubCardDialogView(this@BattleRoomActivity, mRoomData.clubInfo?.clubID
                        ?: 0)
                mClubCardDialogView?.showByDialog()
            }
        }
    }

    private fun showGameRuleDialog() {
        dismissDialog()
        mGameRuleDialog = DialogPlus.newDialog(this)
                .setContentHolder(ViewHolder(R.layout.battle_game_rule_view_layout))
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
        mContinueSendView.mScene = ContinueSendView.EGameScene.GS_Battle
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

        mPersonInfoDialog = PersonInfoDialog.Builder(this, QuickFeedbackFragment.FROM_BATTLE_ROOM, userID, showKick, true, true)
                .setRoomID(mRoomData.gameId)
                .setInviteReplyListener { userInfoModel ->
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService.tryInviteToRelay(userInfoModel.userId, userInfoModel.isFriend, false)
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

//    private fun showBattleManageView(model: BattleActorInfoModel?) {
//        dismissDialog()
//        mInputContainerView.hideSoftInput()
//        mBattleManageDialogView = BattleManageDialogView(this, model, mInviteCallBack)
//        mBattleManageDialogView?.showByDialog()
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowPersonCardEvent) {
        showPersonInfoView(event.uid, event.showKick)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SendGiftByPersonCardEvent) {
        val info = mRoomData.getPlayerInfoById(event.model.userId)

        if (info != null) {
            mGiftPanelView.show(info.userInfo)
        } else {
            mGiftPanelView.show(event.model)
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: PBeginPunish) {
//        MyLog.d(TAG, "PBeginPunish onEvent event = $event 1")
//        if (H.battleRoomData?.hostId == MyUserInfoManager.uid.toInt()) {
//            MyLog.d(TAG, "PBeginPunish onEvent event = $event 2")
//            return
//        }
//
//        mBattlePunishView.showWithGuest(event)
//    }

//    private fun getBattleManageHostDialogView(): BattleManageHostDialogView {
//        if (mBattleManageHostDialogView == null) {
//            mBattleManageHostDialogView = BattleManageHostDialogView(this)
//        }
//
//        return mBattleManageHostDialogView!!
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: BattleSelectSongEvent) {
//        SongManagerActivity.open(this@BattleRoomActivity, mRoomData)
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BuyGiftEvent) {
        if (event.receiver.userId != MyUserInfoManager.uid.toInt()) {
            mContinueSendView.startBuy(event.baseGift, event.receiver)
        } else {
            U.getToastUtil().showShort("只能给正在演唱的其他选手送礼哦～")
        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: BattleHostChangeEvent) {
//        if (mRoomData.isClubHome()) {
//            if (mRoomData.hostId > 0) {
//                val model = mRoomData.getPlayerInfoById(mRoomData.hostId)
//                mCorePresenter?.pretendSystemMsg("${UserInfoManager.getInstance().getRemarkName(model?.userInfo?.userId
//                        ?: 0, model?.userInfo?.nickname)} 已成为新的主持人")
//                if ((model?.userInfo?.userId ?: 0) == MyUserInfoManager.uid.toInt()) {
//                    finishTopActivity("您已成为主持人")
//                    showHostOpTips()
//                }
//            } else {
//                mCorePresenter?.pretendSystemMsg("主持人已下麦，已自动结束所有游戏")
//            }
//        }
//    }

    private fun finishTopActivity(str: String) {
        var hasActivity = false
        for (i in U.getActivityUtils().activityList.size - 1 downTo 0) {
            val activity = U.getActivityUtils().activityList[i]
            if (activity is BattleRoomActivity) {
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

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: PKickoutUserMsg) {
//        //todo 需不需要让被踢人游戏直接结束
//        MyLog.d(TAG, "onEvent event = $event")
//        if (event.kickUser.userInfo.userID == MyUserInfoManager.uid.toInt()) {
//            // 我被踢出去了
//            U.getToastUtil().showSkrCustomLong(CommonToastView.Builder(U.app())
//                    .setImage(R.drawable.touxiangshezhishibai_icon)
//                    .setText("管理员已将你踢出房间")
//                    .build())
//            finish()
//        } else {
//            val opUser = BattlePlayerInfoModel.parseFromPb(event.opUser)
//            val stringBuilder = StringBuilder()
//            if (opUser.isHost()) {
//                stringBuilder.append("主持人")
//            } else if (opUser.isAdmin()) {
//                stringBuilder.append("管理员")
//            }
//
//            val kickUser = BattlePlayerInfoModel.parseFromPb(event.kickUser)
//            stringBuilder.append("将${kickUser.userInfo?.nicknameRemark}踢出了房间")
//            mCorePresenter.pretendSystemMsg(stringBuilder.toString())
//        }
//    }

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
        mConfirmDialog?.dismiss(false)
        mClubCardDialogView?.dismiss(false)
    }

    /**
     * 某个轮次结束了
     */
    override fun showRoundOver(lastRoundInfo: BattleRoundInfoModel?, continueOp: (() -> Unit)?) {
        continueOp?.invoke()
    }

    /**
     * 某个游戏开始了 信息在 realRoundInfo里取
     */
    override fun gameBegin(thisRound: BattleRoundInfoModel?) {
        mBattleGameMainView?.updateRound(thisRound)
    }

    override fun showVoteView(event: PBeginVote) {
        if ((System.currentTimeMillis() - BaseRoomData.shiftTsForRelay) - event.beginTimeMs >= event.endTimeMs - event.beginTimeMs) {
            MyLog.w(TAG, "已经过了投票时间，PBeginVote event.voteTag is ${event.voteTag}")
        } else {
            val battleVoteDialogView = BattleVoteDialogView(this@BattleRoomActivity, event)
            battleVoteDialogView.showByDialog()
        }
    }

    /**
     * 没有游戏了
     */
    override fun showWaiting() {
        mBattleGameMainView?.toEmptyState()

    }

    override fun joinNotice(model: BattlePlayerInfoModel?) {
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
                .setMessageTip(warningMsg)
                .setOkBtnTip("我知道了")
                .setOkBtnClickListener {
                    mTipsDialogView?.dismiss()
                }
                .build()
        mTipsDialogView?.showByDialog()
    }

    override fun beginQuickAnswer(beginTs: Long, endTs: Long) {
        mRightQuickAnswerView?.playCountDown(beginTs, endTs)
    }

    internal var mBeginChangeRoomTs: Long = 0

    override fun onChangeRoomResult(success: Boolean, errMsg: String?) {
        val t = System.currentTimeMillis() - mBeginChangeRoomTs
        if (t > 1500) {
            mChangeRoomTransitionView?.setVisibility(View.GONE)
            if (!success) {
                U.getToastUtil().showShort(errMsg)
            }
        } else {
            mUiHandler.postDelayed({
                if (!success) {
                    U.getToastUtil().showShort(errMsg)
                }
                mChangeRoomTransitionView?.setVisibility(View.GONE)
            }, 1500 - t)
        }
        if (success) {
//            initBgView()
//            hideAllCardView()
            mVipEnterPresenter?.switchRoom()
            mVIPEnterView?.switchRoom()
            mTopContentView.switchRoom()
            // 重新决定显示mic按钮
            mBottomContainerView?.setRoomData(mRoomData!!)
//            mGrabTopContentView.onChangeRoom()
//            adjustSelectSongView()
            // 换房间也要弹窗
            checkGoMicTips()
        }

    }
}
