package com.module.playways.battle.songlist.viewholer

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.utils.dp
import com.common.view.AnimateClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.person.utils.StringFromatUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleTagModel
import com.module.playways.battle.songlist.view.BattleStarView

class BattleListViewHolder(item: View, listener: ((model: BattleTagModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(item) {

    val recordCover: SimpleDraweeView = item.findViewById(R.id.record_cover)
    val nameTv: TextView = item.findViewById(R.id.name_tv)
    val starView: BattleStarView = item.findViewById(R.id.star_view)
    val lockBg: ExImageView = item.findViewById(R.id.lock_bg)
    val lockIv: ImageView = item.findViewById(R.id.lock_iv)
    val onlineNum: ExTextView = item.findViewById(R.id.online_num)

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
                .setCornerRadius(8.dp().toFloat())
                .build())
        nameTv.text = model.tagName
        onlineNum.text = "${StringFromatUtils.formatTenThousand(model.onlineUserCnt)}人正在参加"
        when {
            model.status == BattleTagModel.SST_LOCK -> {
                lockBg.visibility = View.VISIBLE
                lockIv.visibility = View.VISIBLE
                starView.visibility = View.GONE
            }
            model.status == BattleTagModel.SST_UNLOCK -> {
                lockBg.visibility = View.GONE
                lockIv.visibility = View.GONE
                starView.visibility = View.VISIBLE
                starView.bindData(model.starCnt, model.starCnt)
            }
            else -> {
                lockBg.visibility = View.GONE
                starView.visibility = View.GONE
                lockIv.visibility = View.GONE
            }
        }

    }
}