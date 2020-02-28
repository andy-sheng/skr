package com.module.playways.party.home.partyroom

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.component.busilib.model.PartyRoomInfoModel
import com.module.playways.R

class PartyRoomAdapter(var listener: Listener, val type: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<PartyRoomInfoModel>()  // 房间

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_room_view_item_layout, parent, false)
        return PartyRoomViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PartyRoomViewHolder) {
            holder.bindData(position, mDataList[position])
        }
    }

    interface Listener {
        fun onClickRoom(position: Int, model: PartyRoomInfoModel?)
    }
}