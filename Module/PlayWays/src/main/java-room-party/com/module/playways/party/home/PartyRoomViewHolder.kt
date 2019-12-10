package com.module.playways.party.home

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class PartyRoomViewHolder(item: View, var listener: ((position: Int, model: PartyRoomInfoModel?) -> Unit)?) : RecyclerView.ViewHolder(item) {

    private val avatarSdv: SimpleDraweeView = item.findViewById(R.id.avatar_sdv)
    private val roomNameTv: TextView = item.findViewById(R.id.room_name_tv)
    private val compereTv: TextView = item.findViewById(R.id.compere_tv)
    private val tagTv: ExTextView = item.findViewById(R.id.tag_tv)
    private val divider: View = item.findViewById(R.id.divider)
    private val roomPlayerNumTv: TextView = item.findViewById(R.id.room_player_num_tv)
    private val roomDescTv: TextView = item.findViewById(R.id.room_desc_tv)

    var mPos = -1
    var mModel: PartyRoomInfoModel? = null

    init {
        item.setAnimateDebounceViewClickListener { listener?.invoke(mPos, mModel) }
    }

    fun bindData(position: Int, model: PartyRoomInfoModel) {
        this.mModel = model
        this.mPos = position

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
                1 -> tagTv.background = PartyRoomAdapter.blueDrawable
                2 -> tagTv.background = PartyRoomAdapter.redDrawable
                0 -> tagTv.background = PartyRoomAdapter.greenDrawable
            }
        } else {
            tagTv.visibility = View.GONE
        }
        roomPlayerNumTv.text = "${model.playerNum?.toString()}人在线"
    }

}