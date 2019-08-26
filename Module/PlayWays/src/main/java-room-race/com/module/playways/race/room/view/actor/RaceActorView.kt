package com.module.playways.race.room.view.actor

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.LinearLayout
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.module.playways.R
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RacePlayerInfoModel

class RaceActorView(context: Context, mRoomData: RaceRoomData) : ConstraintLayout(context) {

    private val recyclerView: RecyclerView
    val adapter = RaceActorAdapter()

    init {
        View.inflate(context, R.layout.race_actor_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.adapter = adapter

        //todo 构造一点假数据
        adapter.mDataList.clear()
        for (i in 0..10) {
            val race = RacePlayerInfoModel()
            race.userID = MyUserInfoManager.getInstance().uid.toInt()
            race.userInfo = MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo)
            adapter.mDataList.add(race)
        }
        adapter.notifyDataSetChanged()
    }
}