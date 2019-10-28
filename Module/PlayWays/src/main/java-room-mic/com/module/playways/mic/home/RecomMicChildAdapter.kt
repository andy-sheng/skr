package com.module.playways.mic.home

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.R

class RecomMicChildAdapter : RecyclerView.Adapter<RecomMicChildAdapter.RecomChildViewHolder>() {

    var mDataList = ArrayList<UserInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recom_child_item_layout, parent, false)
        return RecomChildViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecomChildViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    inner class RecomChildViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        fun bindData(model: UserInfoModel, position: Int) {

        }

    }
}