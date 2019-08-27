package com.module.playways.race.match.fragment

import android.animation.AnimatorSet
import android.os.Bundle
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.anim.svga.SvgaParserAdapter
import com.common.base.BaseFragment
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.utils.ActivityUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.manager.BgMusicManager
import com.dialog.view.TipsDialogView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.grab.prepare.GrabMatchSuccessFragment
import com.module.playways.race.match.IRaceMatchingView
import com.module.playways.race.match.RaceMatchPresenter
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.ESex
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.greendao.annotation.NotNull
import java.util.*

//这个是匹配界面，之前的FastMatchingSence
class RaceMatchFragment : BaseFragment(), IRaceMatchingView {

    val mTag = "RaceMatchFragment"
    internal var mIvBack: ExImageView? = null
    internal var mIvTop: ExImageView? = null
    internal var mTvMatchedTime: ExTextView? = null
    //    internal var mTvTip: ExTextView? = null
    internal var mIvCancelMatch: ExTextView? = null

    internal var mIconAnimatorSet: AnimatorSet? = null

    internal var mMatchPresenter: RaceMatchPresenter? = null

    internal var mQuotationsArray: List<String>? = null

    internal var mMatchTimeTask: HandlerTaskTimer? = null

    internal var mSdvOwnIcon: SimpleDraweeView? = null

    internal var mRlIcon1Root: ExRelativeLayout? = null

    internal var mSvgaMatchBg: SVGAImageView? = null

    internal var mExitDialog: DialogPlus? = null

    private var mControlTask: HandlerTaskTimer? = null

    override fun initView(): Int {
        return R.layout.grab_match_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mIvBack = rootView.findViewById(R.id.iv_back)
        mIvTop = rootView.findViewById(R.id.iv_top)
        mTvMatchedTime = rootView.findViewById(R.id.tv_matched_time)
        mIvCancelMatch = rootView.findViewById(R.id.iv_cancel_match)
        mSdvOwnIcon = rootView.findViewById(R.id.sdv_own_icon)
        mRlIcon1Root = rootView.findViewById(R.id.rl_icon1_root)
        mSvgaMatchBg = rootView.findViewById(R.id.svga_match_bg)

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back, R.raw.normal_click)
        U.getSoundUtils().preLoad(GrabMatchSuccessFragment.TAG, R.raw.rank_matchpeople, R.raw.rank_matchready, R.raw.normal_countdown)

        AvatarUtils.loadAvatarByUrl(mSdvOwnIcon,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().avatar)
                        .setCircle(true)
                        .setGray(false)
                        .setBorderWidth(U.getDisplayUtils().dip2px(6f).toFloat())
                        .setBorderColor(U.getColor(R.color.white))
                        .build())

        val drawable = DrawableCreator.Builder().setCornersRadius(U.getDisplayUtils().dip2px(45f).toFloat())
                .setStrokeWidth(U.getDisplayUtils().dip2px(3f).toFloat())
                .setStrokeColor(if (MyUserInfoManager.getInstance().sex == ESex.SX_MALE.value) U.getColor(R.color.color_man_stroke_color) else U.getColor(R.color.color_woman_stroke_color))
                .setSolidColor(if (MyUserInfoManager.getInstance().sex == ESex.SX_MALE.value) U.getColor(R.color.color_man_stroke_color_trans_20) else U.getColor(R.color.color_woman_stroke_color_trans_20))
                .build()

        mRlIcon1Root?.background = drawable

        val res = resources
        mQuotationsArray = Arrays.asList(*res.getStringArray(R.array.match_quotations))

        mIvCancelMatch?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                goBack()
            }
        })

        mIvBack?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                goBack()
            }
        })

        mMatchPresenter = RaceMatchPresenter(this)
        addPresent(mMatchPresenter)
        mMatchPresenter?.startLoopMatchTask()

        startTimeTask()
//        startMatchQuotationTask()

        showBackground()
        playBackgroundMusic()
    }

    fun showBackground() {
        mSvgaMatchBg!!.visibility = View.VISIBLE
        mSvgaMatchBg!!.loops = 1

        SvgaParserAdapter.parse("grab_matching.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(@NotNull videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                mSvgaMatchBg!!.loops = -1
                mSvgaMatchBg!!.setImageDrawable(drawable)
                mSvgaMatchBg!!.startAnimation()
            }

            override fun onError() {

            }
        })
    }

//    private fun startMatchQuotationTask() {
//        mControlTask = HandlerTaskTimer.newBuilder().delay(1000)
//                .interval(ANIMATION_DURATION * 2 + 300)
//                .start(object : HandlerTaskTimer.ObserverW() {
//                    override fun onNext(integer: Int) {
//                        changeQuotation(integer)
//                    }
//                })
//    }

