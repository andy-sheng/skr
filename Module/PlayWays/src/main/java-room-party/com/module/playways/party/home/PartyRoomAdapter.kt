package com.module.playways.party.home

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.core.userinfo.model.ClubInfo
import com.component.busilib.model.PartyRoomInfoModel
import com.module.playways.R

class PartyRoomAdapter(var listener: Listener, val type: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var mClubList = ArrayList<ClubInfo>()  // 家族
    var mDataList = ArrayList<PartyRoomInfoModel>()  // 房间

    private val ITEM_TYPE_CLUB = 1
    private val ITEM_TYPE_ROOM = 2
    private val ITEM_TYPE_EMPTY_ROOM = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_ROOM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_room_view_item_layout, parent, false)
                PartyRoomViewHolder(view, listener)
            }
            ITEM_TYPE_EMPTY_ROOM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_empty_room_layout, parent, false)
                PartyEmptyRoomViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_club_view_layout, parent, false)
                PartyClubViewHolder(view, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        if (type == PartyRoomView.TYPE_GAME_HOME) {
            if (mDataList.size == 0) {
                return 2
            }
            return mDataList.size + 1
        } else {
            if (mDataList.size == 0) {
                return 1
            }
            return mDataList.size
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (type == PartyRoomView.TYPE_GAME_HOME) {
            when {
                position == 0 -> ITEM_TYPE_CLUB
                mDataList.size == 0 -> ITEM_TYPE_EMPTY_ROOM
                else -> ITEM_TYPE_ROOM
            }
        } else {
            // type == PartyRoomView.TYPE_PARTY_HOME
            when {
                mDataList.size == 0 -> ITEM_TYPE_EMPTY_ROOM
                else -> ITEM_TYPE_ROOM
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PartyRoomViewHolder) {
            if (type == PartyRoomView.TYPE_GAME_HOME) {
                holder.bindData(position, mDataList[position - 1])
            } else {
                holder.bindData(position, mDataList[position])
            }
        } else if (holder is PartyClubViewHolder) {
            holder.bindData(mClubList)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manger = recyclerView.layoutManager
        if (manger is GridLayoutManager) {
            manger.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (getItemViewType(position)) {
                        // 总数是3:表示当前holder在3中占几个
                        ITEM_TYPE_CLUB -> 2
                        ITEM_TYPE_EMPTY_ROOM -> 2
                        else -> 1
                    }
                }
            }
        }
    }

    interface Listener {
        fun onClickRoom(position: Int, model: PartyRoomInfoModel?)
        fun onClickClub(position: Int, clubInfo: ClubInfo?)
        fun onClickClubMore()
    }
}