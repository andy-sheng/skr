package com.module.playways.party.room

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.view.*
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.base.FragmentDataListener
import com.common.core.kouling.SkrKouLingUtils
import com.common.core.kouling.api.KouLingServerApi
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.flutter.boost.FlutterBoostController
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.component.busilib.constans.GameModeType
import com.component.busilib.view.GameEffectBgView
import com.component.dialog.ClubCardDialogView
import com.component.dialog.ConfirmDialog
import com.component.dialog.PersonInfoDialog
import com.component.person.event.SendGiftByPersonCardEvent
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
import com.module.playways.grab.room.invite.IInviteCallBack
import com.module.playways.grab.room.invite.InviteFriendActivity
import com.module.playways.grab.room.presenter.VipEnterPresenter
import com.module.playways.grab.room.view.GrabChangeRoomTransitionView
import com.module.playways.grab.room.view.VIPEnterView
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.party.bgmusic.getLocalMusicInfo
import com.module.playways.party.home.PartyHomeActivity
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.view.PartySendDiamondBoxDialogView
import com.module.playways.party.room.actor.PartyApplyPanelView
import com.module.playways.party.room.actor.PartyMemberPanelView
import com.module.playways.party.room.bottom.PartyBottomContainerView
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.event.PartySelectSongEvent
import com.module.playways.party.room.fragment.PartyRoomSettingFragment
import com.module.playways.party.room.model.PartyActorInfoModel
import com.module.playways.party.room.model.PartyDiamondboxModel
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.party.room.presenter.PartyCorePresenter
import com.module.playways.party.room.seat.PartySeatView
import com.module.playways.party.room.top.PartyTopContentView
import com.module.playways.party.room.top.PartyTopOpView2
import com.module.playways.party.room.ui.IPartyRoomView
import com.module.playways.party.room.ui.PartyBottomWidgetAnimationController
import com.module.playways.party.room.ui.PartyWidgetAnimationController
import com.module.playways.party.room.view.*
import com.module.playways.party.search.PartyRoomSearchActivity
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
import com.zq.live.proto.Common.PBeginDiamondbox
import com.zq.live.proto.PartyRoom.*
import com.zq.live.proto.broadcast.PartyDiamondbox
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


@Route(path = RouterConstants.ACTIVITY_PARTY_ROOM)
class PartyRoomActivity : BaseActivity(), IPartyRoomView, IGrabVipView {
    companion object {
        val playerTag = "PartyRoomActivity"
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

            if (activity is PartyHomeActivity) {
                continue
            }
            if (activity is PartyRoomSearchActivity) {
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
    internal lateinit var mTopOpView: PartyTopOpView2
    internal lateinit var mTopContentView: PartyTopContentView

    internal lateinit var mGameEffectBgView: GameEffectBgView
    internal lateinit var mPartyPunishView: PartyPunishView

    var mRightOpView: PartyRightOpView? = null
    var mRightQuickAnswerView: PartyRightQuickAnswerView? = null
    var mPartyGameMainView: PartyGameMainView? = null
    var mSeatView: PartySeatView? = null
    var mPartySettingView: PartySettingView? = null
    var mPartyEmojiView: PartyEmojiView? = null
    var mChangeRoomTransitionView: GrabChangeRoomTransitionView? = null
    var mPartySendDiamondBoxView: PartySendDiamondBoxDialogView? = null

    lateinit var mAddSongIv: ImageView
    lateinit var mChangeSongIv: ImageView

    private var mVIPEnterView: VIPEnterView? = null
    // 都是dialogplus
    private var mMainActContainer: ExConstraintLayout? = null
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
    private var mPartyDiamondBoxView: PartyDiamondBoxView? = null

    private var mVipEnterPresenter: VipEnterPresenter? = null

    lateinit var mVoiceRecordUiController: VoiceRecordUiController

    val mWidgetAnimationController = PartyWidgetAnimationController(this)
    val mBottomWidgetAnimationController = PartyBottomWidgetAnimationController(this)

    internal var mSkrAudioPermission = SkrAudioPermission()

    internal var mHostOpTipImageView: ImageView? = null

    val SP_KEY_HOST_TIP_TIMES = "sp_key_host_tips_show_times"
    val REMOVE_HOST_OP_TIP_MSG = 0x01     // 主持人操作提示
    val CHECK_GO_MIC_TIP_MSG = 0x02  // 去上麦或者换房间的提示

    val mUiHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                REMOVE_HOST_OP_TIP_MSG -> {
                    removeHostOpTips()
                }
                CHECK_GO_MIC_TIP_MSG -> {
                    showGoMicTips()
                }
            }
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
        intent.getStringExtra("extra")?.let {
            mRoomData.partyDiamondboxModel = JSON.parseObject(it, PartyDiamondboxModel::class.java)
        }

        H.partyRoomData = mRoomData
        H.setType(GameModeType.GAME_MODE_PARTY, "PartyRoomActivity")

        mCorePresenter = PartyCorePresenter(mRoomData, this)
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

        initGameMainView()
        initMicSeatView()
        initRightOpView()
        initVipEnterView()
        initChangeRoomTransitionView()
        initPunishView()
        initDiamondBoxView()

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

//        if (U.getPreferenceUtils().getSettingBoolean("is_first_enter_party_room", true)) {
//            U.getPreferenceUtils().setSettingBoolean("is_first_enter_party_room", false)
//            showGameRuleDialog()
//        }

        MyUserInfoManager.myUserInfo?.let {
            if (it.ranking != null) {
                mVipEnterPresenter?.addNotice(MyUserInfo.toUserInfoModel(it))
            }
        }

        U.getStatusBarUtil().setTransparentBar(this, false)
        showHostOpTips()
        checkGoMicTips()

    }

