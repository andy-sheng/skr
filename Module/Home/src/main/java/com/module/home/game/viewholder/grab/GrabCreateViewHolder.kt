package com.module.home.game.viewholder.grab

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.module.home.R

class GrabCreateViewHolder(itemView: View, onClickCreateListener: (() -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    var mBackground: ConstraintLayout = itemView.findViewById(R.id.background);

    init {

        var lp = mBackground.layoutParams //此为父布局
        lp.height = 348 * (U.getDisplayUtils().screenWidth / 2 - U.getDisplayUtils().dip2px(18f)) / 510
        mBackground.layoutParams = lp

        itemView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onClickCreateListener?.invoke()
            }
        })
    }
}
