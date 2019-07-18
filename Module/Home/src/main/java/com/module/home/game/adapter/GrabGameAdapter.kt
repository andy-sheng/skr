package com.module.home.game.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.log.MyLog
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.viewholder.GrabSelectViewHolder
import com.module.home.game.viewholder.grab.GrabCreateViewHolder
import com.module.home.game.viewholder.grab.GrabTagViewHolder
import java.util.ArrayList

class GrabGameAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList: MutableList<SpecialModel> = ArrayList()

    val ITEM_TYPE_CREATE_ROOM = 1
    val ITEM_TYPE_NORMLA_TAG = 2

    var onClickCreateListener: (() -> Unit)? = null
    var onClickTagListener: ((model: SpecialModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_CREATE_ROOM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grab_game_create_item_view_layout, parent, false)
            GrabCreateViewHolder(view, onClickCreateListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grab_game_tag_item_view_layout, parent, false)
            GrabTagViewHolder(view, onClickTagListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GrabTagViewHolder) {
            holder.bind(position, mDataList[position - 1])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            ITEM_TYPE_CREATE_ROOM
        } else {
            ITEM_TYPE_NORMLA_TAG
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size + 1
    }
}
