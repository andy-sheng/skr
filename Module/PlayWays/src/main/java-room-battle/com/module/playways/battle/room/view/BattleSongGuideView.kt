package com.module.playways.battle.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.common.image.fresco.BaseImageView
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData

class BattleSongGuideView(viewStub: ViewStub, protected var mRoomData: BattleRoomData?) : ExViewStub(viewStub) {
    lateinit var titleBg: ImageView
    lateinit var leftFirstAvatar: BaseImageView
    lateinit var leftSecondAvatar: BaseImageView
    lateinit var rightFirstAvatar: BaseImageView
    lateinit var rightSecondAvatar: BaseImageView
    lateinit var songNameTv: ExTextView
    lateinit var songLyricTv: ExTextView
    lateinit var singerInfoTv: ExTextView

    override fun init(parentView: View) {
        titleBg = parentView.findViewById(R.id.title_bg)
        leftFirstAvatar = parentView.findViewById(R.id.left_first_avatar)
        leftSecondAvatar = parentView.findViewById(R.id.left_second_avatar)
        rightFirstAvatar = parentView.findViewById(R.id.right_first_avatar)
        rightSecondAvatar = parentView.findViewById(R.id.right_second_avatar)
        songNameTv = parentView.findViewById(R.id.song_name_tv)
        songLyricTv = parentView.findViewById(R.id.song_lyric_tv)
        singerInfoTv = parentView.findViewById(R.id.singer_info_tv)

    }

    override fun layoutDesc(): Int {
        return R.layout.battle_song_guide_view_layout
    }
}