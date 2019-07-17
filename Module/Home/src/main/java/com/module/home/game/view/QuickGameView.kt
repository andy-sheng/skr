package com.module.home.game.view

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExRelativeLayout
import com.component.busilib.beauty.FROM_MATCH
import com.component.busilib.constans.GameModeType
import com.component.busilib.friends.FriendMoreRoomFragment
import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.component.busilib.verify.SkrVerifyUtils
import com.module.RouterConstants
import com.module.home.R
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.model.BannerModel
import com.module.home.game.model.FuncationModel
import com.module.home.game.model.QuickJoinRoomModel
import com.module.home.game.model.RecommendRoomModel
import com.module.home.game.presenter.QuickGamePresenter
import com.module.home.model.GameKConfigModel
import com.module.home.model.SlideShowModel
import com.module.playways.IPlaywaysModeService
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.quick_game_view_layout.view.*

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
        mQuickGamePresenter = QuickGamePresenter(this)
        mSkrAudioPermission = SkrAudioPermission()
        mCameraPermission = SkrCameraPermission()

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(true)
        mGameAdapter = GameAdapter(fragment)
        mGameAdapter.onCreateRoomListener = {
            // 创建房间
            MyLog.d(TAG, "createRoom")
            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
            iRankingModeService?.tryGoCreateRoom()
            StatisticsAdapter.recordCountEvent("grab", "room_create", null)

        }
        mGameAdapter.onMoreRoomListener = {
            // 更多房间
            U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(fragment.activity, FriendMoreRoomFragment::class.java)
                    .setAddToBackStack(true)
                    .setHasAnimation(true)
                    .build())
        }
        mGameAdapter.onEnterRoomListener = {
            // 进入房间
        }
        mGameAdapter.onClickTaskListener = {
            // 进入任务
            ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                    .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://test.app.inframe.mobi/task"))
                    .navigation()
            StatisticsAdapter.recordCountEvent("grab", "task_click", null)
        }
        mGameAdapter.onClickRankListener = {
            // 新的排行榜
            ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKED)
                    .navigation()
            StatisticsAdapter.recordCountEvent("grab", "ranklist", null)
        }
        mGameAdapter.onClickPracticeListener = {
            // 进入练歌房
            ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                    .withBoolean("selectSong", true)
                    .navigation()
            StatisticsAdapter.recordCountEvent("grab", "practice", null)
        }
        mGameAdapter.onSelectSpecialListener = { it ->
            // 选择专场
            MyLog.d(TAG, "selectSpecial specialModel=$it")
            it?.let {
                if (it.tagType == SpecialModel.TYPE_VIDEO) {
                    mSkrAudioPermission.ensurePermission({
                        mCameraPermission.ensurePermission({
                            mRealNameVerifyUtils.checkJoinVideoPermission {
                                mRealNameVerifyUtils.checkAgeSettingState {
                                    // 进入视频预览
                                    ARouter.getInstance()
                                            .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                            .withInt("mFrom", FROM_MATCH)
                                            .withSerializable("mSpecialModel", it)
                                            .navigation()
                                }
                            }
                        }, true)
                    }, true)
                } else {
                    mSkrAudioPermission.ensurePermission({
                        mRealNameVerifyUtils.checkJoinAudioPermission(it.tagID) {
                            mRealNameVerifyUtils.checkAgeSettingState {
                                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                                if (iRankingModeService != null) {
                                    if (it != null) {
                                        iRankingModeService.tryGoGrabMatch(it.tagID)
                                    }
                                }
                            }
                        }
                    }, true)
                }
            }
            StatisticsAdapter.recordCountEvent("grab", "categoryall2", null)
        }

        mGameAdapter.onPkRoomListener = {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_PLAY_WAYS)
                    .withInt("key_game_type", GameModeType.GAME_MODE_CLASSIC_RANK)
                    .withBoolean("selectSong", true)
                    .navigation()
        }

        mGameAdapter.onDoubleRoomListener = {
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

        recycler_view.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recycler_view.adapter = mGameAdapter
    }

    fun initData() {
        mQuickGamePresenter.initOperationArea(false)
        mQuickGamePresenter.initRecommendRoom(mRecommendInterval)
        mQuickGamePresenter.getRemainTimes(false)
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
        if (list == null || list.size == 0) {
            // 清空好友派对列表
            mGameAdapter.updateRecommendRoomInfo(null)
            return
        }
        val recommendRoomModel = RecommendRoomModel(list)
        mGameAdapter.updateRecommendRoomInfo(recommendRoomModel)
    }

    override fun setQuickRoom(list: MutableList<SpecialModel>?, offset: Int) {
        MyLog.d(TAG, "setQuickRoom list=$list offset=$offset")
        // TODO: 2019/4/1 过滤一下空的背景
        if (list != null && list.size > 0) {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val specialModel = iterator.next()
                if (specialModel != null) {
                    if (TextUtils.isEmpty(specialModel.bgImage2) || TextUtils.isEmpty(specialModel.bgImage1)) {
                        iterator.remove()
                    }
                }
            }
        }

        if (list == null || list.size == 0) {
            // 快速加入专场空了，清空数据
            mGameAdapter.updateQuickJoinRoomInfo(null)
            return
        }

        val quickJoinRoomModel = QuickJoinRoomModel(list, offset)
        mGameAdapter.updateQuickJoinRoomInfo(quickJoinRoomModel)
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
        destory()
    }

    fun destory() {
        mQuickGamePresenter?.destroy()
    }
}
