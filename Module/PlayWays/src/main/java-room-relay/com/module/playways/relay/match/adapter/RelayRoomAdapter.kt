package com.module.playways.relay.match.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.view.AvatarLevelView
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.relay.match.model.RelayRecommendRoomInfo
import com.zq.live.proto.Common.ESex

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

        private val imageBg: ImageView = item.findViewById(R.id.image_bg)
        private val avatarLevel: AvatarView = item.findViewById(R.id.avatar_level)
        private val nicknameView: NickNameView = item.findViewById(R.id.nickname_view)
        private val levelTv: ExTextView = item.findViewById(R.id.level_tv)
        private val ageTv: ExTextView = item.findViewById(R.id.age_tv)
        private val sexTv: ExTextView = item.findViewById(R.id.sex_tv)

        private val bottomArea: ImageView = item.findViewById(R.id.bottom_area)
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

            val blueDrawable = DrawableCreator.Builder()
                    .setCornersRadius(16.dp().toFloat())
                    .setGradientColor(Color.parseColor("#DEF1FF"), Color.parseColor("#C4D7FF"))
                    .build()

            val blueBottomDrawable = DrawableCreator.Builder()
                    .setCornersRadius(16.dp().toFloat(), 16.dp().toFloat(), 0f, 0f)
                    .setGradientColor(Color.parseColor("#DEF1FF"), Color.parseColor("#D0E3FF"))
                    .build()

            val boyDrawable = DrawableCreator.Builder()
                    .setCornersRadius(4.dp().toFloat())
                    .setSolidColor(Color.parseColor("#6AB1DC"))
                    .build()

            val redDrawable = DrawableCreator.Builder()
                    .setCornersRadius(16.dp().toFloat())
                    .setGradientColor(Color.parseColor("#FFDEDE"), Color.parseColor("#FFC4DB"))
                    .build()

            val redBottomDrawable = DrawableCreator.Builder()
                    .setCornersRadius(16.dp().toFloat(), 16.dp().toFloat(), 0f, 0f)
                    .setGradientColor(Color.parseColor("#FFE4E4"), Color.parseColor("#FFE4EE"))
                    .build()

            val girlDrawable = DrawableCreator.Builder()
                    .setCornersRadius(4.dp().toFloat())
                    .setSolidColor(Color.parseColor("#FFA2D5"))
                    .build()

            when {
                model.user?.sex == ESex.SX_MALE.value -> {
                    imageBg.background = blueDrawable
                    bottomArea.background = blueBottomDrawable
                    sexTv.visibility = View.VISIBLE
                    sexTv.text = "男生"
                    sexTv.background = boyDrawable
                }
                model.user?.sex == ESex.SX_FEMALE.value -> {
                    imageBg.background = redDrawable
                    bottomArea.background = redBottomDrawable
                    sexTv.visibility = View.VISIBLE
                    sexTv.text = "女生"
                    sexTv.background = girlDrawable
                }
                else -> {
                    sexTv.visibility = View.GONE
                }
            }

            levelTv.text = model.user?.ranking?.rankingDesc
            avatarLevel.bindData(model.user)
            nicknameView.setHonorText(model.user?.nicknameRemark, model.user?.honorInfo)
            songNameTv.text = "《${model.item?.itemName}》"
            if (!TextUtils.isEmpty(model.user?.ageStageString)) {
                ageTv.visibility = View.VISIBLE
                ageTv.text = model.user?.ageStageString
            } else {
                ageTv.visibility = View.GONE
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