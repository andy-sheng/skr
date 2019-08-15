package com.module.feeds.songmanage.viewholder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.feeds.R
import com.module.feeds.songmanage.adapter.FeedSongManageListener
import com.module.feeds.songmanage.model.FeedSongInfoModel
import com.module.feeds.watch.model.FeedSongModel

class FeedSongViewHolder(itemView: View, val listener: FeedSongManageListener) : RecyclerView.ViewHolder(itemView) {

    val songSelectTv: ExTextView = itemView.findViewById(R.id.song_select_tv)
    val songNameTv: TextView = itemView.findViewById(R.id.song_name_tv)
    val songDescTv: TextView = itemView.findViewById(R.id.song_desc_tv)


    var mModel: FeedSongInfoModel? = null
    var mPosition: Int = 0

    init {
        songSelectTv.setOnClickListener(object : AnimateClickListener() {
            override fun click(v: View?) {
                listener.onClickSing(mPosition, mModel)
            }
        })
    }

    fun bindData(pos: Int, model: FeedSongInfoModel) {
        this.mPosition = pos
        this.mModel = model

        songNameTv.text = "《${model.song?.songTpl?.songName}》"
        if (!TextUtils.isEmpty(model.song?.songTpl?.getSongDesc())) {
            songDescTv.visibility = View.VISIBLE
            songDescTv.text = model.song?.songTpl?.getSongDesc()
        } else {
            songDescTv.visibility = View.GONE
        }

    }
}