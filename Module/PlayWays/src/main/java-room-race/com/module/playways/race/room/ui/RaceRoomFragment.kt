package com.module.playways.race.room.ui

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.component.dialog.PersonInfoDialog
import com.component.person.event.ShowPersonCardEvent
import com.component.report.fragment.QuickFeedbackFragment
import com.module.playways.R
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.bottom.RaceBottomContainerView
import com.module.playways.race.room.inter.IRaceRoomView
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
    internal lateinit var mRaceTurnInfoCardView: RaceTurnInfoCardView
    internal lateinit var mRaceTopVsView: RaceTopVsView
    internal lateinit var mRaceSelfSingLyricView: RaceSelfSingLyricView

    internal lateinit var mRaceActorPanelView: RaceActorPanelView  //参与的人

    internal var mPersonInfoDialog: PersonInfoDialog? = null
    lateinit var mVoiceRecordUiController: VoiceRecordUiController
    val mRaceWidgetAnimationController = RaceWidgetAnimationController(this)

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
        initLyricView()

        mUiHanlder.postDelayed(Runnable {
            mRaceWidgetAnimationController.close()
        }, 500)
    }

    private fun initLyricView() {
        mRaceSelfSingLyricView = RaceSelfSingLyricView(rootView.findViewById(R.id.race_self_sing_lyric_view_stub) as ViewStub, null)
    }

    private fun initInputView() {
        mInputContainerView = rootView.findViewById(R.id.input_container_view)
        mInputContainerView.setRoomData(mRoomData)
    }

    private fun initTurnSenceView() {
        mRaceTurnInfoCardView = rootView.findViewById(R.id.race_turn_card_view)
        mRaceTurnInfoCardView.visibility = View.GONE
    }

    private fun initBottomView() {
        run {
            val voiceStub = rootView.findViewById<ViewStub>(R.id.voice_record_tip_view_stub)
            mVoiceRecordTipsView = VoiceRecordTipsView(voiceStub)

            val actorStub = rootView.findViewById<ViewStub>(R.id.race_actor_panel_view_stub)
            mRaceActorPanelView = RaceActorPanelView(actorStub, mRoomData)
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
            override fun changeRoom() {
//                val grabRoundInfoModel = mRoomData.getRealRoundInfo<GrabRoundInfoModel>()
//                if (grabRoundInfoModel != null) {
//                    for (wantSingerInfo in grabRoundInfoModel.wantSingInfos) {
//                        if (wantSingerInfo.userID.toLong() == MyUserInfoManager.getInstance().uid) {
//                            U.getToastUtil().showShort("演唱时不能切换房间哦～")
//                            return
//                        }
//                    }
//                }
//                mBeginChangeRoomTs = System.currentTimeMillis()
//                mGrabChangeRoomTransitionView.setVisibility(View.VISIBLE)
//                mCorePresenter.changeRoom()
//                mGrabGiveupView.hideWithAnimation(false)
            }

            override fun onClickVoiceAudition() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
//                if (mGrabVoiceControlPanelView == null) {
//                    mGrabVoiceControlPanelView = GrabVoiceControlPanelView(context)
//                    mGrabVoiceControlPanelView.setRoomData(mRoomData)
//                }
//                mGrabVoiceControlPanelView.bindData()
//                if (mVoiceControlDialog == null) {
//                    mVoiceControlDialog = DialogPlus.newDialog(context)
//                            .setContentHolder(ViewHolder(mGrabVoiceControlPanelView))
//                            .setContentBackgroundResource(R.color.transparent)
//                            .setOverlayBackgroundResource(R.color.black_trans_50)
//                            .setExpanded(false)
//                            .setCancelable(true)
//                            .setGravity(Gravity.BOTTOM)
//                            .create()
//                }
//                mVoiceControlDialog.show()
            }

            override fun onClickFeedBack() {
//                U.getFragmentUtils().addFragment(
//                        FragmentUtils.newAddParamsBuilder(activity, QuickFeedbackFragment::class.java)
//                                .setAddToBackStack(true)
//                                .setHasAnimation(true)
//                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_RANK_ROOM)
//                                .addDataBeforeAdd(1, QuickFeedbackFragment.FEED_BACK)
//                                .setEnterAnim(R.anim.slide_in_bottom)
//                                .setExitAnim(R.anim.slide_out_bottom)
//                                .build())
            }

            override fun closeBtnClick() {
//                if (mRoomData.isOwner() && mRoomData.getPlayerInfoList().size >= 2) {
//                    quitGame()
//                } else {
//                    mCorePresenter.exitRoom("closeBtnClick")
//                }
            }

            override fun onVoiceChange(voiceOpen: Boolean) {
//                mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen, true)
            }

            override fun onClickGameRule() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
//                if (mGameRuleDialog != null) {
//                    mGameRuleDialog.dismiss()
//                }
//                mGameRuleDialog = DialogPlus.newDialog(context)
//                        .setContentHolder(ViewHolder(R.layout.grab_game_rule_view_layout))
//                        .setContentBackgroundResource(R.color.transparent)
//                        .setOverlayBackgroundResource(R.color.black_trans_50)
//                        .setExpanded(false)
//                        .setGravity(Gravity.CENTER)
//                        .create()
//                mGameRuleDialog.show()
            }

            override fun onClickCamera() {
//                if (mRoomData.isVideoRoom()) {
//                    val grabRoundInfoModel = mRoomData.getRealRoundInfo<GrabRoundInfoModel>()
//                    if (grabRoundInfoModel != null) {
//                        if (grabRoundInfoModel.isSelfGrab) {
//                            return
//                        }
//                    }
//                    // 进入视频预览 判断是否实名验证过
//                    mSkrCameraPermission.ensurePermission(Runnable {
//                        ARouter.getInstance()
//                                .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
//                                .withInt("mFrom", JumpBeautyFromKt.FROM_IN_GRAB_ROOM)
//                                .navigation()
//                    }, true)
//                } else {
//                    U.getToastUtil().showShort("只在视频房间才能开启视频设置")
//                }
            }
        })


        mRaceTopContentView = rootView.findViewById(R.id.race_top_content_view)
        mRaceTopContentView.setRoomData(mRoomData)

        mRaceTopContentView.setListener(object : RaceTopContentView.Listener {
            override fun clickMore() {
                mRaceActorPanelView.show()
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

    override fun singBySelfFirstRound(songModel: SongModel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun singByOtherFirstRound(songModel: SongModel?, userModel: UserInfoModel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun singBySelfSecondRound(songModel: SongModel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun singByOtherSecondRound(songModel: SongModel?, userModel: UserInfoModel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun roundOver(overReason: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showWaiting() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showChoicing(showNextRound: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun useEventBus(): Boolean {
        return true
    }
}
