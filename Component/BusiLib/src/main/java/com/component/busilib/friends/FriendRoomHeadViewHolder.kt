package com.component.busilib.friends

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.view.AnimateClickListener
import com.common.view.recyclerview.RecyclerOnItemClickListener

class FriendRoomHeadViewHolder(itemView: View, mOnItemClickListener: RecyclerOnItemClickListener<RecommendModel>) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                mOnItemClickListener.onItemClicked(view, 0, null)
            }
        })
    }
}
