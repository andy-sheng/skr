package com.module.feeds.rank.holder

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.common.core.userinfo.UserInfoManager
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.module.feeds.R
import com.module.feeds.rank.adapter.FeedTagListener
import com.module.feeds.watch.model.FeedsWatchModel

class FeedDetailTagViewHolder(itemView: View, val listener: FeedTagListener) : RecyclerView.ViewHolder(itemView) {

    val rankSeqTv: TextView = itemView.findViewById(R.id.rank_seq_tv)
    val collectIcon: ExImageView = itemView.findViewById(R.id.collect_icon)
    val songNameTv: TextView = itemView.findViewById(R.id.song_name_tv)
    val songDescTv: TextView = itemView.findViewById(R.id.song_desc_tv)

    var mPosition = 0
    var mModel: FeedsWatchModel? = null

    init {
        collectIcon.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener.onClickCollect(mPosition, mModel)
            }
        })

        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener.onClickItem(mPosition, mModel)
            }
        })
    }

    fun bindData(pos: Int, mode: FeedsWatchModel) {
        this.mPosition = pos
        this.mModel = mode

        rankSeqTv.text = mode.rankSeq.toString()
        if (!TextUtils.isEmpty(mode.song?.workName)) {
            songNameTv.visibility = View.VISIBLE
            songNameTv.text = mode.song?.workName
        } else if (!TextUtils.isEmpty(mode.song?.songTpl?.songName)) {
            songNameTv.visibility = View.VISIBLE
            songNameTv.text = mode.song?.songTpl?.songName
        } else {
            songNameTv.visibility = View.GONE
        }
        if (mode.song?.needShareTag == true) {
            songDescTv.text = mode.song?.songTpl?.singer
        } else {
            songDescTv.text = UserInfoManager.getInstance().getRemarkName(mode.user?.userId
                    ?: 0, mode.user?.nickname)
        }

        refreshCollects()
    }

    // 刷新收藏状态
    fun refreshCollects() {
        var drawble = R.drawable.feed_not_collection
        if (mModel?.isCollected == true) {
            drawble = R.drawable.feed_collect_selected_icon
        }
        collectIcon.setImageResource(drawble)
    }
}