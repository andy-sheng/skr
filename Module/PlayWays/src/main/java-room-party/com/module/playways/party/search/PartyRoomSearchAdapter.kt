package com.module.playways.party.search

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.ClubInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.model.PartyRoomInfoModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class PartyRoomSearchAdapter(var listener: Listener) : RecyclerView.Adapter<PartyRoomSearchAdapter.PartyRoomSearchViewHolder>() {

    var mDataList = ArrayList<PartyRoomInfoModel>()  // 房间

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyRoomSearchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_room_search_item_layout, parent, false)
        return PartyRoomSearchViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PartyRoomSearchViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }


    inner class PartyRoomSearchViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val roomLogoSdv: SimpleDraweeView = item.findViewById(R.id.room_logo_sdv)
        private val roomNameTv: TextView = item.findViewById(R.id.room_name_tv)
        private val roomDescTv: ExTextView = item.findViewById(R.id.room_desc_tv)
        private val roomNumTv: TextView = item.findViewById(R.id.room_num_tv)
        private val roomIdTv: ExTextView = item.findViewById(R.id.room_id_tv)
        private val divider: View = item.findViewById(R.id.divider)

        var mModel: PartyRoomInfoModel? = null
        var mPos = -1

        init {
            item.setDebounceViewClickListener {
                listener.onClickItem(mPos, mModel)
            }
        }

        fun bindData(model: PartyRoomInfoModel, position: Int) {
            this.mModel = model
            this.mPos = position

            AvatarUtils.loadAvatarByUrl(roomLogoSdv,
                    AvatarUtils.newParamsBuilder(model.avatarUrl)
                            .setCircle(false)
                            .setBorderColor(Color.WHITE)
                            .setBorderWidth(1.dp().toFloat())
                            .setCornerRadius(8.dp().toFloat())
                            .build())
            roomNameTv.text = model.roomName
            roomDescTv.text = model.gameName
            roomNumTv.text = model.playerNum.toString()
            roomIdTv.text = "房间ID:${model.roomID}"
        }
    }

    interface Listener {
        fun onClickItem(position: Int, model: PartyRoomInfoModel?)
    }
}