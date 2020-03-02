package com.module.playways.battle.songlist.viewholer

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.common.utils.SpanUtils
import com.common.view.DebounceViewClickListener
import com.component.busilib.view.AvatarView
import com.component.busilib.view.BitmapTextView
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleRankInfoModel
import com.module.playways.battle.songlist.view.BattleStarView

class BattleRankViewHolder(item: View, var listener: ((model: BattleRankInfoModel?, position: Int) -> Unit)?) : RecyclerView.ViewHolder(item) {

    private val seqTv: BitmapTextView = item.findViewById(R.id.seq_tv)
    private val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    private val blightTv: BitmapTextView = item.findViewById(R.id.blight_tv)
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

        if (model.rankSeq > 3) {
            seqTv.setDrawTextColor(Color.parseColor("#80111E60"))
        } else {
            seqTv.setDrawTextColor(Color.parseColor("#EF9D00"))
        }
        seqTv.setText(model.rankSeq.toString())

        avatarIv.bindData(model.user)
        nameTv.text = model.user?.nicknameRemark
        if(TextUtils.isEmpty(model.user?.ranking?.rankingDesc)){
            levelTv.visibility = View.GONE
        }else{
            levelTv.visibility = View.VISIBLE
            levelTv.text = model.user?.ranking?.rankingDesc
        }

        starView.bindData(model.starCnt, model.starCnt)
        blightTv.setText(model.blightCnt.toString())

    }

}