package com.module.playways.battle.songlist.viewholer

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.common.utils.SpanUtils
import com.common.view.DebounceViewClickListener
import com.component.busilib.view.AvatarView
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleRankInfoModel
import com.module.playways.battle.songlist.view.BattleStarView

class BattleRankViewHolder(item: View, var listener: ((model: BattleRankInfoModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(item) {

    private val seqTv: TextView = item.findViewById(R.id.seq_tv)
    private val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    private val blightTv: TextView = item.findViewById(R.id.blight_tv)
    private val starView: BattleStarView = item.findViewById(R.id.star_view)
    private val nameTv: TextView = item.findViewById(R.id.name_tv)
    private val levelTv: TextView = item.findViewById(R.id.level_tv)

    var mPosition = 0
    var mModel: BattleRankInfoModel? = null

    init {
        item.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                listener?.invoke(mModel, mPosition)
            }
        })
    }

    fun bindData(model: BattleRankInfoModel, pos: Int) {
        this.mModel = model
        this.mPosition = pos

        seqTv.text = model.rankSeq.toString()
        if (model.rankSeq > 3) {
            seqTv.setTextColor(Color.parseColor("#80111E60"))
        } else {
            seqTv.setTextColor(Color.parseColor("#EF9D00"))
        }
        avatarIv.bindData(model.user)
        nameTv.text = model.user?.nicknameRemark
        levelTv.text = model.user?.ranking?.rankingDesc
        starView.bindData(model.starCnt, model.starCnt)

        val spanStringBuilder = SpanUtils()
                .append(model.blightCnt.toString()).setForegroundColor(Color.parseColor("#cc111E60")).setFontSize(20, true)
                .append("爆灯").setForegroundColor(Color.parseColor("#80111E60")).setFontSize(10, true)
                .create()
        blightTv.text = spanStringBuilder

    }

}