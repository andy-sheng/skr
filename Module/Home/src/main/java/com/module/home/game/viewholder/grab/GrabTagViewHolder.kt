package com.module.home.game.viewholder.grab

import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.view.AnimateClickListener
import com.component.busilib.friends.SpecialModel

class GrabTagViewHolder(itemView: View, onClickTagListener: ((model: SpecialModel?) -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    var model: SpecialModel? = null
    var pos: Int = 0

    init {
        itemView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onClickTagListener?.invoke(model)
            }
        })
    }

    fun bind(position: Int, model: SpecialModel) {
        this.model = model
        this.pos = position
    }
}
