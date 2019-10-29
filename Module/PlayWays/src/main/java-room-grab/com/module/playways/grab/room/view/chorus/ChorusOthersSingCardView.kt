package com.module.playways.grab.room.view.chorus

import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout

import com.common.anim.svga.SvgaParserAdapter
import com.common.core.account.UserAccountManager
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.engine.EngineEvent
import com.engine.UserStatus
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.event.GrabChorusUserStatusChangeEvent
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.grab.room.model.ChorusRoundInfoModel
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.common.view.ExViewStub
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.room.data.H
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 别人唱歌是，自己看到的板子
 */
class ChorusOthersSingCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

    val TAG = "ChorusOthersSingCardView"

    internal var mCountDownStatus = COUNT_DOWN_STATUS_WAIT

    internal var mLeftSingSvga: SVGAImageView? = null
    internal var mRightSingSvga: SVGAImageView? = null
    internal var mChorusOtherArea: LinearLayout? = null

    internal var mLeftIv: SimpleDraweeView? = null
    internal var mLeftStatusArea: ExRelativeLayout? = null
    internal var mLeftStatus: ExTextView? = null
    internal var mLeftName: ExTextView? = null

    internal var mRightIv: SimpleDraweeView? = null
    internal var mRightStatusArea: ExRelativeLayout? = null
    internal var mRightStatus: ExTextView? = null
    internal var mRightName: ExTextView? = null

    internal var mSingCountDownView: SingCountDownView2? = null

    internal var mEnterTranslateAnimation: TranslateAnimation? = null // 飞入的进场动画
    internal var mLeaveTranslateAnimation: TranslateAnimation? = null // 飞出的离场动画

    internal var mHasPlayFullAnimation = false

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_ENSURE_PLAY) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                countDown("handleMessage")
            } else if (msg.what == MSG_LEFT_SPEAK_OVER) {
                stopSingAnimation(mLeftSingSvga)
            } else if (msg.what == MSG_RIGHT_SPEAK_OVER) {
                stopSingAnimation(mRightSingSvga)
            }
        }
    }
    internal var mLeftUserInfoModel: UserInfoModel? = null
    internal var mRightUserInfoModel: UserInfoModel? = null
    internal var mLeftChorusRoundInfoModel: ChorusRoundInfoModel? = null
    internal var mRightChorusRoundInfoModel: ChorusRoundInfoModel? = null

    override fun init(parentView: View) {
        mChorusOtherArea = mParentView!!.findViewById<View>(R.id.chorus_other_area) as LinearLayout
        mLeftSingSvga = mParentView!!.findViewById<View>(R.id.left_sing_svga) as SVGAImageView
        mRightSingSvga = mParentView!!.findViewById<View>(R.id.right_sing_svga) as SVGAImageView

        mLeftStatusArea = mParentView!!.findViewById<View>(R.id.left_status_area) as ExRelativeLayout
        mLeftIv = mParentView!!.findViewById<View>(R.id.left_iv) as SimpleDraweeView
        mLeftStatus = mParentView!!.findViewById<View>(R.id.left_status) as ExTextView
        mLeftName = mParentView!!.findViewById<View>(R.id.left_name) as ExTextView

        mRightStatusArea = mParentView!!.findViewById<View>(R.id.right_status_area) as ExRelativeLayout
        mRightIv = mParentView!!.findViewById<View>(R.id.right_iv) as SimpleDraweeView
        mRightStatus = mParentView!!.findViewById<View>(R.id.right_status) as ExTextView
        mRightName = mParentView!!.findViewById<View>(R.id.right_name) as ExTextView

        mSingCountDownView = mParentView!!.findViewById(R.id.sing_count_down_view)

        val offsetX = (U.getDisplayUtils().screenWidth / 2 - U.getDisplayUtils().dip2px(16f)) / 2
        mLeftSingSvga!!.translationX = (-offsetX).toFloat()
        mRightSingSvga!!.translationX = offsetX.toFloat()

        mLeftIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mLeftUserInfoModel != null) {
                    EventBus.getDefault().post(ShowPersonCardEvent(mLeftUserInfoModel!!.userId))
                }
            }
        })

        mRightIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mRightUserInfoModel != null) {
                    EventBus.getDefault().post(ShowPersonCardEvent(mRightUserInfoModel!!.userId))
                }
            }
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_chorus_other_sing_card_stub_layout
    }

    override fun onViewAttachedToWindow(v: View) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation!!.setAnimationListener(null)
            mEnterTranslateAnimation!!.cancel()
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation!!.setAnimationListener(null)
            mLeaveTranslateAnimation!!.cancel()
        }
        if (mLeftSingSvga != null) {
            mLeftSingSvga!!.callback = null
            mRightSingSvga!!.stopAnimation(true)
        }
        if (mRightSingSvga != null) {
            mRightSingSvga!!.callback = null
            mRightSingSvga!!.stopAnimation(true)
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }

        mUiHandler.removeCallbacksAndMessages(null)
    }

    fun bindData() {
        tryInflate()
        mLeftStatus?.visibility = View.GONE
        mRightStatus?.visibility = View.GONE
        mLeftStatusArea?.visibility = View.GONE
        mRightStatusArea?.visibility = View.GONE
        mLeftChorusRoundInfoModel = null
        mRightChorusRoundInfoModel = null
        mLeftUserInfoModel = null
        mRightUserInfoModel = null

        if(H.isGrabRoom()){
            val now = H.grabRoomData?.realRoundInfo ?: return
            val list = now.chorusRoundInfoModels
            if (list != null && list.size >= 2) {
                mLeftChorusRoundInfoModel = list[0]
                mRightChorusRoundInfoModel = list[1]
                mLeftUserInfoModel =  H.grabRoomData?.getPlayerOrWaiterInfo(mLeftChorusRoundInfoModel!!.userID)
                mRightUserInfoModel =  H.grabRoomData?.getPlayerOrWaiterInfo(mRightChorusRoundInfoModel!!.userID)
            }
        }else if(H.isMicRoom()){
            val now = H.micRoomData?.realRoundInfo ?: return
            val list = now.chorusRoundInfoModels
            if (list != null && list.size >= 2) {
                mLeftChorusRoundInfoModel = list[0]
                mRightChorusRoundInfoModel = list[1]
                mLeftUserInfoModel =  H.micRoomData?.getPlayerOrWaiterInfo(mLeftChorusRoundInfoModel!!.userID)
                mRightUserInfoModel =  H.micRoomData?.getPlayerOrWaiterInfo(mRightChorusRoundInfoModel!!.userID)
            }
        }

        if (mLeftUserInfoModel != null && mRightUserInfoModel != null && mLeftChorusRoundInfoModel != null && mRightChorusRoundInfoModel != null) {
            mHasPlayFullAnimation = false
            mUiHandler.removeCallbacksAndMessages(null)
            mParentView!!.visibility = View.VISIBLE
            AvatarUtils.loadAvatarByUrl(mLeftIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel!!.avatar)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())
            mLeftName?.text = mLeftUserInfoModel!!.nicknameRemark

            AvatarUtils.loadAvatarByUrl(mRightIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel!!.avatar)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())
            mRightName?.text = mRightUserInfoModel!!.nicknameRemark

            setShowFlag(mLeftChorusRoundInfoModel!!, mLeftStatusArea, mLeftStatus)
            setShowFlag(mRightChorusRoundInfoModel!!, mRightStatusArea, mRightStatus)
            animationGo()

            mCountDownStatus = COUNT_DOWN_STATUS_WAIT
            mSingCountDownView?.reset()

            if(H.isGrabRoom()){
                val grabRoundInfoModel = H.grabRoomData?.realRoundInfo ?: return

                if (!grabRoundInfoModel.isParticipant && grabRoundInfoModel.isEnterInSingStatus) {
                    mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                    countDown("中途进来,直接播放")
                } else {
                    mSingCountDownView?.startPlay(0, grabRoundInfoModel.singTotalMs, false)
                    mUiHandler.removeMessages(MSG_ENSURE_PLAY)
                    mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000)
                }
            }else if(H.isMicRoom()){
                val grabRoundInfoModel = H.micRoomData?.realRoundInfo ?: return

                if (!grabRoundInfoModel.isParticipant && grabRoundInfoModel.isEnterInSingStatus) {
                    mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                    countDown("中途进来,直接播放")
                } else {
                    mSingCountDownView?.startPlay(0, grabRoundInfoModel.singTotalMs, false)
                    mUiHandler.removeMessages(MSG_ENSURE_PLAY)
                    mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000)
                }
            }
        }
    }

    private fun setShowFlag(chorusRoundInfoModel: ChorusRoundInfoModel?, relativeLayout: ExRelativeLayout?, textView: ExTextView?) {
        if (chorusRoundInfoModel?.isHasGiveUp == true) {
            textView?.visibility = View.VISIBLE
            relativeLayout?.visibility = View.VISIBLE
            textView?.text = "不唱了"
        } else if (chorusRoundInfoModel?.isHasExit == true) {
            textView?.visibility = View.VISIBLE
            relativeLayout?.visibility = View.VISIBLE
            textView?.text = "退出了"
        } else {
            textView?.visibility = View.GONE
            relativeLayout?.visibility = View.GONE
        }
    }


    // 播放声纹动画
    private fun playSingAnimation(svgaImageView: SVGAImageView?) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation svgaImageView=$svgaImageView")
            return
        }

        if (svgaImageView != null && svgaImageView.isAnimating) {
            // 正在播放
            return
        }

        svgaImageView.visibility = View.VISIBLE
        svgaImageView.loops = -1

        SvgaParserAdapter.parse("grab_main_stage.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                svgaImageView.setImageDrawable(drawable)
                svgaImageView.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    // 停止声纹动画
    private fun stopSingAnimation(uid: Int) {
        if (uid == mLeftUserInfoModel!!.userId) {
            stopSingAnimation(mLeftSingSvga)
        } else if (uid == mRightUserInfoModel!!.userId) {
            stopSingAnimation(mRightSingSvga)
        } else {
            MyLog.w(TAG, "stopSingAnimation uid=$uid")
        }
    }

    private fun stopSingAnimation(svgaImageView: SVGAImageView?) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "stopSingAnimation svgaImageView=$svgaImageView")
            return
        }

        svgaImageView.callback = null
        svgaImageView.stopAnimation(true)
        svgaImageView.visibility = View.GONE
    }

    fun tryStartCountDown() {
        if (mParentView == null || mParentView!!.visibility == View.GONE) {
            return
        }
        MyLog.d(TAG, "tryStartCountDown")
        mUiHandler.removeMessages(MSG_ENSURE_PLAY)
        if (mCountDownStatus == COUNT_DOWN_STATUS_WAIT) {
            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
            countDown("tryStartCountDown")
        }
    }

    private fun countDown(from: String) {
        MyLog.d(TAG, "countDown from=$from")
        if(H.isGrabRoom()){
            val infoModel = H.grabRoomData?.realRoundInfo ?: return
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
            mSingCountDownView?.startPlay(progress, leaveTime, true)
        }else if(H.isMicRoom()){
            val infoModel = H.micRoomData?.realRoundInfo ?: return
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
            mSingCountDownView?.startPlay(progress, leaveTime, true)
        }
    }

    private fun animationGo() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation((-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f, 0.0f)
            mEnterTranslateAnimation!!.duration = 200
        }
        mParentView!!.startAnimation(mEnterTranslateAnimation)
    }

    /**
     * 离场动画
     */
    fun hide() {
        if (mParentView != null) {
            if (mParentView!!.visibility == View.VISIBLE) {
                if (mLeaveTranslateAnimation == null) {
                    mLeaveTranslateAnimation = TranslateAnimation(0.0f, U.getDisplayUtils().screenWidth.toFloat(), 0.0f, 0.0f)
                    mLeaveTranslateAnimation!!.duration = 200
                }
                mLeaveTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        mParentView!!.clearAnimation()
                        mParentView!!.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                mParentView!!.startAnimation(mLeaveTranslateAnimation)
            } else {
                mParentView!!.clearAnimation()
                mParentView!!.visibility = View.GONE
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabChorusUserStatusChangeEvent) {
        if (mParentView == null || mParentView!!.visibility == View.GONE) {
            return
        }

        if (event.mChorusRoundInfoModel != null) {
            stopSingAnimation(event.mChorusRoundInfoModel.userID)
            if (mLeftUserInfoModel != null && event.mChorusRoundInfoModel.userID == mLeftUserInfoModel!!.userId) {
                setShowFlag(event.mChorusRoundInfoModel, mLeftStatusArea, mLeftStatus)
            } else if (mRightUserInfoModel != null && event.mChorusRoundInfoModel.userID == mRightUserInfoModel!!.userId) {
                setShowFlag(event.mChorusRoundInfoModel, mRightStatusArea, mRightStatus)
            } else {
                MyLog.w(TAG, "onEvent不是麦上的人？？？ event=$event")
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (mParentView == null || mParentView!!.visibility == View.GONE) {
            return
        }
        when (event.getType()) {
            EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION -> {
                // 有人在说话,播2秒动画
                val l = event.getObj<List<EngineEvent.UserVolumeInfo>>()
                for (u in l) {
                    var uid = u.uid
                    if (uid == 0) {
                        uid = UserAccountManager.uuidAsLong.toInt()
                    }
                    if (u.volume > 30) {
                        if (mLeftUserInfoModel != null && mLeftUserInfoModel!!.userId == uid) {
                            mUiHandler.removeMessages(MSG_LEFT_SPEAK_OVER)
                            mUiHandler.sendEmptyMessageDelayed(MSG_LEFT_SPEAK_OVER, 2000)
                            playSingAnimation(mLeftSingSvga)
                        } else if (mRightUserInfoModel != null && mRightUserInfoModel!!.userId == uid) {
                            mUiHandler.removeMessages(MSG_RIGHT_SPEAK_OVER)
                            mUiHandler.sendEmptyMessageDelayed(MSG_RIGHT_SPEAK_OVER, 2000)
                            playSingAnimation(mRightSingSvga)
                        } else {
                            MyLog.w(TAG, "onEvent 不是唱歌两人说话 event=$event")
                        }
                    }
                }
            }
            EngineEvent.TYPE_USER_MUTE_AUDIO -> {
                //用户闭麦，开麦
                val userStatus = event.getUserStatus()
                if (userStatus != null) {
                    val userId = userStatus.userId
                    if (userStatus.isAudioMute) {
                        stopSingAnimation(userId)
                    }
                }
            }
        }
    }

    companion object {
        internal val MSG_ENSURE_PLAY = 1
        internal val MSG_LEFT_SPEAK_OVER = 2
        internal val MSG_RIGHT_SPEAK_OVER = 3

        internal val COUNT_DOWN_STATUS_WAIT = 2
        internal val COUNT_DOWN_STATUS_PLAYING = 3
    }

}
