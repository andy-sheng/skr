package com.module.playways.party.home

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.core.userinfo.model.ClubInfo
import com.component.busilib.model.PartyRoomInfoModel
import com.module.playways.R

class PartyRoomAdapter(var listener: Listener, val type: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<PartyRoomInfoModel>()  // 房间

    private val ITEM_TYPE_ROOM = 1
    private val ITEM_TYPE_QUICK_KTV = 2
    private val ITEM_TYPE_QUICK_GAME_PK = 3
    private val ITEM_TYPE_EMPTY_ROOM = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_ROOM -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_room_view_item_layout, parent, false)
                PartyRoomViewHolder(view, listener)
            }
            ITEM_TYPE_QUICK_KTV -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_quick_ktv_item_layout, parent, false)
                PartyQuickKTVViewHolder(view, listener)
            }
            ITEM_TYPE_QUICK_GAME_PK -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_quick_game_pk_item_layout, parent, false)
                PartyQuickGamePKViewHolder(view, listener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_empty_room_layout, parent, false)
                PartyEmptyRoomViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        if (type == PartyRoomView.TYPE_GAME_HOME) {
            if (mDataList.size == 0) {
                return 2
            }
            return mDataList.size + 2
        } else {
            if (mDataList.size == 0) {
                return 2
            }
            return mDataList.size + 2
        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_TYPE_QUICK_KTV
            1 -> ITEM_TYPE_QUICK_GAME_PK
            else -> ITEM_TYPE_ROOM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PartyRoomViewHolder) {
            getItemModelByPosition(position)?.let {
                holder.bindData(position, it)
            }
        }
    }

    private fun getItemModelByPosition(position: Int): PartyRoomInfoModel? {
        return if (position >= 2) {
            mDataList[position - 2]
        } else {
            null
        }
    }

//    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
//        super.onAttachedToRecyclerView(recyclerView)
//        val manger = recyclerView.layoutManager
//        if (manger is GridLayoutManager) {
//            manger.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
//                override fun getSpanSize(position: Int): Int {
//                    return when (getItemViewType(position)) {
//                        // 总数是2:表示当前holder在2中占几个
//                        ITEM_TYPE_CLUB -> 2
//                        ITEM_TYPE_EMPTY_ROOM -> 2
//                        else -> 1
//                    }
//                }
//            }
//        }
//    }

    interface Listener {
        fun onClickRoom(position: Int, model: PartyRoomInfoModel?)
        fun onClickClub(position: Int, clubInfo: ClubInfo?)
        fun onClickClubMore()
        fun onClickQuickKTV()
        fun onClickQuickGamePK()
    }
}