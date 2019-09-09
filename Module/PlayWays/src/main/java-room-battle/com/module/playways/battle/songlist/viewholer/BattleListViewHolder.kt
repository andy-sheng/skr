package com.module.playways.battle.songlist.viewholer

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.view.AnimateClickListener
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleTagModel
import com.module.playways.battle.songlist.view.BattleStarView

class BattleListViewHolder(item: View, listener: ((model: BattleTagModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(item) {

    val recordFilm: ImageView = item.findViewById(R.id.record_film)
    val recordCover: SimpleDraweeView = item.findViewById(R.id.record_cover)
    val nameTv: TextView = item.findViewById(R.id.name_tv)
    val starView: BattleStarView = item.findViewById(R.id.star_view)
    val lockTv: ExTextView = item.findViewById(R.id.lock_tv)

    var mModel: BattleTagModel? = null
    var mPosition: Int = 0

    init {
        item.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                listener?.invoke(mModel, mPosition)
            }
        })
    }

    fun bindData(model: BattleTagModel, pos: Int) {
        this.mModel = model
        this.mPosition = pos

        AvatarUtils.loadAvatarByUrl(recordCover, AvatarUtils.newParamsBuilder(model.coverURL)
                .setCircle(true)
                .build())
        nameTv.text = model.tagName
        when {
            model.status == BattleTagModel.SST_LOCK -> {
                lockTv.visibility = View.VISIBLE
                starView.visibility = View.GONE
                recordFilm.alpha = 0.2f
                recordCover.alpha = 0.2f
            }
            model.status == BattleTagModel.SST_UNLOCK -> {
                lockTv.visibility = View.GONE
                starView.visibility = View.VISIBLE
                starView.bindData(model.starCnt, 5)
                recordFilm.alpha = 1f
                recordCover.alpha = 1f
            }
            else -> {
                starView.visibility = View.INVISIBLE
                lockTv.visibility = View.INVISIBLE
                recordFilm.alpha = 1f
                recordCover.alpha = 1f
            }
        }

    }
}