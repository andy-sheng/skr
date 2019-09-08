package com.module.playways.battle.songlist

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.view.AnimateClickListener
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.battle.songlist.view.BattleStarView

class BattleListViewHolder(item: View, listener: ((model: BattleModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(item) {

    val recordFilm: ImageView = item.findViewById(R.id.record_film)
    val recordCover: SimpleDraweeView = item.findViewById(R.id.record_cover)
    val nameTv: TextView = item.findViewById(R.id.name_tv)
    val starView: BattleStarView = item.findViewById(R.id.star_view)
    val lockTv: ExTextView = item.findViewById(R.id.lock_tv)

    var mModel: BattleModel? = null
    var mPosition: Int = 0

    init {
        item.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener?.invoke(mModel, mPosition)
            }
        })
    }

    fun bindData(model: BattleModel, pos: Int) {
        this.mModel = model
        this.mPosition = pos

    }
}