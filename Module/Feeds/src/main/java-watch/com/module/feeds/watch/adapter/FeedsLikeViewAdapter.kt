package com.module.feeds.watch.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.utils.U
import com.module.feeds.R
import com.module.feeds.watch.model.FeedsLikeModel
import com.module.feeds.watch.viewholder.FeedsLikeViewHolder

class FeedsLikeViewAdapter : RecyclerView.Adapter<FeedsLikeViewHolder>() {

    var mDataList = ArrayList<FeedsLikeModel>()
    var mCurrentPlayModel: FeedsLikeModel? = null

    var onClickPlayListener: ((model: FeedsLikeModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsLikeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_like_item_holder_layout, parent, false)
        return FeedsLikeViewHolder(view, onClickPlayListener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: FeedsLikeViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
        if (mDataList[position] == mCurrentPlayModel) {
            holder.mSongPlayIv.isSelected = true
            holder.mSongNameTv.setTextColor(Color.parseColor("#FFC15B"))
        } else {
            holder.mSongNameTv.setTextColor(U.getColor(R.color.black_trans_80))
            holder.mSongPlayIv.isSelected = false
        }
    }
}