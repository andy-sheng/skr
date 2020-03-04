package com.module.playways.battle.room.view

import android.view.View
import android.view.ViewStub
import com.common.image.fresco.BaseImageView
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.grab.room.view.SingCountDownView2

class BattleOtherSingCardView(viewStub: ViewStub, protected var mRoomData: BattleRoomData?) : ExViewStub(viewStub) {
    lateinit var singCountDownView: SingCountDownView2
    lateinit var cardIv: ExImageView
    lateinit var singerAvatarIv: BaseImageView
    lateinit var songNameTv: ExTextView
    lateinit var singerInfoTv:ExTextView

    override fun init(parentView: View) {
        singCountDownView = parentView.findViewById(R.id.sing_count_down_view)
        cardIv = parentView.findViewById(R.id.card_iv)
        singerAvatarIv = parentView.findViewById(R.id.singer_avatar_iv)
        songNameTv = parentView.findViewById(R.id.item_name_tv)
        singerInfoTv = parentView.findViewById(R.id.singer_info_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_other_sing_view_layout
    }
}