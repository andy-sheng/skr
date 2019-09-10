package com.module.playways.grab.room.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.base.BaseFragment
import com.common.base.FragmentDataListener
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.core.userinfo.ResponseCallBack
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.component.busilib.beauty.FROM_IN_GRAB_ROOM
import com.component.busilib.constans.GrabRoomType
import com.component.busilib.manager.BgMusicManager
import com.component.dialog.ConfirmDialog
import com.component.dialog.PersonInfoDialog
import com.component.person.event.ShowPersonCardEvent
import com.component.report.fragment.QuickFeedbackFragment
import com.component.toast.CommonToastView
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.IHomeService
import com.module.playways.R
import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.activity.GrabRoomActivity
import com.module.playways.grab.room.bottom.GrabBottomContainerView
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent
import com.module.playways.grab.room.event.GrabWantInviteEvent
import com.module.playways.grab.room.event.LightOffAnimationOverEvent
import com.module.playways.grab.room.inter.IGrabRoomView
import com.module.playways.grab.room.inter.IGrabVipView
import com.module.playways.grab.room.invite.fragment.InviteFriendFragment2
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.presenter.DoubleRoomInvitePresenter
import com.module.playways.grab.room.presenter.GrabCorePresenter
import com.module.playways.grab.room.presenter.GrabRedPkgPresenter
import com.module.playways.grab.room.presenter.VipEnterPresenter
import com.module.playways.grab.room.top.GrabTopContentView
import com.module.playways.grab.room.top.GrabTopOpView
import com.module.playways.grab.room.view.*
import com.module.playways.grab.room.view.control.OthersSingCardView
import com.module.playways.grab.room.view.control.RoundOverCardView
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.grab.room.view.control.SingBeginTipsCardView
import com.module.playways.grab.room.view.video.GrabVideoDisplayView
import com.module.playways.grab.room.view.video.GrabVideoSelfSingCardView
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController
import com.module.playways.listener.SVGAListener
import com.module.playways.room.gift.event.BuyGiftEvent
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent
import com.module.playways.room.gift.event.UpdateMeiGuiFreeCountEvent
import com.module.playways.room.gift.view.ContinueSendView
import com.module.playways.room.gift.view.GiftDisplayView
import com.module.playways.room.gift.view.GiftPanelView
import com.module.playways.room.prepare.model.OnlineInfoModel
import com.module.playways.room.room.comment.CommentView
import com.module.playways.room.room.comment.listener.CommentViewItemListener
import com.module.playways.room.room.gift.GiftBigAnimationViewGroup
import com.module.playways.room.room.gift.GiftBigContinuousView
import com.module.playways.room.room.gift.GiftContinueViewGroup
import com.module.playways.room.room.gift.GiftOverlayAnimationViewGroup
import com.module.playways.room.room.view.BottomContainerView
import com.module.playways.room.room.view.InputContainerView
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.event.ChangeTagSuccessEvent
import com.moudule.playways.beauty.view.BeautyControlPanelView
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.EMsgRoomMediaType
import com.zq.live.proto.Room.EQRoundStatus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class GrabRoomFragment : BaseFragment(), IGrabRoomView, IRedPkgCountDownView, IUpdateFreeGiftCountView, IGrabVipView {

    val TAG_MANAGE_SONG_TIP_VIEW = "ownerShowTimes"
    val TAG_INVITE_TIP_VIEW = "inviteShowTimes"
    val TAG_CHANLLENGE_TIP_VIEW = "showChallengeTime"
    val TAG_GRAB_ROB_TIP_VIEW = "showGrabRobTime"
    val TAG_BURST_TIP_VIEW = "tag_burst_tips"
    val TAG_SELF_SING_TIP_VIEW = "tag_self_sing_tips"
    val TAG_NOACC_SROLL_TIP_VIEW = "tag_noacc_sroll_tips"

    //    public static final int MSG_ENSURE_GAME_OVER = 6;

    //自己演唱玩
    //    public static final int MSG_SEND_SELF_SING_END = 7;

    var mRoomData: GrabRoomData? = null

    lateinit var mGrabRootView: GrabRootView

    lateinit var mGrabRoomBgFlag: ExImageView

    lateinit var mInputContainerView: InputContainerView

    lateinit var mBottomBgVp: ViewGroup

    lateinit var mBottomContainerView: GrabBottomContainerView

    //    GiftTimerPresenter mGiftTimerPresenter;

    //    GrabVoiceControlPanelView mVoiceControlView;

    //    RedPkgCountDownView mRedPkgView;

    lateinit var mCommentView: CommentView

    //    GrabTopContainerView mTopContainerView;// 顶部，抢唱阶段，以及非本人的演唱阶段
    lateinit var mGrabTopOpView: GrabTopOpView

    lateinit var mGrabTopContentView: GrabTopContentView

    lateinit var mPracticeFlagIv: ExImageView // 练习中

    internal var mCorePresenter: GrabCorePresenter? = null

    internal var mVipEnterPresenter: VipEnterPresenter? = null

    internal var mGrabRedPkgPresenter: GrabRedPkgPresenter? = null

    internal var mDoubleRoomInvitePresenter: DoubleRoomInvitePresenter? = null

    //    DownLoadScoreFilePresenter mDownLoadScoreFilePresenter;

    lateinit var mTurnInfoCardView: TurnInfoCardView //歌曲次序 以及 对战开始卡片

    lateinit var mSongInfoCardView: SongInfoCardView// 歌曲信息卡片

    lateinit var mRoundOverCardView: RoundOverCardView // 轮次结束卡片

    lateinit var mOthersSingCardView: OthersSingCardView// 他人演唱卡片

    lateinit var mSelfSingCardView: SelfSingCardView // 自己演唱卡片

    lateinit var mSingBeginTipsCardView: SingBeginTipsCardView// 演唱提示卡片

    lateinit var mGrabOpBtn: GrabOpView // 抢 倒计时 灭 等按钮

    lateinit var mGrabGiveupView: GrabGiveupView

    lateinit var mMiniOwnerMicIv: ExImageView

    lateinit var mGrabGameOverView: GrabGameOverView

    internal var mDengBigAnimation: GrabDengBigAnimationView? = null

    lateinit var mGrabChangeRoomTransitionView: GrabChangeRoomTransitionView

    lateinit var mGrabScoreTipsView: GrabScoreTipsView

    internal var mQuitTipsDialog: DialogPlus? = null

    internal var mPersonInfoDialog: PersonInfoDialog? = null

    internal var mGameRuleDialog: DialogPlus? = null

    internal var mGrabKickDialog: ConfirmDialog? = null

    internal var mGrabVoiceControlPanelView: GrabVoiceControlPanelView? = null

    lateinit var mGiftPanelView: GiftPanelView

    lateinit var mGiftContinueViewGroup: GiftContinueViewGroup

    lateinit var mContinueSendView: ContinueSendView

    internal var mVoiceControlDialog: DialogPlus? = null

    internal var mTipsDialogView: TipsDialogView? = null

    lateinit var mGrabVideoDisplayView: GrabVideoDisplayView // 视频展示view

    var mGrabVideoSelfSingCardView: GrabVideoSelfSingCardView? = null // 视频模式下 自己演唱时的歌词面板view

    lateinit var mBeautyControlPanelView: BeautyControlPanelView

    lateinit var mVoiceRecordTipsView: VoiceRecordTipsView

    internal var mVoiceRecordUiController: VoiceRecordUiController? = null

    internal var mAnimatorList: MutableList<Animator> = ArrayList()  //存放所有需要尝试取消的动画

    internal var mIsGameEndAniamtionShow = false // 标记对战结束动画是否播放

    internal var mBeginChangeRoomTs: Long = 0

    internal var mOwnerBeginGameIv: ImageView? = null

    internal var playbookWaitStatusIv: ImageView? = null

    internal var mVIPEnterView: VIPEnterView? = null

    var mGameTipsManager = GameTipsManager()

    internal var mSkrAudioPermission = SkrAudioPermission()

    internal var mSkrCameraPermission = SkrCameraPermission()

    internal var mGrabAudioUiController = GrabAudioUiController(this)

    internal var mGrabVideoUiController = GrabVideoUiController(this)

    internal var mGrabBaseUiController: GrabBaseUiController = mGrabAudioUiController

    var mGrabWidgetAnimationController: GrabWidgetAnimationController = GrabWidgetAnimationController(this)

    internal var mUiHanlder: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                //                case MSG_ENSURE_READYGO_OVER:
                //                    onReadyGoOver();
                //                    break;
                MSG_ENSURE_BATTLE_BEGIN_OVER -> onBattleBeginPlayOver()
                MSG_ENSURE_SONGCARD_OVER -> onSongInfoCardPlayOver("MSG_ENSURE_SONGCARD_OVER", msg.obj as PendingPlaySongCardData)
                MSG_ENSURE_SING_BEGIN_TIPS_OVER -> onSingBeginTipsPlayOver()
                MSG_ENSURE_ROUND_OVER_PLAY_OVER -> onRoundOverPlayOver(msg.arg1 == 1, msg.obj as GrabRoundInfoModel?)
            }//                case MSG_ENSURE_GAME_OVER:
            //                    onGrabGameOver("MSG_ENSURE_GAME_OVER");
            //                    break;
            //                case MSG_SEND_SELF_SING_END:
            //                    mCorePresenter.sendRoundOverInfo();
            //                    break;
        }
    }

    internal var mGetRedPkgFailedDialog: DialogPlus? = null

    override fun initView(): Int {
        return R.layout.grab_room_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        if (mRoomData == null) {
            Log.w(TAG, "initData，mRoomData == null, 直接finish Activity")
            activity?.finish()
            return
        }

        if (mRoomData!!.gameStartTs > 0 && System.currentTimeMillis() - mRoomData!!.gameStartTs > 3 * 60 * 1000) {
            Log.w(TAG, "隔了很久从后台返回的，直接finish Activity")
            if (activity != null) {
                activity!!.finish()
            }
            return
        }

        // 请保证从下面的view往上面的view开始初始化
        mGrabRootView = rootView.findViewById(R.id.grab_root_view)
        //        mGrabRootView.setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View v) {
        //                mInputContainerView.hideSoftInput();
        //            }
        //        });
        mGrabRootView.addOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    mInputContainerView.hideSoftInput()
                }
                return false
            }
        })

        initBgView()
        initInputView()
        initBottomView()
        initCommentView()
        initTopView()
        initTurnChangeView()
        initGiftDisplayView()
        initGrabOpView()
        initSingStageView()
        initChangeRoomTransitionView()
        //        initCountDownView();
        initScoreView()
        initGiftPanelView()
        initVideoView()
        initVipEnterView()
        if (MyLog.isDebugLogOpen()) {
            val viewStub = rootView.findViewById<ViewStub>(R.id.debug_log_view_stub)
            val debugLogView = DebugLogView(viewStub)
            debugLogView.tryInflate()
        }

        mCorePresenter = GrabCorePresenter(this, mRoomData!!, activity!! as BaseActivity)
        addPresent(mCorePresenter)
        mGrabRedPkgPresenter = GrabRedPkgPresenter(this)
        addPresent(mGrabRedPkgPresenter)
        mGrabRedPkgPresenter?.checkRedPkg()
        mCorePresenter?.setGrabRedPkgPresenter(mGrabRedPkgPresenter!!)
        mDoubleRoomInvitePresenter = DoubleRoomInvitePresenter()
        addPresent(mDoubleRoomInvitePresenter)
        mVipEnterPresenter = VipEnterPresenter(this, mRoomData!!)
        addPresent(mVipEnterPresenter)

        if (mRoomData!!.isVideoRoom) {
            mGrabBaseUiController = mGrabVideoUiController
        } else {
            mGrabBaseUiController = mGrabAudioUiController
        }
        U.getSoundUtils().preLoad(TAG, R.raw.grab_challengelose, R.raw.grab_challengewin,
                R.raw.grab_gameover, R.raw.grab_iwannasing,
                R.raw.grab_nobodywants, R.raw.grab_readygo,
                R.raw.grab_xlight, R.raw.normal_click)

        MyLog.w(TAG, "gameid 是 " + mRoomData!!.gameId + " userid 是 " + MyUserInfoManager.getInstance().uid)

        mUiHanlder.postDelayed({
            onBattleBeginPlayOver()
            var openOpBarTimes = U.getPreferenceUtils().getSettingInt("key_open_op_bar_times", 0)
            if (openOpBarTimes < 2) {
                mGrabWidgetAnimationController.open()
                openOpBarTimes++
                U.getPreferenceUtils().setSettingInt("key_open_op_bar_times", openOpBarTimes)
            } else {
                mGrabWidgetAnimationController.close()
            }
        }, 500)

        BgMusicManager.getInstance().isRoom = true
        if (mRoomData!!.isOwner) {
            if (!mRoomData!!.hasGameBegin() && mOwnerBeginGameIv == null) {
                // 是房主并且游戏未开始，增加一个 开始游戏 的按钮
                mOwnerBeginGameIv = ExImageView(context)
                mOwnerBeginGameIv?.setImageResource(R.drawable.fz_kaishiyouxi)
                val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
                lp.rightMargin = U.getDisplayUtils().dip2px(10f)
                lp.addRule(RelativeLayout.ALIGN_TOP, R.id.bottom_bg_vp)
                lp.topMargin = -U.getDisplayUtils().dip2px(55f)
                val index = mGrabRootView.indexOfChild(mInputContainerView)
                mGrabRootView.addView(mOwnerBeginGameIv, index, lp)
                mOwnerBeginGameIv?.setOnClickListener(object : DebounceViewClickListener() {
                    override fun clickValid(v: View) {
                        mCorePresenter?.ownerBeginGame()
                    }
                })
            }
            tryShowInviteTipView()
            tryShowManageSongTipView()
        } else {
            if (mRoomData?.roomType == GrabRoomType.ROOM_TYPE_PLAYBOOK && mRoomData?.hasGameBegin() == false) {
                // 歌单战，游戏未开始
                playbookWaitStatusIv = ExImageView(context)
                playbookWaitStatusIv?.setImageResource(R.drawable.race_wait_begin_icon)
                val lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                lp.addRule(RelativeLayout.CENTER_IN_PARENT)
//                lp.rightMargin = U.getDisplayUtils().dip2px(10f)
//                lp.addRule(RelativeLayout.ALIGN_TOP, R.id.bottom_bg_vp)
//                lp.topMargin = -U.getDisplayUtils().dip2px(55f)
                val index = mGrabRootView.indexOfChild(mInputContainerView)
                mGrabRootView.addView(playbookWaitStatusIv, index, lp)
            }
        }
        enterRoomEvent()

        MyUserInfoManager.getInstance().myUserInfo?.let {
            mVipEnterPresenter?.addNotice(MyUserInfo.toUserInfoModel(it))
        }
    }

    private fun enterRoomEvent() {
        if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_COMMON) {
            StatisticsAdapter.recordCountEvent("grab", "normalroom_enter", null)
        } else if (mRoomData!!.ownerId.toLong() != MyUserInfoManager.getInstance().uid) {
            if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_PUBLIC) {
                StatisticsAdapter.recordCountEvent("grab", "hostroom_enter", null)
            } else if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_FRIEND) {
                StatisticsAdapter.recordCountEvent("grab", "friendroom_enter", null)
            } else if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_SECRET) {
                StatisticsAdapter.recordCountEvent("grab", "privateroom_enter", null)
            }
        }
    }

    private fun tryShowInviteTipView() {
        GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_yindao_yaoqinghaoyou)
                .setActivity(activity)
                .setMargins(0, U.getDisplayUtils().dip2px(75f), U.getDisplayUtils().dip2px(54f), 0)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .hasAnimation(true)
                .setShowCount(3)
                .setBaseTranslateY(if (mGrabWidgetAnimationController.isOpen) U.getDisplayUtils().dip2px(32f) else 0)
                .setTag(TAG_INVITE_TIP_VIEW)
                .tryShow(mGameTipsManager)
    }

    private fun tryShowManageSongTipView() {
        GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_yindao_fangzhukongzhizhongxin)
                .setActivity(activity)
                .setMargins(0, 0, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(68f))
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1)
                .setIndex(mGrabRootView.indexOfChild(mBottomBgVp) + 1)
                .hasAnimation(true)
                .setShowCount(3)
                .setTag(TAG_MANAGE_SONG_TIP_VIEW)
                .tryShow(mGameTipsManager)

    }

    private fun tryShowChallengeTipView() {
        GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_jiabei_tiaozhan)
                .setActivity(activity)
                .setMargins(0, U.getDisplayUtils().dip2px(2f), U.getDisplayUtils().dip2px(20f), 0)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.BELOW, R.id.grab_op_btn)
                .setIndex(mGrabRootView.indexOfChild(mGrabOpBtn) + 1)
                .hasAnimation(true)
                .setShowCount(3)
                .setTag(TAG_CHANLLENGE_TIP_VIEW)
                .tryShow(mGameTipsManager)
    }

    // 抢唱提示
    private fun tryShowGrabTipView() {
        GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_qiangchang_yindao)
                .setActivity(activity)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.ABOVE, R.id.grab_op_btn)
                .setMargins(0, 0, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(2f))
                .setIndex(mGrabRootView.indexOfChild(mGrabOpBtn) + 1)
                .hasAnimation(false)
                .setShowCount(2)
                .setTag(TAG_GRAB_ROB_TIP_VIEW)
                .tryShow(mGameTipsManager)
    }

    // 爆灯提示
    private fun tryShowBurstTipView() {
        GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_baodeng_yindao)
                .setActivity(activity)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.ABOVE, R.id.grab_op_btn)
                .setMargins(0, 0, U.getDisplayUtils().dip2px(50f), -U.getDisplayUtils().dip2px(5f))
                .setIndex(mGrabRootView.indexOfChild(mGrabOpBtn) + 1)
                .hasAnimation(false)
                .setTag(TAG_BURST_TIP_VIEW)
                .setShowCount(1)
                .tryShow(mGameTipsManager)
    }

    // 歌词提示
    fun tryShowGrabSelfSingTipView() {
        GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_daojishi_yindao)
                .setActivity(activity)
                .addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1)
                .addRule(RelativeLayout.ALIGN_PARENT_TOP, -1)
                .setMargins(U.getDisplayUtils().dip2px(70f), U.getDisplayUtils().dip2px(28f), 0, 0)
                .setBaseTranslateY(if (mGrabWidgetAnimationController.isOpen) U.getDisplayUtils().dip2px(32f) else 0)
                .hasAnimation(false)
                .setTag(TAG_SELF_SING_TIP_VIEW)
                .setShowCount(1)
                .tryShow(mGameTipsManager)
    }

    // 清唱手势滑动
    fun tryShowNoAccSrollTipsView() {

        val mFingerTipViewAnimator = ObjectAnimator()
        mFingerTipViewAnimator.setProperty(View.TRANSLATION_Y)
        mFingerTipViewAnimator.setFloatValues(0f, -U.getDisplayUtils().dip2px(80f).toFloat())
        mFingerTipViewAnimator.repeatCount = 2
        mFingerTipViewAnimator.duration = 1500
        mFingerTipViewAnimator.repeatMode = ValueAnimator.RESTART
        mFingerTipViewAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                removeNoAccSrollTipsView()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationRepeat(animation: Animator) {

            }
        })

        GameTipsManager.GameTipsView(mGrabRootView, R.drawable.grab_sroll_finger_icon)
                .setActivity(activity)
                .setSize(U.getDisplayUtils().dip2px(64f), U.getDisplayUtils().dip2px(54f))
                .addRule(RelativeLayout.ALIGN_PARENT_TOP, -1)
                .addRule(RelativeLayout.CENTER_HORIZONTAL, -1)
                .setMargins(0, U.getDisplayUtils().dip2px(285f), 0, 0)
                .hasAnimation(true)
                .setAniamtion(mFingerTipViewAnimator)
                .setTag(TAG_NOACC_SROLL_TIP_VIEW)
                .setShowCount(1)
                .tryShow(mGameTipsManager)
    }

    private fun removeNoAccSrollTipsView() {
        mGameTipsManager.dismiss(TAG_NOACC_SROLL_TIP_VIEW, activity)
    }

    private fun removeGrabSelfSingTipView() {
        mGameTipsManager.dismiss(TAG_SELF_SING_TIP_VIEW, activity)
    }

    private fun removeBurstTipView() {
        mGameTipsManager.dismiss(TAG_BURST_TIP_VIEW, activity)
    }

    private fun removeGrabTipView() {
        mGameTipsManager.dismiss(TAG_GRAB_ROB_TIP_VIEW, activity)
    }

    private fun removeInviteTipView() {
        mGameTipsManager.dismiss(TAG_INVITE_TIP_VIEW, activity)
    }

    private fun removeManageSongTipView() {
        mGameTipsManager.dismiss(TAG_MANAGE_SONG_TIP_VIEW, activity)
    }

    private fun removeChallengeTipView() {
        mGameTipsManager.dismiss(TAG_CHANLLENGE_TIP_VIEW, activity)
    }

    //    private boolean isActivityExit() {
    //        Activity activity = getActivity();
    //        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
    //            return true;
    //        }
    //        return false;
    //    }


    override fun onStart() {
        super.onStart()
        //mSkrAudioPermission.ensurePermission(null, true);
    }

    //    private void initCountDownView() {
    //        mRedPkgView = (RedPkgCountDownView) mRootView.findViewById(R.id.red_pkg_view);
    //    }

    private fun initBgView() {
        mGrabRoomBgFlag = rootView.findViewById(R.id.grab_room_bg_flag)
        mGrabRoomBgFlag.visibility = View.VISIBLE
        if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_SECRET) {
            mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_simi)
        } else if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_FRIEND) {
            mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_haoyou)
        } else if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_COMMON) {
            //mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_simi);
            mGrabRoomBgFlag.visibility = GONE
        } else if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_PUBLIC) {
            mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_haohua)
        }
    }

    private fun initGiftPanelView() {
        mGiftPanelView = rootView.findViewById<View>(R.id.gift_panel_view) as GiftPanelView
        mGiftPanelView.setRoomData(mRoomData!!)
        mContinueSendView = rootView.findViewById<View>(R.id.continue_send_view) as ContinueSendView
        mContinueSendView.mScene = ContinueSendView.EGameScene.GS_Stand
        mContinueSendView.setRoomData(mRoomData!!)
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

    private fun initVipEnterView() {
        mVIPEnterView = VIPEnterView(rootView.findViewById(R.id.vip_enter_view_stub))
    }

    private fun initVideoView() {
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_video_display_view_stub)
            mGrabVideoDisplayView = GrabVideoDisplayView(viewStub, mRoomData!!)
            mGrabVideoDisplayView.setSelfSingCardListener { mCorePresenter?.sendRoundOverInfo() }
            mGrabVideoDisplayView.setListener {
                mBeautyControlPanelView?.show()
            }
        }
        mGrabVideoSelfSingCardView = GrabVideoSelfSingCardView(rootView, mRoomData!!)

        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_beauty_control_panel_view_stub)
            mBeautyControlPanelView = BeautyControlPanelView(viewStub)
        }
    }

    private fun initInputView() {
        mInputContainerView = rootView.findViewById(R.id.input_container_view)
        mInputContainerView.setRoomData(mRoomData!!)
    }

    private fun initBottomView() {
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.voice_record_tip_view_stub)
            mVoiceRecordTipsView = VoiceRecordTipsView(viewStub)
        }

        mBottomBgVp = rootView.findViewById(R.id.bottom_bg_vp)
        val lp = mBottomBgVp.layoutParams as RelativeLayout.LayoutParams
        /**
         * 按比例适配手机
         */
        lp.height = U.getDisplayUtils().screenHeight * 284 / 667

        mBottomContainerView = rootView.findViewById<View>(R.id.bottom_container_view) as GrabBottomContainerView
        mBottomContainerView.setListener(object : BottomContainerView.Listener() {
            override fun showInputBtnClick() {
                if (mPersonInfoDialog != null && mPersonInfoDialog!!.isShowing) {
                    mPersonInfoDialog!!.dismiss()
                }
                mInputContainerView.showSoftInput()
            }

            override fun clickRoomManagerBtn() {
                //                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(GrabRoomFragment.this.getActivity(), OwnerManageFragment.class)
                //                        .setAddToBackStack(true)
                //                        .setHasAnimation(true)
                //                        .setEnterAnim(R.anim.slide_right_in)
                //                        .setExitAnim(R.anim.slide_right_out)
                //                        .addDataBeforeAdd(0, mRoomData!!)
                //                        .build());
                SongManagerActivity.open(activity, mRoomData!!)
                removeManageSongTipView()
            }

            override fun showGiftPanel() {
                if (mRoomData!!.realRoundInfo != null) {
                    val now = mRoomData!!.realRoundInfo
                    if (now != null) {
                        if (now.isPKRound && now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                            if (now.getsPkRoundInfoModels().size == 2) {
                                val userId = now.getsPkRoundInfoModels()[1].userID
                                mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData!!, userId.toLong()))
                            } else {
                                mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData!!, now.userID.toLong()))
                            }
                        } else {
                            mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData!!, now.userID.toLong()))
                        }
                    } else {
                        mGiftPanelView.show(null)
                    }
                } else {
                    mGiftPanelView.show(null)
                }

                mContinueSendView.visibility = GONE
            }
        })
        mBottomContainerView.setRoomData(mRoomData!!)
    }

    private fun initCommentView() {
        mCommentView = rootView.findViewById(R.id.comment_view)
        mCommentView.setListener(CommentViewItemListener { userId -> showPersonInfoView(userId) })
        mCommentView.roomData = mRoomData!!
        //        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentView.getLayoutParams();
        //        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(430 + 60);
        mVoiceRecordUiController = VoiceRecordUiController(mBottomContainerView.mVoiceRecordBtn, mVoiceRecordTipsView, mCommentView)
    }

    private fun initChangeRoomTransitionView() {
        mGrabChangeRoomTransitionView = rootView.findViewById(R.id.change_room_transition_view)
        mGrabChangeRoomTransitionView.visibility = GONE
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(activity)
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
    fun onEvent(event: GrabWantInviteEvent) {
        // 房主想要邀请别人加入游戏
        // 打开邀请面板
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(activity, InviteFriendFragment2::class.java)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, InviteFriendFragment2.FROM_GRAB_ROOM)
                .addDataBeforeAdd(1, mRoomData!!.gameId)
                .addDataBeforeAdd(2, if (mRoomData!!.isVideoRoom) EMsgRoomMediaType.EMR_VIDEO.value else EMsgRoomMediaType.EMR_AUDIO.value)
                .addDataBeforeAdd(3, mRoomData!!.tagId)
                .build()
        )

        removeInviteTipView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ChangeTagSuccessEvent) {
        mRoomData!!.specialModel = event.specialModel
        mRoomData!!.tagId = event.specialModel.tagID
    }

    override fun startEnterAnimation(playerInfoModel: UserInfoModel, finishCall: () -> Unit) {
        mVIPEnterView?.enter(playerInfoModel, finishCall)
    }

    private fun showPersonInfoView(userID: Int) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!")
            return
        }
        mInputContainerView.hideSoftInput()

        val mShowKick: Boolean
        if (mRoomData!!.roomType == GrabRoomType.ROOM_TYPE_COMMON) {
            // 普通房
            mShowKick = true
        } else {
            if (mRoomData!!.isOwner) {
                mShowKick = true
            } else {
                mShowKick = false
            }
        }

        mPersonInfoDialog = PersonInfoDialog.Builder(activity, QuickFeedbackFragment.FROM_GRAB_ROOM, userID, mShowKick, true)
                .setRoomID(mRoomData!!.gameId)
                .setInviteDoubleListener { userInfoModel ->
                    if (userInfoModel.isFriend) {
                        mDoubleRoomInvitePresenter?.inviteToDoubleRoom(userInfoModel.userId)
                    } else {
                        UserInfoManager.getInstance().checkIsFans(MyUserInfoManager.getInstance().uid.toInt(), userInfoModel.userId, object : ResponseCallBack<Boolean>() {
                            override fun onServerSucess(isFans: Boolean?) {
                                if (isFans!!) {
                                    mDoubleRoomInvitePresenter?.inviteToDoubleRoom(userInfoModel.userId)
                                } else {
                                    mTipsDialogView = TipsDialogView.Builder(U.getActivityUtils().topActivity)
                                            .setMessageTip("对方不是您的好友或粉丝\n要花2金币邀请ta加入双人唱聊房吗？")
                                            .setConfirmTip("邀请")
                                            .setCancelTip("取消")
                                            .setConfirmBtnClickListener(object : AnimateClickListener() {
                                                override fun click(view: View) {
                                                    mDoubleRoomInvitePresenter?.inviteToDoubleRoom(userInfoModel.userId)
                                                    mTipsDialogView?.dismiss()
                                                }
                                            })
                                            .setCancelBtnClickListener(object : AnimateClickListener() {
                                                override fun click(view: View) {
                                                    mTipsDialogView?.dismiss()
                                                }
                                            })
                                            .build()
                                    mTipsDialogView?.showByDialog()
                                }
                            }

                            override fun onServerFailed() {

                            }
                        })
                    }
                }
                .setKickListener { userInfoModel -> showKickConfirmDialog(userInfoModel) }
                .build()
        mPersonInfoDialog?.show()
    }

    override fun getCashSuccess(cash: Float) {
        val dialogPlus = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(R.layout.congratulation_get_cash_view_layout))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setMargin(0, 0, 0, 0)
                .setExpanded(false)
                .setCancelable(true)
                .setOnClickListener { dialog, view -> dialog.dismiss() }
                .create()

        val textView = dialogPlus.findViewById(R.id.tv_cash) as TextView?
        textView?.text = cash.toString()
        dialogPlus.show()
    }

    override fun showGetRedPkgFailed() {
        val tipsDialogView = TipsDialogView.Builder(context)
                .setMessageTip("注册账号行为异常，红包激活不成功")
                .setOkBtnTip("确定")
                .setOkBtnClickListener {
                    if (mGetRedPkgFailedDialog != null) {
                        mGetRedPkgFailedDialog?.dismiss()
                    }
                }
                .build()

        if (mGetRedPkgFailedDialog == null) {
            mGetRedPkgFailedDialog = DialogPlus.newDialog(context!!)
                    .setContentHolder(ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create()
        }

        mGetRedPkgFailedDialog?.show()
    }

    private fun initTopView() {
        mGrabTopOpView = rootView.findViewById(R.id.grab_top_op_view)
        mGrabTopOpView.setRoomData(mRoomData!!)
        mGrabTopContentView = rootView.findViewById(R.id.grab_top_content_view)
        mGrabTopContentView.setRoomData(mRoomData!!)
        mGrabTopOpView.setListener(object : GrabTopOpView.Listener {
            override fun changeRoom() {
                val grabRoundInfoModel = mRoomData!!.realRoundInfo
                if (grabRoundInfoModel != null) {
                    for (wantSingerInfo in grabRoundInfoModel.wantSingInfos) {
                        if (wantSingerInfo.userID.toLong() == MyUserInfoManager.getInstance().uid) {
                            U.getToastUtil().showShort("演唱时不能切换房间哦～")
                            return
                        }
                    }
                }

                mBeginChangeRoomTs = System.currentTimeMillis()
                mGrabChangeRoomTransitionView.visibility = View.VISIBLE
                mCorePresenter?.changeRoom()
                mGrabGiveupView.hideWithAnimation(false)
            }

            override fun onClickVoiceAudition() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
                if (mGrabVoiceControlPanelView == null) {
                    mGrabVoiceControlPanelView = GrabVoiceControlPanelView(context)
                    mGrabVoiceControlPanelView!!.setRoomData(mRoomData!!)
                }
                mGrabVoiceControlPanelView!!.bindData()
                if (mVoiceControlDialog == null) {
                    mVoiceControlDialog = DialogPlus.newDialog(context!!)
                            .setContentHolder(ViewHolder(mGrabVoiceControlPanelView))
                            .setContentBackgroundResource(R.color.transparent)
                            .setOverlayBackgroundResource(R.color.black_trans_50)
                            .setExpanded(false)
                            .setCancelable(true)
                            .setGravity(Gravity.BOTTOM)
                            .create()
                }
                mVoiceControlDialog!!.show()
            }

            override fun onClickFeedBack() {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(activity, QuickFeedbackFragment::class.java)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_GRAB_ROOM)
                                .addDataBeforeAdd(1, QuickFeedbackFragment.FEED_BACK)
                                .addDataBeforeAdd(3, mRoomData!!.gameId)
                                .setEnterAnim(R.anim.slide_in_bottom)
                                .setExitAnim(R.anim.slide_out_bottom)
                                .build())
            }

            override fun closeBtnClick() {
                if (mRoomData!!.isOwner && mRoomData!!.getPlayerInfoList()!!.size >= 2) {
                    quitGame()
                } else {
                    mCorePresenter?.exitRoom("closeBtnClick")
                }
            }

            override fun onVoiceChange(voiceOpen: Boolean) {
                mCorePresenter?.muteAllRemoteAudioStreams(!voiceOpen, true)
            }

            override fun onClickGameRule() {

                mGameRuleDialog?.dismiss()

                U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
                mGameRuleDialog = DialogPlus.newDialog(context!!)
                        .setContentHolder(ViewHolder(R.layout.grab_game_rule_view_layout))
                        .setContentBackgroundResource(R.color.transparent)
                        .setOverlayBackgroundResource(R.color.black_trans_50)
                        .setMargin(U.getDisplayUtils().dip2px(16f), -1, U.getDisplayUtils().dip2px(16f), -1)
                        .setExpanded(false)
                        .setGravity(Gravity.CENTER)
                        .create()
                mGameRuleDialog?.show()
            }

            override fun onClickCamera() {
                if (mRoomData!!.isVideoRoom) {
                    val grabRoundInfoModel = mRoomData!!.realRoundInfo
                    if (grabRoundInfoModel != null) {
                        if (grabRoundInfoModel.isSelfGrab) {
                            return
                        }
                    }
                    // 进入视频预览 判断是否实名验证过
                    mSkrCameraPermission.ensurePermission({
                        ARouter.getInstance()
                                .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                .withInt("mFrom", FROM_IN_GRAB_ROOM)
                                .navigation()
                    }, true)
                } else {
                    U.getToastUtil().showShort("只在视频房间才能开启视频设置")
                }
            }
        })
        mGrabTopContentView.setListener { open ->
            if (open) {
                mGrabWidgetAnimationController.open()
            } else {
                mGrabWidgetAnimationController.close()
            }
        }
        mPracticeFlagIv = rootView.findViewById(R.id.practice_flag_iv)
        // 加上状态栏的高度
        val statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(context!!)

        run {
            val topLayoutParams = mGrabTopOpView.layoutParams as RelativeLayout.LayoutParams
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin
        }
        run {
            val topLayoutParams = mGrabTopContentView.layoutParams as RelativeLayout.LayoutParams
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin
        }
    }

    override fun updateGiftCount(count: Int, ts: Long) {
        UpdateMeiGuiFreeCountEvent.sendEvent(count, ts)
    }

    /**
     * 转场动画相关初始化
     */
    private fun initTurnChangeView() {
        mTurnInfoCardView = rootView.findViewById(R.id.turn_info_iv)
        mSongInfoCardView = rootView.findViewById(R.id.turn_change_song_info_card_view)
        run {
            val viewStub = rootView.findViewById<ViewStub>(R.id.grab_sing_begin_tips_card_stub)
            mSingBeginTipsCardView = SingBeginTipsCardView(viewStub, mRoomData!!)
        }
        mRoundOverCardView = RoundOverCardView(rootView, mRoomData!!)
        mGrabGameOverView = rootView.findViewById(R.id.grab_game_over_view)
    }

    private fun initGiftDisplayView() {
        mGiftContinueViewGroup = rootView.findViewById(R.id.gift_continue_vg)
        mGiftContinueViewGroup.setRoomData(mRoomData!!)
        val giftOverlayAnimationViewGroup = rootView.findViewById<GiftOverlayAnimationViewGroup>(R.id.gift_overlay_animation_vg)
        giftOverlayAnimationViewGroup.setRoomData(mRoomData!!)
        val giftBigAnimationViewGroup = rootView.findViewById<GiftBigAnimationViewGroup>(R.id.gift_big_animation_vg)
        giftBigAnimationViewGroup.setRoomData(mRoomData!!)

        val giftBigContinueView = rootView.findViewById<GiftBigContinuousView>(R.id.gift_big_continue_view)
        giftBigAnimationViewGroup.setGiftBigContinuousView(giftBigContinueView)

        mDengBigAnimation = rootView.findViewById<View>(R.id.deng_big_animation) as GrabDengBigAnimationView
    }

    private fun initScoreView() {
        mGrabScoreTipsView = rootView.findViewById(R.id.grab_score_tips_view)
        mGrabScoreTipsView.setRoomData(mRoomData!!)
    }

    private fun initGrabOpView() {
        mGrabOpBtn = rootView.findViewById(R.id.grab_op_btn)
        mGrabOpBtn.setGrabRoomData(mRoomData!!)
        mGrabOpBtn.setListener(object : GrabOpView.Listener {
            override fun clickGrabBtn(seq: Int, challenge: Boolean) {
                mSkrAudioPermission.ensurePermission(activity, {
                    val r = Runnable {
                        U.getSoundUtils().play(TAG, R.raw.grab_iwannasing)
                        mCorePresenter?.grabThisRound(seq, challenge)
                    }
                    if (mRoomData!!.isVideoRoom) {
                        mSkrCameraPermission.ensurePermission(activity, r, true)
                    } else {
                        r.run()
                    }
                }, true)
            }

            override fun clickLightOff() {
                mCorePresenter?.lightsOff()
            }

            override fun grabCountDownOver() {
                mCorePresenter?.sendMyGrabOver("grabCountDownOver")
                mGrabGiveupView.hideWithAnimation(false)
            }

            override fun countDownOver() {

            }

            override fun clickBurst(seq: Int) {
                mCorePresenter?.lightsBurst()
            }

            override fun showChallengeTipView() {
                tryShowChallengeTipView()
            }

            override fun hideChallengeTipView() {
                removeChallengeTipView()
            }

            override fun showGrabTipView() {
                tryShowGrabTipView()
            }

            override fun hideGrabTipView() {
                removeGrabTipView()
            }

            override fun showBurstTipView() {
                tryShowBurstTipView()
            }

            override fun hideBurstTipView() {
                removeBurstTipView()
            }
        })

        mGrabOpBtn.hide("initGrabOpView")

        mGrabGiveupView = rootView.findViewById<View>(R.id.grab_giveup_view) as GrabGiveupView
        mGrabGiveupView.setListener(object : GrabGiveupView.Listener {
            override fun giveUp(ownerControl: Boolean) {
                val infoModel = mRoomData!!.realRoundInfo
                //                if (infoModel != null) {
                //                    HashMap map = new HashMap();
                //                    map.put("songId2", String.valueOf(infoModel.getMusic().getItemID()));
                //                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                //                            "give_up_sing", map);
                //                }
                mCorePresenter?.giveUpSing(ownerControl)
            }
        })
        mGrabGiveupView.hideWithAnimation(false)

        mMiniOwnerMicIv = rootView.findViewById(R.id.mini_owner_mic_iv)
        mMiniOwnerMicIv.setOnClickListener(object : DebounceViewClickListener() {
            internal var mute = true

            override fun clickValid(v: View) {
                mute = !mute
                if (mute) {
                    mMiniOwnerMicIv.setImageResource(R.drawable.mini_owner_mute)
                } else {
                    mMiniOwnerMicIv.setImageResource(R.drawable.mini_owner_normal)
                }
                mCorePresenter?.miniOwnerMic(mute)
            }
        })
    }

    private fun initSingStageView() {
        mSelfSingCardView = SelfSingCardView(rootView, mRoomData!!)
        mSelfSingCardView.setListener {
            removeNoAccSrollTipsView()
            removeGrabSelfSingTipView()
            mCorePresenter?.sendRoundOverInfo()
        }
        mSelfSingCardView.setListener4FreeMic { mCorePresenter?.sendMyGrabOver("onSelfSingOver") }
        mOthersSingCardView = OthersSingCardView(rootView, mRoomData!!)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightOffEvent) {
        // 灭灯
        if (event.getUid().toLong() == MyUserInfoManager.getInstance().uid) {
            U.getSoundUtils().play(TAG, R.raw.grab_xlight)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ShowHalfRechargeFragmentEvent) {
        val channelService = ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation() as IHomeService
        val baseFragmentClass = channelService.getData(2, null) as Class<Fragment>
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(activity, baseFragmentClass)
                        .setEnterAnim(R.anim.slide_in_bottom)
                        .setExitAnim(R.anim.slide_out_bottom)
                        .setAddToBackStack(true)
                        .setFragmentDataListener(object : FragmentDataListener {
                            override fun onFragmentResult(requestCode: Int, resultCode: Int, bundle: Bundle?, obj: Any?) {
                                //充值成功
                                if (requestCode == 100 && resultCode == 0) {
                                    mGiftPanelView?.updateZS()
                                    mGiftPanelView?.show(RoomDataUtils.getPlayerInfoById(mRoomData!!, (mRoomData!!.realRoundInfo?.userID
                                            ?: 0).toLong()))
                                }
                            }
                        })
                        .setHasAnimation(true)
                        .build())
    }

    override fun joinNotice(playerInfoModel: UserInfoModel?) {
        playerInfoModel?.let {
            mVipEnterPresenter?.addNotice(playerInfoModel)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightBurstEvent) {
        // 爆灯
        if (RoomDataUtils.isMyRound(mRoomData!!.realRoundInfo)) {
            // 当前我是演唱者
            mDengBigAnimation?.translationY = U.getDisplayUtils().dip2px(200f).toFloat()
            mDengBigAnimation?.playBurstAnimation(event.uid.toLong() == MyUserInfoManager.getInstance().uid)
        } else {
            mDengBigAnimation?.playBurstAnimation(event.uid.toLong() == MyUserInfoManager.getInstance().uid)
        }
    }

    private fun removeAllEnsureMsg() {
        mUiHanlder.removeMessages(MSG_ENSURE_SONGCARD_OVER)
        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER)
        mUiHanlder.removeMessages(MSG_ENSURE_ROUND_OVER_PLAY_OVER)
        mUiHanlder.removeMessages(MSG_ENSURE_BATTLE_BEGIN_OVER)
    }

    //    private void onReadyGoOver() {
    //        MyLog.w(TAG, "onReadyGoOver");
    //        mUiHanlder.removeMessages(MSG_ENSURE_READYGO_OVER);
    //        mCorePresenter?.onOpeningAnimationOver();
    //    }

    private fun onBattleBeginPlayOver() {
        mUiHanlder.removeMessages(MSG_ENSURE_BATTLE_BEGIN_OVER)
        mTurnInfoCardView.visibility = GONE
        mCorePresenter?.onOpeningAnimationOver()
    }


    /**
     * 抢唱阶段开始
     *
     * @param seq       当前轮次的序号
     * @param songModel 要唱的歌信息
     */
    override fun grabBegin(seq: Int, songModel: SongModel) {
        MyLog.d(TAG, "grabBegin seq=$seq songModel=$songModel")
        removeAllEnsureMsg()
        if (mOwnerBeginGameIv != null) {
            // 如果房主开始游戏的按钮还在的话，将其移除
            mGrabRootView.removeView(mOwnerBeginGameIv)
            mOwnerBeginGameIv = null
        }
        if (playbookWaitStatusIv != null) {
            mGrabRootView.removeView(playbookWaitStatusIv)
            playbookWaitStatusIv = null
        }
        // 播放3秒导唱
        mOthersSingCardView.setVisibility(GONE)
        mSelfSingCardView.setVisibility(GONE)
        mMiniOwnerMicIv.visibility = GONE
        mGrabBaseUiController.grabBegin()
        val pendingPlaySongCardData = PendingPlaySongCardData(seq, songModel)
        val msg = mUiHanlder.obtainMessage(MSG_ENSURE_SONGCARD_OVER)
        msg.obj = pendingPlaySongCardData
        if (seq == 1) {
            mUiHanlder.sendMessageDelayed(msg, 5000)
            mUiHanlder.postDelayed({ U.getSoundUtils().play(TAG, R.raw.grab_readygo) }, 100)
        } else {
            mUiHanlder.sendMessageDelayed(msg, 2000)
        }

        val grabRoundInfoModel = mRoomData!!.realRoundInfo
        if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant && grabRoundInfoModel.enterStatus == EQRoundStatus.QRS_INTRO.value) {
            mTurnInfoCardView.visibility = GONE
            onSongInfoCardPlayOver("中途进来", pendingPlaySongCardData)
        } else {
            mTurnInfoCardView.setModeSongSeq(seq == 1, object : SVGAListener {
                override fun onFinished() {
                    mTurnInfoCardView.visibility = GONE
                    onSongInfoCardPlayOver("动画结束进来", pendingPlaySongCardData)
                }
            })
        }
        mGrabTopContentView.setModeGrab()
    }

    internal fun onSongInfoCardPlayOver(from: String, pendingPlaySongCardData: PendingPlaySongCardData) {
        MyLog.d(TAG, "onSongInfoCardPlayOver pendingPlaySongCardData=$pendingPlaySongCardData from=$from")
        mUiHanlder.removeMessages(MSG_ENSURE_SONGCARD_OVER)
        mSingBeginTipsCardView.setVisibility(GONE)
        mGrabGiveupView.hideWithAnimation(false)
        val grabRoundInfoModel = mRoomData!!.realRoundInfo
        if (grabRoundInfoModel != null && grabRoundInfoModel.isFreeMicRound) {
            // 自由麦环节 直接显示卡片了
            mSelfSingCardView.playLyric()
            mGrabOpBtn.hide("onSongInfoCardPlayOver2")
            if (mRoomData!!.isOwner) {
                mGrabGiveupView.delayShowGiveUpView(true)
            }
        } else {
            mSongInfoCardView.bindSongModel(mRoomData!!.realRoundSeq, mRoomData!!.grabConfigModel.totalGameRoundSeq, pendingPlaySongCardData.songModel)
            if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant && grabRoundInfoModel.enterStatus == EQRoundStatus.QRS_INTRO.value) {
                MyLog.d(TAG, "这轮刚进来，不播放导唱过场")
                mGrabOpBtn.hide("onSongInfoCardPlayOver1")
            } else {
                if (mRoomData!!.isInPlayerList) {
                    mGrabOpBtn.playCountDown(pendingPlaySongCardData.seq, 4, pendingPlaySongCardData.songModel)
                }
            }
        }
        mCorePresenter?.playGuide()
    }

    override fun singBySelf() {
        removeAllEnsureMsg()
        mCorePresenter?.stopGuide()
        val now = mRoomData!!.realRoundInfo
        // 第二轮不播这个动画
        mGrabTopContentView.setModeSing()
        mSongInfoCardView.hide()

        mSingBeginTipsCardView.setVisibility(View.VISIBLE)

        mGrabOpBtn.hide("singBySelf")
        mGrabOpBtn.setGrabPreRound(true)

        val msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER)
        mUiHanlder.sendMessageDelayed(msg, 4000)

        if (now != null && now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            // pk的第二轮，没有 vs 的演唱开始提示了
            onSingBeginTipsPlayOver()
        } else {
            singBeginTipsPlay(Runnable { onSingBeginTipsPlayOver() })
        }

        StatisticsAdapter.recordCountEvent("grab", "game_sing", null)
    }

    override fun singByOthers() {
        removeAllEnsureMsg()
        mCorePresenter?.stopGuide()
        mGrabTopContentView.setModeSing()
        mSongInfoCardView.hide()
        mGrabOpBtn.hide("singByOthers")
        mGrabOpBtn.setGrabPreRound(false)
        mGrabGiveupView.hideWithAnimation(false)
        mSingBeginTipsCardView.setVisibility(View.VISIBLE)

        val msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER)
        mUiHanlder.sendMessageDelayed(msg, 2600)

        val now = mRoomData!!.realRoundInfo
        if (now != null && now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            // pk的第二轮，没有 vs 的演唱开始提示了
            if (now != null && now.isParticipant
                    && mRoomData!!.isInPlayerList
                    && !RoomDataUtils.isRoundSinger(now, MyUserInfoManager.getInstance().uid)) {
                // 不是参与者 不是选手 如果是pk的轮次 pk的参与者 都没有爆灭灯按钮
                mGrabOpBtn.toOtherSingState()
            } else {
                mGrabOpBtn.hide("singByOthers2")
            }
            onSingBeginTipsPlayOver()
        } else {
            singBeginTipsPlay(Runnable {
                val grabRoundInfoModel = mRoomData!!.realRoundInfo
                if (grabRoundInfoModel != null
                        && grabRoundInfoModel.isParticipant
                        && mRoomData!!.isInPlayerList
                        && !RoomDataUtils.isRoundSinger(now, MyUserInfoManager.getInstance().uid)
                        && !(grabRoundInfoModel.isMiniGameRound && mRoomData!!.isOwner)) {
                    // 参与者 & 游戏列表中 & 不是本轮演唱者 &  不是小游戏中的房主
                    mGrabOpBtn.toOtherSingState()
                } else {
                    mGrabOpBtn.hide("singByOthers2")
                }
                onSingBeginTipsPlayOver()
            })
        }
    }

    private fun singBeginTipsPlay(runnable: Runnable) {
        val grabRoundInfoModel = mRoomData!!.realRoundInfo
        if (grabRoundInfoModel != null) {
            if (!grabRoundInfoModel.isParticipant && grabRoundInfoModel.isEnterInSingStatus) {
                MyLog.d(TAG, " 进入时已经时演唱阶段了，则不用播卡片了")
                runnable.run()
            } else {
                mSingBeginTipsCardView.bindData { runnable.run() }
            }
        } else {
            MyLog.w(TAG, "singBeginTipsPlay" + " grabRoundInfoModel = null ")
        }
    }

    private fun onSingBeginTipsPlayOver() {
        MyLog.d(TAG, "onSingBeginTipsPlayOver")
        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER)
        mSingBeginTipsCardView.setVisibility(GONE)
        mGrabScoreTipsView.reset()
        val now = mRoomData!!.realRoundInfo
        if (now != null) {
            if (now.singBySelf()) {
                mGrabGiveupView.delayShowGiveUpView(false)
                mCorePresenter?.beginSing()
                mGrabBaseUiController.singBySelf()
            } else {
                if (mRoomData!!.isOwner && now.isMiniGameRound && !RoomDataUtils.isMyRound(mRoomData!!.realRoundInfo)) {
                    mMiniOwnerMicIv.visibility = View.VISIBLE
                    mMiniOwnerMicIv.setImageResource(R.drawable.mini_owner_mute)
                    mGrabGiveupView.delayShowGiveUpView(true)
                } else {
                    mMiniOwnerMicIv.visibility = GONE
                }
                mGrabBaseUiController.singByOthers()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: LightOffAnimationOverEvent) {
        //灭灯动画播放结束
        if (RoomDataUtils.isMyRound(mRoomData!!.realRoundInfo)) {

        } else {
            mOthersSingCardView.tryStartCountDown()
        }
    }

    override fun roundOver(lastInfoModel: GrabRoundInfoModel?, playNextSongInfoCard: Boolean, now: GrabRoundInfoModel?) {
        removeAllEnsureMsg()
        val msg = mUiHanlder.obtainMessage(MSG_ENSURE_ROUND_OVER_PLAY_OVER)
        msg.arg1 = if (playNextSongInfoCard) 1 else 0
        msg.obj = now
        mUiHanlder.sendMessageDelayed(msg, 4000)
        mMiniOwnerMicIv.visibility = GONE
        removeNoAccSrollTipsView()
        removeGrabSelfSingTipView()
        mSongInfoCardView.hide()
        mGrabOpBtn.hide("roundOver")
        mGrabGiveupView.hideWithAnimation(false)
        mGrabBaseUiController.roundOver()
        if (lastInfoModel?.isFreeMicRound == true) {
            mSelfSingCardView.setVisibility(GONE)
        }
        mRoundOverCardView.bindData(lastInfoModel) {
            now?.let {
                onRoundOverPlayOver(playNextSongInfoCard, now)
            }
        }
    }

    private fun onRoundOverPlayOver(playNextSongInfoCard: Boolean, now: GrabRoundInfoModel?) {
        mUiHanlder.removeMessages(MSG_ENSURE_ROUND_OVER_PLAY_OVER)
        mRoundOverCardView.setVisibility(GONE)
        if (playNextSongInfoCard && now != null) {
            grabBegin(now.roundSeq, now.music)
        }
    }

    //    @Override
    //    public void redPkgCountDown(long duration) {
    //        mRedPkgView.setVisibility(View.VISIBLE);
    //        mRedPkgView.startCountDown(duration);
    //        mUiHanlder.postDelayed(new Runnable() {
    //            @Override
    //            public void run() {
    //                mRedPkgView.setVisibility(View.GONE);
    //            }
    //        }, duration);
    //    }

    override fun useEventBus(): Boolean {
        return true
    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            data?.let {
                mRoomData = it as GrabRoomData
            }
        }
    }

    override fun destroy() {
        super.destroy()
        MyLog.d(TAG, "destroy")
        dismissDialog()
        if (mQuitTipsDialog != null && mQuitTipsDialog!!.isShowing) {
            mQuitTipsDialog?.dismiss(false)
            mQuitTipsDialog = null
        }
        mGrabAudioUiController.destroy()
        mGrabVideoUiController.destroy()
        mUiHanlder.removeCallbacksAndMessages(null)
        mIsGameEndAniamtionShow = false
        if (mAnimatorList != null) {
            for (animator in mAnimatorList) {
                animator?.cancel()
            }
            mAnimatorList.clear()
        }


        mContinueSendView?.destroy()



        mGiftPanelView?.destroy()


        mGameTipsManager.destory()


        mGrabWidgetAnimationController.destroy()

        U.getSoundUtils().release(TAG)
        BgMusicManager.getInstance().isRoom = false
    }

    override fun onBackPressed(): Boolean {
        if (mInputContainerView.onBackPressed()) {
            return true
        }

        if (mGiftPanelView.onBackPressed()) {
            return true
        }
        if (mBeautyControlPanelView.onBackPressed()) {
            return true
        }
        quitGame()
        return true
    }

    override fun exitInRound() {

    }

    override fun updateUserState(jsonOnLineInfoList: List<OnlineInfoModel>) {

    }

    private fun quitGame() {
        dismissDialog()
        if (mQuitTipsDialog == null) {
            var msg = "提前退出会破坏其他玩家的对局体验\n确定退出么？"
            if (mRoomData!!.isOwner) {
                msg = "房主退出后房间将解散,是否确认退出?"
            }
            val tipsDialogView = TipsDialogView.Builder(context)
                    .setMessageTip(msg)
                    .setConfirmTip("确定")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener {
                        mQuitTipsDialog?.dismiss(false)
                        mCorePresenter?.exitRoom("quitGame tipsDialog")
                    }
                    .setCancelBtnClickListener {
                        mQuitTipsDialog?.dismiss()
                    }
                    .build()

            mQuitTipsDialog = DialogPlus.newDialog(context!!)
                    .setContentHolder(ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create()
        }
        mQuitTipsDialog?.show()
    }

    override fun gameFinish() {
        MyLog.w(TAG, "游戏结束了")
        //        mUiHanlder.removeMessages(MSG_ENSURE_GAME_OVER);
        //        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_GAME_OVER);
        //        mUiHanlder.sendMessageDelayed(msg, 4000);
        mSelfSingCardView.setVisibility(GONE)
        mOthersSingCardView.hide()
        mTurnInfoCardView.visibility = GONE
        mSingBeginTipsCardView.setVisibility(GONE)
        mRoundOverCardView.setVisibility(GONE)
        mGrabGameOverView.visibility = View.VISIBLE
        mGrabGameOverView.starSVGAAnimation(object : SVGAListener {
            override fun onFinished() {
                mCorePresenter?.exitRoom("gameFinish")
                //onGrabGameOver("onFinished");
            }
        })
    }

    override fun onGetGameResult(success: Boolean) {
        //        if (success) {
        onGrabGameOver("onGetGameResultSuccess")
        //        } else {
        //            if (getActivity() != null) {
        //                getActivity().finish();
        //            }
        //        }
    }

    override fun onChangeRoomResult(success: Boolean, errMsg: String?) {
        val t = System.currentTimeMillis() - mBeginChangeRoomTs
        if (t > 1500) {
            mGrabChangeRoomTransitionView.visibility = GONE
            if (!success) {
                U.getToastUtil().showShort(errMsg)
            }
        } else {
            mUiHanlder.postDelayed({
                if (!success) {
                    U.getToastUtil().showShort(errMsg)
                }
                mGrabChangeRoomTransitionView.visibility = GONE
            }, 1500 - t)
        }
        if (success) {
            initBgView()
            hideAllCardView()
            mVipEnterPresenter?.switchRoom()
            mVIPEnterView?.switchRoom()
            // 重新决定显示mic按钮
            mBottomContainerView.setRoomData(mRoomData!!)
        }
    }

    override fun giveUpSuccess(seq: Int) {
        mGrabGiveupView.giveUpSuccess()
    }

    override fun updateScrollBarProgress(score: Int, songLineNum: Int) {
        mGrabScoreTipsView.updateScore(score, songLineNum)
    }

    private fun onGrabGameOver(from: String) {
        MyLog.w(TAG, "onGrabGameOver1 $from")
        //        mUiHanlder.removeMessages(MSG_ENSURE_GAME_OVER);
        val activity = activity
        if (activity != null) {
            if (!activity.isDestroyed && !activity.isFinishing) {
                MyLog.w(TAG, "onGrabGameOver activity gogogo")
                ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_RESULT)
                        .withSerializable("room_data", mRoomData)
                        .navigation()
                for (activity1 in U.getActivityUtils().activityList) {
                    if (activity1 is GrabRoomActivity) {
                        activity1.finish()
                    }
                    if (activity1 is SongManagerActivity) {
                        activity1.finish()
                    }
                }

            } else {
                MyLog.d(TAG, "onGrabGameOver activity hasdestroy")
            }
        } else {
            MyLog.w(TAG, "onGrabGameOver activity==null")
        }
    }

    // 确认踢人弹窗
    private fun showKickConfirmDialog(userInfoModel: UserInfoModel) {
        MyLog.d(TAG, "showKickConfirmDialog userInfoModel=$userInfoModel")
        dismissDialog()
        U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)
        val type: Int
        if (mRoomData!!.isOwner) {
            if (mRoomData!!.ownerKickTimes > 0) {
                type = ConfirmDialog.TYPE_OWNER_KICK_CONFIRM
            } else {
                type = ConfirmDialog.TYPE_KICK_CONFIRM
            }
        } else {
            type = ConfirmDialog.TYPE_KICK_CONFIRM
        }

        if (mRoomData!!.isOwner) {
            // 房主不消耗金币
            mGrabKickDialog = ConfirmDialog(U.getActivityUtils().topActivity, userInfoModel, type, 0)
        } else {
            mGrabKickDialog = ConfirmDialog(U.getActivityUtils().topActivity, userInfoModel, type, mRoomData!!.grabConfigModel.kickUserConsumCoinCnt)
        }
        mGrabKickDialog?.setListener { userInfoModel ->
            // 发起踢人请求
            mCorePresenter?.reqKickUser(userInfoModel.userId)
        }
        mGrabKickDialog?.show()
    }

    private fun dismissDialog() {

        mGameRuleDialog?.dismiss(false)


        mBottomContainerView?.dismissPopWindow()


        mPersonInfoDialog?.dismiss()


        mGrabKickDialog?.dismiss(false)


        mVoiceControlDialog?.dismiss(false)


        mTipsDialogView?.dismiss(false)

    }

    // 请求踢人弹窗
    override fun showKickVoteDialog(userId: Int, sourceUserId: Int) {
        MyLog.d(TAG, "showKickReqDialog userId=$userId sourceUserId=$sourceUserId")
        dismissDialog()
        U.getKeyBoardUtils().hideSoftInputKeyBoard(activity)

        val playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, userId.toLong())
        if (playerInfoModel != null) {
            val userInfoModel = playerInfoModel.userInfo
            mGrabKickDialog = ConfirmDialog(U.getActivityUtils().topActivity, userInfoModel, ConfirmDialog.TYPE_KICK_REQUEST, 5)
            mGrabKickDialog?.setListener {
                // 同意踢人
                mCorePresenter?.voteKickUser(true, userId, sourceUserId)
            }
            mGrabKickDialog?.show()
        }
    }

    override fun kickBySomeOne(isOwner: Boolean) {
        MyLog.d(TAG, "kickBySomeOne isOwner=$isOwner")
        //onGrabGameOver("kickBySomeOne");
        U.getToastUtil().showSkrCustomLong(CommonToastView.Builder(U.app())
                .setImage(R.drawable.touxiangshezhishibai_icon)
                .setText(if (isOwner) "房主将你请出了房间" else "超过半数玩家请你出房间，要友好文明游戏哦~")
                .build())
        mCorePresenter?.exitRoom("kickBySomeOne")
    }

    override fun dimissKickDialog() {

        mGrabKickDialog?.dismiss()

    }

    override fun showPracticeFlag(flag: Boolean) {
        if (flag) {
            mPracticeFlagIv.visibility = View.VISIBLE
        } else {
            mPracticeFlagIv.visibility = GONE
        }
    }

    override fun hideInviteTipView() {
        removeInviteTipView()
    }

    override fun hideManageTipView() {
        removeManageSongTipView()
    }

    fun hideAllCardView() {
        mRoundOverCardView.setVisibility(GONE)
        mOthersSingCardView.setVisibility(GONE)
        mSelfSingCardView.setVisibility(GONE)
        mSingBeginTipsCardView.setVisibility(GONE)
    }

    override fun beginOuath() {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=singer"))
                .greenChannel().navigation()
        activity?.finish()
    }

    override fun changeRoomMode(isVideo: Boolean) {
        if (isVideo) {
            mGrabAudioUiController.stopWork()
            mGrabBaseUiController = mGrabVideoUiController
        } else {
            mGrabVideoUiController.stopWork()
            mGrabBaseUiController = mGrabAudioUiController
        }
    }

    internal class PendingPlaySongCardData(var seq: Int, var songModel: SongModel) {

        override fun toString(): String {
            return "PendingPlaySongCardData{" +
                    "seq=" + seq +
                    ", songModel=" + songModel +
                    '}'.toString()
        }
    }

    companion object {

        val TAG = "GrabRoomFragment"

        //    public static final int MSG_ENSURE_READYGO_OVER = 1;

        val MSG_ENSURE_SONGCARD_OVER = 2

        val MSG_ENSURE_SING_BEGIN_TIPS_OVER = 3

        val MSG_ENSURE_ROUND_OVER_PLAY_OVER = 4

        val MSG_ENSURE_BATTLE_BEGIN_OVER = 5
    }

}
