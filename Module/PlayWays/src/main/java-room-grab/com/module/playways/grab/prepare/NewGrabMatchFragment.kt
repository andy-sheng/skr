package com.module.playways.grab.prepare

import android.animation.AnimatorSet
import android.content.res.Resources
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View

import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.anim.svga.SvgaParserAdapter
import com.common.base.BaseFragment
import com.common.base.FragmentDataListener
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.ActivityUtils
import com.common.utils.FragmentUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.constans.GameModeType
import com.component.busilib.friends.GrabSongApi
import com.component.busilib.manager.BgMusicManager
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.playways.grab.prepare.presenter.RankMatchPresenter
import com.module.playways.room.msg.event.JoinActionEvent
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel
import com.module.playways.room.prepare.model.PrepareData
import com.module.playways.room.prepare.presenter.BaseMatchPresenter
import com.module.playways.room.prepare.presenter.GrabMatchPresenter
import com.module.playways.room.prepare.view.IGrabMatchingView
import com.module.playways.room.prepare.view.IRankMatchingView
import com.module.playways.R
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.greendao.annotation.NotNull

import java.util.Arrays
import java.util.Collections

//这个是匹配界面，之前的FastMatchingSence
class NewGrabMatchFragment : BaseFragment(), IGrabMatchingView, IRankMatchingView {

    val ANIMATION_DURATION: Long = 1800
    lateinit var mTvMatchedTime: ExTextView
    lateinit var mTvTip: ExTextView
    lateinit var mIvCancelMatch: ExTextView
    lateinit var mMatchPresenter: BaseMatchPresenter
    lateinit var mQuotationsArray: List<String>

    internal var mIconAnimatorSet: AnimatorSet? = null
    internal var mPrepareData: PrepareData? = null
    internal var mMatchTimeTask: HandlerTaskTimer? = null

    internal var mSvgaMatchBg: SVGAImageView? = null

    internal var mExitDialog: DialogPlus? = null

    private var mControlTask: HandlerTaskTimer? = null

    override fun initView(): Int {
        return R.layout.new_grab_match_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTvMatchedTime = rootView.findViewById(R.id.tv_matched_time)
        mTvTip = rootView.findViewById(R.id.tv_tip)
        mIvCancelMatch = rootView.findViewById(R.id.iv_cancel_match)

        mSvgaMatchBg = rootView.findViewById(R.id.svga_match_bg)

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back, R.raw.normal_click)
        U.getSoundUtils().preLoad(GrabMatchSuccessFragment.TAG, R.raw.rank_matchpeople, R.raw.rank_matchready, R.raw.normal_countdown)

        val res = resources
        mQuotationsArray = Arrays.asList(*res.getStringArray(R.array.match_quotations))

