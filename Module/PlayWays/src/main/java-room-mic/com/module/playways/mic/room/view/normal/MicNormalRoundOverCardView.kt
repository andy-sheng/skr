package com.module.playways.mic.room.view.normal

import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.mic.room.model.MicRoundInfoModel

/**
 * 轮次结束 合唱和正常结束都用此板
 */
class MicNormalRoundOverCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

     var resultBgIv: ImageView?=null
     var songNameTv: TextView?=null
     var singerNameTv:TextView?=null
     var scoreDescIv:ImageView?=null
     var rankDescTv:TextView?=null


    override fun init(parentView: View) {
        resultBgIv = parentView.findViewById(R.id.result_bg_iv)
        songNameTv = parentView.findViewById(R.id.song_name_tv)
        singerNameTv = parentView.findViewById(R.id.singer_name_tv)
        scoreDescIv = parentView.findViewById(R.id.score_desc_iv)
        rankDescTv = parentView.findViewById(R.id.rank_desc_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_normal_round_over_card_stub_layout
    }

    fun bindData(lastRoundInfo: MicRoundInfoModel, overListener:()->Unit) {
        tryInflate()
    }
}
