package com.module.playways.race.room.view

import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.common.anim.svga.SvgaParserAdapter
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.countdown.CircleCountDownView
import com.common.view.ex.ExTextView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.zq.live.proto.Room.EQRoundStatus
import org.greenrobot.eventbus.EventBus

class RaceOtherSingCardView(viewStub: ViewStub, val roomData: RaceRoomData) : ExViewStub(viewStub){

    val TAG = "RaceOtherSingCardView"

    lateinit var raceStageView:SVGAImageView
    lateinit var circleCountDownView:CircleCountDownView
    lateinit var singAvatarView:BaseImageView
    lateinit var descTv:ExTextView

    internal val MSG_ENSURE_PLAY = 1

    private val COUNT_DOWN_STATUS_WAIT = 2
    private val COUNT_DOWN_STATUS_PLAYING = 3
    private var mCountDownStatus = COUNT_DOWN_STATUS_WAIT

    private var mUseId: Int = 0   // 当前唱歌人的id

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

    protected override fun init(parentView: View) {
        raceStageView = parentView.findViewById(R.id.race_stage_view)
        circleCountDownView = parentView.findViewById(R.id.circle_count_down_view)
        singAvatarView = parentView.findViewById(R.id.sing_avatar_view)
        descTv = parentView.findViewById(R.id.desc_tv)

        singAvatarView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mUseId != 0) {
                    EventBus.getDefault().post(ShowPersonCardEvent(mUseId))
                }
            }
        })
    }

    protected override fun layoutDesc(): Int {
        return R.layout.race_other_sing_card_stub_layout
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
        val infoModel = roomData.realRoundInfo
        val userInfoModel = roomData.getUserInfo(infoModel.userID)
        mUiHandler!!.removeCallbacksAndMessages(null)
        mHasPlayFullAnimation = false
        visibility = View.VISIBLE
        if (userInfoModel != null) {
            this.mUseId = userInfoModel.userId
            AvatarUtils.loadAvatarByUrl(singAvatarView,
                    AvatarUtils.newParamsBuilder(userInfoModel.avatar)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(3f).toFloat())
                            .setCircle(true)
                            .build())
            // todo 得改成歌得名字
            descTv.text = userInfoModel.nicknameRemark
        } else {
            MyLog.w(TAG, "userInfoModel==null 加载选手信息失败")
        }

        // 淡出效果
        if (mEnterAlphaAnimation == null) {
            mEnterAlphaAnimation = AlphaAnimation(0f, 1f)
            mEnterAlphaAnimation!!.duration = 1000
        }
        mParentView.startAnimation(mEnterAlphaAnimation)

        raceStageView.visibility = View.VISIBLE
        raceStageView.loops = 1

        SvgaParserAdapter.parse("grab_main_stage.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                raceStageView.loops = -1
                raceStageView.setImageDrawable(drawable)
                raceStageView.startAnimation()
            }

            override fun onError() {

            }
        })

        //todo 等接口再来补全逻辑
//        val grabRoundInfoModel = mGrabRoomData.getRealRoundInfo<GrabRoundInfoModel>() ?: return
//        mCountDownStatus = COUNT_DOWN_STATUS_WAIT
//        mCircleCountDownView!!.cancelAnim()
//        mCircleCountDownView!!.max = 360
//        mCircleCountDownView!!.progress = 0
//        if (!grabRoundInfoModel.isParticipant && grabRoundInfoModel.enterStatus == EQRoundStatus.QRS_SING.value) {
//            mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
//            countDown("中途进来")
//        } else {
//            mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
//            mUiHandler!!.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000)
//        }
    }

    fun tryStartCountDown() {
        if (mParentView == null || mParentView.getVisibility() == View.GONE) {
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
        //todo 等接口补全倒计时
//        val infoModel = mGrabRoomData.getRealRoundInfo<GrabRoundInfoModel>() ?: return
//        val totalMs = infoModel.singTotalMs
//        val progress: Int  //当前进度条
//        val leaveTime: Int //剩余时间
//        MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant + " enterStatus=" + infoModel.enterStatus)
//        if (!infoModel.isParticipant && infoModel.enterStatus == EQRoundStatus.QRS_SING.value) {
//            MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
//            progress = infoModel.elapsedTimeMs * 100 / totalMs
//            leaveTime = totalMs - infoModel.elapsedTimeMs
//        } else {
//            progress = 1
//            leaveTime = totalMs
//        }
//        mCircleCountDownView!!.go(progress, leaveTime)
    }

    fun hide() {
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT
        mHasPlayFullAnimation = false
        mUiHandler!!.removeCallbacksAndMessages(null)
        if (mParentView != null) {
            if (mParentView.visibility == View.VISIBLE) {
                if (mLeaveTranslateAnimation == null) {
                    mLeaveTranslateAnimation = TranslateAnimation(0.0f, U.getDisplayUtils().screenWidth.toFloat(), 0.0f, 0.0f)
                    mLeaveTranslateAnimation!!.duration = 200
                }
                mParentView.startAnimation(mLeaveTranslateAnimation)
                mLeaveTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        mParentView.clearAnimation()
                        visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            } else {
                mParentView.clearAnimation()
                visibility = View.GONE
            }
            if (circleCountDownView != null) {
                circleCountDownView.cancelAnim()
                circleCountDownView.max = 360
                circleCountDownView.progress = 0

                mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
            }
        }
    }
}