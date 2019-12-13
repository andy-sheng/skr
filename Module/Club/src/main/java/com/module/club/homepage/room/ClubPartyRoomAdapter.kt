package com.module.club.homepage.room

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.model.PartyRoomInfoModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.club.R

class ClubPartyRoomAdapter : RecyclerView.Adapter<ClubPartyRoomAdapter.RoomViewHolder>() {

    var mDataList = ArrayList<PartyRoomInfoModel>()

    val blueDrawable = DrawableCreator.Builder()
            .setShape(DrawableCreator.Shape.Rectangle)
            .setSolidColor(Color.parseColor("#A5D7F4"))
            .setCornersRadius(4.dp().toFloat())
            .build()

    val redDrawable = DrawableCreator.Builder()
            .setShape(DrawableCreator.Shape.Rectangle)
            .setSolidColor(Color.parseColor("#F4B2E2"))
            .setCornersRadius(4.dp().toFloat())
            .build()

    val greenDrawable = DrawableCreator.Builder()
            .setShape(DrawableCreator.Shape.Rectangle)
            .setSolidColor(Color.parseColor("#A1D299"))
            .setCornersRadius(4.dp().toFloat())
            .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.club_party_room_view_item_layout, parent, false)
        return RoomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class RoomViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarSdv: SimpleDraweeView = item.findViewById(R.id.avatar_sdv)
        private val roomNameTv: TextView = item.findViewById(R.id.room_name_tv)
        private val compereTv: TextView = item.findViewById(R.id.compere_tv)
        private val tagTv: ExTextView = item.findViewById(R.id.tag_tv)
        private val divider: View = item.findViewById(R.id.divider)
        private val roomPlayerNumTv: TextView = item.findViewById(R.id.room_player_num_tv)
        private val roomDescTv: TextView = item.findViewById(R.id.room_desc_tv)

        var mPos = -1
        var mModel: PartyRoomInfoModel? = null

        fun bindData(position: Int, model: PartyRoomInfoModel) {
            this.mPos = position
            this.mModel = model

            AvatarUtils.loadAvatarByUrl(avatarSdv,
                    AvatarUtils.newParamsBuilder(model.avatarUrl)
                            .setCircle(false)
                            .setBorderColor(Color.WHITE)
                            .setBorderWidth(1.dp().toFloat())
                            .setCornerRadius(8.dp().toFloat())
                            .build())
            roomNameTv.text = model.roomName
            compereTv.text = "主持：${UserInfoManager.getInstance().getRemarkName(model.ownerID
                    ?: 0, model.ownerName)}"
            if (!TextUtils.isEmpty(model.gameName)) {
                roomDescTv.visibility = View.VISIBLE
                roomDescTv.text = model.gameName
            } else {
                roomDescTv.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(model.topicName)) {
                tagTv.visibility = View.VISIBLE
                tagTv.text = model.topicName
                when (position % 3) {
                    1 -> tagTv.background = blueDrawable
                    2 -> tagTv.background = redDrawable
                    0 -> tagTv.background = greenDrawable
                }
            } else {
                tagTv.visibility = View.GONE
            }
            roomPlayerNumTv.text = "${model.playerNum?.toString()}人在线"

        }
    }
}