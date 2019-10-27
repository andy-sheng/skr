package com.module.playways.grab.room.view.video

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Message
import android.support.constraint.ConstraintLayout
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.log.DebugLogView
import com.common.view.ex.ExTextView
import com.engine.EngineEvent
import com.engine.UserStatus
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.common.view.ExViewStub
import com.module.playways.grab.room.ui.GrabWidgetAnimationController
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.moudule.playways.beauty.BeautyPreviewActivity
import com.moudule.playways.beauty.event.ReturnFromBeautyActivityEvent
import com.zq.mediaengine.capture.CameraCapture
import com.zq.mediaengine.kit.ZqEngineKit

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import java.util.ArrayList

class GrabVideoDisplayView(viewStub: ViewStub, private val mRoomData: GrabRoomData) : ExViewStub(viewStub) {
    val TAG = "GrabVideoDisplayView"

    internal var mMainVideoView: TextureView?=null
    internal var mMiddleGuide: View?=null
    internal var mLeftAvatarIv: BaseImageView?=null
    internal var mRightAvatarIv: BaseImageView?=null
    internal var mMiddleAvatarIv: BaseImageView?=null

    internal var mLeftTipsTv: TextView?=null
    internal var mRightTipsTv: TextView?=null
    internal var mMiddleTipsTv: TextView?=null
    internal var mSingCountDownView: SingCountDownView2? = null
    internal var mBeautySettingBtn: ImageView? = null
    internal var mLeftNameTv: ExTextView?=null
    internal var mRightNameTv: ExTextView?=null
    internal var mBg1View: View?=null
    internal var mBg2View: View?=null

    internal var mPkBeginTipsView: TextView?=null

    var mSelfSingCardListener:(()->Unit)?=null
    var mBeautyListener: (()->Unit)?=null
    internal var mMainUserId = 0
    internal var mLeftUserId = 0
    internal var mRightUserId = 0
    internal var mPkBeginTipsAnimation: AnimatorSet? = null

