package com.module.feeds.make.make

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.feeds.R
import com.module.feeds.watch.viewholder.EmptyFeedWallHolder
import com.module.feeds.watch.viewholder.FeedsWallViewHolder
import com.module.feeds.watch.viewholder.FeedsWatchViewHolder

class FeedsLyricMakeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val lyrics: ArrayList<LyricItem> = ArrayList()

    var songName: String? = null

    fun setData(songName: String, lyrics: ArrayList<LyricItem>) {
        this.songName = songName
        this.lyrics.clear()
        this.lyrics.addAll(lyrics)
        notifyDataSetChanged()
    }

    fun getData(): ArrayList<LyricItem> {
        return this.lyrics
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_song_name_make_item_layout, parent, false)
            val h = FeedsSongNameMakeHolder(view)
            h.songNameChangeListener = {
                this.songName = it
            }
            return h
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_lyric_make_item_layout, parent, false)
            return FeedsLyricMakeHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return lyrics.size+1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FeedsLyricMakeHolder) {
            holder.bindData(position-1, lyrics[position-1])
        } else if (holder is FeedsSongNameMakeHolder) {
            holder.bindData(position, songName ?: "")
        }

    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return 1
        } else {
            return 2
        }
    }
}

class LyricItem(var content: String) {
    var newContent = content
    var startTs = 0
    var endTs = 0
}