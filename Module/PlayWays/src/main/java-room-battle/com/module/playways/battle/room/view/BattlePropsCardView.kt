package com.module.playways.battle.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData

class BattlePropsCardView(viewStub: ViewStub, protected var mRoomData: BattleRoomData?) : ExViewStub(viewStub) {
    lateinit var singCardIv: ImageView
    lateinit var singCountTv: ExTextView
    lateinit var switchSongIv: ImageView
    lateinit var switchCountTv: ExTextView
    lateinit var attentionIv: ImageView

    var useSwitchSongCardFuc: (() -> Unit)? = null
    var useSingCardFuc: (() -> Unit)? = null

    override fun init(parentView: View) {
        singCardIv = parentView.findViewById(R.id.sing_card_iv)
        singCountTv = parentView.findViewById(R.id.sing_count_tv)
        switchSongIv = parentView.findViewById(R.id.switch_song_iv)
        switchCountTv = parentView.findViewById(R.id.switch_count_tv)
        attentionIv = parentView.findViewById(R.id.attention_iv)
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_props_card_view_layout
    }

    fun show() {
        tryInflate()
        var battleRoundInfoModel = mRoomData?.realRoundInfo
        if (battleRoundInfoModel == null) {
            battleRoundInfoModel = mRoomData?.expectRoundInfo
        }
    }

    fun hide() {

    }
}