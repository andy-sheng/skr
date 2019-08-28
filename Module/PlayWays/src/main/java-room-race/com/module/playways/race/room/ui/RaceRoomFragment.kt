package com.module.playways.race.room.ui

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.component.dialog.PersonInfoDialog
import com.component.person.event.ShowPersonCardEvent
import com.component.report.fragment.QuickFeedbackFragment
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.listener.AnimationListener
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.bottom.RaceBottomContainerView
import com.module.playways.race.room.event.RaceScoreChangeEvent
import com.module.playways.race.room.event.RaceWantSingChanceEvent
import com.module.playways.race.room.inter.IRaceRoomView
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.race.room.presenter.RaceCorePresenter
import com.module.playways.race.room.view.*
import com.module.playways.race.room.view.actor.RaceActorPanelView
import com.module.playways.race.room.view.topContent.RaceTopContentView
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
import com.zq.live.proto.RaceRoom.ERaceRoundOverReason
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceRoomFragment : BaseFragment(), IRaceRoomView {

    internal lateinit var mCorePresenter: RaceCorePresenter

    internal lateinit var mInputContainerView: RaceInputContainerView
    internal lateinit var mBottomContainerView: RaceBottomContainerView
    internal lateinit var mVoiceRecordTipsView: VoiceRecordTipsView
    internal lateinit var mCommentView: CommentView
    internal lateinit var mGiftPanelView: GiftPanelView
    internal lateinit var mContinueSendView: ContinueSendView
    internal lateinit var mRaceTopOpView: RaceTopOpView
    internal lateinit var mRaceTopContentView: RaceTopContentView
    internal lateinit var mRaceTopVsView: RaceTopVsView

    internal lateinit var mRaceRightOpView: RaceRightOpView

    private lateinit var mRaceSelectSongView: RaceSelectSongView   // 选歌
    private lateinit var mRaceWaitingCardView: RaceWaitingCardView   // 等待中
    private lateinit var mRaceTurnInfoCardView: RaceTurnInfoCardView  // 下一局
    private lateinit var mRaceSelfSingLyricView: RaceSelfSingLyricView  // 自己唱
    private lateinit var mRaceOtherSingCardView: RaceOtherSingCardView   // 别人唱
    private lateinit var mRaceNoSingCardView: RaceNoSingerCardView    // 无人响应
    private lateinit var mRaceMiddleResultView: RaceMiddleResultView   // 比赛结果

    // 都是dialogplus
    private var mRaceActorPanelView: RaceActorPanelView? = null  //参与的人
    private var mPersonInfoDialog: PersonInfoDialog? = null
    private var mRaceVoiceControlPanelView: RaceVoiceControlPanelView? = null

    lateinit var mVoiceRecordUiController: VoiceRecordUiController
    val mRaceWidgetAnimationController = RaceWidgetAnimationController(this)

    var mLastSceneView: View? = null
        set(value) {
            MyLog.d(TAG, "mLastSceneView = $value")
            if (value != mLastSceneView) {
                mLastSceneView?.visibility = View.GONE
                value?.visibility = View.VISIBLE
                field = value
            }
        }

    val mUiHanlder = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
        }
    }


    var mRoomData = RaceRoomData()
    override fun initView(): Int {
        return R.layout.race_room_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mCorePresenter = RaceCorePresenter(mRoomData, this)
        addPresent(mCorePresenter)
        // 请保证从下面的view往上面的view开始初始化
        rootView.setOnTouchListener { v, event ->
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
        initSelectSongView()
        initResuleView()

        initSingSenceView()
        initRightView()

        mCorePresenter.onOpeningAnimationOver()

        mUiHanlder.postDelayed(Runnable {
            mRaceWidgetAnimationController.close()
        }, 500)
    }

    private fun initResuleView() {
        mRaceNoSingCardView = rootView.findViewById(R.id.race_nosinger_result_view)
        mRaceMiddleResultView = rootView.findViewById(R.id.race_middle_result_view)
        mRaceMiddleResultView.setRaceRoomData(mRoomData)
    }


    private fun initSelectSongView() {
        mRaceSelectSongView = rootView.findViewById(R.id.race_select_song_view)
        mRaceSelectSongView.setRoomData(mRoomData) {
            mCorePresenter.wantSingChance(it)
        }
        mRaceSelectSongView.visibility = View.GONE
    }

    private fun initRightView() {
        mRaceRightOpView = rootView.findViewById(R.id.race_right_op_view)
        mRaceRightOpView.setListener(object : RightOpListener {
            override fun onClickGiveUp() {
                // 放弃演唱
                mCorePresenter.sendBLight {
                    if (it) {
                        mRaceRightOpView.showGiveUp(true)
                    } else {
                        MyLog.e(TAG, "onClickGiveUp 请求失败了")
                    }
                }
            }

            override fun onClickVote() {
                // 投票
                mCorePresenter.giveupSing {
                    if (it) {
                        mRaceRightOpView.showVote(true)
                    } else {
                        MyLog.e(TAG, "onClickVote 请求失败了")
                    }
                }
            }
        })
    }

    private fun initSingSenceView() {
        mRaceSelfSingLyricView = RaceSelfSingLyricView(rootView.findViewById(R.id.race_self_sing_lyric_view_stub) as ViewStub, null)
        mRaceOtherSingCardView = RaceOtherSingCardView(rootView.findViewById(R.id.race_other_sing_lyric_view_stub) as ViewStub, mRoomData)
    }

    private fun initInputView() {
        mInputContainerView = rootView.findViewById(R.id.input_container_view)
        mInputContainerView.setRoomData(mRoomData)
    }

    private fun initTurnSenceView() {
        mRaceWaitingCardView = rootView.findViewById(R.id.race_wait_card_view)
        mRaceWaitingCardView.visibility = View.GONE
        mRaceTurnInfoCardView = rootView.findViewById(R.id.race_turn_card_view)
        mRaceTurnInfoCardView.visibility = View.GONE
    }

    private fun initBottomView() {
        run {
            val voiceStub = rootView.findViewById<ViewStub>(R.id.voice_record_tip_view_stub)
            mVoiceRecordTipsView = VoiceRecordTipsView(voiceStub)
        }
//        mBottomBgVp = rootView.findViewById<ViewGroup>(R.id.bottom_bg_vp)
//        val lp = mBottomBgVp.getLayoutParams() as RelativeLayout.LayoutParams
//        /**
//         * 按比例适配手机
//         */
//        lp.height = U.getDisplayUtils().screenHeight * 284 / 667

        mBottomContainerView = rootView.findViewById(R.id.bottom_container_view)
        mBottomContainerView.setRoomData(mRoomData)
        mBottomContainerView.setListener(object : BottomContainerView.Listener() {
            override fun showInputBtnClick() {
                if (mPersonInfoDialog?.isShowing() == true) {
                    mPersonInfoDialog?.dismiss()
                }
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
                mGiftPanelView.show(null)
                mContinueSendView.setVisibility(View.GONE)
            }
        })
    }

    private fun initTopView() {
        mRaceTopOpView = rootView.findViewById(R.id.race_top_op_view)
        mRaceTopOpView.setRoomData(mRoomData)
        mRaceTopOpView.setListener(object : RaceTopOpView.Listener {
            override fun onClickVoiceAudition() {
                // 调音面板
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
                if (mRaceVoiceControlPanelView == null) {
                    mRaceVoiceControlPanelView = RaceVoiceControlPanelView(this@RaceRoomFragment)
                    mRaceVoiceControlPanelView?.setRoomData(mRoomData)
                }
                mRaceVoiceControlPanelView?.showByDialog()
            }

            override fun onClickFeedBack() {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(activity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_RACE_ROOM)
                                .addDataBeforeAdd(1, QuickFeedbackFragment.FEED_BACK)
                                .addDataBeforeAdd(3, mRoomData.gameId)
                                .setEnterAnim(R.anim.slide_in_bottom)
                                .setExitAnim(R.anim.slide_out_bottom)
                                .build())
            }

            override fun closeBtnClick() {
                // todo 退出游戏
//                if (mRoomData.isOwner() && mRoomData.getPlayerInfoList().size >= 2) {
//                    quitGame()
//                } else {
//                    mCorePresenter.exitRoom("closeBtnClick")
//                }
            }

            override fun onClickGameRule() {
                // todo 显示游戏规则
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
            }
        })


        mRaceTopContentView = rootView.findViewById(R.id.race_top_content_view)
        mRaceTopContentView.setRoomData(mRoomData)

        mRaceTopContentView.setListener(object : RaceTopContentView.Listener {
            override fun clickMore() {
                if (mRaceActorPanelView == null) {
                    mRaceActorPanelView = RaceActorPanelView(this@RaceRoomFragment, mRoomData)
                }
                mRaceActorPanelView?.showByDialog()
            }

            override fun clickArrow(open: Boolean) {
                if (open) {
                    mRaceWidgetAnimationController.open()
                } else {
                    mRaceWidgetAnimationController.close()
                }
            }
        })
//        mPracticeFlagIv = rootView.findViewById<ExImageView>(R.id.practice_flag_iv)
        // 加上状态栏的高度
        val statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(context)

//        run {
//            val topLayoutParams = mRaceTopOpView.getLayoutParams() as ConstraintLayout.LayoutParams
//            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin
//        }
//        run {
//            val topLayoutParams = mRaceTopContentView.getLayoutParams() as ConstraintLayout.LayoutParams
//            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin
//        }

        mRaceTopVsView = rootView.findViewById(R.id.race_top_vs_view)
        mRaceTopVsView.setRaceRoomData(mRoomData)
    }

    private fun initCommentView() {
        mCommentView = rootView.findViewById(R.id.comment_view)
        mCommentView.setListener(CommentViewItemListener { userId ->
            showPersonInfoView(userId)
        })
        mCommentView.roomData = mRoomData
        val layoutParams = mCommentView.layoutParams
        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px((430 + 60).toFloat())
        mVoiceRecordUiController = VoiceRecordUiController(mBottomContainerView.mVoiceRecordBtn, mVoiceRecordTipsView, mCommentView)
    }

    private fun initGiftPanelView() {
        mGiftPanelView = rootView.findViewById<View>(R.id.gift_panel_view) as GiftPanelView
        mGiftPanelView.setRoomData(mRoomData)
        mContinueSendView = rootView.findViewById<View>(R.id.continue_send_view) as ContinueSendView
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
        val giftContinueViewGroup = rootView.findViewById<GiftContinueViewGroup>(R.id.gift_continue_vg)
        giftContinueViewGroup.setRoomData(mRoomData)
        val giftOverlayAnimationViewGroup = rootView.findViewById<GiftOverlayAnimationViewGroup>(R.id.gift_overlay_animation_vg)
        giftOverlayAnimationViewGroup.setRoomData(mRoomData)
        val giftBigAnimationViewGroup = rootView.findViewById<GiftBigAnimationViewGroup>(R.id.gift_big_animation_vg)
        giftBigAnimationViewGroup.setRoomData(mRoomData)
        val giftBigContinueView = rootView.findViewById<GiftBigContinuousView>(R.id.gift_big_continue_view)
        giftBigAnimationViewGroup.setGiftBigContinuousView(giftBigContinueView)
        //mDengBigAnimation = rootView.findViewById<View>(R.id.deng_big_animation) as GrabDengBigAnimationView
    }

    private fun showPersonInfoView(userID: Int) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!")
            return
        }
        mInputContainerView.hideSoftInput()

        val mShowKick: Boolean = false
