package com.module.playways.relay.match.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.module.playways.R
import com.module.playways.room.song.model.SongModel

class RelayRoomAdapter : RecyclerView.Adapter<RelayRoomAdapter.RelayRoomViewHolder>() {

    var mDataList = ArrayList<SongModel>()
    var listener: RelayRoomListener? = null
    private val cardAdapterHelper = CardAdapterHelper(8, 12)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelayRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.relay_room_card_item_layout, parent, false)
        cardAdapterHelper.onCreateViewHolder(parent, view)
        return RelayRoomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RelayRoomViewHolder, position: Int) {
        cardAdapterHelper.onBindViewHolder(holder.itemView, position, itemCount, listener?.getRecyclerViewPosition() == position || mDataList.size == 1)
        holder.bindData(position, mDataList[position])
    }

    inner class RelayRoomViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        var mPos = -1
        var mModel: SongModel? = null

        fun bindData(position: Int, model: SongModel) {
            this.mPos = position
            this.mModel = model
        }
    }

    interface RelayRoomListener {
        fun getRecyclerViewPosition(): Int
    }
}