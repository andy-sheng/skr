package com.module.playways.race.room.view.actor

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ScoreStateModel
import com.common.core.userinfo.model.UserInfoModel
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.module.playways.R
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.event.UpdateAudienceCountEvent
import com.module.playways.race.room.model.RacePlayerInfoModel
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener
import com.zq.live.proto.RaceRoom.ERUserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class RaceActorView(context: Context, val type: Int, val mRoomData: RaceRoomData) : ConstraintLayout(context), CoroutineScope by MainScope() {

    companion object {
        const val TYPE_ACTOR = 1    // 选手
        const val TYPE_AUDIENCE = 2   //观众
    }

    private val smartRefresh: SmartRefreshLayout
    private val recyclerView: RecyclerView

    val adapter = RaceActorAdapter(mRoomData)

    private val userServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    private val roomRaceServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)
    private var offset = 0
    private val mCnt = 30
    private var hasMore = true

    init {
        View.inflate(context, R.layout.race_actor_view_layout, this)

        smartRefresh = rootView.findViewById(R.id.smart_refresh)
        recyclerView = rootView.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.adapter = adapter

        smartRefresh.apply {
            if (type == TYPE_AUDIENCE) {
                setEnableLoadMore(true)
                setEnableRefresh(false)
                setEnableLoadMoreWhenContentNotFull(false)
                setEnableOverScrollDrag(false)

                setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
                    override fun onLoadMore(refreshLayout: RefreshLayout) {
                        getUserLists(offset, false)
                    }

                    override fun onRefresh(refreshLayout: RefreshLayout) {

                    }
                })
            } else {
                setEnableLoadMore(false)
                setEnableRefresh(false)
                setEnableLoadMoreWhenContentNotFull(false)
                setEnableOverScrollDrag(false)
            }
        }
    }


    fun initData() {
        if (type == TYPE_AUDIENCE) {
            smartRefresh.setEnableLoadMore(true)
        }
        getUserLists(0, true)
    }

    fun getUserLists(off: Int, isClean: Boolean) {
        if (type == TYPE_ACTOR) {
            // 演唱参与的人
            val list = mRoomData.getPlayerAndWaiterInfoList()
            launch {
                val userIDs = ArrayList<Int>()
                for (model in list) {
                    userIDs.add(model.userID)
                }
                val result = subscribe { userServerApi.getRankings(userIDs) }
                if (result.errno == 0) {
                    val scoreList = JSON.parseArray(result.data.getString("items"), ScoreStateModel::class.java)
                    // 补充演唱参与人的段位信息
                    val map = HashMap<Int, ScoreStateModel>()
                    if (!scoreList.isNullOrEmpty()) {
                        for (model in scoreList) {
                            map[model.userID] = model
                        }
                    }
                    if (!list.isNullOrEmpty()) {
                        for (model in list) {
                            model.userInfo?.ranking = map[model.userID]
                            model.fakeUserInfo = mRoomData.getFakeInfo(model.userID)
                            model.isFake = mRoomData.isFakeForMe(model.userID)
                        }
                    }
                    addUserList(list, isClean)
                } else {

                }
            }
        } else {
            // 观众
            launch {
                val result = subscribe { roomRaceServerApi.getAudienceList(mRoomData.gameId, off, mCnt) }
                if (result.errno == 0) {
                    val userInfoList = JSON.parseArray(result.data.getString("audiences"), UserInfoModel::class.java)
                    offset = result.data.getIntValue("offset")
                    hasMore = result.data.getBooleanValue("hasMore")
                    val totalCount = result.data.getIntValue("total")
                    EventBus.getDefault().post(UpdateAudienceCountEvent(totalCount))
                    var list = ArrayList<RacePlayerInfoModel>()
                    userInfoList?.forEach {
                        val model = RacePlayerInfoModel()
                        model.userInfo = it
                        model.role = ERUserRole.ERUR_AUDIENCE.value
                        model.fakeUserInfo = null
                        list.add(model)
                    }
                    smartRefresh.setEnableLoadMore(hasMore)
                    smartRefresh.finishLoadMore()
                    smartRefresh.finishRefresh()
                    addUserList(list, isClean)
                } else {
                    smartRefresh.finishLoadMore()
                    smartRefresh.finishRefresh()
                }
            }
        }
    }

    private fun addUserList(list: List<RacePlayerInfoModel>?, clean: Boolean) {
        if (clean) {
            adapter.mDataList.clear()
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
            }
            adapter.notifyDataSetChanged()
        } else {
            if (!list.isNullOrEmpty()) {
                adapter.mDataList.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun destory() {
        cancel()
    }
}