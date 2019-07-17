package com.module.home.game.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.viewholder.GrabSelectViewHolder

import java.util.ArrayList

class GrabSelectAdapter(var mRecyclerOnItemClickListener: RecyclerOnItemClickListener<SpecialModel>) : RecyclerView.Adapter<GrabSelectViewHolder>() {
    internal var mDataList: List<SpecialModel> = ArrayList()

    var dataList: List<SpecialModel>?
        get() = mDataList
        set(dataList) {
            if (dataList != null && dataList.size != 0) {
                mDataList = dataList
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrabSelectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_grab_select_item_view, parent, false)
        return GrabSelectViewHolder(view, mRecyclerOnItemClickListener)
    }

    override fun onBindViewHolder(holder: GrabSelectViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}
