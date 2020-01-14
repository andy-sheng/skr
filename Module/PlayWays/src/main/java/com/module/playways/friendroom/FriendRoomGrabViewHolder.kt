package com.module.playways.friendroom


import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.view.NickNameView
import com.component.busilib.view.VoiceChartView
import com.component.level.utils.LevelConfigUtils
import com.component.person.view.CommonAudioView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class FriendRoomGrabViewHolder(itemView: View, var mOnItemClickListener: FriendRoomAdapter.FriendRoomClickListener) : RecyclerView.ViewHolder(itemView) {

    val mTag = "FriendRoomVerticalViewHolder"

    private var mFriendRoomModel: RecommendRoomModel? = null
    private var mPos: Int = 0

    private val container: ConstraintLayout = itemView.findViewById(R.id.container)
    private val topIconIv: ImageView = itemView.findViewById(R.id.top_icon_iv)
    private val roomTagTv: TextView = itemView.findViewById(R.id.room_tag_tv)
    private val recommendTagSdv: SimpleDraweeView = itemView.findViewById(R.id.recommend_tag_sdv)
    private val mediaTagSdv: SimpleDraweeView = itemView.findViewById(R.id.media_tag_sdv)

    private val avatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    private val levelBg: ImageView = itemView.findViewById(R.id.level_bg)
    private val nameView: NickNameView = itemView.findViewById(R.id.name_view)

    private val audioView: CommonAudioView = itemView.findViewById(R.id.audio_view)

    private val bottomBg: ImageView = itemView.findViewById(R.id.bottom_bg)
    private val roomPlayerNumTv: ExTextView = itemView.findViewById(R.id.room_player_num_tv)
    private val roomInfoTv: ExTextView = itemView.findViewById(R.id.room_info_tv)

    init {
        itemView.setAnimateDebounceViewClickListener {
            mOnItemClickListener.onClickGrabRoom(mPos, mFriendRoomModel)
        }

        audioView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                mOnItemClickListener.onClickGrabVoice(mPos, mFriendRoomModel)
            }
        })
    }

    fun bindData(model: RecommendRoomModel, position: Int) {
        this.mFriendRoomModel = model
        this.mPos = position

        model.grabRoom?.let {
            bindData(it, position)
        }
    }

    private fun bindData(friendRoomModel: RecommendGrabRoomModel, position: Int) {
        adjustBg(position)
        if (friendRoomModel.userInfo != null) {
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(friendRoomModel.userInfo?.avatar)
                    .setCircle(true)
                    .build())
            if (friendRoomModel.userInfo?.ranking != null && LevelConfigUtils.getRaceCenterAvatarBg(friendRoomModel.userInfo?.ranking?.mainRanking
                            ?: 0) != 0) {
                levelBg.visibility = View.VISIBLE
                levelBg.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(friendRoomModel.userInfo?.ranking?.mainRanking
                        ?: 0))
            } else {
                levelBg.visibility = View.GONE
            }
        }

        if (friendRoomModel.voiceInfo != null) {
            audioView.visibility = View.VISIBLE
            audioView.bindData(friendRoomModel.voiceInfo?.duration ?: 0)
        } else {
            audioView.visibility = View.GONE
        }

        if (friendRoomModel.userInfo != null && friendRoomModel.roomInfo != null) {
            if (!TextUtils.isEmpty(friendRoomModel.roomInfo!!.roomTagURL)) {
                recommendTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(recommendTagSdv, ImageFactory.newPathImage(friendRoomModel.roomInfo?.roomTagURL)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                recommendTagSdv.visibility = View.GONE
            }

            nameView.setHonorText(friendRoomModel.userInfo?.nicknameRemark, friendRoomModel.userInfo?.honorInfo)
            if (!TextUtils.isEmpty(friendRoomModel.roomInfo?.mediaTagURL)) {
                mediaTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(mediaTagSdv, ImageFactory.newPathImage(friendRoomModel.roomInfo?.mediaTagURL)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                mediaTagSdv.visibility = View.GONE
            }

            roomPlayerNumTv.text = friendRoomModel.roomInfo?.inPlayersNum.toString() + "/" + friendRoomModel.roomInfo?.totalPlayersNum


            if (!TextUtils.isEmpty(friendRoomModel.roomInfo?.roomName)) {
                roomInfoTv.visibility = View.VISIBLE
                roomInfoTv.text = friendRoomModel.roomInfo?.roomName
            } else {
                roomInfoTv.visibility = View.GONE
            }
            if (friendRoomModel.tagInfo != null) {
                // 只显示专场名称
                val stringBuilder = SpanUtils()
                        .append("${friendRoomModel.tagInfo?.tagName}")
                        .create()
                roomTagTv.visibility = View.VISIBLE
                roomTagTv.text = stringBuilder
            } else {
                roomTagTv.visibility = View.GONE
                MyLog.w(mTag, "服务器数据有问题 friendRoomModel=$friendRoomModel position=$position")
            }
        } else {
            MyLog.w(mTag, "bindData friendRoomModel=$friendRoomModel position=$position")
        }
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

        val topDrawable1 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#EACD92"))
                .build()

        val topDrawable2 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#A1D299"))
                .build()

        val topDrawable3 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#A7C7EB"))
                .build()

        val topDrawable4 = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#EBB2B2"))
                .build()

        when (position % 4) {
            1 -> {
                container.background = drawable1
                bottomBg.background = drawableBottom1
                topIconIv.background = topDrawable1
            }
            2 -> {
                container.background = drawable2
                bottomBg.background = drawableBottom2
                topIconIv.background = topDrawable2
            }
            3 -> {
                container.background = drawable3
                bottomBg.background = drawableBottom3
                topIconIv.background = topDrawable3
            }
            else -> {
                container.background = drawable4
                bottomBg.background = drawableBottom4
                topIconIv.background = topDrawable4
            }
        }

    }

    fun startPlay() {
        audioView.setPlay(true)
    }

    fun stopPlay() {
        audioView.setPlay(false)
    }
}
