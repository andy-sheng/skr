package com.module.feeds.songmanage.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.songmanage.model.FeedSongInfoModel
import com.module.feeds.songmanage.viewholder.FeedSongViewHolder
import com.module.feeds.watch.model.FeedSongModel

class FeedSongManageAdapter(val listener: FeedSongManageListener) : RecyclerView.Adapter<FeedSongViewHolder>() {

    var mDataList = ArrayList<FeedSongInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedSongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_song_item_layout, parent, false)
        return FeedSongViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedSongViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    override fun onBindViewHolder(holder: FeedSongViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }
}

interface FeedSongManageListener {
    fun onClickSing(position: Int, model: FeedSongInfoModel?)
}