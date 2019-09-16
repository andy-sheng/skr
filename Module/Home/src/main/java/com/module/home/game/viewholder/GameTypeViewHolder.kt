package com.module.home.game.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.adapter.ClickGameListener
import com.module.home.game.model.GameTypeModel

class GameTypeViewHolder(itemView: View,
                         val listener: ClickGameListener) : RecyclerView.ViewHolder(itemView) {

    val mDoubleIv: ImageView = itemView.findViewById(R.id.double_iv)
    val mPkIv: ImageView = itemView.findViewById(R.id.pk_iv)
    val mCreateRoomIv: ImageView = itemView.findViewById(R.id.create_room_iv)
    val mGrabIv: ImageView = itemView.findViewById(R.id.grab_iv)
    val mBattleIv: ImageView = itemView.findViewById(R.id.battle_iv)


    var mGameTypeModel: GameTypeModel? = null

    init {
        mDoubleIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                if ((mGameTypeModel?.mRemainTime) ?: 0 > 0) {
                    listener.onDoubleRoomListener()
                } else {
                    U.getToastUtil().showLong("今日唱聊匹配次数用完啦～")
                }
            }

        })

        mPkIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener.onPkRoomListener()
            }
        })

        mCreateRoomIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener.onCreateRoomListener()
            }
        })

        mGrabIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener.onGrabRoomListener()
            }
        })

        mBattleIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener.onBattleRoomListener()
            }
        })
    }

    fun bindData(gameTypeModel: GameTypeModel) {
        this.mGameTypeModel = gameTypeModel
    }
}