package com.module.playways.race.room.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceGamePlayInfo
import com.module.playways.race.room.model.RaceWantSingInfo


class RaceSelectSongItemView : ExConstraintLayout {
    lateinit var songNameTv: ExTextView
    lateinit var avatarIv1: BaseImageView
    lateinit var avatarIv2: BaseImageView
    lateinit var avatarIv3: BaseImageView
    var roomData: RaceRoomData? = null
    var info: RaceGamePlayInfo? = null
    var hasAnimate: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, com.module.playways.R.layout.race_select_song_item_layout, this)
        songNameTv = findViewById(com.module.playways.R.id.song_name_tv) as ExTextView
        avatarIv1 = findViewById(com.module.playways.R.id.avatar_iv_1) as BaseImageView
        avatarIv2 = findViewById(com.module.playways.R.id.avatar_iv_2) as BaseImageView
        avatarIv3 = findViewById(com.module.playways.R.id.avatar_iv_3) as BaseImageView
        reset()
    }

    fun setRaceRoomData(data: RaceRoomData) {
        this.roomData = data
    }

    fun setSong(info: RaceGamePlayInfo?) {
        info?.let {
            this.info = it
            songNameTv.text = info.commonMusic?.itemName
        }
    }

    fun getSong(): RaceGamePlayInfo? {
        return info
    }

    fun bindData(list: ArrayList<RaceWantSingInfo>?) {
        list?.let {
            avatarIv1.visibility = View.GONE
            avatarIv2.visibility = View.GONE
            avatarIv3.visibility = View.GONE
            for (i in 0 until it.size) {
                if (roomData?.getUserInfo(it[i].userID)?.userId == MyUserInfoManager.getInstance().uid.toInt() && !hasAnimate) {
                    startSelectedAnimation()
                }

                when (i) {
                    0 -> {
                        avatarIv1.visibility = View.VISIBLE
                        AvatarUtils.loadAvatarByUrl(avatarIv1, AvatarUtils.newParamsBuilder(roomData?.getUserInfo(it[i].userID)?.avatar)
                                .setCornerRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .build())
                    }
                    1 -> {
                        avatarIv2.visibility = View.VISIBLE
                        AvatarUtils.loadAvatarByUrl(avatarIv2, AvatarUtils.newParamsBuilder(roomData?.getUserInfo(it[i].userID)?.avatar)
                                .setCornerRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .build())
                    }
                    2 -> {
                        avatarIv3.visibility = View.VISIBLE
                        AvatarUtils.loadAvatarByUrl(avatarIv3, AvatarUtils.newParamsBuilder(roomData?.getUserInfo(it[i].userID)?.avatar)
                                .setCornerRadius(U.getDisplayUtils().dip2px(20f).toFloat())
                                .build())
                    }
                }
            }
        }
    }

    fun startSelectedAnimation() {
        hasAnimate = true
        val drawable = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FFF8CBFF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                .build()
        setBackground(drawable)
        val animation = ScaleAnimation(
                1.0f, 1.15f, 1.0f, 1.15f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)

        animation.duration = 500
        animation.fillAfter = true
        startAnimation(animation)
    }

    fun reset() {
        clearAnimation()
        val drawable = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FFEBE7FF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                .build()
        setBackground(drawable)
        avatarIv1.visibility = View.GONE
        avatarIv2.visibility = View.GONE
        avatarIv3.visibility = View.GONE
        hasAnimate = false
    }
}
