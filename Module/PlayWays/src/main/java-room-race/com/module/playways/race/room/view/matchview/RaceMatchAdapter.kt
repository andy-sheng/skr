package com.module.playways.race.room.view.matchview

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.race.room.model.RacePlayerInfoModel

class RaceMatchAdapter : RecyclerView.Adapter<RaceMatchAdapter.RankMatchViewHolder>() {

    var mDataList = ArrayList<RacePlayerInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankMatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.race_match_item_layout, parent, false)
        return RankMatchViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun onBindViewHolder(holder: RankMatchViewHolder, position: Int) {
        if (!mDataList.isNullOrEmpty()) {
            holder.bindData(mDataList[position % mDataList.size], position)
        }
    }


    inner class RankMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val avatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
        private val nameTv: ExTextView = itemView.findViewById(R.id.name_tv)

        fun bindData(model: RacePlayerInfoModel, pos: Int) {
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.userInfo.avatar)
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(2.dp().toFloat())
                    .setCircle(true)
                    .build())
            nameTv.text = model.userInfo.nicknameRemark
        }
    }
}