//    private fun changeQuotation(integer: Int?) {
//        val size = mQuotationsArray?.size
//        if (integer!! % size!! == 0) {
//            Collections.shuffle(mQuotationsArray)
//        }
//        val index = integer % (size - 1)
//        var string = mQuotationsArray!![index]
//        var rString = ""
//
//        while (string.length > 15) {
//            rString = rString + string.substring(0, 15) + "\n"
//            string = string.substring(15)
//        }
//
//        rString = rString + string
//        mTvTip?.text = rString
//    }

    /**
     * 更新已匹配时间
     */
    fun startTimeTask() {
        mMatchTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(-1)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(integer: Int) {
                        if (integer == 61) {
                            U.getToastUtil().showShort("现在小伙伴有点少，稍后再匹配试试吧～")
                            mMatchPresenter?.cancelMatch()
                            stopTimeTask()
                            BgMusicManager.getInstance().destory()
                            if (activity != null) {
                                activity!!.finish()
                            }
                            return
                        }

                        mTvMatchedTime?.text = String.format(U.app().getString(R.string.match_time_info), integer)
                    }
                })
    }

    fun stopTimeTask() {
        if (mMatchTimeTask != null) {
            mMatchTimeTask!!.dispose()
        }
    }

    override fun useEventBus(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
        if (event.foreground) {
            playBackgroundMusic()
        } else {
            BgMusicManager.getInstance().destory()
        }

    }

    override fun setData(type: Int, data: Any?) {
        super.setData(type, data)
        if (type == 0) {
            //            mPrepareData = (PrepareData) data;
        }
    }

    override fun destroy() {
        super.destroy()
        if (mExitDialog != null && mExitDialog!!.isShowing) {
            mExitDialog!!.dismiss()
        }
        stopTimeTask()
        if (mControlTask != null) {
            mControlTask!!.dispose()
        }
        if (mSvgaMatchBg != null) {
            mSvgaMatchBg!!.callback = null
            mSvgaMatchBg!!.stopAnimation(true)
        }
        U.getSoundUtils().release(TAG)
    }

    override fun onDetach() {
        super.onDetach()
        mMatchPresenter?.destroy()
        stopTimeTask()

        if (mControlTask != null) {
            mControlTask!!.dispose()
        }

        if (mIconAnimatorSet != null && mIconAnimatorSet!!.isRunning) {
            mIconAnimatorSet!!.cancel()
        }
    }

    internal fun goBack() {
        val tipsDialogView = TipsDialogView.Builder(context)
                .setMessageTip("马上要为你匹配到对手了\n还要退出吗？")
                .setCancelTip("退出")
                .setConfirmTip("继续匹配")
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        if (mExitDialog != null) {
                            mExitDialog!!.dismiss()
                        }

                        U.getSoundUtils().release(GrabMatchSuccessFragment.TAG)
                        mMatchPresenter?.cancelMatch()
                        BgMusicManager.getInstance().destory()
                        stopTimeTask()
                        if (activity != null) {
                            activity!!.finish()
                        }
                    }
                })
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        // 继续匹配
                        if (mExitDialog != null) {
                            mExitDialog!!.dismiss()
                        }
                    }
                })
                .build()

        mExitDialog = DialogPlus.newDialog(context!!)
                .setContentHolder(ViewHolder(tipsDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .create()
        mExitDialog!!.show()

    }

    //竞赛
    override fun matchRaceSucess(joinRaceRoomRspModel: JoinRaceRoomRspModel) {
        MyLog.d(TAG, "matchSucess event=$joinRaceRoomRspModel")
        BgMusicManager.getInstance().destory()
        stopTimeTask()

        //先跳转
        ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_ROOM)
                .withSerializable("JoinRaceRoomRspModel", joinRaceRoomRspModel)
                .navigation()

        //结束当前Activity
        if (activity != null) {
            activity!!.finish()
        }
    }

    override fun onBackPressed(): Boolean {
        goBack()
        return true
    }

    override fun notifyToShow() {
        MyLog.d(TAG, "toStaskTop")
        playBackgroundMusic()
        rootView.visibility = View.VISIBLE
    }

    private fun playBackgroundMusic() {
        //        if (!BgMusicManager.getInstance().isPlaying() && mPrepareData != null && RaceMatchFragment.this.getFragmentVisible()) {
        //            if (!TextUtils.isEmpty(mPrepareData.getBgMusic())) {
        //                BgMusicManager.getInstance().starPlay(mPrepareData.getBgMusic(), 0, "GrabMatchFragment1");
        //            } else {
        //                GrabSongApi grabSongApi = ApiManager.getInstance().createService(GrabSongApi.class);
        //                ApiMethods.subscribe(grabSongApi.getSepcialBgVoice(), new ApiObserver<ApiResult>() {
        //                    @Override
        //                    public void process(ApiResult result) {
        //                        if (result.getErrno() == 0) {
        //                            List<String> musicURLs = JSON.parseArray(result.getData().getString("musicURL"), String.class);
        //                            if (musicURLs != null && !musicURLs.isEmpty()) {
        //                                mPrepareData.setBgMusic(musicURLs.get(0));
        //                                BgMusicManager.getInstance().starPlay(mPrepareData.getBgMusic(), 0, "GrabMatchFragment2");
        //                            }
        //                        } else {
        //
        //                        }
        //                    }
        //
        //                    @Override
        //                    public void onNetworkError(ErrorType errorType) {
        //                        super.onNetworkError(errorType);
        //                    }
        //                }, this);
        //            }
        //        }
    }

    /**
     * MatchSuccessFragment add后，动画播放完再remove掉匹配中页面
     */
    override fun notifyToHide() {
        if (mExitDialog != null && mExitDialog!!.isShowing) {
            mExitDialog!!.dismiss(false)
        }
        rootView.visibility = View.GONE
    }

    companion object {

        val ANIMATION_DURATION: Long = 1800
    }
}
