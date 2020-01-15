package com.module.playways.mic.home

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.core.view.setAnimateDebounceViewClickListener
import com.module.playways.friendroom.FriendRoomAdapter

class FriendInviteViewHolder(itemView: View, var mOnItemClickListener: FriendRoomAdapter.FriendRoomClickListener) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.setAnimateDebounceViewClickListener {
            mOnItemClickListener.onClickInvite()
        }
    }
}