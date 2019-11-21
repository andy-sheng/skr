package com.module.playways.race.room.view

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
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.component.level.utils.LevelConfigUtils
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.event.RaceBlightByMeEvent
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceOtherSingCardView(viewStub: ViewStub, val roomData: RaceRoomData) : ExViewStub(viewStub) {

    val TAG = "RaceOtherSingCardView"

    lateinit var raceStageView: SVGAImageView
    lateinit var singCountDownView: SingCountDownView2
    lateinit var levelBg: ImageView
    lateinit var singAvatarView: BaseImageView
    lateinit var descTv: ExTextView

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
        singCountDownView = parentView.findViewById(R.id.sing_count_down_view)
        levelBg = parentView.findViewById(R.id.level_bg)
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
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceBlightByMeEvent) {
        // 重新刷一下UI
        val infoModel = roomData.realRoundInfo
        val userInfoModel = roomData.getPlayerOrWaiterInfo(infoModel?.getSingerIdNow())
        AvatarUtils.loadAvatarByUrl(singAvatarView,
                AvatarUtils.newParamsBuilder(RoomDataUtils.getRaceDisplayAvatar(roomData, userInfoModel))
                        .setBorderColor(U.getColor(R.color.white))
                        .setSizeType(ImageUtils.SIZE.SIZE_320)
                        .setBorderWidth(U.getDisplayUtils().dip2px(3f).toFloat())
                        .setCircle(true)
                        .build())
    }

    fun bindData() {
        tryInflate()
        val infoModel = roomData.realRoundInfo
        val userInfoModel = roomData.getPlayerOrWaiterInfo(infoModel?.getSingerIdNow())
        mParentView?.clearAnimation()
        mUiHandler!!.removeCallbacksAndMessages(null)
        mHasPlayFullAnimation = false
        setVisibility(View.VISIBLE)
        descTv.text = "《${infoModel?.getSongModelNow()?.displaySongName}》"
        if (userInfoModel != null) {
            this.mUseId = userInfoModel.userId
            AvatarUtils.loadAvatarByUrl(singAvatarView,
                    AvatarUtils.newParamsBuilder(RoomDataUtils.getRaceDisplayAvatar(roomData, userInfoModel))
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(3f).toFloat())
                            .setCircle(true)
                            .build())
            if (LevelConfigUtils.getRaceCenterAvatarBg(userInfoModel.ranking.mainRanking) != 0) {
                levelBg.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(userInfoModel.ranking.mainRanking))
            }
        } else {
            MyLog.w(TAG, "userInfoModel==null 加载选手信息失败")
        }

        // 淡出效果
        if (mEnterAlphaAnimation == null) {
            mEnterAlphaAnimation = AlphaAnimation(0f, 1f)
            mEnterAlphaAnimation!!.duration = 1000
        }
        mParentView?.startAnimation(mEnterAlphaAnimation)

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

        roomData.realRoundInfo?.let {
            mCountDownStatus = COUNT_DOWN_STATUS_WAIT
            singCountDownView.reset()
            singCountDownView.setBackColor(U.getColor(R.color.black_trans_30))
            singCountDownView.setSongName(roomData.realRoundInfo?.getSongModelNow()?.itemName)
            if (it.enterStatus == ERaceRoundStatus.ERRS_ONGOINE.value && it.enterSubRoundSeq == it.subRoundSeq) {
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                countDown("中途进来")
            } else {
                singCountDownView.startPlay(0, it.getSingTotalMs(), false)
                mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
                mUiHandler!!.sendEmptyMessageDelayed(MSG_ENSURE_PLAY, 1000)
            }
        }
    }

    fun tryStartCountDown() {
        if (mParentView == null || mParentView?.visibility == View.GONE) {
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
        roomData.realRoundInfo?.let {
            val totalMs = it.getSingTotalMs()
            val progress: Int  //当前进度条
            val leaveTime: Int //剩余时间
            if (it.enterStatus == ERaceRoundStatus.ERRS_ONGOINE.value && it.enterSubRoundSeq == it.subRoundSeq) {
                MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
                progress = it.elapsedTimeMs * 100 / totalMs
                leaveTime = totalMs - it.elapsedTimeMs
            } else {
                progress = 1
                leaveTime = totalMs
            }
            singCountDownView.startPlay(progress, leaveTime, true)
        }
    }

    fun hide() {
        mCountDownStatus = COUNT_DOWN_STATUS_WAIT
        mHasPlayFullAnimation = false
        mUiHandler!!.removeCallbacksAndMessages(null)
        if (mParentView != null) {
            if (mParentView?.visibility == View.VISIBLE) {
                if (mLeaveTranslateAnimation == null) {
                    mLeaveTranslateAnimation = TranslateAnimation(0.0f, U.getDisplayUtils().screenWidth.toFloat(), 0.0f, 0.0f)
                    mLeaveTranslateAnimation!!.duration = 200
                }
                mParentView?.startAnimation(mLeaveTranslateAnimation)
                mLeaveTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        mParentView?.clearAnimation()
                        setVisibility(View.GONE)
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            } else {
                mParentView?.clearAnimation()
                setVisibility(View.GONE)
            }
            mUiHandler!!.removeMessages(MSG_ENSURE_PLAY)
        }
    }
}