//        if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_COMMON) {
//            // 普通房
//            mShowKick = true
//        } else {
//            if (mRoomData.isOwner()) {
//                mShowKick = true
//            } else {
//                mShowKick = false
//            }
//        }

        mPersonInfoDialog = PersonInfoDialog.Builder(activity, QuickFeedbackFragment.FROM_GRAB_ROOM, userID, mShowKick, true)
                .setRoomID(mRoomData.gameId)
                .setInviteDoubleListener { userInfoModel ->
                    if (userInfoModel.isFriend) {
//                        mDoubleRoomInvitePresenter.inviteToDoubleRoom(userInfoModel.userId)
                    } else {
                        UserInfoManager.getInstance().checkIsFans(MyUserInfoManager.getInstance().uid.toInt(), userInfoModel.userId, object : ResponseCallBack<Boolean>() {
                            override fun onServerSucess(isFans: Boolean?) {
//                                if (isFans!!) {
//                                    mDoubleRoomInvitePresenter.inviteToDoubleRoom(userInfoModel.userId)
//                                } else {
//                                    mTipsDialogView = TipsDialogView.Builder(U.getActivityUtils().topActivity)
//                                            .setMessageTip("对方不是您的好友或粉丝\n要花2金币邀请ta加入双人唱聊房吗？")
//                                            .setConfirmTip("邀请")
//                                            .setCancelTip("取消")
//                                            .setConfirmBtnClickListener(object : AnimateClickListener() {
//                                                override fun click(view: View) {
//                                                    mDoubleRoomInvitePresenter.inviteToDoubleRoom(userInfoModel.userId)
//                                                    mTipsDialogView.dismiss()
//                                                }
//                                            })
//                                            .setCancelBtnClickListener(object : AnimateClickListener() {
//                                                override fun click(view: View) {
//                                                    mTipsDialogView.dismiss()
//                                                }
//                                            })
//                                            .build()
//                                    mTipsDialogView.showByDialog()
//                                }
                            }

                            override fun onServerFailed() {

                            }
                        })
                    }
                }
                .setKickListener { userInfoModel ->
                    //                    showKickConfirmDialog(userInfoModel)
                }
                .build()
        mPersonInfoDialog?.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowPersonCardEvent) {
        showPersonInfoView(event.uid)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceScoreChangeEvent) {
        mRaceTopVsView.updateData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceWantSingChanceEvent) {
        mRaceSelectSongView.updateSelectState()
    }

    override fun singBySelfFirstRound(songModel: SongModel?) {
        mLastSceneView = mRaceTopVsView
        mRaceRightOpView.showGiveUp(false)
        mRaceTopVsView.startVs()
        mRaceTopVsView.startSingBySelf {
            mRaceTopVsView.visibility = View.GONE
            mRaceSelfSingLyricView.startFly {
                mCorePresenter.sendSingComplete()
            }
            mLastSceneView = mRaceSelfSingLyricView.realView
        }
    }

    override fun singByOtherFirstRound(songModel: SongModel?, userModel: UserInfoModel?) {
        mLastSceneView = mRaceTopVsView
        if (mRoomData.realRoundInfo?.isSingerByUserId(MyUserInfoManager.getInstance().uid.toInt()) == true) {
            mRaceRightOpView.visibility = View.GONE
        } else {
            mRaceRightOpView.showVote(false)
        }
        mRaceTopVsView.startVs()
        mRaceTopVsView.startSingByOther {
            mRaceSelfSingLyricView.startFly(null)
            mLastSceneView = mRaceSelfSingLyricView.realView
        }
    }

    override fun singBySelfSecondRound(songModel: SongModel?) {
        mRaceRightOpView.showGiveUp(false)
        mRaceTopVsView.startSingBySelf {
            mRaceSelfSingLyricView.startFly {
                mCorePresenter.sendSingComplete()
            }
            mLastSceneView = mRaceSelfSingLyricView.realView
        }
    }

    override fun singByOtherSecondRound(songModel: SongModel?, userModel: UserInfoModel?) {
        if (mRoomData.realRoundInfo?.isSingerByUserId(MyUserInfoManager.getInstance().uid.toInt()) == true) {
            mRaceRightOpView.visibility = View.GONE
        } else {
            mRaceRightOpView.showVote(false)
        }
        mRaceTopVsView.startSingByOther {
            mRaceSelfSingLyricView.startFly(null)
            mLastSceneView = mRaceSelfSingLyricView.realView
        }
    }

    override fun showRoundOver(lastRoundInfo: RaceRoundInfoModel, continueOp:(()->Unit)?) {
        mRaceRightOpView.visibility = View.GONE
        if(lastRoundInfo.overReason == ERaceRoundOverReason.ERROR_NO_ONE_SING.value ||
                lastRoundInfo.overReason == ERaceRoundOverReason.ERROR_NOT_ENOUTH_PLAYER.value ){
            // 无人应战
            mLastSceneView = mRaceNoSingCardView
            mRaceNoSingCardView.showAnimation(object : AnimationListener {
                override fun onFinish() {
                    continueOp?.invoke()
                }
            })
        } else {
            mLastSceneView = mRaceMiddleResultView
            mRaceMiddleResultView.showResult(lastRoundInfo){
                continueOp?.invoke()
            }
        }
    }

    override fun showWaiting(showAnimation: Boolean) {
        mRaceRightOpView.visibility = View.GONE
        mLastSceneView = mRaceWaitingCardView
        if (showAnimation) {
            mLastSceneView = mRaceWaitingCardView
            mRaceWaitingCardView.animationEnter()
        } else {
            mRaceWaitingCardView.visibility = View.VISIBLE
        }
    }

    override fun showChoicing(showNextRound: Boolean) {
        mRaceRightOpView.visibility = View.GONE
        if (mRaceWaitingCardView.visibility == View.VISIBLE) {
            mLastSceneView = mRaceWaitingCardView
            mRaceWaitingCardView.animationLeave(object : AnimationListener {
                override fun onFinish() {
                    if (showNextRound) {
                        mRaceTurnInfoCardView.showAnimation(object : AnimationListener {
                            override fun onFinish() {
                                showSelectSongView()
                            }
                        })
                    } else {
                        showSelectSongView()
                    }
                }
            })
        } else {
            if (showNextRound) {
                mLastSceneView = mRaceTurnInfoCardView
                mRaceTurnInfoCardView.showAnimation(object : AnimationListener {
                    override fun onFinish() {
                        showSelectSongView()
                    }
                })
            } else {
                showSelectSongView()
            }
        }
    }

    private fun showSelectSongView() {
        mRaceSelectSongView.visibility = View.VISIBLE
        mLastSceneView = mRaceSelectSongView
        mRaceSelectSongView.setSongName {
            mCorePresenter.sendIntroOver()
        }
    }

    override fun goResultPage(lastRound: RaceRoundInfoModel) {
        finish()
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_RESULT)
                .withInt("roomID",mRoomData.gameId)
                .withInt("roundSeq",lastRound.roundSeq)
                .navigation()
    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun setData(type: Int, data: Any?) {
        if(type==0){
            mRoomData = data as RaceRoomData
        }
    }

    override fun destroy() {
        super.destroy()
        mRaceActorPanelView?.dismiss(false)
    }
}