    private fun initDiamondBoxView() {

        mPartyDiamondBoxView = PartyDiamondBoxView(findViewById(R.id.diamond_box_waiting))
        mBottomContainerView.enableDiamondBoxSenderBtn {
            mPartySendDiamondBoxView = mPartySendDiamondBoxView?: PartySendDiamondBoxDialogView(this)
            mPartySendDiamondBoxView?.show(this, mRoomData.gameId)
        }

        refreshDiamondBox()
    }

    private fun refreshDiamondBox(){
        // 存在宝箱，展示宝箱
        mRoomData.partyDiamondboxModel?.let {
            showDiamondBoxCuntDown(it)
        }

    }

    private fun showDiamondBoxCuntDown(partyDiamondboxModel: PartyDiamondboxModel){
        mPartyDiamondBoxView?.setVisibility(View.VISIBLE)
        mPartyDiamondBoxView?.bindData(partyDiamondboxModel)
    }


    private fun checkGoMicTips() {
        if (mRoomData.joinSrc == JoinPartyRoomRspModel.JRS_QUICK_JOIN || mRoomData.joinSrc == JoinPartyRoomRspModel.JRS_CHANGE_ROOM) {
            mUiHandler.removeMessages(CHECK_GO_MIC_TIP_MSG)
            mUiHandler.sendEmptyMessageDelayed(CHECK_GO_MIC_TIP_MSG, 6000L)
        }
    }

    private fun showGoMicTips() {
        // 用户不在麦上、有空位、房间允许观众自由上麦
        if ((mRoomData.myUserInfo?.isGuest() != true && mRoomData.myUserInfo?.isHost() != true
                        && mRoomData.getSeatMode == 1) && mRoomData.hasEmptySeat()) {
            // 不在麦上, 且不需要申请上麦，且座位还没满
            MyLog.d(TAG, "need showGoMicTips")
            var roundInfoModel = mRoomData.realRoundInfo
            if (roundInfoModel == null) {
                roundInfoModel = mRoomData.expectRoundInfo
            }
            val gameInfoModel = roundInfoModel?.sceneInfo
            dismissDialog()
            mTipsDialogView = TipsDialogView.Builder(this)
                    .setTitleTip(gameInfoModel?.rule?.ruleName)
                    .setMessageTip("快上麦一起玩吧")
                    .setCancelTip("换个房间")
                    .setConfirmTip("立即上麦")
                    .setConfirmBtnClickListener {
                        mCorePresenter.selfGetSeat()
                        mTipsDialogView?.dismiss(false)
                    }
                    .setCancelBtnClickListener {
                        StatisticsAdapter.recordCountEvent("party", "popup_change_room", null)
                        mCorePresenter.changeRoom()
                        mTipsDialogView?.dismiss(false)
                    }
                    .build()
            mTipsDialogView?.showByDialog()
        }
    }

