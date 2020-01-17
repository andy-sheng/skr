package com.module.playways.party.home

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.model.PartyRoomInfoModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class PartyRoomViewHolder(item: View, var listener: PartyRoomAdapter.Listener) : RecyclerView.ViewHolder(item) {

    private val contentBg: ConstraintLayout = item.findViewById(R.id.content_bg)
    private val topIconIv: ImageView = item.findViewById(R.id.top_icon_iv)
    private val avatarSdv: SimpleDraweeView = item.findViewById(R.id.avatar_sdv)
    private val roomNameTv: TextView = item.findViewById(R.id.room_name_tv)
    private val tagTv: ExTextView = item.findViewById(R.id.tag_tv)
    private val bottomArea: ExImageView = item.findViewById(R.id.bottom_area)
    private val roomPlayerNumTv: TextView = item.findViewById(R.id.room_player_num_tv)
    private val roomDescTv: TextView = item.findViewById(R.id.room_desc_tv)
    private val widgetSdv: SimpleDraweeView = item.findViewById(R.id.widget_sdv)

    var mPos = -1
    var mModel: PartyRoomInfoModel? = null

    init {
        item.setAnimateDebounceViewClickListener { listener.onClickRoom(mPos, mModel) }
    }

    fun bindData(position: Int, model: PartyRoomInfoModel) {
        this.mModel = model
        this.mPos = position

        var contentDrawable: Drawable?
        var topDrawable: Drawable?
        var bottomDrawable: Drawable?
        var tagDrawable: Drawable?
        var bottomTextColor: Int?
        when (position % 4) {
            1 -> {
                contentDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#E5FFE8"))
                        .setCornersRadius(8.dp().toFloat())
                        .build()
                topDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#A1D299"))
                        .build()
                bottomDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#D2FAD7"))
                        .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                        .build()
                tagDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#A1D299"))
                        .setCornersRadius(4.dp().toFloat())
                        .build()
                bottomTextColor = Color.parseColor("#7EC473")
            }
            2 -> {
                contentDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#FFF8D5"))
                        .setCornersRadius(8.dp().toFloat())
                        .build()
                topDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#EACD92"))
                        .build()
                bottomDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#F5ECBD"))
                        .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                        .build()
                tagDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#EACD92"))
                        .setCornersRadius(4.dp().toFloat())
                        .build()
                bottomTextColor = Color.parseColor("#CEAB65")
            }
            3 -> {
                contentDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#FFE5E5"))
                        .setCornersRadius(8.dp().toFloat())
                        .build()
                topDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#EBB2B2"))
                        .build()
                bottomDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#F7D7D7"))
                        .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                        .build()
                tagDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#EBB2B2"))
                        .setCornersRadius(4.dp().toFloat())
                        .build()
                bottomTextColor = Color.parseColor("#D88383")
            }
            else -> {
                contentDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#D5E9FF"))
                        .setCornersRadius(8.dp().toFloat())
                        .build()
                topDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#A7C7EB"))
                        .build()
                bottomDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#C2DAF5"))
                        .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                        .build()
                tagDrawable = DrawableCreator.Builder()
                        .setShape(DrawableCreator.Shape.Rectangle)
                        .setSolidColor(Color.parseColor("#A7C7EB"))
                        .setCornersRadius(4.dp().toFloat())
                        .build()
                bottomTextColor = Color.parseColor("#74A3D6")
            }
        }
        contentBg.background = contentDrawable
        topIconIv.background = topDrawable
        bottomArea.background = bottomDrawable
        roomDescTv.setTextColor(bottomTextColor)
        roomPlayerNumTv.setTextColor(bottomTextColor)

        AvatarUtils.loadAvatarByUrl(avatarSdv,
                AvatarUtils.newParamsBuilder(model.avatarUrl)
                        .setCircle(false)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(1.dp().toFloat())
                        .setCornerRadius(8.dp().toFloat())
                        .build())
        if (model.roomtype == 1) {
            roomNameTv.text = "个人房 ${model.roomName}"
        } else {
            roomNameTv.text = "家族房 ${model.roomName}"
        }

        if (!TextUtils.isEmpty(model.gameName)) {
            roomDescTv.visibility = View.VISIBLE
            roomDescTv.text = model.gameName
        } else {
            roomDescTv.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(model.topicName)) {
            tagTv.visibility = View.VISIBLE
            tagTv.text = model.topicName
            tagTv.background = tagDrawable
        } else {
            tagTv.visibility = View.GONE
        }
        roomPlayerNumTv.text = "${model.playerNum?.toString()}"

        if (TextUtils.isEmpty(model.widgetUrl)) {
            widgetSdv.visibility = View.GONE
        } else {
            widgetSdv.visibility = View.VISIBLE
            FrescoWorker.loadImage(widgetSdv, ImageFactory.newPathImage(model.widgetUrl)
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_320)
                    .build<BaseImage>())
        }
    }

}

class PartyEmptyRoomViewHolder(item: View) : RecyclerView.ViewHolder(item)