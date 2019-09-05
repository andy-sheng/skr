package com.module.playways.race.room.view.actor

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.component.person.model.ScoreStateModel
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RacePlayerInfoModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class RaceActorView(context: Context, val mRoomData: RaceRoomData) : ConstraintLayout(context), CoroutineScope by MainScope() {

    private val recyclerView: RecyclerView
    val adapter = RaceActorAdapter(mRoomData)

    val userServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    init {
        View.inflate(context, R.layout.race_actor_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.adapter = adapter

        initData()
    }

    fun initData() {
        val list = ArrayList<RaceActorInfoModel>()
        val playList = mRoomData.getPlayerInfoList()
        playList?.let {
            for (model in it) {
                list.add(RaceActorInfoModel(model))
            }
        }

        launch {
            val userIDs = ArrayList<Int>()
            for (model in list) {
                userIDs.add(model.plyer.userID)
            }
            val result = subscribe { userServerApi.getRankings(userIDs) }
            if (result.errno == 0) {
                val scoreList = JSON.parseArray(result.data.getString("items"), ScoreStateModel::class.java)
                val map = HashMap<Int, ScoreStateModel>()
                if (!scoreList.isNullOrEmpty()) {
                    for (model in scoreList) {
                        map[model.userID] = model
                    }
                }
                if (!list.isNullOrEmpty()) {
                    for (model in list) {
                        model.scoreState = map[model.plyer.userID]
                    }
                }
                adapter.mDataList.clear()
                adapter.mDataList.addAll(list)
                adapter.notifyDataSetChanged()
            } else {

            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}