    private fun showHostOpTips() {
        if (mRoomData.myUserInfo?.isHost() == true) {
            val times = U.getPreferenceUtils().getSettingInt(SP_KEY_HOST_TIP_TIMES, 0)
            if (times < 2) {
                U.getPreferenceUtils().setSettingInt(SP_KEY_HOST_TIP_TIMES, times + 1)
                mHostOpTipImageView = ImageView(this)
                mHostOpTipImageView?.setImageResource(R.drawable.party_host_tips_icon)
                val layoutParams = ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.rightMargin = 15.dp()
                layoutParams.bottomMargin = 50.dp()
                mMainActContainer?.addView(mHostOpTipImageView, layoutParams)
                mUiHandler.removeMessages(REMOVE_HOST_OP_TIP_MSG)
                mUiHandler.sendEmptyMessageDelayed(REMOVE_HOST_OP_TIP_MSG, 15000L)
                mHostOpTipImageView?.setDebounceViewClickListener {
                    removeHostOpTips()
                }
            }
        }
    }

    private fun removeHostOpTips() {
        mUiHandler.removeMessages(REMOVE_HOST_OP_TIP_MSG)
        if (mMainActContainer?.indexOfChild(mHostOpTipImageView) != -1) {
            mMainActContainer?.removeView(mHostOpTipImageView)
        }
    }

