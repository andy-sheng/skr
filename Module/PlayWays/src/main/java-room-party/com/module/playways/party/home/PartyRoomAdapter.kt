package com.module.playways.party.home

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.utils.dp
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.R

class PartyRoomAdapter : RecyclerView.Adapter<PartyRoomViewHolder>() {

    var mDataList = ArrayList<PartyRoomInfoModel>()
    var listener: ((position: Int, model: PartyRoomInfoModel?) -> Unit)? = null

    companion object {
        val blueDrawable = DrawableCreator.Builder()
                .setShape(DrawableCreator.Shape.Rectangle)
                .setSolidColor(Color.parseColor("#A5D7F4"))
                .setCornersRadius(4.dp().toFloat())
                .build()

        val redDrawable = DrawableCreator.Builder()
                .setShape(DrawableCreator.Shape.Rectangle)
                .setSolidColor(Color.parseColor("#F4B2E2"))
                .setCornersRadius(4.dp().toFloat())
                .build()

        val greenDrawable = DrawableCreator.Builder()
                .setShape(DrawableCreator.Shape.Rectangle)
                .setSolidColor(Color.parseColor("#A1D299"))
                .setCornersRadius(4.dp().toFloat())
                .build()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_room_view_item_layout, parent, false)
        return PartyRoomViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PartyRoomViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }
}