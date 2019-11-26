package com.module.playways.relay.match.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.component.busilib.view.NickNameView
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.relay.match.model.RelayRecommendRoomInfo
import com.module.playways.room.song.model.SongModel

class RelayRoomAdapter : RecyclerView.Adapter<RelayRoomAdapter.RelayRoomViewHolder>() {

    var mDataList = ArrayList<RelayRecommendRoomInfo>()
    var listener: RelayRoomListener? = null
    private val cardAdapterHelper = CardAdapterHelper(8, 12)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelayRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.relay_room_card_item_layout, parent, false)
        cardAdapterHelper.onCreateViewHolder(parent, view)
        return RelayRoomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RelayRoomViewHolder, position: Int) {
        cardAdapterHelper.onBindViewHolder(holder.itemView, position, itemCount, listener?.getRecyclerViewPosition() == position || mDataList.size == 1)
        holder.bindData(position, mDataList[position])
    }

    inner class RelayRoomViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)
        val levelBg: ImageView = item.findViewById(R.id.level_bg)
        val nicknameView: NickNameView = item.findViewById(R.id.nickname_view)
        val songNameTv: TextView = item.findViewById(R.id.song_name_tv)
        val recommendTagSdv: SimpleDraweeView = item.findViewById(R.id.recommend_tag_sdv)
        val joinTv: TextView = item.findViewById(R.id.join_tv)

        var mPos = -1
        var mModel: RelayRecommendRoomInfo? = null

        init {
            joinTv.setDebounceViewClickListener {
                listener?.selectRoom(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: RelayRecommendRoomInfo) {
            this.mPos = position
            this.mModel = model
        }
    }

    interface RelayRoomListener {
        fun getRecyclerViewPosition(): Int
        fun selectRoom(position: Int, model: RelayRecommendRoomInfo?)
    }
}