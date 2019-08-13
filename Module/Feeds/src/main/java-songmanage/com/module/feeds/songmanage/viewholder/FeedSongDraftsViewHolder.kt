package com.module.feeds.songmanage.viewholder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.songmanage.adapter.FeedSongDraftsListener

class FeedSongDraftsViewHolder(item: View, listener: FeedSongDraftsListener) : RecyclerView.ViewHolder(item) {

    val songSelectTv: ExTextView = item.findViewById(R.id.song_select_tv)
    val songNameTv: TextView = item.findViewById(R.id.song_name_tv)
    val songDescTv: TextView = item.findViewById(R.id.song_desc_tv)

    var mPosition: Int = 0
    var mModel: FeedsMakeModel? = null

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

    fun bindData(position: Int, model: FeedsMakeModel) {
        this.mPosition = position
        this.mModel = model

        if (!TextUtils.isEmpty(model.songModel?.workName)) {
            songNameTv.text = "《${model.songModel?.workName}》"
        } else {
            songNameTv.text = "《${model.songModel?.songTpl?.songName}》"
        }

        songDescTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model?.draftUpdateTs, System.currentTimeMillis())
        if (TextUtils.isEmpty(model?.audioUploadUrl)) {
            songSelectTv.text = "演唱"
        } else {
            songSelectTv.text = "发布"
        }
    }

}