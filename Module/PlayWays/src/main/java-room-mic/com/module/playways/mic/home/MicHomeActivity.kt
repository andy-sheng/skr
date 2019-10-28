package com.module.playways.mic.home

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.recommend.RA
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleTagModel
import com.module.playways.mic.room.MicRoomServerApi
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import kotlinx.coroutines.launch

@Route(path = RouterConstants.ACTIVITY_MIC_HOME)
class MicHomeActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var quickBegin: ImageView
    lateinit var createRoom: ImageView
    lateinit var smartRefresh: SmartRefreshLayout
    lateinit var recyclerView: RecyclerView

    var adapter: RecomMicAdapter? = null

    val micRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    var offset = 0

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.mic_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        U.getStatusBarUtil().setTransparentBar(this, false)

        titlebar = findViewById(R.id.titlebar)
        quickBegin = findViewById(R.id.quick_begin)
        createRoom = findViewById(R.id.create_room)
        smartRefresh = findViewById(R.id.smart_refresh)
        recyclerView = findViewById(R.id.recycler_view)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }
        quickBegin.setAnimateDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_MIC_MATCH)
                    .navigation()
        }
        createRoom.setAnimateDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_CREATE_MIC_ROOM)
                    .navigation()
        }

        smartRefresh.apply {
            setEnableLoadMore(true)
            setEnableRefresh(false)
            setEnableLoadMoreWhenContentNotFull(false)
            setEnableOverScrollDrag(true)

            setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    // 加载更多
                    getRecomRoomList(offset, false)
                }

                override fun onRefresh(refreshLayout: RefreshLayout) {

                }
            })
        }

        adapter = RecomMicAdapter(object : RecomMicListener {
            override fun onClickEnterRoom(model: RecomMicInfoModel?, position: Int) {
                //todo 进入房间
            }

            override fun onClickUserVoice(model: RecomMicInfoModel?, position: Int, userInfoModel: UserInfoModel?, childPos: Int) {
                //todo 播放或者暂停声音
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        getRecomRoomList(0, true)
    }

    private fun getRecomRoomList(off: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getMicHomeRoomList", ControlType.CancelThis)) {
                micRoomServerApi.getMicHomeRoomList(off, RA.getTestList(), RA.getVars())
            }
            if (result.errno == 0) {
                offset = result.data.getIntValue("offset")
                val list = JSON.parseArray(result.data.getString("rooms"), RecomMicInfoModel::class.java)
                addRoomList(list, isClear)
            } else {
                smartRefresh.finishLoadMore()
                smartRefresh.finishRefresh()
            }
        }
    }

    private fun addRoomList(list: List<RecomMicInfoModel>?, isClear: Boolean) {
        if (isClear) {
            adapter?.mDataList?.clear()
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
            }
            adapter?.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                adapter?.mDataList?.addAll(list)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}