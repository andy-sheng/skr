package com.module.club.home.viewholder

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.core.userinfo.model.ClubInfo
import com.module.club.R
import com.module.club.home.ClubHomeClickListener

class ClubListViewHolder(item: View, var listener: ClubHomeClickListener) : RecyclerView.ViewHolder(item) {

    private val recyclerView: RecyclerView = item.findViewById(R.id.recycler_view)
    private val adapter: ClubListAdapter

    init {
        adapter = ClubListAdapter(object : ClubListAdapter.Listener {
            override fun onClickItem(position: Int, model: ClubInfo?) {
                listener.onClickClubInfo(model)
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(item.context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }


    fun bindData(list: ArrayList<ClubInfo>, isClean: Boolean) {
        if (isClean) {
            adapter.mDataList.clear()
            adapter.mDataList.addAll(list)
            adapter.notifyDataSetChanged()
        } else {
            val oldSize = adapter.mDataList.size
            adapter.mDataList.clear()
            adapter.mDataList.addAll(list)
            adapter.notifyDataSetChanged()
        }
    }
}
