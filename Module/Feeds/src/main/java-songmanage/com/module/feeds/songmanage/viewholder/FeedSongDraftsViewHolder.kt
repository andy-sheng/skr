package com.module.feeds.songmanage.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.feeds.R
import com.module.feeds.songmanage.adapter.FeedSongDraftsListener
import com.module.feeds.songmanage.adapter.FeedSongManageListener
import com.module.feeds.songmanage.model.FeedSongDraftsModel

class FeedSongDraftsViewHolder(item: View, listener: FeedSongDraftsListener) : RecyclerView.ViewHolder(item) {

    val songSelectTv: ExTextView = item.findViewById(R.id.song_select_tv)
    val songNameTv: TextView = item.findViewById(R.id.song_name_tv)
    val songDescTv: TextView = item.findViewById(R.id.song_desc_tv)

    var mPosition: Int = 0
    var mModel: FeedSongDraftsModel? = null

    init {

        songSelectTv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener.onClickSing(mPosition, mModel)
            }
        })

        item.setOnLongClickListener {
            listener.onLongClick(mPosition, mModel)
            false
        }

    }

    fun bindData(position: Int, model: FeedSongDraftsModel) {
        this.mPosition = position
        this.mModel = model

        songNameTv.text = model.workName
        songDescTv.text = "刚刚"
    }

}