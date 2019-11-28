package com.module.playways.relay.match.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.component.busilib.view.NickNameView
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.component.level.utils.LevelConfigUtils
import com.facebook.drawee.drawable.ScalingUtils
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

    fun addData(list: List<RelayRecommendRoomInfo>) {
        if (list.isNotEmpty()) {
            val startNotifyIndex = if (mDataList.size > 0) mDataList.size - 1 else 0
            mDataList.addAll(list)
            notifyItemRangeChanged(startNotifyIndex, mDataList.size - startNotifyIndex)
        }
    }

    inner class RelayRoomViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)
        private val levelBg: ImageView = item.findViewById(R.id.level_bg)
        private val nicknameView: NickNameView = item.findViewById(R.id.nickname_view)
        private val songNameTv: TextView = item.findViewById(R.id.song_name_tv)
        private val recommendTagSdv: SimpleDraweeView = item.findViewById(R.id.recommend_tag_sdv)
        private val joinTv: TextView = item.findViewById(R.id.join_tv)

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

            AvatarUtils.loadAvatarByUrl(avatarIv,
                    AvatarUtils.newParamsBuilder(model.user?.avatar)
                            .setCircle(true)
                            .build())
            if (LevelConfigUtils.getImageResoucesLevel(model.user?.ranking?.mainRanking
                            ?: 0) != 0) {
                levelBg.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(model.user?.ranking?.mainRanking
                        ?: 0))
            }
            nicknameView.setAllStateText(model.user)
            songNameTv.text = model.item?.itemName

            if (!TextUtils.isEmpty(model.recommendTag?.url)) {
                recommendTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(recommendTagSdv, ImageFactory.newPathImage(model.recommendTag?.url)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                recommendTagSdv.visibility = View.GONE
            }

        }
    }

    interface RelayRoomListener {
        fun getRecyclerViewPosition(): Int
        fun selectRoom(position: Int, model: RelayRecommendRoomInfo?)
    }
}