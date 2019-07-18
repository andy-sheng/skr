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
import com.component.busilib.beauty.FROM_MATCH
import com.component.busilib.friends.SpecialModel
import com.component.busilib.verify.SkrVerifyUtils
import com.module.RouterConstants
import com.module.home.R
import com.module.home.game.adapter.GrabGameAdapter
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

        refreshLayout.setEnableRefresh(true)
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
        mGrabGameAdapter.onClickCreateListener = {
            // 创建房间
            MyLog.d(TAG, "createRoom")
            val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
            iRankingModeService?.tryGoCreateRoom()
            StatisticsAdapter.recordCountEvent("game", "grab_create", null)
        }
        mGrabGameAdapter.onClickTagListener = {
            // 点击专场
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
            StatisticsAdapter.recordCountEvent("game", "grab_category", null)
        }
        recycler_view.adapter = mGrabGameAdapter
    }

    fun initData(flag: Boolean) {
        mPresenter.initQuickRoom(flag)
    }

    override fun setQuickRoom(list: MutableList<SpecialModel>?, offset: Int) {
        MyLog.d(TAG, "setQuickRoom list=$list offset=$offset")
        // TODO: 2019/4/1 过滤一下空的背景
        if (list != null && list.size > 0) {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val specialModel = iterator.next()
                if (specialModel != null) {
                    if (specialModel.biggest == null || specialModel.longer == null) {
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

    fun destory() {
    }
}