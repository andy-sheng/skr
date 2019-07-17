package com.module.home.game.viewholder.grab

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.view.AnimateClickListener

class GrabCreateViewHolder(itemView: View, onClickCreateListener: (() -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onClickCreateListener?.invoke()
            }
        })
    }
}
