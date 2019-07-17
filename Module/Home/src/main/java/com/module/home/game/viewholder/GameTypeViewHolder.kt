package com.module.home.game.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import com.common.view.AnimateClickListener
import com.common.view.DebounceViewClickListener
import com.component.busilib.friends.SpecialModel
import com.module.home.R
import com.module.home.game.model.GameTypeModel

class GameTypeViewHolder(itemView: View,
                         onDoubleRoomListener: (() -> Unit)?,
                         onPkRoomListener: (() -> Unit)?,
                         onCreateRoomListener: (() -> Unit)?,
                         onSelectSpecialListener: ((specialModel: SpecialModel?) -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    val mDoubleIv: ImageView = itemView.findViewById(R.id.double_iv)
    val mPkIv: ImageView = itemView.findViewById(R.id.pk_iv)
    val mCreateRoomIv: ImageView = itemView.findViewById(R.id.create_room_iv)
    val mGrabIv: ImageView = itemView.findViewById(R.id.grab_iv)

    var mGameTypeModel: GameTypeModel? = null

    init {
        mDoubleIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onDoubleRoomListener?.invoke()
            }

        })

        mPkIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onPkRoomListener?.invoke()
            }
        })

        mCreateRoomIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onCreateRoomListener?.invoke()
            }
        })

        mGrabIv.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onSelectSpecialListener?.invoke(mGameTypeModel?.mSpecialModel)
            }
        })
    }

    fun bindData(gameTypeModel: GameTypeModel) {
        this.mGameTypeModel = mGameTypeModel
    }
}