    private fun initPunishView() {
        mPartyPunishView = PartyPunishView(findViewById(R.id.party_punish_view_layout_viewStub), mRoomData)
    }

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
        mPartyApplyPanelView?.destory()
        mPartyMemberPanelView?.destory()
        mWidgetAnimationController.destroy()
        mBottomWidgetAnimationController.destroy()
        mGiftPanelView?.destroy()
        H.reset("PartyRoomActivity")
    }

    override fun finish() {
        super.finish()
        MyLog.w(TAG, "finish")
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeyboardShow(): Boolean {
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
                            if (mRoomData.getSeatMode == EGetSeatMode.EGSM_NO_APPLY.value) {
                                // 产品说让直接就上去了
                                mRightOpView?.selfGetSeat()
                            } else {
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
                removeHostOpTips()
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
            override fun onClickPunishment() {
                mPartyPunishView.show()
                mBottomWidgetAnimationController.close(PartyBottomWidgetAnimationController.OPEN_TYPE_SETTING)
            }

            override fun onClickRoomSetting() {
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
                            ?: 0)?.userInfo)
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

    val mInviteCallBack = object : IInviteCallBack {
        override fun getFrom(): Int {
            return GameModeType.GAME_MODE_PARTY
        }

        override fun getInviteDialogText(kouling: String?): String {
            return SkrKouLingUtils.genJoinPartyRoomText(kouling)
        }

        override fun getShareTitle(): String {
            return "这个房间有点意思，还不戳进来看看！"
        }

        override fun getShareDes(): String {
            return "我在这个房间唱歌玩游戏，邀你一起嗨"
        }

        override fun getInviteObservable(model: UserInfoModel?): Observable<ApiResult> {
            StatisticsAdapter.recordCountEvent("party", "invite", null)
            MyLog.d(TAG, "inviteMicFriend roomID=${H.partyRoomData?.gameId ?: 0} model=$model")
            val map = mutableMapOf("roomID" to H.partyRoomData?.gameId, "userID" to model?.getUserId())
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            return ApiManager.getInstance().createService(PartyRoomServerApi::class.java).invite(body)
        }

        override fun getRoomID(): Int {
            return H.partyRoomData?.gameId ?: 0
        }

        override fun getKouLingTokenObservable(): Observable<ApiResult> {
            val code = String.format("inframeskr://room/joinparty?owner=%s&gameId=%s&ask=1&mediaType=%s", MyUserInfoManager.uid, mRoomData.gameId, 0)
            return ApiManager.getInstance().createService(KouLingServerApi::class.java).setTokenByCode(code)
        }

        override fun needShowFans(): Boolean {
            return true
        }
    }

    private fun initTopView() {
        mTopOpView = findViewById(R.id.top_op_view)
        mTopOpView.setListener(object : PartyTopOpView2.Listener {
            override fun onClickInviteRoom() {
//                ARouter.getInstance().build(RouterConstants.ACTIVITY_INVITE_FRIEND)
//                        .withInt("from", GameModeType.GAME_MODE_PARTY)
//                        .withInt("roomId", H.partyRoomData?.gameId ?: 0)
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
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(this@PartyRoomActivity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_PARTY_ROOM)
                                .addDataBeforeAdd(1, QuickFeedbackFragment.REPORT)
                                .addDataBeforeAdd(3, mRoomData.gameId)
                                .addDataBeforeAdd(4, mRoomData.roomName)
                                .setEnterAnim(R.anim.slide_in_bottom)
                                .setExitAnim(R.anim.slide_out_bottom)
                                .build())
            }

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
        mTopOpView.bindData()
        mTopContentView = findViewById(R.id.top_content_view)
        mTopContentView.roomData = mRoomData
        mTopContentView.bindData()
        mTopContentView.listener = object : PartyTopContentView.Listener {
            override fun showPartyRankList() {
                FlutterBoostController.openFlutterPage(this@PartyRoomActivity, "RoomRankPage", mutableMapOf(
                        "roomID" to mRoomData.gameId
                ))
            }

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
                        mCorePresenter.becomeClubHost {
                            dismissDialog()
                            mTipsDialogView = TipsDialogView.Builder(this@PartyRoomActivity)
                                    .setMessageTip("为保障绿色、文明的主题房游戏环境，需要对主持人进行实名认证哦！")
                                    .setConfirmTip("立即认证")
                                    .setCancelTip("暂不")
                                    .setConfirmBtnClickListener {
                                        dismissDialog()
                                        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                                .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=room"))
                                                .greenChannel().navigation();
                                    }
                                    .setCancelBtnClickListener {
                                        dismissDialog()
                                    }
                                    .build()
                            mTipsDialogView?.showByDialog()
                        }
                        dismissDialog()
                    }
                    function2.visibility = View.GONE
                }.showByDialog()
            }

            override fun showPartyOpHost() {
                getPartyManageHostDialogView().apply {
                    function1.text = "上麦"
                    function1.setDebounceViewClickListener {
                        mCorePresenter.takeClubHost()
                        dismissDialog()
                    }

                    function2.visibility = View.VISIBLE
                    function2.setDebounceViewClickListener {
                        dismissDialog() // 2个对话框
                        EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.hostId))
                    }
                }.showByDialog()
            }

            override fun showPartySelfOpHost() {
                getPartyManageHostDialogView().apply {
                    function1.text = "下麦"
                    function1.setDebounceViewClickListener {
                        mCorePresenter.giveClubHost()
                        dismissDialog()
                    }

                    function2.visibility = View.VISIBLE
                    function2.setDebounceViewClickListener {
                        dismissDialog() // 2个对话框
                        EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.hostId))
                    }
                }.showByDialog()
            }

            override fun showClubInfoCard() {
                dismissDialog()
                mClubCardDialogView = ClubCardDialogView(this@PartyRoomActivity, mRoomData.clubInfo)
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

    private fun showPartyManageView(model: PartyActorInfoModel?) {
        dismissDialog()
        mInputContainerView.hideSoftInput()
        mPartyManageDialogView = PartyManageDialogView(this, model, mInviteCallBack)
        mPartyManageDialogView?.showByDialog()
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PBeginPunish) {
        MyLog.d(TAG, "PBeginPunish onEvent event = $event 1")
        if (H.partyRoomData?.hostId == MyUserInfoManager.uid.toInt()) {
            MyLog.d(TAG, "PBeginPunish onEvent event = $event 2")
            return
        }

        mPartyPunishView.showWithGuest(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyDiamondboxModel){
        mRoomData.partyDiamondboxModel = event
        showDiamondBoxCuntDown(event)
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
                    showHostOpTips()
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
        refreshDiamondBox()
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

            //换房间要销毁前一个Room的宝箱
            mPartyDiamondBoxView?.destroy()
        }
    }

    override fun canGoPersonPage(): Boolean {
        return false
    }
}
