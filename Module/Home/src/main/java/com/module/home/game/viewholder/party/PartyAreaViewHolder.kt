package com.module.home.game.viewholder.party

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.component.busilib.model.PartyRoomInfoModel
import com.module.home.R
import com.module.home.game.adapter.ClickGameListener

class PartyAreaViewHolder(itemView: View,
                          listener: ClickGameListener) : RecyclerView.ViewHolder(itemView) {

    val titleTv: TextView = itemView.findViewById(R.id.title_tv)
    val moreTv: TextView = itemView.findViewById(R.id.more_tv)
    val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_view)
    val adapter: PartyAreaAdapter = PartyAreaAdapter()

    init {
        recyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        moreTv.setDebounceViewClickListener {
            listener.onPartyRoomListener()
        }

        adapter.clickListener = { position, model ->
            listener.onClickPartyRoom(position, model)
        }
    }

    fun bindData(list: List<PartyRoomInfoModel>?) {
        if (!list.isNullOrEmpty()) {
            adapter.mDataList.clear()
            adapter.mDataList.addAll(list)
            adapter.notifyDataSetChanged()
        } else {
            adapter.mDataList.clear()
            adapter.notifyDataSetChanged()
        }

    }

}