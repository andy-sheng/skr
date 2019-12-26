package com.module.playways.relay.match.adapter

import android.graphics.Color
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
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.view.AvatarLevelView
import com.component.busilib.view.NickNameView
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.component.level.utils.LevelConfigUtils
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.relay.match.model.RelayRecommendRoomInfo
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.Common.ESex

class RelayRoomAdapter : RecyclerView.Adapter<RelayRoomAdapter.RelayRoomViewHolder>() {

    var mDataList = ArrayList<RelayRecommendRoomInfo>()
    var listener: RelayRoomListener? = null
    private val cardAdapterHelper = CardAdapterHelper(8, 12)

    val blueDrawable = DrawableCreator.Builder()
            .setCornersRadius(16.dp().toFloat())
            .setGradientColor(Color.parseColor("#FFFFFF"), Color.parseColor("#AFE1FF"))
            .build()

    val redDrawable = DrawableCreator.Builder()
            .setCornersRadius(16.dp().toFloat())
            .setGradientColor(Color.parseColor("#FFFFFF"), Color.parseColor("#FFD6E9"))
            .build()

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

        private val imageBg: ImageView = item.findViewById(R.id.image_bg)
        private val avatarLevel: AvatarLevelView = item.findViewById(R.id.avatar_level)
        private val nicknameView: NickNameView = item.findViewById(R.id.nickname_view)
        private val ageTv: ExTextView = item.findViewById(R.id.age_tv)
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

            if (model.user?.sex == ESex.SX_MALE.value) {
                imageBg.background = blueDrawable
            } else {
                imageBg.background = redDrawable
            }

            avatarLevel.bindData(model.user)
            nicknameView.setAllStateText(model.user)
            songNameTv.text = "《${model.item?.itemName}》"
            if (!TextUtils.isEmpty(model.user?.ageStageString)) {
                ageTv.visibility = View.VISIBLE
                ageTv.text = model.user?.ageStageString
            } else {
                ageTv.visibility = View.INVISIBLE
            }
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