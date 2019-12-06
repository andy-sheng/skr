package com.module.playways.party.room.seat

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.playways.R
import com.module.playways.party.room.model.PartyActorInfoModel

class PartySeatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_NORMAL = 1  // 有人的席位
    val VIEW_TYPE_EMPTY = 2  // 空席位 (分为空的和被关闭的两种)

    var mDataList = HashMap<Int, PartyActorInfoModel>()  // 席位对应的嘉宾

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_NORMAL) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.party_seat_item_view_layout, parent, false)
            SeatViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.party_seat_empty_item_layout, parent, false)
            EmptySeatViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataList[position] == null) {
            VIEW_TYPE_EMPTY
        } else if (mDataList[position]?.seat?.seatStatus == 2) {
            VIEW_TYPE_EMPTY
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun getItemCount(): Int {
        return 6
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (holder is SeatViewHolder) {
            if (payloads.isEmpty()) {
                holder.bindData(position, mDataList[position])
            } else {
                payloads.forEach { refreshType ->
                    if (refreshType is Int) {
                        when (refreshType) {
                            REFRESH_MUTE -> {
                                holder.refreshMute()
                            }
                            REFRESH_EMOJI -> {

                            }
                        }
                    }
                }
            }
        } else if (holder is EmptySeatViewHolder) {
            holder.bindData(position, mDataList[position])
        }
    }


    companion object {
        const val REFRESH_MUTE = 1   // 局部刷新，静音
        const val REFRESH_EMOJI = 2   // 局部刷新，表情
    }

}