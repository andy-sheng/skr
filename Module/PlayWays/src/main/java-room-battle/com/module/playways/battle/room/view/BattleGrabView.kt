package com.module.playways.battle.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.common.core.view.setDebounceViewClickListener
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.grab.room.view.RoundRectangleView

class BattleGrabView(viewStub: ViewStub, protected var mRoomData: BattleRoomData?) : BaseSceneView(viewStub) {
    var singIv: ImageView? = null
    var rrlProgress: RoundRectangleView? = null

    var clickGrabFuc: (() -> Unit)? = null

    override fun init(parentView: View) {
        singIv = parentView.findViewById(R.id.sing_iv)
        rrlProgress = parentView.findViewById(R.id.rrl_progress)

        singIv?.setDebounceViewClickListener {
            clickGrabFuc?.invoke()
        }

        rrlProgress?.startCountDown(15000)
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_grab_view_layout
    }

    fun show() {
        enterAnimation()
        rrlProgress?.startCountDown(15000)
    }

    fun hide() {
        setVisibility(View.GONE)
        rrlProgress?.stopCountDown()
    }
}