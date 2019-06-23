package com.module.home.game.view

import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseFragment
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExRelativeLayout
import com.component.busilib.beauty.FROM_FRIEND_RECOMMEND
import com.component.busilib.beauty.FROM_MATCH
import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.component.busilib.verify.RealNameVerifyUtils
import com.module.RouterConstants
import com.module.home.R
import com.module.home.game.adapter.GameAdapter
import com.module.home.game.model.BannerModel
import com.module.home.game.model.FuncationModel
import com.module.home.game.model.QuickJoinRoomModel
import com.module.home.game.presenter.QuickGamePresenter
import com.module.home.model.GameKConfigModel
import com.module.home.model.SlideShowModel
import com.module.playways.IPlaywaysModeService
import kotlinx.android.synthetic.main.quick_game_view_layout.view.*

/**
 * 快速游戏
 */
class QuickGameView : ExRelativeLayout, IQuickGameView3 {

    companion object {
        const val TAG: String = "QuickGameView"
    }

    var mFragment: BaseFragment
    lateinit var mQuickGamePresenter: QuickGamePresenter
    lateinit var mSkrAudioPermission: SkrAudioPermission
    lateinit var mCameraPermission: SkrCameraPermission
    lateinit var mGameAdapter: GameAdapter
    internal var mRealNameVerifyUtils = RealNameVerifyUtils()

    constructor(fragment: BaseFragment) : super(fragment.context) {
        mFragment = fragment
        initView()
    }

    init {
        View.inflate(context, R.layout.quick_game_view_layout, this)
    }

    fun initView() {
        mQuickGamePresenter = QuickGamePresenter(this)
        mSkrAudioPermission = SkrAudioPermission()
        mCameraPermission = SkrCameraPermission()

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(true)
        mGameAdapter = GameAdapter(mFragment, object : GameAdapter.GameAdapterListener {
            override fun clickPractice() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_AUDIOROOM)
                        .withBoolean("selectSong", true)
                        .navigation()
            }

            override fun clickRank() {
                // 新的排行榜
                ARouter.getInstance().build(RouterConstants.ACTIVITY_RANKED)
                        .navigation()
            }

            override fun clickTask() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                        .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://test.app.inframe.mobi/task"))
                        .navigation()
                StatisticsAdapter.recordCountEvent("grab", "task_click", null)
            }

            override fun createRoom() {
                MyLog.d(TAG, "createRoom")
                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                iRankingModeService?.tryGoCreateRoom()
                StatisticsAdapter.recordCountEvent("grab", "room_create", null)
            }

            override fun selectSpecial(specialModel: SpecialModel?) {
                MyLog.d(TAG, "selectSpecial specialModel=$specialModel")
                if (specialModel != null) {
                    if (specialModel.tagType == SpecialModel.TYPE_VIDEO) {
                        mSkrAudioPermission.ensurePermission({
                            mCameraPermission.ensurePermission({
                                mRealNameVerifyUtils.checkJoinVideoPermission {
                                    // 进入视频预览
                                    ARouter.getInstance()
                                            .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                            .withInt("from", FROM_MATCH)
                                            .withSerializable("SpecialModel", specialModel)
                                            .navigation()
                                }
                            }, true)
                        }, true)
                    } else {
                        mSkrAudioPermission.ensurePermission({
                            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                            if (iRankingModeService != null) {
                                if (specialModel != null) {
                                    iRankingModeService.tryGoGrabMatch(specialModel.tagID)
                                }
                            }
                        }, true)
                    }
                } else {

                }
                StatisticsAdapter.recordCountEvent("grab", "categoryall2", null)
            }

            override fun enterRoom(friendRoomModel: RecommendModel?) {
                MyLog.d(TAG, "enterRoom friendRoomModel=$friendRoomModel")
                if (friendRoomModel != null) {
                    if (friendRoomModel.mediaType == SpecialModel.TYPE_VIDEO) {
                        mSkrAudioPermission.ensurePermission({
                            mCameraPermission.ensurePermission({
                                mRealNameVerifyUtils.checkJoinVideoPermission {
                                    // 进入视频预览
                                    ARouter.getInstance()
                                            .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                            .withInt("from", FROM_FRIEND_RECOMMEND)
                                            .withSerializable("RecommendModel", friendRoomModel)
                                            .navigation()
                                }
                            }, true)
                        }, true)
                    } else {
                        mSkrAudioPermission.ensurePermission({
                            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                            iRankingModeService?.tryGoGrabRoom(friendRoomModel.roomInfo.roomID, 0)
                        }, true)
                    }
                } else {

                }
                StatisticsAdapter.recordCountEvent("grab", "room_click2", null)
            }

            override fun moreRoom() {
                MyLog.d(TAG, "moreRoom")
                StatisticsAdapter.recordCountEvent("grab", "room_more", null)
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_FRIEND_ROOM)
                        .navigation()
            }
        })
        recycler_view.setLayoutManager(LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false))
        recycler_view.setAdapter(mGameAdapter)
    }

    fun initData() {
        mQuickGamePresenter.initOperationArea(false)
        mQuickGamePresenter.initQuickRoom(false)
        mQuickGamePresenter.checkTaskRedDot()
    }

    override fun setBannerImage(slideShowModelList: List<SlideShowModel>) {
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

//    override fun setRecommendInfo(list: MutableList<RecommendModel>?) {
//        if (list == null || list.size == 0) {
//            // 清空好友派对列表
//            mGameAdapter.updateRecommendRoomInfo(null)
//            return
//        }
//        val recommendRoomModel = RecommendRoomModel(list)
//        mGameAdapter.updateRecommendRoomInfo(recommendRoomModel)
//    }

    override fun setQuickRoom(list: MutableList<SpecialModel>, offset: Int) {
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
