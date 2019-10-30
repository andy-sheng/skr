package com.module.home.game.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.component.person.model.UserRankModel
import com.module.home.R
import com.module.home.game.model.GrabSpecialModel
import com.module.home.game.viewholder.grab.GameRaceViewHolder
import com.module.home.game.viewholder.grab.GameTagViewHolder
import java.util.ArrayList

// type == 1 表示抢唱页面   type == 2 表示首页
class GrabGameAdapter(val type: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList: MutableList<GrabSpecialModel> = ArrayList()
    var mReginDiff: UserRankModel? = null

    var onClickTagListener: ((model: GrabSpecialModel?) -> Unit)? = null

    // 针对首页加个类型
    val VIEW_TYPE_RACE = 1
    val VIEW_TYPE_NORMAL = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (type == 1) {
            // 留给之前等抢唱页面吧
            val view = LayoutInflater.from(parent.context).inflate(R.layout.grab_game_tag_item_view_layout, parent, false)
            GameTagViewHolder(view, onClickTagListener)
        } else {
            if (viewType == VIEW_TYPE_NORMAL) {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.game_home_tag_item_view_layout, parent, false)
                GameTagViewHolder(view, onClickTagListener)
            } else {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.game_home_race_tag_view_layout, parent, false)
                GameRaceViewHolder(view, onClickTagListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GameTagViewHolder) {
            holder.bind(position, mDataList[position], type)
        } else if (holder is GameRaceViewHolder) {
            holder.bind(position, mDataList[position], mReginDiff)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (type == 2) {
            return if (mDataList[position].type == GrabSpecialModel.TBT_RACE_TAB) {
                VIEW_TYPE_RACE
            } else {
                VIEW_TYPE_NORMAL
            }
        }
        return 0
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}