    private val mUiHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_ENSURE_FIRST_CAMERA_DECODED -> onCameraFirstFrameRendered()
                MSG_ENSURE_MIDDLE_FIRST_VIDEO_FRAME_DECODED -> onFirstVideoFrameRendered(mMainUserId, true)
                MSG_ENSURE_LEFT_FIRST_VIDEO_FRAME_DECODED -> onFirstVideoFrameRendered(mLeftUserId, true)
                MSG_ENSURE_RIGHT_FIRST_VIDEO_FRAME_DECODED -> onFirstVideoFrameRendered(mRightUserId, true)
            }
        }
    }

    /**
     * 这里为什么不用 U.getActivityUtils().getTopActivity() instanceof BeautyPreviewActivity 判断呢
     * 因为想从美颜界面返回时快速渲染出ui，所以在BeautyPreviewActivity finish时就可以渲染了，
     * 但这时U.getActivityUtils().getTopActivity() 还是  BeautyPreviewActivity
     *///boolean topBeautyActivityVisiable = U.getActivityUtils().getTopActivity() instanceof BeautyPreviewActivity;
    val isBeautyActivityVisiable: Boolean
        get() = BeautyPreviewActivity.hasCreate

    override fun init(parentView: View) {
        mMainVideoView = mParentView!!.findViewById(R.id.main_video_view)
        run {
            mLeftAvatarIv = mParentView!!.findViewById(R.id.left_avatar_iv)
            val lp = mLeftAvatarIv?.layoutParams
            lp?.width = U.getDisplayUtils().screenWidth / 2
            mLeftTipsTv = mParentView!!.findViewById(R.id.left_tips_tv)
            mLeftNameTv = mParentView!!.findViewById(R.id.left_name_tv)
        }
        run {
            mRightAvatarIv = mParentView!!.findViewById(R.id.right_avatar_iv)
            val lp = mRightAvatarIv?.layoutParams
            lp?.width = U.getDisplayUtils().screenWidth / 2
            mRightTipsTv = mParentView!!.findViewById(R.id.right_tips_tv)
            mRightNameTv = mParentView!!.findViewById(R.id.right_name_tv)
        }
        run {
            mMiddleAvatarIv = mParentView!!.findViewById(R.id.middle_avatar_iv)
            mMiddleTipsTv = mParentView!!.findViewById(R.id.middle_tips_tv)

        }
        run {
            mBg1View = mParentView!!.findViewById(R.id.bg1_view)
            mBg2View = mParentView!!.findViewById(R.id.bg2_view)
        }
        mSingCountDownView = mParentView!!.findViewById(R.id.sing_count_down_view)
        mSingCountDownView!!.setListener(mSelfSingCardListener)
        mBeautySettingBtn = mParentView!!.findViewById(R.id.beauty_setting_btn)
        mBeautySettingBtn!!.setOnClickListener {
            if (mBeautyListener != null) {
                mBeautyListener!!.invoke()
            }
        }
        mPkBeginTipsView = mParentView!!.findViewById(R.id.pk_begin_tips_view)
        mMiddleGuide = mParentView!!.findViewById(R.id.middle_guide)
        config()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_room_video_display_view_stub_layout
    }

    protected fun config() {
        // 设置推流分辨率
        ZqEngineKit.getInstance().setPreviewResolution(ZqEngineKit.VIDEO_RESOLUTION_480P)
        ZqEngineKit.getInstance().setTargetResolution(ZqEngineKit.VIDEO_RESOLUTION_360P)

        // 设置推流帧率
        ZqEngineKit.getInstance().previewFps = 30f
        ZqEngineKit.getInstance().targetFps = 30f

        // 设置视频方向（横屏、竖屏）
        //mIsLandscape = false;
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ZqEngineKit.getInstance().rotateDegrees = 0

        // 选择前后摄像头
        ZqEngineKit.getInstance().cameraFacing = CameraCapture.FACING_FRONT

        // 设置预览View
        //ZqEngineKit.getInstance().setDisplayPreview(mMainVideoView);
    }

    fun bindVideoStream(userId: UserInfoModel) {
        MyLog.d(TAG, "bindVideoStream userId=$userId")
        tryInflate()
        ensureBindDisplayView()
        setVisibility(View.VISIBLE)
        run {
            val lp = mParentView!!.layoutParams
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        run {
            val lp = mMainVideoView?.layoutParams
            lp?.height = U.getDisplayUtils().screenWidth * 16 / 9
        }
        mBg1View?.visibility = View.VISIBLE
        mBg2View?.visibility = View.VISIBLE
        mMainUserId = userId.userId
        mMiddleAvatarIv?.visibility = View.VISIBLE
        AvatarUtils.loadAvatarByUrl(mMiddleAvatarIv, AvatarUtils.newParamsBuilder(userId.avatar)
                .setBlur(true)
                .build()
        )
        tryBindMainVideoStream(false)
        if (userId.userId.toLong() == MyUserInfoManager.getInstance().uid) {
            mBeautySettingBtn!!.visibility = View.VISIBLE
        } else {
            mBeautySettingBtn!!.visibility = View.GONE
        }
        startSingCountDown()
    }

    /**
     * @param userID1
     * @param userID2
     * @param roundFlag 0 普通轮次 | 1 pk第一轮 | 2 pk第二轮
     */
    fun bindVideoStream(userID1: UserInfoModel, userID2: UserInfoModel, roundFlag: Int) {
        MyLog.d(TAG, "bindVideoStream roundFlag=$roundFlag")
        tryInflate()
        ensureBindDisplayView()
        setVisibility(View.VISIBLE)
        run {
            val lp = mMainVideoView?.layoutParams
            lp?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        mLeftNameTv?.visibility = View.VISIBLE
        mRightNameTv?.visibility = View.VISIBLE
        mLeftNameTv?.text = userID1.nicknameRemark
        mRightNameTv?.text = userID2.nicknameRemark
        val lp = mParentView!!.layoutParams
        lp.height = U.getDisplayUtils().dip2px(315f)
        mBg1View?.visibility = View.GONE
        mBg2View?.visibility = View.GONE
        mLeftUserId = userID1.userId
        mRightUserId = userID2.userId
        if (roundFlag != 2) {
            mLeftAvatarIv?.visibility = View.VISIBLE
            AvatarUtils.loadAvatarByUrl(mLeftAvatarIv, AvatarUtils.newParamsBuilder(userID1.avatar)
                    .setBlur(true)
                    .build()
            )
            mRightAvatarIv?.visibility = View.VISIBLE
            AvatarUtils.loadAvatarByUrl(mRightAvatarIv, AvatarUtils.newParamsBuilder(userID2.avatar)
                    .setBlur(true)
                    .build()
            )
            tryBindLeftVideoStream(false)
            tryBindRightVideoStream(false)
        }
        startSingCountDown()
        if (mRightUserId.toLong() == MyUserInfoManager.getInstance().uid || mLeftUserId.toLong() == MyUserInfoManager.getInstance().uid) {
            mBeautySettingBtn!!.visibility = View.VISIBLE
        } else {
            mBeautySettingBtn!!.visibility = View.GONE
        }
        if (roundFlag == 1) {
            pkBeginTips(true)
        } else if (roundFlag == 2) {
            pkBeginTips(false)
        } else {
            mPkBeginTipsView?.visibility = View.GONE
        }
    }

    private fun pkBeginTips(left: Boolean) {
        val lp = mPkBeginTipsView?.layoutParams as ConstraintLayout.LayoutParams
        if (left) {
            lp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
            lp.rightToLeft = mMiddleGuide!!.id
            lp.leftToRight = -1
            lp.rightToRight = -1
        } else {
            lp.leftToLeft = -1
            lp.rightToLeft = -1
            lp.leftToRight = mMiddleGuide!!.id
            lp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        }
        mPkBeginTipsView?.layoutParams = lp
        mPkBeginTipsView?.visibility = View.VISIBLE
        if (mPkBeginTipsAnimation != null) {
            mPkBeginTipsAnimation!!.cancel()
        }
        val objectAnimator1 = ObjectAnimator.ofFloat<View>(mPkBeginTipsView, View.TRANSLATION_Y, mPkBeginTipsView!!.height.toFloat(), 0f)
        objectAnimator1.setDuration(300)
        val objectAnimator2 = ObjectAnimator.ofFloat<View>(mPkBeginTipsView, View.TRANSLATION_Y, 0f, mPkBeginTipsView!!.height.toFloat())
        objectAnimator2.setDuration(300)
        objectAnimator2.setStartDelay(1000)
        mPkBeginTipsAnimation = AnimatorSet()
        mPkBeginTipsAnimation!!.playSequentially(objectAnimator1, objectAnimator2)
        mPkBeginTipsAnimation!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                mPkBeginTipsView?.visibility = View.GONE
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mPkBeginTipsView?.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mPkBeginTipsView?.visibility = View.VISIBLE
            }
        })
        mPkBeginTipsAnimation!!.start()
    }

    internal fun startSingCountDown() {
        val infoModel = mRoomData.realRoundInfo
        if (mSingCountDownView != null) {
            if (infoModel != null) {
                val totalMs = infoModel.singTotalMs
                val progress: Int  //当前进度条
                val leaveTime: Int //剩余时间
                MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant + " enterStatus=" + infoModel.enterStatus)
                if (!infoModel.isParticipant && infoModel.isEnterInSingStatus) {
                    MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
                    progress = infoModel.elapsedTimeMs * 100 / totalMs
                    leaveTime = totalMs - infoModel.elapsedTimeMs
                } else {
                    progress = 1
                    leaveTime = totalMs
                }
                mSingCountDownView!!.startPlay(progress, leaveTime, true)
            }
        }
    }

    fun reset() {
        MyLog.d(TAG, "reset")
        if (!isBeautyActivityVisiable) {
            ZqEngineKit.getInstance().stopCameraPreview()
            ZqEngineKit.getInstance().unbindAllRemoteVideo()
        }
        mMainUserId = 0
        mLeftUserId = 0
        mRightUserId = 0
        if (mParentView != null) {
            mLeftAvatarIv?.visibility = View.GONE
            mLeftTipsTv?.visibility = View.GONE
            mLeftNameTv?.visibility = View.GONE
            mRightAvatarIv?.visibility = View.GONE
            mRightTipsTv?.visibility = View.GONE
            mRightNameTv?.visibility = View.GONE
            mMiddleAvatarIv?.visibility = View.GONE
            mMiddleTipsTv?.visibility = View.GONE
            mPkBeginTipsView?.visibility = View.GONE
            mSingCountDownView!!.reset()
            setMarginTop(0)
        }
        if (mPkBeginTipsAnimation != null) {
            mPkBeginTipsAnimation!!.cancel()
        }
        mUiHandler.removeCallbacksAndMessages(null)
    }

    internal fun ensureBindDisplayView() {
        if (!isBeautyActivityVisiable) {
            // 美颜界面不在顶部
            if (ZqEngineKit.getInstance().displayPreview !== mMainVideoView) {
                ZqEngineKit.getInstance().setDisplayPreview(mMainVideoView)
            }
        }
    }

    internal fun tryBindMainVideoStream(force: Boolean) {
        if (isBeautyActivityVisiable) {
            // 美颜界面在顶部 不操作
            return
        }
        if (mMainUserId != 0) {
            if (mMainUserId.toLong() == MyUserInfoManager.getInstance().uid) {
                // 是自己
                ZqEngineKit.getInstance().setLocalVideoRect(0f, 0f, 1f, 1f, 1f)
                ZqEngineKit.getInstance().startCameraPreview()
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_FIRST_CAMERA_DECODED, 1000)
                //                mMiddleAvatarIv.setVisibility(View.VISIBLE);
            } else {
                // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
                // 但如果set时别人的视频流还没过来，
                if (ZqEngineKit.getInstance().isFirstVideoDecoded(mMainUserId) || force) {
                    ZqEngineKit.getInstance().bindRemoteVideoRect(mMainUserId, 0f, 0f, 1f, 1f, 1f)
                    mMiddleAvatarIv?.visibility = View.GONE
                } else {
                    DebugLogView.println(TAG, mMainUserId.toString() + "首帧还没到!!!")
                    mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_MIDDLE_FIRST_VIDEO_FRAME_DECODED, 2000)
                    mMiddleAvatarIv?.visibility = View.VISIBLE
                }
            }
        }
    }

    internal fun tryBindLeftVideoStream(force: Boolean) {
        if (isBeautyActivityVisiable) {
            // 美颜界面在顶部 不操作
            return
        }
        if (mLeftUserId != 0) {
            if (mLeftUserId.toLong() == MyUserInfoManager.getInstance().uid) {
                // 是自己
                ZqEngineKit.getInstance().setLocalVideoRect(0f, 0f, 0.5f, 1f, 1f)
                ZqEngineKit.getInstance().startCameraPreview()
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_FIRST_CAMERA_DECODED, 1000)
                //                mLeftAvatarIv.setVisibility(View.GONE);
                //                mLeftTipsTv.setVisibility(View.GONE);
            } else {
                // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
                // 但如果set时别人的视频流还没过来，
                if (ZqEngineKit.getInstance().isFirstVideoDecoded(mLeftUserId) || force) {
                    ZqEngineKit.getInstance().bindRemoteVideoRect(mLeftUserId, 0f, 0f, 0.5f, 1f, 1f)
                    mLeftAvatarIv?.visibility = View.GONE
                    mLeftTipsTv?.visibility = View.GONE
                } else {
                    DebugLogView.println(TAG, mLeftUserId.toString() + "首帧还没到!!!")
                    mLeftAvatarIv?.visibility = View.VISIBLE
                }
            }
        }
    }

    internal fun tryBindRightVideoStream(force: Boolean) {
        if (isBeautyActivityVisiable) {
            // 美颜界面在顶部 不操作
            return
        }
        if (mRightUserId != 0) {
            if (mRightUserId.toLong() == MyUserInfoManager.getInstance().uid) {
                // 是自己
                ZqEngineKit.getInstance().setLocalVideoRect(0.5f, 0f, 0.5f, 1f, 1f)
                ZqEngineKit.getInstance().startCameraPreview()
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_FIRST_CAMERA_DECODED, 1000)
                //                mRightAvatarIv.setVisibility(View.GONE);
                //                mRightTipsTv.setVisibility(View.GONE);
            } else {
                // 别人唱，两种情况。一是我绑定时别人的首帧视频流已经过来了，这是set没问题。
                // 但如果set时别人的视频流还没过来，
                if (ZqEngineKit.getInstance().isFirstVideoDecoded(mRightUserId) || force) {
                    ZqEngineKit.getInstance().bindRemoteVideoRect(mRightUserId, 0.5f, 0f, 0.5f, 1f, 1f)
                    mRightAvatarIv?.visibility = View.GONE
                    mRightTipsTv?.visibility = View.GONE
                } else {
                    DebugLogView.println(TAG, mRightUserId.toString() + "首帧还没到!!!")
                    mRightAvatarIv?.visibility = View.VISIBLE
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (event.type != EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION && event.type != EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            DebugLogView.println(TAG, event.toString())
        }
        if (mParentView == null || mParentView!!.visibility != View.VISIBLE) {
            return
        }
        if (event.getType() == EngineEvent.TYPE_FIRST_REMOTE_VIDEO_DECODED) {
            val userId = event.getUserStatus().userId
            onFirstVideoFrameRendered(userId, false)
        } else if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
            // 自己
            val roleChangeInfo = event.getObj<EngineEvent.RoleChangeInfo>()
            if (roleChangeInfo.newRole == 2) {
                if (!isBeautyActivityVisiable) {
                    // 美颜界面在顶部 不操作
                    ZqEngineKit.getInstance().stopCameraPreview()
                }
                //变成观众了 2 是观众
                if (MyUserInfoManager.getInstance().uid == mLeftUserId.toLong()) {
                    mLeftAvatarIv?.visibility = View.VISIBLE
                    mLeftTipsTv?.visibility = View.VISIBLE
                    mLeftTipsTv?.text = "不唱了"
                } else if (MyUserInfoManager.getInstance().uid == mRightUserId.toLong()) {
                    mRightAvatarIv?.visibility = View.VISIBLE
                    mRightTipsTv?.visibility = View.VISIBLE
                    mRightTipsTv?.text = "不唱了"
                }
            }
        } else if (event.getType() == EngineEvent.TYPE_USER_LEAVE) {
            // 用户离开了
            val userId = event.getUserStatus().userId
            ZqEngineKit.getInstance().unbindRemoteVideo(userId)
            if (userId == mLeftUserId) {
                mLeftAvatarIv?.visibility = View.VISIBLE
                mLeftTipsTv?.visibility = View.VISIBLE
                mLeftTipsTv?.text = "不唱了"
            } else if (userId == mRightUserId) {
                mRightAvatarIv?.visibility = View.VISIBLE
                mRightTipsTv?.visibility = View.VISIBLE
                mRightTipsTv?.text = "不唱了"
            }
        } else if (event.getType() == EngineEvent.TYPE_CAMERA_FIRST_FRAME_RENDERED) {
            onCameraFirstFrameRendered()
        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_VIDEO) {
            val userStatus = event.getUserStatus()
            if (userStatus != null) {
                val userId = event.getUserStatus().userId
                if (userStatus.isVideoMute) {
                    if (userId == mLeftUserId) {
                        mLeftTipsTv?.visibility = View.VISIBLE
                        mLeftTipsTv?.text = "溜走了"
                    } else if (userId == mRightUserId) {
                        mRightTipsTv?.visibility = View.VISIBLE
                        mRightTipsTv?.text = "溜走了"
                    } else if (userId == mMainUserId) {
                        mMiddleTipsTv?.visibility = View.VISIBLE
                        mMiddleTipsTv?.text = "溜走了"
                    }
                } else {
                    if (userId == mLeftUserId) {
                        mLeftTipsTv?.visibility = View.GONE
                    } else if (userId == mRightUserId) {
                        mRightTipsTv?.visibility = View.GONE
                    } else if (userId == mMainUserId) {
                        mMiddleTipsTv?.visibility = View.GONE
                    }
                }
            }
        }

    }

    internal fun onCameraFirstFrameRendered() {
        mUiHandler.removeMessages(MSG_ENSURE_FIRST_CAMERA_DECODED)
        if (MyUserInfoManager.getInstance().uid == mLeftUserId.toLong()) {
            mLeftAvatarIv?.visibility = View.GONE
            mLeftTipsTv?.visibility = View.GONE
        } else if (MyUserInfoManager.getInstance().uid == mRightUserId.toLong()) {
            mRightAvatarIv?.visibility = View.GONE
            mRightTipsTv?.visibility = View.GONE
        } else if (MyUserInfoManager.getInstance().uid == mMainUserId.toLong()) {
            mMiddleAvatarIv?.visibility = View.GONE
        }
    }

    internal fun onFirstVideoFrameRendered(userId: Int, force: Boolean) {
        if (force) {
            DebugLogView.println(TAG, "onFirstVideoFrameRendered 超时，强制渲染 userId=$userId")
        }
        if (userId == mMainUserId) {
            mUiHandler.removeMessages(MSG_ENSURE_MIDDLE_FIRST_VIDEO_FRAME_DECODED)
            tryBindMainVideoStream(force)
        } else if (userId == mLeftUserId) {
            mUiHandler.removeMessages(MSG_ENSURE_LEFT_FIRST_VIDEO_FRAME_DECODED)
            tryBindLeftVideoStream(force)
        } else if (userId == mRightUserId) {
            mUiHandler.removeMessages(MSG_ENSURE_RIGHT_FIRST_VIDEO_FRAME_DECODED)
            tryBindRightVideoStream(force)
        }
    }

    /**
     * 从美颜预览返回
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ReturnFromBeautyActivityEvent) {
        if (mParentView != null) {
            if (mParentView!!.visibility == View.VISIBLE) {
                ensureBindDisplayView()
                tryBindMainVideoStream(false)
                tryBindLeftVideoStream(false)
                tryBindRightVideoStream(false)
            } else {
                ZqEngineKit.getInstance().unbindAllRemoteVideo()
            }
        }
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        EventBus.getDefault().unregister(this)
    }

    /**
     * 内部的两个小部件的动画
     *
     * @param open
     * @param topContentViewVisiable
     * @return
     */
    fun getInnerAnimator(open: Boolean, topContentViewVisiable: Boolean): List<Animator>? {
        if (mSingCountDownView == null) {
            return null
        }
        val ty = getTranslateY(open, topContentViewVisiable)

        val list = ArrayList<Animator>()

        val objectAnimator1 = ObjectAnimator.ofFloat<View>(mSingCountDownView, View.TRANSLATION_Y, mSingCountDownView!!.translationY, ty.toFloat())
        list.add(objectAnimator1)

        val objectAnimator2 = ObjectAnimator.ofFloat<View>(mBeautySettingBtn, View.TRANSLATION_Y, mSingCountDownView!!.translationY, ty.toFloat())
        list.add(objectAnimator2)
        return list
    }

    fun setSelfSingCardListener(selfSingCardListener: ()->Unit) {
        mSelfSingCardListener = selfSingCardListener
    }

    fun adjustViewPostion(open: Boolean, topContentViewVisiable: Boolean) {
        val translateY = getTranslateY(open, topContentViewVisiable)
        if (mSingCountDownView != null) {
            mSingCountDownView!!.translationY = translateY.toFloat()
        }
        if (mBeautySettingBtn != null) {
            mBeautySettingBtn!!.translationY = translateY.toFloat()
        }
    }

    fun adjustViewPostionWhenSolo() {
        val translateY = U.getStatusBarUtil().getStatusBarHeight(U.app())
        if (mSingCountDownView != null) {
            mSingCountDownView!!.translationY = translateY.toFloat()
        }
        if (mBeautySettingBtn != null) {
            mBeautySettingBtn!!.translationY = translateY.toFloat()
        }
    }

    private fun getTranslateY(open: Boolean, topContentViewVisiable: Boolean): Int {
        var ty = 0
        if (mParentView != null) {
            val lp = mParentView!!.layoutParams
            if (open) {
                if (topContentViewVisiable && lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    ty = U.getDisplayUtils().dip2px(58f)
                } else {
                    ty = U.getDisplayUtils().dip2px(14f)
                }
            } else {
                if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    ty = U.getDisplayUtils().dip2px(88f)
                } else {
                    ty = U.getDisplayUtils().dip2px(14f)
                }
            }
        }
        return ty
    }

    fun setMarginTop(px: Int) {
        if (mParentView != null) {
            val lp = mParentView!!.layoutParams as RelativeLayout.LayoutParams
            lp.topMargin = px
        }
    }

    /**
     * 需要额外往下移动的数值，如全屏到非全屏时，需要加个顶部栏
     *
     * @return
     */
    fun getExtraTranslateYWhenOpen(openType: Int): Int {
        if (openType == GrabWidgetAnimationController.OPEN_TYPE_FOR_LYRIC) {
            // 演唱者
            return U.getStatusBarUtil().getStatusBarHeight(U.app())
        } else {
            // 非演唱者
            if (mParentView != null) {
                val lp = mParentView!!.layoutParams
                return if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                    U.getStatusBarUtil().getStatusBarHeight(U.app())
                } else {
                    0
                }
            } else {
                return 0
            }
        }
    }

    companion object {

        internal val MSG_ENSURE_FIRST_CAMERA_DECODED = 9
        internal val MSG_ENSURE_MIDDLE_FIRST_VIDEO_FRAME_DECODED = 10
        internal val MSG_ENSURE_LEFT_FIRST_VIDEO_FRAME_DECODED = 11
        internal val MSG_ENSURE_RIGHT_FIRST_VIDEO_FRAME_DECODED = 12
    }
}
