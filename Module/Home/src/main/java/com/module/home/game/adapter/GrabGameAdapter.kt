package com.module.home.game.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.model.GrabSpecialModel
import com.module.home.game.viewholder.grab.GrabCreateViewHolder
import com.module.home.game.viewholder.grab.GrabTagViewHolder
import java.util.ArrayList

// type == 1 表示抢唱页面   type == 2 表示首页
class GrabGameAdapter(val type: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList: MutableList<GrabSpecialModel> = ArrayList()

    var onClickTagListener: ((model: GrabSpecialModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (type == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grab_game_tag_item_view_layout, parent, false)
            GrabTagViewHolder(view, onClickTagListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grab_home_tag_item_view_layout, parent, false)
            GrabTagViewHolder(view, onClickTagListener)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GrabTagViewHolder) {
            holder.bind(position, mDataList[position], type)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}
