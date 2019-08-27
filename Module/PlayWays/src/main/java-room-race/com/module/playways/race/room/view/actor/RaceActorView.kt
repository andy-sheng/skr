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

class RaceActorView(context: Context, val mRoomData: RaceRoomData) : ConstraintLayout(context) {

    private val recyclerView: RecyclerView
    val adapter = RaceActorAdapter()

    init {
        View.inflate(context, R.layout.race_actor_view_layout, this)

        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    fun initData() {
        val list = ArrayList<RaceActorInfoModel>()
        mRoomData.realRoundInfo?.playUsers?.let {
            for (model in it) {
                var isSing = false
                for (subRoundInfo in mRoomData.realRoundInfo!!.subRoundInfo) {
                    if (model.userID == subRoundInfo.userID) {
                        isSing = true
                    }
                }
                if (isSing) {
                    list.add(RaceActorInfoModel(model, 1))
                } else {
                    list.add(RaceActorInfoModel(model, 0))
                }
            }
        }
        mRoomData.realRoundInfo?.waitUsers?.let {
            for (model in it) {
                list.add(RaceActorInfoModel(model, 2))
            }
        }

        //todo 差一个段位信息
        adapter.mDataList.clear()
        adapter.mDataList.addAll(list)
        adapter.notifyDataSetChanged()
    }
}