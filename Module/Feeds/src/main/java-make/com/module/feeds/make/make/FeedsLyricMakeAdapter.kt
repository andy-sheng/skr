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

    fun setData(lyrics: ArrayList<LyricItem>) {
        this.lyrics.clear()
        this.lyrics.addAll(lyrics)
        notifyDataSetChanged()
    }

    fun getData():ArrayList<LyricItem> {
        return this.lyrics
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feeds_lyric_make_item_layout, parent, false)
        return FeedsLyricMakeHolder(view)
    }

    override fun getItemCount(): Int {
        return lyrics.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is FeedsLyricMakeHolder) {
            holder.bindData(position, lyrics[position])
        }

    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}

class LyricItem(var type: Int, var content: String) {
    companion object {
        val TYPE_TITLE = 1
        val TYPE_NORMAL = 2
    }

    var newContent = content
    var startTs = 0
    var endTs = 0
}