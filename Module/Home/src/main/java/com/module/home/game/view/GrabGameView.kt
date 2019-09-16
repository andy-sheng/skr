package com.module.home.game.view

import android.content.Context
import android.support.v7.widget.StaggeredGridLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.log.MyLog
import com.common.statistics.StatisticsAdapter
import com.common.utils.U
import com.component.busilib.beauty.FROM_MATCH
import com.component.busilib.friends.SpecialModel
import com.component.busilib.verify.SkrVerifyUtils
import com.module.RouterConstants
import com.module.home.R
import com.module.home.game.adapter.GrabGameAdapter
import com.module.home.game.model.GrabSpecialModel
import com.module.home.game.presenter.GrabGamePresenter
import com.module.playways.IPlaywaysModeService
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.android.synthetic.main.friend_room_view_layout.view.*

// 抢唱
class GrabGameView(context: Context) : RelativeLayout(context), IGrabGameView {

    private val TAG = "GrabGameView"

    private var mPresenter: GrabGamePresenter
    private var mGrabGameAdapter: GrabGameAdapter

    private val mSkrAudioPermission: SkrAudioPermission = SkrAudioPermission()
    private val mCameraPermission: SkrCameraPermission = SkrCameraPermission()
    private val mRealNameVerifyUtils = SkrVerifyUtils()

    init {
        View.inflate(context, R.layout.grab_game_view_layout, this)

        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableLoadMore(false)
        refreshLayout.setEnableLoadMoreWhenContentNotFull(false)
        refreshLayout.setEnableOverScrollDrag(true)
        refreshLayout.setHeaderMaxDragRate(1.5f)
        refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onLoadMore(refreshLayout: RefreshLayout) {

            }

            override fun onRefresh(refreshLayout: RefreshLayout) {

            }
        })

        recycler_view.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        mPresenter = GrabGamePresenter(this)
        mGrabGameAdapter = GrabGameAdapter()
        mGrabGameAdapter.onClickTagListener = {
            // 点击专场
            MyLog.d(TAG, "selectSpecial grabSpecialModel=$it")
            it?.let {
                when {
                    it.type == GrabSpecialModel.TBT_STANDCREATE -> {
                        // 创建房间
                        MyLog.d(TAG, "createRoom")
                        val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                        iRankingModeService?.tryGoCreateRoom()
                        StatisticsAdapter.recordCountEvent("game", "grab_create", null)

                    }
                    it.type == GrabSpecialModel.TBT_PLAYBOOK -> {
                        // 歌单战
                        StatisticsAdapter.recordCountEvent("game_grab", "song_list", null)
                        openBattleActivity(context)
                    }
                    it.type == GrabSpecialModel.TBT_SPECIAL -> {
                        // 进入视频预览
                        it.model?.let { specialModel ->
                            if (specialModel.tagType == SpecialModel.TYPE_VIDEO) {
                                mSkrAudioPermission.ensurePermission({
                                    mCameraPermission.ensurePermission({
                                        mRealNameVerifyUtils.checkJoinVideoPermission {
                                            mRealNameVerifyUtils.checkAgeSettingState {
                                                // 进入视频预览
                                                ARouter.getInstance()
                                                        .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                                        .withInt("mFrom", FROM_MATCH)
                                                        .withSerializable("mSpecialModel", specialModel)
                                                        .navigation()
                                            }
                                        }
                                    }, true)
                                }, true)
                            } else {
                                mSkrAudioPermission.ensurePermission({
                                    mRealNameVerifyUtils.checkJoinAudioPermission(specialModel.tagID) {
                                        mRealNameVerifyUtils.checkAgeSettingState {
                                            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                                            if (iRankingModeService != null) {
                                                if (it != null) {
                                                    iRankingModeService.tryGoGrabMatch(specialModel.tagID)
                                                }
                                            }
                                        }
                                    }
                                }, true)
                            }
                        }
                    }
                    else ->{
                        U.getToastUtil().showShort("未知类型")
                    }
                }
            }
            StatisticsAdapter.recordCountEvent("game", "grab_category", null)
        }
        recycler_view.adapter = mGrabGameAdapter
    }

    fun initData(flag: Boolean) {
        mPresenter.initQuickRoom(flag)
    }

    override fun setQuickRoom(list: MutableList<GrabSpecialModel>?) {
        MyLog.d(TAG, "setQuickRoom list=$list")
        // TODO: 2019/4/1 过滤一下空的背景
        if (list != null && list.size > 0) {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val specialModel = iterator.next()
                if (specialModel != null) {
                    if (specialModel.type == null || specialModel.model == null
                            || specialModel.model?.biggest == null) {
                        iterator.remove()
                    }
                }
            }
        }

        if (list == null || list.size == 0) {
            // 快速加入专场空了，清空数据
            mGrabGameAdapter.mDataList.clear()
            return
        }

        mGrabGameAdapter.mDataList = list
        mGrabGameAdapter.notifyDataSetChanged()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //destory()
    }

    fun destory() {
        mPresenter.destroy()
    }
}