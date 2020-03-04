package com.module.playways.battle.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData

class BattleGiveUpView(viewStub: ViewStub, protected var mRoomData: BattleRoomData?) : ExViewStub(viewStub) {
    lateinit var giveUpIv: ImageView
    var clickGiveUpFuc: (() -> Unit)? = null

    override fun init(parentView: View) {
        giveUpIv = parentView.findViewById(R.id.give_up_iv)

        giveUpIv.setDebounceViewClickListener {
            clickGiveUpFuc?.invoke()
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_give_up_view_layout
    }

    fun showSingView() {
        tryInflate()
        setVisibility(View.VISIBLE)
    }

    fun hideGrabView() {
        setVisibility(View.GONE)
    }
}