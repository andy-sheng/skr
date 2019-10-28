package com.module.playways.mic.home

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.component.busilib.view.VoiceChartView
import com.component.level.utils.LevelConfigUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class RecomMicChildAdapter : RecyclerView.Adapter<RecomMicChildAdapter.RecomChildViewHolder>() {

    var mDataList = ArrayList<UserInfoModel>()

    var onClickVoice: ((model: UserInfoModel?, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomChildViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recom_child_item_layout, parent, false)
        return RecomChildViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecomChildViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    inner class RecomChildViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val levelIv: ImageView = item.findViewById(R.id.level_iv)
        val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)
        val playBg: ExImageView = item.findViewById(R.id.play_bg)
        val playIv: ImageView = item.findViewById(R.id.play_iv)
        val voiceChartView: VoiceChartView = item.findViewById(R.id.voice_chart_view)
        val nameTv: TextView = item.findViewById(R.id.name_tv)

        var mModel: UserInfoModel? = null
        var mPosition: Int = 0

        fun bindData(model: UserInfoModel, position: Int) {
            this.mModel = model
            this.mPosition = position

            if (LevelConfigUtils.getRaceCenterAvatarBg(model.ranking.mainRanking) != 0) {
                levelIv.visibility = View.VISIBLE
                levelIv.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(model.ranking.mainRanking))
            } else {
                levelIv.visibility = View.INVISIBLE
            }
            nameTv.text = model.nicknameRemark
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.avatar)
                    .setCircle(true)
                    .setBorderWidth(2f.dp().toFloat())
                    .setBorderColor(Color.WHITE)
                    .build())
        }

    }
}