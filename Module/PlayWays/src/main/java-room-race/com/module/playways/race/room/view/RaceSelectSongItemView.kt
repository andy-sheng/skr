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
import com.module.playways.race.room.model.RaceGameInfo


class RaceSelectSongItemView : ExConstraintLayout {
    lateinit var songNameTv: ExTextView
    lateinit var avatarIv1: BaseImageView
    lateinit var avatarIv2: BaseImageView
    lateinit var avatarIv3: BaseImageView
    var roomData: RaceRoomData? = null
    var info: RaceGameInfo? = null
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

    fun setSong(info: RaceGameInfo?) {
        info?.let {
            this.info = it
            songNameTv.text = info.commonMusic?.itemName
        }
    }

    fun getSong(): RaceGameInfo? {
        return info
    }

    fun bindData(list: ArrayList<Int>?) {
        list?.let {
            for (i in 0 until it.size) {
                if (roomData?.getUserInfo(it[i])?.userId == MyUserInfoManager.getInstance().uid.toInt() && !hasAnimate) {
                    startSelectedAnimation()
                }

                when (i) {
                    0 -> {
                        AvatarUtils.loadAvatarByUrl(avatarIv1, AvatarUtils.newParamsBuilder(getAvatarById(it[i]))
                                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                                .setBlur(true)
                                .build())
                    }
                    1 -> {
                        AvatarUtils.loadAvatarByUrl(avatarIv2, AvatarUtils.newParamsBuilder(getAvatarById(it[i]))
                                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                                .setBlur(true)
                                .build())
                    }
                    2 -> {
                        AvatarUtils.loadAvatarByUrl(avatarIv3, AvatarUtils.newParamsBuilder(getAvatarById(it[i]))
                                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                                .setBlur(true)
                                .build())
                    }
                }
            }
        }
    }

    fun getAvatarById(id: Int): String {
        roomData?.let {
            it.realRoundInfo?.playUsers?.forEach {
                if (it.userInfo.userId == id) {
                    return it.userInfo.avatar
                }
            }
        }

        return ""
    }

    fun startSelectedAnimation() {
        hasAnimate = true
        val drawable = DrawableCreator.Builder()
                .setSolidColor(Color.parseColor("#FFF8CBFF"))
                .setCornersRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                .build()
        setBackground(drawable)
        val animation = ScaleAnimation(
                1.0f, 1.1f, 1.0f, 1.1f,
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
