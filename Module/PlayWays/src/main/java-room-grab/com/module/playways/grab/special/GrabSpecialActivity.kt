package com.module.playways.grab.special

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.permission.SkrCameraPermission
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.beauty.FROM_MATCH
import com.component.busilib.friends.SpecialModel
import com.component.busilib.verify.SkrVerifyUtils
import com.module.RouterConstants
import com.module.playways.IPlaywaysModeService
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

// 抢唱专场详情页
@Route(path = RouterConstants.ACTIVITY_GRAB_SPECIAL)
class GrabSpecialActivity : BaseActivity() {

    private var titlebar: CommonTitleBar? = null
    private var createRoom: ImageView? = null
    private var refreshLayout: SmartRefreshLayout? = null
    private var recyclerView: RecyclerView? = null
    private val adapter: GrabSpecialAdapter = GrabSpecialAdapter()

    private val grabRooServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)
    var offset = 0
    var hasMore = true
    val mCNT = 20

    val mSkrAudioPermission: SkrAudioPermission = SkrAudioPermission()
    val mCameraPermission: SkrCameraPermission = SkrCameraPermission()
    val mRealNameVerifyUtils = SkrVerifyUtils()

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.grab_special_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getStatusBarUtil().setTransparentBar(this, false)
        titlebar = findViewById(R.id.titlebar)
        createRoom = findViewById(R.id.create_room)
        refreshLayout = findViewById(R.id.refreshLayout)
        recyclerView = findViewById(R.id.recycler_view)

        titlebar?.leftTextView?.setDebounceViewClickListener { finish() }
        createRoom?.setAnimateDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_CREATE_ROOM)
                    .navigation()
        }

        refreshLayout?.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    // 加载更多
                    getStandTagList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView?.adapter = adapter
        adapter.onClickListener = { model, _ ->
            model?.let { specialModel ->
                if (specialModel.tagDetailType == GrabTagDetailModel.TAG_TYPE_BATTLE) {
                    // 2v2battle
                    mSkrAudioPermission.ensurePermission({
                        mRealNameVerifyUtils.checkJoinAudioPermission(specialModel.tagID) {
                            mRealNameVerifyUtils.checkAgeSettingState {
                                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_RANKINGMODE).navigation() as IPlaywaysModeService
                                iRankingModeService.tryGoBattleMatch(specialModel.tagID)
                            }
                        }
                    }, true)
                } else {
                    if (specialModel.tagType == SpecialModel.TYPE_VIDEO) {
                        mSkrAudioPermission.ensurePermission({
                            mCameraPermission.ensurePermission({
                                mRealNameVerifyUtils.checkJoinVideoPermission {
                                    mRealNameVerifyUtils.checkAgeSettingState {
                                        // 进入视频预览
                                        val special = SpecialModel()
                                        special.tagID = specialModel.tagID
                                        special.tagName = specialModel.tagName
                                        special.tagType = specialModel.tagType
                                        ARouter.getInstance()
                                                .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                                .withInt("mFrom", FROM_MATCH)
                                                .withSerializable("mSpecialModel", special)
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
                                    iRankingModeService.tryGoGrabMatch(specialModel.tagID)
                                }
                            }
                        }, true)
                    }
                }

            }
        }

        adapter.onClickRankListener = { model, _ ->
            ARouter.getInstance().build(RouterConstants.ACTIVITY_BATTLE_RANK)
                    .withInt("tagID", model?.tagID ?: 0)
                    .navigation()
        }

    }

    override fun onResume() {
        super.onResume()
        hasMore = true
        getStandTagList(0, true)
    }

    private fun getStandTagList(off: Int, isClean: Boolean) {
        if (!hasMore) {
            U.getToastUtil().showShort("没有更多了")
            return
        }

        launch {
            val result = subscribe(RequestControl("getStandTagList", ControlType.CancelThis)) {
                grabRooServerApi.getStandTagList(MyUserInfoManager.uid, off, mCNT)
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                hasMore = result.data.getBooleanValue("hasMore")

                val list = JSON.parseArray(result.data.getString("details"), GrabTagDetailModel::class.java)
                addStandTagList(list, isClean)
            } else {
                refreshLayout?.finishLoadMore()
                refreshLayout?.finishRefresh()
            }
        }
    }

    private fun addStandTagList(list: List<GrabTagDetailModel>?, clean: Boolean) {
        refreshLayout?.finishLoadMore()
        refreshLayout?.finishRefresh()
        refreshLayout?.setEnableLoadMore(hasMore)

        if (clean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        } else {
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }

        //todo 是否需要空页面
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

}