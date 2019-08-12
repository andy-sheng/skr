package com.module.feeds.songmanage.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.songmanage.viewholder.FeedSongDraftsViewHolder

class FeedSongDraftsAdapter(val listener: FeedSongDraftsListener) : RecyclerView.Adapter<FeedSongDraftsViewHolder>() {

    private var mDataList = ArrayList<FeedsMakeModel>()

    fun setData(list: ArrayList<FeedsMakeModel>) {
        mDataList.clear()
        mDataList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedSongDraftsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_song_drafts_item_layout, parent, false)
        return FeedSongDraftsViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedSongDraftsViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    override fun onBindViewHolder(holder: FeedSongDraftsViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
    }

    fun delete( model: FeedsMakeModel) {
        if (mDataList.contains(model)) {
            mDataList.remove(model)
            notifyDataSetChanged()
        }
    }
}

interface FeedSongDraftsListener {
    fun onClickSing(position: Int, model: FeedsMakeModel?)

    fun onLongClick(position: Int, model: FeedsMakeModel?)
}