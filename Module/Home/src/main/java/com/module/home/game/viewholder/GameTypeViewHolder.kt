package com.module.home.game.viewholder

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.widget.ImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.adapter.ClickGameListener
import com.module.home.game.adapter.GrabGameAdapter
import com.module.home.game.model.GameTypeModel
import com.module.home.game.model.GrabSpecialModel
import kotlinx.android.synthetic.main.friend_room_view_layout.view.*

class GameTypeViewHolder(itemView: View,
                         val listener: ClickGameListener) : RecyclerView.ViewHolder(itemView) {

    val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view)
    val mGrabGameAdapter = GrabGameAdapter(2)

    var mGameTypeModel: GameTypeModel? = null

    init {
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        mGrabGameAdapter.onClickTagListener = {
            when (it?.type) {
                GrabSpecialModel.TBT_PLAYBOOK -> listener.onBattleRoomListener()
                GrabSpecialModel.TBT_STANDCREATE -> listener.onCreateRoomListener()
                GrabSpecialModel.TBT_DOUBLECHAT -> listener.onDoubleRoomListener()
                GrabSpecialModel.TBT_RACE -> listener.onPkRoomListener()
                GrabSpecialModel.TBT_SPECIAL -> listener.onGrabRoomListener(it?.model)
            }
        }
        recyclerView.adapter = mGrabGameAdapter
    }

    fun bindData(gameTypeModel: GameTypeModel) {
        this.mGameTypeModel = gameTypeModel
        gameTypeModel.mSpecialModel?.let {
            mGrabGameAdapter.mDataList.clear()
            mGrabGameAdapter.mDataList.addAll(it)
            mGrabGameAdapter.notifyDataSetChanged()
        }
    }
}