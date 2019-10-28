package com.module.playways.mic.home

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.playways.R


class RecomMicAdapter : RecyclerView.Adapter<RecomMicViewHolder>() {

    var mDataList = ArrayList<RecomMicRoomModel>()

    var isPlay = false    //标记是否播放
    var playPosition = -1   //标记播放的位置
    var playChildPosition = -1  //标记播放的是holder里面某个位置

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomMicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recom_item_layout, parent, false)
        return RecomMicViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecomMicViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecomMicViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

}
