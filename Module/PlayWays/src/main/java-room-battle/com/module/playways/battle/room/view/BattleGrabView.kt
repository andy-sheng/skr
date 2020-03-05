package com.module.playways.battle.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.grab.room.view.RoundRectangleView

class BattleGrabView(viewStub: ViewStub, protected var mRoomData: BattleRoomData?) : ExViewStub(viewStub) {
    lateinit var singIv: ImageView
    lateinit var rrlProgress: RoundRectangleView

    var clickSingFuc: (() -> Unit)? = null

    override fun init(parentView: View) {
        singIv = parentView.findViewById(R.id.sing_iv)
        rrlProgress = parentView.findViewById(R.id.rrl_progress)

        singIv.setDebounceViewClickListener {
            clickSingFuc?.invoke()
        }
    }

    fun MutableList<Int>.swap(index1: Int, index2: Int) {
        val tmp = this[index1] // “this”对应该列表
        this[index1] = this[index2]
        this[index2] = tmp
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_grab_view_layout
    }

    fun show() {
        tryInflate()
        setVisibility(View.VISIBLE)
        var battleRoundInfoModel = mRoomData?.realRoundInfo
        if (battleRoundInfoModel == null) {
            battleRoundInfoModel = mRoomData?.expectRoundInfo
        }
    }

    fun hide() {
        setVisibility(View.GONE)
    }
}