        mIvCancelMatch.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                goBack()
            }
        })

        if (mPrepareData!!.gameType == GameModeType.GAME_MODE_CLASSIC_RANK) {
            mMatchPresenter = RankMatchPresenter(this)
            addPresent(mMatchPresenter)
            mMatchPresenter.startLoopMatchTask(mPrepareData!!.songModel.itemID, mPrepareData!!.gameType)
        } else if (mPrepareData!!.gameType == GameModeType.GAME_MODE_GRAB) {
            mMatchPresenter = GrabMatchPresenter(this, mPrepareData!!.isNewUser)
            addPresent(mMatchPresenter)
            mMatchPresenter.startLoopMatchTask(mPrepareData!!.tagId, mPrepareData!!.gameType)
        }

        startTimeTask()
        startMatchQuotationTask()

        showBackground()
        playBackgroundMusic()
    }

    fun showBackground() {
        mSvgaMatchBg?.visibility = View.VISIBLE
        mSvgaMatchBg?.loops = 1

        SvgaParserAdapter.parse("matching.svga", object : SVGAParser.ParseCompletion {
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

    private fun startMatchQuotationTask() {
        mControlTask = HandlerTaskTimer.newBuilder()
                .delay(1000)
                .interval(ANIMATION_DURATION * 2 + 300)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        changeQuotation(t)
                    }
                })
    }

    private fun changeQuotation(integer: Int?) {
        val size = mQuotationsArray.size
        if (integer!! % size == 0) {
            Collections.shuffle(mQuotationsArray)
        }
        val index = integer % (size - 1)
        var string = mQuotationsArray[index]
        var rString = ""

        while (string.length > 15) {
            rString = rString + string.substring(0, 15) + "\n"
            string = string.substring(15)
        }

        rString = rString + string
        mTvTip.text = rString
    }

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
                            mMatchPresenter.cancelMatch()
                            stopTimeTask()
                            if (mPrepareData!!.gameType == GameModeType.GAME_MODE_GRAB) {
                                BgMusicManager.getInstance().destory()
                            }
                            if (activity != null) {
                                activity!!.finish()
                            }

                            if (mPrepareData!!.gameType == GameModeType.GAME_MODE_CLASSIC_RANK) {
                                ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                                        .withInt("key_game_type", mPrepareData!!.gameType)
                                        .withBoolean("selectSong", true)
                                        .navigation()
                            }
                            return
                        }

                        mTvMatchedTime.text = String.format(U.app().getString(R.string.match_time_info), integer)
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
            mPrepareData = data as PrepareData?
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
        mMatchPresenter.destroy()
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
                .setConfirmBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        if (mExitDialog != null) {
                            mExitDialog!!.dismiss()
                        }
                    }
                })
                .setCancelBtnClickListener(object : AnimateClickListener() {
                    override fun click(view: View) {
                        if (mExitDialog != null) {
                            mExitDialog!!.dismiss()
                        }
                        U.getSoundUtils().release(GrabMatchSuccessFragment.TAG)
                        mMatchPresenter.cancelMatch()
                        if (mPrepareData!!.gameType == GameModeType.GAME_MODE_GRAB) {
                            BgMusicManager.getInstance().destory()
                        }
                        stopTimeTask()
                        if (activity != null) {
                            activity!!.finish()
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

    //pk
    override fun matchRankSucess(event: JoinActionEvent) {
        BgMusicManager.getInstance().destory()
        mPrepareData!!.gameId = event.gameId
        mPrepareData!!.sysAvatar = event.info.sender.avatar
        mPrepareData!!.gameCreatMs = event.gameCreateMs
        mPrepareData!!.playerInfoList = event.playerInfoList
        mPrepareData!!.songModelList = event.songModelList
        mPrepareData!!.gameConfigModel = event.gameConfigModel
        mPrepareData!!.agoraToken = event.agoraToken
        stopTimeTask()

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(activity, GrabMatchSuccessFragment::class.java)
                .setAddToBackStack(false)
                .setNotifyHideFragment(NewGrabMatchFragment::class.java)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, mPrepareData)
                .build())

        //匹配成功直接先把自己pop掉
        U.getFragmentUtils().popFragment(FragmentUtils.PopParams.Builder()
                .setPopFragment(this@NewGrabMatchFragment)
                .setPopAbove(false)
                .setHasAnimation(false)
                .build())
    }

    //一唱到底
    override fun matchGrabSucess(grabCurGameStateModel: JoinGrabRoomRspModel) {
        MyLog.d(TAG, "matchSucess event=$grabCurGameStateModel")
        BgMusicManager.getInstance().destory()
        mPrepareData!!.joinGrabRoomRspModel = grabCurGameStateModel
        stopTimeTask()

        //先跳转
        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                .withSerializable("prepare_data", grabCurGameStateModel)
                .withBoolean("is_new_user", mPrepareData!!.isNewUser)
                .navigation()

        //结束当前Activity
        if (activity != null) {
            activity!!.finish()
        }
    }

    override fun channelIsOffLine() {
        //        stopTimeTask();
        //        if (NewGrabMatchFragment.this.getActivity() != null) {
        //            NewGrabMatchFragment.this.getActivity().finish();
        //        }
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
        if (!BgMusicManager.getInstance().isPlaying && mPrepareData != null && this@NewGrabMatchFragment.fragmentVisible) {
            if (!TextUtils.isEmpty(mPrepareData!!.bgMusic)) {
                BgMusicManager.getInstance().starPlay(mPrepareData!!.bgMusic, 0, "GrabMatchFragment1")
            } else {
                val grabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)
                ApiMethods.subscribe(grabSongApi.sepcialBgVoice, object : ApiObserver<ApiResult>() {
                    override fun process(result: ApiResult) {
                        if (result.errno == 0) {
                            val musicURLs = JSON.parseArray(result.data!!.getString("musicURL"), String::class.java)
                            if (musicURLs != null && !musicURLs.isEmpty()) {
                                mPrepareData!!.bgMusic = musicURLs[0]
                                BgMusicManager.getInstance().starPlay(mPrepareData!!.bgMusic, 0, "GrabMatchFragment2")
                            }
                        } else {

                        }
                    }

                    override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                        super.onNetworkError(errorType)
                    }
                }, this)
            }
        }
    }

    /**
     * MatchSuccessFragment add后，动画播放完再remove掉匹配中页面
     */
    override fun notifyToHide() {
        if (mExitDialog != null && mExitDialog!!.isShowing) {
            mExitDialog!!.dismiss(false)
        }
        rootView.visibility = View.GONE
        //        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
        //                .setPopFragment(this)
        //                .setPopAbove(false)
        //                .build()
        //        );
    }
}
