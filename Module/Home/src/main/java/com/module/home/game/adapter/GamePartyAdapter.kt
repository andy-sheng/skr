package com.module.home.game.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.utils.dp
import com.component.busilib.model.PartyRoomInfoModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.home.R

class GamePartyAdapter : RecyclerView.Adapter<GamePartyAdapter.GamePartyInfoViewHolder>() {
    var mDataList = ArrayList<PartyRoomInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamePartyInfoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_party_info_item_layout, parent, false)
        return GamePartyInfoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: GamePartyInfoViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class GamePartyInfoViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val partyLogo: SimpleDraweeView = item.findViewById(R.id.party_logo)
        val partyName: TextView = item.findViewById(R.id.party_name)
        val partyDesc: TextView = item.findViewById(R.id.party_desc)

        var mPos = -1
        var mModel: PartyRoomInfoModel? = null

        fun bindData(position: Int, model: PartyRoomInfoModel) {
            this.mPos = position
            this.mModel = model

            partyName.text = model.roomName
            partyDesc.text = model.gameName
            AvatarUtils.loadAvatarByUrl(partyLogo,
                    AvatarUtils.newParamsBuilder(model.avatarUrl)
                            .setCircle(false)
                            .setBorderColor(Color.WHITE)
                            .setBorderWidth(1.dp().toFloat())
                            .setCornerRadius(8.dp().toFloat())
                            .build())

        }
    }
}