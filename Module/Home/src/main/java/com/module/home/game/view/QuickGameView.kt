package com.module.home.game.view

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.core.upgrade.UpgradeManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExRelativeLayout
import com.component.busilib.model.PartyRoomInfoModel
import com.component.busilib.verify.SkrVerifyUtils
import com.component.busilib.view.SelectSexDialogView
import com.component.person.model.UserRankModel
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.module.home.game.adapter.ClickGameListener
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.presenter.QuickGamePresenter
import com.module.home.model.GameKConfigModel
import com.component.busilib.banner.SlideShowModel
import com.facebook.drawee.drawable.RoundedColorDrawable
import com.module.playways.IPlaywaysModeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.android.synthetic.main.quick_game_view_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 快速游戏
 */
class QuickGameView(var fragment: BaseFragment) : ExRelativeLayout(fragment.context!!), IQuickGameView3 {

    val TAG: String = "QuickGameView"

    var mQuickGamePresenter: QuickGamePresenter
    var mSkrAudioPermission: SkrAudioPermission
    var mCameraPermission: SkrCameraPermission
    var mGameAdapter: GameAdapter
    var mRealNameVerifyUtils = SkrVerifyUtils()

    var mRecommendInterval = 0

    var mSelectSexDialogPlus: DialogPlus? = null
    var mSelectView: SelectSexDialogView? = null

    init {
        View.inflate(context, R.layout.quick_game_view_layout, this)
        mQuickGamePresenter = QuickGamePresenter(fragment, this)
        mSkrAudioPermission = SkrAudioPermission()
        mCameraPermission = SkrCameraPermission()

        refreshLayout.setEnableRefresh(true)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(true)
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {
            }

            override fun onRefresh(refreshLayout: RefreshLayout) {
                initData(true)
            }
        })
        mGameAdapter = GameAdapter(fragment, object : ClickGameListener {

            override fun onClickPartyRoom(pos: Int, model: PartyRoomInfoModel?) {
                // pos是在横向列表中位置
                model?.roomID?.let {
                    val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                    iRankingModeService.tryGoPartyRoom(it, 1, model.roomtype ?: 0)
                }
            }

            override fun onPartyRoomListener() {
                // 进入主题房
                StatisticsAdapter.recordCountEvent("game_express", "party", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_PARTY_HOME)
                        .navigation()
            }

            override fun onRelayRoomListener() {
                // 进入双人接唱
                StatisticsAdapter.recordCountEvent("game_express", "chorus", null)
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService.tryGoRelayHome()
            }

            override fun onClickTaskListener() {
                // 进入任务
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://test.app.inframe.mobi/task/new?title=1"))
                        .navigation()
                StatisticsAdapter.recordCountEvent("game", "express_tasks", null)
            }

            override fun onClickPracticeListener() {
                // 进入练歌房
                ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                        .withBoolean("selectSong", true)
                        .navigation()
                StatisticsAdapter.recordCountEvent("game", "express_practice", null)
            }

            override fun onClickRankListener() {
                // 新的排行榜
                ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKED)
                        .navigation()
                StatisticsAdapter.recordCountEvent("game", "express_ranklist", null)
            }

            override fun onClickMallListner() {
                // 进入商城
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MALL_MALL)
                        .navigation()
            }

            override fun onCreateRoomListener() {
                // 创建房间
                MyLog.d(TAG, "createRoom")
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService?.tryGoCreateRoom()
                StatisticsAdapter.recordCountEvent("game", "express_create", null)
            }

            override fun onRaceRoomListener() {
                StatisticsAdapter.recordCountEvent("game", "express_rank", null)
                mRealNameVerifyUtils.checkAgeSettingState {
                    openRaceActivity(context, false)
                }
            }

            override fun onDoubleRoomListener() {
                StatisticsAdapter.recordCountEvent("game", "express_cp", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_HOME)
                        .navigation()
            }

            override fun onBattleRoomListener() {
                StatisticsAdapter.recordCountEvent("game", "express_grab_song_list", null)
                // 歌单抢唱
                openBattleActivity(context)
            }

            override fun onGrabRoomListener() {
                // 首页抢唱
                StatisticsAdapter.recordCountEvent("game", "express_grab_hot", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_SPECIAL)
                        .navigation()
            }

            override fun onMicRoomListener() {
                // 小K房
                StatisticsAdapter.recordCountEvent("game", "express_KTV", null)
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MIC_HOME)
                        .navigation()
            }

            override fun onClickRankArea() {
                StatisticsAdapter.recordCountEvent("game_rank", "ranklist", null)
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                val baseFragment = iRankingModeService.leaderboardFragmentClass as Class<BaseFragment>
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(fragment.activity, baseFragment)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build())
            }
        })

        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = mGameAdapter
    }

    fun initData(flag: Boolean) {
        mQuickGamePresenter.initOperationArea(flag)
        mQuickGamePresenter.checkTaskRedDot()
        mQuickGamePresenter.getPartyRoomList(flag)
        if (!flag) {
            if (mQuickGamePresenter.isUserInfoChange) {
                mQuickGamePresenter.isUserInfoChange = false
                mQuickGamePresenter.getRegionDiff(true)
            } else {
                mQuickGamePresenter.getRegionDiff(false)
            }
        } else {
            mQuickGamePresenter.getRegionDiff(true)
        }
    }

    fun stopTimer() {
        // 原来用来停止更新的
        mQuickGamePresenter.stopTimer()
    }

    fun showSexFilterView(needMatch: Boolean) {
        if (mSelectSexDialogPlus == null) {
            mSelectView = SelectSexDialogView(this@QuickGameView.context)
            mSelectSexDialogPlus = DialogPlus.newDialog(context!!)
                    .setContentHolder(ViewHolder(mSelectView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create()
        }

        mSelectView?.onClickMatch = { isFindMale, isMeMale ->
            mSelectSexDialogPlus?.dismiss()
            if (needMatch) {
                val bundle = Bundle()
                bundle.putBoolean("is_find_male", isFindMale
                        ?: true)
                bundle.putBoolean("is_me_male", isMeMale
                        ?: true)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_DOUBLE_MATCH)
                        .withBundle("bundle", bundle)
                        .navigation()
            }
        }

        mSelectView?.reset()
        mSelectSexDialogPlus?.show()
    }

    override fun setBannerImage(slideShowModelList: List<SlideShowModel>?) {
        refreshLayout.finishRefresh()
        mGameAdapter.updateBanner(slideShowModelList)
    }

    override fun showTaskRedDot(show: Boolean) {
        mGameAdapter.updateFunction(show)
    }

    override fun setRegionDiff(model: UserRankModel?) {
        mGameAdapter.updateRegionDiff(model)
    }

    override fun setPartyRoomList(list: List<PartyRoomInfoModel>?) {
        mGameAdapter.updatePartyList(list)
    }

    fun showRedOperationView(homepagesBean: GameKConfigModel.HomepagesitefirstBean?) {
        FrescoWorker.loadImage(iv_red_pkg, ImageFactory.newPathImage(homepagesBean?.pic)
                .setWidth(U.getDisplayUtils().dip2px(48f))
                .setHeight(U.getDisplayUtils().dip2px(53f))
                .build<BaseImage>()
        )

        iv_red_pkg.visibility = View.VISIBLE
        iv_red_pkg.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                        .withString("uri", homepagesBean?.schema)
                        .navigation()
            }
        })
    }

    fun hideRedOperationView() {
        iv_red_pkg.visibility = View.GONE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //destory()
    }

    fun destory() {
        mQuickGamePresenter.destroy()
    }
}

