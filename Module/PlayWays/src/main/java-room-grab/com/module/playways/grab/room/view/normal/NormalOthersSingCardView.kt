package com.module.playways.grab.room.view.normal


import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import com.common.anim.svga.SvgaParserAdapter
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.component.level.utils.LevelConfigUtils
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.room.data.H
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.zq.live.proto.GrabRoom.EQRoundStatus
import com.zq.live.proto.MicRoom.EMRoundStatus
import org.greenrobot.eventbus.EventBus

/**
 * 其他人主场景
 */
class NormalOthersSingCardView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "NormalOthersSingCardView"

    internal var mCountDownStatus = COUNT_DOWN_STATUS_WAIT

    internal var mUseId: Int = 0   // 当前唱歌人的id

    internal var mGrabStageView: SVGAImageView? = null
    internal var mSingAvatarView: BaseImageView? = null
    //    internal var mCircleCountDownView: CircleCountDownView? = null
    internal var mCircleCountDownView: SingCountDownView2? = null
    internal var mTvSingerName: ExTextView? = null
    internal var mLevelBg: ImageView? = null

    internal var mEnterAlphaAnimation: AlphaAnimation? = null                // 进场动画
    internal var mLeaveTranslateAnimation: TranslateAnimation? = null   // 出场动画

    internal var mUiHandler: Handler? = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_ENSURE_PLAY) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                countDown("handleMessage")
            }
        }
    }

    internal var mHasPlayFullAnimation = false

    override fun init(parentView: View) {
        mGrabStageView = mParentView!!.findViewById<View>(R.id.grab_stage_view) as SVGAImageView
        mSingAvatarView = mParentView!!.findViewById<View>(R.id.sing_avatar_view) as BaseImageView
        mLevelBg = mParentView!!.findViewById<View>(R.id.level_bg) as ImageView
        mCircleCountDownView = mParentView!!.findViewById<View>(R.id.sing_count_down_view) as SingCountDownView2
        mTvSingerName = mParentView!!.findViewById<View>(R.id.tv_singer_name) as ExTextView

        mSingAvatarView?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mUseId != 0) {
                    EventBus.getDefault().post(ShowPersonCardEvent(mUseId))
                }
            }
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_normal_other_sing_card_stub_layout
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        if (mEnterAlphaAnimation != null) {
            mEnterAlphaAnimation!!.setAnimationListener(null)
            mEnterAlphaAnimation!!.cancel()
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation!!.setAnimationListener(null)
            mLeaveTranslateAnimation!!.cancel()
        }
        if (mUiHandler != null) {
            mUiHandler!!.removeCallbacksAndMessages(null)
        }
    }

    fun bindData() {
        tryInflate()
        var userInfoModel: UserInfoModel? = null
        if (H.isGrabRoom()) {
            userInfoModel = H.grabRoomData?.getPlayerOrWaiterInfo(H.grabRoomData?.realRoundInfo?.userID)
        } else if (H.isMicRoom()) {
            userInfoModel = H.micRoomData?.getPlayerOrWaiterInfo(H.micRoomData?.realRoundInfo?.userID)
        }else if(H.isRelayRoom()){
            if(H.relayRoomData?.isSingByMeNow() == true){
                userInfoModel = MyUserInfo.toUserInfoModel(MyUserInfoManager.myUserInfo)
            }else{
                userInfoModel = H.relayRoomData?.peerUser?.userInfo
            }
        }
        mUiHandler?.removeCallbacksAndMessages(null)
        mHasPlayFullAnimation = false
        setVisibility(View.VISIBLE)
        if (userInfoModel != null) {
            this.mUseId = userInfoModel.userId
            AvatarUtils.loadAvatarByUrl(mSingAvatarView,
                    AvatarUtils.newParamsBuilder(userInfoModel.avatar)
                            .setCircle(true)
                            .build())
            mTvSingerName?.text = userInfoModel.nicknameRemark
            mLevelBg?.setBackground(U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(userInfoModel.ranking.mainRanking)))
        } else {
            MyLog.w(TAG, "userInfoModel==null 加载选手信息失败")
        }

        // 淡出效果
        if (mEnterAlphaAnimation == null) {
            mEnterAlphaAnimation = AlphaAnimation(0f, 1f)
            mEnterAlphaAnimation!!.duration = 1000
        }
        mParentView!!.startAnimation(mEnterAlphaAnimation)

        mGrabStageView?.visibility = View.VISIBLE
        mGrabStageView?.loops = 1

        SvgaParserAdapter.parse("grab_main_stage.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                mGrabStageView?.loops = -1
                mGrabStageView?.setImageDrawable(drawable)
                mGrabStageView?.startAnimation()
            }

            override fun onError() {

            }
        })
        if (H.isGrabRoom()) {
            val grabRoundInfoModel = H.grabRoomData?.realRoundInfo ?: return
            mCountDownStatus = COUNT_DOWN_STATUS_WAIT
            mCircleCountDownView!!.reset()
            if (!grabRoundInfoModel.isParticipant && grabRoundInfoModel.enterStatus == EQRoundStatus.QRS_SING.value) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                countDown("中途进来")
            } else {
                mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
                mUiHandler!!.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000)
            }
        } else if (H.isMicRoom()) {
            val grabRoundInfoModel = H.micRoomData?.realRoundInfo ?: return
            mCountDownStatus = COUNT_DOWN_STATUS_WAIT
            mCircleCountDownView!!.reset()
            if (!grabRoundInfoModel.isParticipant && grabRoundInfoModel.enterStatus == EMRoundStatus.MRS_SING.value) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                countDown("中途进来")
            } else {
                mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
                mUiHandler!!.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000)
            }
        }else if(H.isRelayRoom()){
            mCircleCountDownView?.reset()
        }

    }

    fun tryStartCountDown() {
        if (mParentView == null || mParentView!!.visibility == View.GONE) {
            return
        }
        MyLog.d(TAG, "tryStartCountDown")
        mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
            countDown("tryStartCountDown")
        }
    }

    private fun countDown(from: String) {
        MyLog.d(TAG, "countDown from=$from")
        if (H.isGrabRoom()) {
            val infoModel = H.grabRoomData?.realRoundInfo ?: return
            val totalMs = infoModel.singTotalMs
            val progress: Int  //当前进度条
            val leaveTime: Int //剩余时间
            MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant + " enterStatus=" + infoModel.enterStatus)
            if (!infoModel.isParticipant && infoModel.enterStatus == EQRoundStatus.QRS_SING.value) {
                MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
                progress = infoModel.elapsedTimeMs * 100 / totalMs
                leaveTime = totalMs - infoModel.elapsedTimeMs
            } else {
                progress = 1
                leaveTime = totalMs
            }
            mCircleCountDownView!!.startPlay(progress, leaveTime, true)
        } else if (H.isMicRoom()) {
            val infoModel = H.micRoomData?.realRoundInfo ?: return
            val totalMs = infoModel.singTotalMs
            val progress: Int  //当前进度条
            val leaveTime: Int //剩余时间
            MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant + " enterStatus=" + infoModel.enterStatus)
            if (!infoModel.isParticipant && infoModel.enterStatus == EMRoundStatus.MRS_SING.value) {
                MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
                progress = infoModel.elapsedTimeMs * 100 / totalMs
                leaveTime = totalMs - infoModel.elapsedTimeMs
            } else {
                progress = 1
                leaveTime = totalMs
            }
            mCircleCountDownView!!.startPlay(progress, leaveTime, true)
        }else if(H.isRelayRoom()){

        }
    }

    fun hide() {
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT
        mHasPlayFullAnimation = false
        mUiHandler!!.removeCallbacksAndMessages(null)
        if (mParentView != null) {
            if (mParentView!!.visibility == View.VISIBLE) {
                if (mLeaveTranslateAnimation == null) {
                    mLeaveTranslateAnimation = TranslateAnimation(0.0f, U.getDisplayUtils().screenWidth.toFloat(), 0.0f, 0.0f)
                    mLeaveTranslateAnimation!!.duration = 200
                }
                mParentView!!.startAnimation(mLeaveTranslateAnimation)
                mLeaveTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        mParentView!!.clearAnimation()
                        setVisibility(View.GONE)
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            } else {
                mParentView!!.clearAnimation()
                setVisibility(View.GONE)
            }
            if (mCircleCountDownView != null) {
                mCircleCountDownView!!.reset()
                mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
            }
        }
    }

    companion object {
        internal val MSG_ENSURE_PLAY = 1

        internal val COUNT_DOWN_STATUS_WAIT = 2
        internal val COUNT_DOWN_STATUS_PLAYING = 3
    }
}
