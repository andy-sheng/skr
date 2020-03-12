package com.module.playways.battle.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.ActivityUtils
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.battle.room.event.BattleSwitchCardChangeEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class BattlePropsCardView(viewStub: ViewStub, protected var mRoomData: BattleRoomData) : BaseSceneView(viewStub) {
    var singCardIv: ImageView? = null
    var singCountTv: ExTextView? = null
    var switchSongIv: ImageView? = null
    var switchCountTv: ExTextView? = null
    var attentionIv: ImageView? = null

    var useSwitchSongCardFuc: (() -> Unit)? = null
    var useSingCardFuc: (() -> Unit)? = null

    override fun init(parentView: View) {
        singCardIv = parentView.findViewById(R.id.sing_card_iv)
        singCountTv = parentView.findViewById(R.id.sing_count_tv)
        switchSongIv = parentView.findViewById(R.id.switch_song_iv)
        switchCountTv = parentView.findViewById(R.id.switch_count_tv)
        attentionIv = parentView.findViewById(R.id.attention_iv)

        singCardIv?.setDebounceViewClickListener {
            useSingCardFuc?.invoke()
        }

        switchSongIv?.setDebounceViewClickListener {
            useSwitchSongCardFuc?.invoke()
        }
        if(!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_props_card_view_layout
    }

    fun show() {
        enterAnimation()

        singCountTv?.text = "X${mRoomData.config.helpCardCnt}"
        switchCountTv?.text = "X${mRoomData.config.switchCardCnt}"
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BattleSwitchCardChangeEvent) {
        switchCountTv?.text = "X${mRoomData.config.switchCardCnt}"
        singCountTv?.text = "X${mRoomData.config.helpCardCnt}"
    }


    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        EventBus.getDefault().unregister(this)
    }

    fun hide() {
        setVisibility(View.GONE)
    }
}