fun openBattleActivity(ctx: Context) {
    GlobalScope.launch(Dispatchers.Main) {
        var tipsDialogView: TipsDialogView? = null
        val api = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
        val check = subscribe(RequestControl("CheckGameConfig", ControlType.CancelThis)) { api.getGameConfig(6, MyLog.isDebugLogOpen()) }
        if (check.errno == 0) {
            val supprot = check.data.getBooleanValue("isSupport")
            val jo = check.data.getJSONObject("detail")
            if (supprot && jo != null && !jo.getBooleanValue("isOpen")) {
                tipsDialogView?.dismiss()
                tipsDialogView = TipsDialogView.Builder(ctx)
                        .setMessageTip(jo.getString("content"))
                        .setOkBtnTip("确定")
                        .setOkBtnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                tipsDialogView?.dismiss()
                                if (jo.getBooleanValue("needUpdate")) {
                                    UpgradeManager.getInstance().checkUpdate2()
                                } else {
                                    // donothing
                                }
                            }
                        })
                        .build()
                tipsDialogView?.showByDialog()
            } else {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_BATTLE_LIST)
                        .navigation()
            }
        } else {
            if (check.errno == ERROR_NETWORK_BROKEN) {
                tipsDialogView?.dismiss()
                tipsDialogView = TipsDialogView.Builder(ctx)
                        .setMessageTip("网络连接不可用，请检查网络后重试")
                        .setOkBtnTip("确定")
                        .setOkBtnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                tipsDialogView?.dismiss()
                            }
                        })
                        .build()
                tipsDialogView?.showByDialog()
            } else if (check.errno > 0) {
                tipsDialogView?.dismiss()
                tipsDialogView = TipsDialogView.Builder(ctx)
                        .setMessageTip(check.errmsg)
                        .setOkBtnTip("确定")
                        .setOkBtnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                tipsDialogView?.dismiss()
                            }
                        })
                        .build()
                tipsDialogView?.showByDialog()
            }
        }
    }
}

fun openRaceActivity(ctx: Context, audience: Boolean) {
    GlobalScope.launch(Dispatchers.Main) {
        var tipsDialogView: TipsDialogView? = null
        val api = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
        val check = subscribe(RequestControl("checkRank", ControlType.CancelThis)) { api.checkRank(1) }
        if (check.errno == 0) {
            // 可以进房间
            val skrAudioPermission = SkrAudioPermission()
            skrAudioPermission.ensurePermission({
                ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_MATCH_ROOM)
                        .withBoolean("audience", audience)
                        .navigation()
            }, true)
        } else {
            if (check.errno == ERROR_NETWORK_BROKEN) {
                tipsDialogView?.dismiss()
                tipsDialogView = TipsDialogView.Builder(ctx)
                        .setMessageTip("网络连接不可用，请检查网络后重试")
                        .setOkBtnTip("确定")
                        .setOkBtnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                tipsDialogView?.dismiss()
                            }
                        })
                        .build()
                tipsDialogView?.showByDialog()
            } else if (check.errno > 0) {
                tipsDialogView?.dismiss()
                tipsDialogView = TipsDialogView.Builder(ctx)
                        .setMessageTip(check.errmsg)
                        .setOkBtnTip("确定")
                        .setOkBtnClickListener(object : DebounceViewClickListener() {
                            override fun clickValid(v: View?) {
                                tipsDialogView?.dismiss()
                            }
                        })
                        .build()
                tipsDialogView?.showByDialog()
            }
        }
    }
}