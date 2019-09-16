package com.module.home.game.view

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.core.upgrade.UpgradeManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExRelativeLayout
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.beauty.FROM_MATCH
import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.component.busilib.verify.SkrVerifyUtils
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.home.MainPageSlideApi
import com.module.home.R
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.model.BannerModel
import com.module.home.game.model.FuncationModel
import com.module.home.game.model.RecommendRoomModel
import com.module.home.game.presenter.QuickGamePresenter
import com.module.home.model.GameKConfigModel
import com.module.home.model.SlideShowModel
import com.module.playways.IPlaywaysModeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.quick_game_view_layout.view.*
import kotlinx.android.synthetic.main.quick_game_view_layout.view.recycler_view
import kotlinx.android.synthetic.main.quick_game_view_layout.view.refreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 快速游戏
 */
class QuickGameView(var fragment: BaseFragment) : ExRelativeLayout(fragment.context), IQuickGameView3 {

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
        mGameAdapter = GameAdapter(fragment)
        mGameAdapter.onCreateRoomListener = {
            // 创建房间
            MyLog.d(TAG, "createRoom")
            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
            iRankingModeService?.tryGoCreateRoom()
            StatisticsAdapter.recordCountEvent("game", "express_create", null)

        }
        mGameAdapter.onMoreRoomListener = {
            // 更多房间
            ARouter.getInstance().build(RouterConstants.ACTIVITY_FRIEND_ROOM)
                    .navigation()
        }
        mGameAdapter.onEnterRoomListener = {
            // 进入房间
            if (it.roomInfo.mediaType == SpecialModel.TYPE_VIDEO) {
                mSkrAudioPermission.ensurePermission({
                    mCameraPermission.ensurePermission({
                        mRealNameVerifyUtils.checkJoinVideoPermission {
                            // 进入视频预览
                            ARouter.getInstance()
                                    .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                    .withInt("mFrom", FROM_FRIEND_RECOMMEND)
                                    .withInt("mRoomId", it.roomInfo.roomID)
                                    .withInt("mInviteType", 0)
                                    .navigation()
                        }
                    }, true)
                }, true)
            } else {
                mSkrAudioPermission.ensurePermission({
                    mRealNameVerifyUtils.checkJoinAudioPermission(it.roomInfo.tagID) {
                        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                        iRankingModeService?.tryGoGrabRoom(it.roomInfo.roomID, 0)
                    }
                }, true)
            }
            StatisticsAdapter.recordCountEvent("game", "express_room_outsideclick", null)
        }
        mGameAdapter.onClickTaskListener = {
            // 进入任务
            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                    .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://test.app.inframe.mobi/task"))
                    .navigation()
            StatisticsAdapter.recordCountEvent("game", "express_tasks", null)
        }
        mGameAdapter.onClickRankListener = {
            // 新的排行榜
            ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKED)
                    .navigation()
            StatisticsAdapter.recordCountEvent("game", "express_ranklist", null)
        }
        mGameAdapter.onClickPracticeListener = {
            // 进入练歌房
            ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                    .withBoolean("selectSong", true)
                    .navigation()
            StatisticsAdapter.recordCountEvent("game", "express_practice", null)
        }

        mGameAdapter.onPkRoomListener = {
            StatisticsAdapter.recordCountEvent("game", "express_rank", null)
            openPlayWaysActivityByRank(context)
        }

        mGameAdapter.onDoubleRoomListener = {
            StatisticsAdapter.recordCountEvent("game", "express_cp", null)
            if (!U.getNetworkUtils().hasNetwork()) {
                U.getToastUtil().showLong("网络连接失败 请检查网络")
            } else {
                mSkrAudioPermission.ensurePermission({
                    mRealNameVerifyUtils.checkJoinDoubleRoomPermission {
                        /**
                         * 判断有没有年龄段
                         */
                        if (!MyUserInfoManager.getInstance().hasAgeStage()) {
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_EDIT_AGE)
                                    .withInt("from", 0)
                                    .navigation()
                        } else {
                            val sex = object {
                                var mIsFindMale: Boolean? = null
                                var mMeIsMale: Boolean? = null
                            }

                            Observable.create<Boolean> {
                                if (U.getPreferenceUtils().hasKey("is_find_male") && U.getPreferenceUtils().hasKey("is_me_male")) {
                                    sex.mIsFindMale = U.getPreferenceUtils().getSettingBoolean("is_find_male", true)
                                    sex.mMeIsMale = U.getPreferenceUtils().getSettingBoolean("is_me_male", true)
                                    it.onNext(true)
                                } else {
                                    it.onNext(false)
                                }

                                it.onComplete()
                            }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                        if (it) {
                                            val bundle = Bundle()
                                            bundle.putBoolean("is_find_male", sex.mIsFindMale
                                                    ?: true)
                                            bundle.putBoolean("is_me_male", sex.mMeIsMale
                                                    ?: true)
                                            ARouter.getInstance()
                                                    .build(RouterConstants.ACTIVITY_DOUBLE_MATCH)
                                                    .withBundle("bundle", bundle)
                                                    .navigation()
                                        } else {
                                            showSexFilterView(true)
                                        }
                                    }, {
                                        MyLog.e("SelectSexDialogView", it)
                                    })
                        }
                    }
                }, true)
            }
        }

        mGameAdapter.onGrabRoomListener = {
            var tagID = when {
                MyUserInfoManager.getInstance().ageStage == 1 -> 44
                MyUserInfoManager.getInstance().ageStage == 2 -> 45
                MyUserInfoManager.getInstance().ageStage == 3 -> 47
                MyUserInfoManager.getInstance().ageStage == 4 -> 48
                else -> 47
            }
            mSkrAudioPermission.ensurePermission({
                mRealNameVerifyUtils.checkJoinAudioPermission(tagID) {
                    mRealNameVerifyUtils.checkAgeSettingState {
                        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                        iRankingModeService?.tryGoGrabMatch(tagID)
                    }
                }
            }, true)
            /**
             * 点击首页热门
             */
            StatisticsAdapter.recordCountEvent("game", "express_grab_hot", null)
        }

        mGameAdapter.onBattleRoomListener = {
            StatisticsAdapter.recordCountEvent("game", "express_grab_song_list", null)
            // 歌单抢唱
            openBattleActivity(context)
        }

        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = mGameAdapter
    }

    fun initData(flag: Boolean) {
        mQuickGamePresenter.initOperationArea(flag)
        mQuickGamePresenter.initRecommendRoom(flag, mRecommendInterval)
        mQuickGamePresenter.getRemainTimes(flag)
//        mQuickGamePresenter.initQuickRoom(false)
        mQuickGamePresenter.checkTaskRedDot()
    }

    fun stopTimer() {
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
        if (slideShowModelList == null || slideShowModelList.size == 0) {
            MyLog.w(TAG, "initOperationArea 为null")
            mGameAdapter.updateBanner(null)
            return
        }
        val bannerModel = BannerModel(slideShowModelList)
        mGameAdapter.updateBanner(bannerModel)
    }

    override fun showTaskRedDot(show: Boolean) {
        var moFuncationModel = FuncationModel(show)
        mGameAdapter.updateFuncation(moFuncationModel)
    }

    override fun setRecommendInfo(list: MutableList<RecommendModel>?) {
        refreshLayout.finishRefresh()
        if (list == null || list.size == 0) {
            // 清空好友派对列表
            mGameAdapter.updateRecommendRoomInfo(null)
            return
        }
        val recommendRoomModel = RecommendRoomModel(list)
        mGameAdapter.updateRecommendRoomInfo(recommendRoomModel)
    }

    override fun showRemainTimes(remainTimes: Int) {
        mGameAdapter.updateDoubleRemainTime(remainTimes)
    }

    fun showRedOperationView(homepagesitefirstBean: GameKConfigModel.HomepagesitefirstBean?) {
        FrescoWorker.loadImage(iv_red_pkg, ImageFactory.newPathImage(homepagesitefirstBean!!.getPic())
                .setWidth(U.getDisplayUtils().dip2px(48f))
                .setHeight(U.getDisplayUtils().dip2px(53f))
                .build<BaseImage>()
        )

        iv_red_pkg.visibility = View.VISIBLE
        iv_red_pkg.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_SCHEME)
                        .withString("uri", homepagesitefirstBean.getSchema())
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
        mQuickGamePresenter?.destroy()
    }
}

fun openBattleActivity(ctx: Context) {
    GlobalScope.launch(Dispatchers.Main) {
        var tipsDialogView: TipsDialogView? = null
        val api = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
        val check = subscribe(RequestControl("CheckGameConfig", ControlType.CancelThis)) { api.getGameConfig(6, false) }
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
