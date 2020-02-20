package com.module.playways.friendroom

import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.level.utils.LevelConfigUtils
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class RecommendPartyViewHolder(item: View, roomListener: FriendRoomAdapter.FriendRoomClickListener) : RecyclerView.ViewHolder(item) {

    val contentBg: ConstraintLayout = item.findViewById(R.id.content_bg)
    val avatarSdv: SimpleDraweeView = item.findViewById(R.id.avatar_sdv)
    val levelBg: ImageView = item.findViewById(R.id.level_bg)
    val topIconIv: ImageView = item.findViewById(R.id.top_icon_iv)
    val roomNameTv: TextView = item.findViewById(R.id.room_name_tv)
    val recommendTagSdv: SimpleDraweeView = item.findViewById(R.id.recommend_tag_sdv)
    val tagTv: ExTextView = item.findViewById(R.id.tag_tv)
    val bottomArea: ExImageView = item.findViewById(R.id.bottom_area)
    val audienceIv: ImageView = item.findViewById(R.id.audience_iv)
    val roomPlayerNumTv: TextView = item.findViewById(R.id.room_player_num_tv)
    val roomDescTv: TextView = item.findViewById(R.id.room_desc_tv)

    private var mModel: RecommendRoomModel? = null
    private var mPos = -1

    init {
        item.setDebounceViewClickListener {
            roomListener.onClickPartyRoom(mPos, mModel)
        }
    }

    fun bindData(model: RecommendRoomModel, position: Int) {
        mModel = model
        mPos = position
        adjustBg(position)


        AvatarUtils.loadAvatarByUrl(avatarSdv,
                AvatarUtils.newParamsBuilder(model.partyRoom?.userInfo?.avatar)
                        .setCircle(true)
                        .build())
        if (LevelConfigUtils.getRaceCenterAvatarBg(model.partyRoom?.userInfo?.ranking?.mainRanking
                        ?: 0) != 0) {
            levelBg.visibility = View.VISIBLE
            levelBg.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(model.partyRoom?.userInfo?.ranking?.mainRanking
                    ?: 0))
        } else {
            levelBg.visibility = View.GONE
        }

        if (!TextUtils.isEmpty(model.partyRoom?.roomInfoModel?.roomTagURL)) {
            recommendTagSdv.visibility = View.VISIBLE
            FrescoWorker.loadImage(recommendTagSdv, ImageFactory.newPathImage(model.partyRoom?.roomInfoModel?.roomTagURL)
                    .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                    .build())
        } else {
            recommendTagSdv.visibility = View.GONE
        }

        if (model.partyRoom?.roomInfoModel?.roomtype == 1) {
            roomNameTv.text = "个人房 ${model.partyRoom?.roomInfoModel?.roomName}"
        } else {
            roomNameTv.text = "家族房 ${model.partyRoom?.roomInfoModel?.roomName}"
        }

        if (!TextUtils.isEmpty(model.partyRoom?.roomInfoModel?.gameName)) {
            roomDescTv.visibility = View.VISIBLE
            roomDescTv.text = model.partyRoom?.roomInfoModel?.gameName
        } else {
            roomDescTv.visibility = View.GONE
        }
        if (!TextUtils.isEmpty(model.partyRoom?.roomInfoModel?.topicName)) {
            tagTv.visibility = View.VISIBLE
            tagTv.text = model.partyRoom?.roomInfoModel?.topicName
        } else {
            tagTv.visibility = View.GONE
        }
        roomPlayerNumTv.text = "${model.partyRoom?.roomInfoModel?.playerNum?.toString()}"
    }

    private fun adjustBg(position: Int) {
        val drawable1 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#F8EBCA"))
                .setCornersRadius(8.dp().toFloat())
                .build()
        val drawable2 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#E5FFE8"))
                .setCornersRadius(8.dp().toFloat())
                .build()
        val drawable3 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#D5E9FF"))
                .setCornersRadius(8.dp().toFloat())
                .build()
        val drawable4 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FFE5E5"))
                .setCornersRadius(8.dp().toFloat())
                .build()

        val drawableBottom1 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#F2E1B8"))
                .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                .build()

        val drawableBottom2 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#D2FAD7"))
                .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                .build()

        val drawableBottom3 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#C2DAF5"))
                .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                .build()

        val drawableBottom4 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#F7D7D7"))
                .setCornersRadius(8.dp().toFloat(), 8.dp().toFloat(), 0f, 0f)
                .build()

        val tagDrawable1 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#EACD92"))
                .setCornersRadius(4.dp().toFloat())
                .build()

        val tagDrawable2 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#A1D299"))
                .setCornersRadius(4.dp().toFloat())
                .build()

        val tagDrawable3 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#A7C7EB"))
                .setCornersRadius(4.dp().toFloat())
                .build()

        val tagDrawable4 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#EBB2B2"))
                .setCornersRadius(4.dp().toFloat())
                .build()

        when (position % 4) {
            1 -> {
                contentBg.background = drawable1
                bottomArea.background = drawableBottom1
                tagTv.background = tagDrawable1
            }
            2 -> {
                contentBg.background = drawable2
                bottomArea.background = drawableBottom2
                tagTv.background = tagDrawable2
            }
            3 -> {
                contentBg.background = drawable3
                bottomArea.background = drawableBottom3
                tagTv.background = tagDrawable3
            }
            else -> {
                contentBg.background = drawable4
                bottomArea.background = drawableBottom4
                tagTv.background = tagDrawable4
            }
        }

    }
}