package com.module.feeds.make.make

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.module.feeds.R
import com.module.feeds.watch.viewholder.EmptyFeedWallHolder
import com.module.feeds.watch.viewholder.FeedsWallViewHolder
import com.module.feeds.watch.viewholder.FeedsWatchViewHolder

class FeedsLyricMakeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val labelTv: TextView
    val editEt: EditText
    val wordNumTv: TextView

    init {
        labelTv = itemView.findViewById(R.id.label_tv)
        editEt = itemView.findViewById(R.id.edit_et)
        wordNumTv = itemView.findViewById(R.id.word_num_tv)

    }

    fun bindData(pos: Int, item: LyricItem) {
        if (pos == 0) {
            labelTv.visibility = View.VISIBLE
            labelTv.text = "歌曲名"
        } else if (pos == 1) {
            labelTv.visibility = View.VISIBLE
            labelTv.text = "歌 词"
        } else {
            labelTv.visibility = View.GONE
        }
        editEt.setText(item.content)
    }
}