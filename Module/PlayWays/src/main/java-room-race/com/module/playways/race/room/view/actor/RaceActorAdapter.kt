package com.module.playways.race.room.view.actor

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.utils.dp
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.race.room.model.RacePlayerInfoModel

class RaceActorAdapter : RecyclerView.Adapter<RaceActorAdapter.RaceActorViewHolder>() {

    var mDataList = ArrayList<RacePlayerInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceActorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.race_actor_item_layout, parent, false)
        return RaceActorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RaceActorViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class RaceActorViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val statusTv: TextView = item.findViewById(R.id.status_tv)
        val nameTv: TextView = item.findViewById(R.id.name_tv)
        val descTv: TextView = item.findViewById(R.id.desc_tv)
        val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)


        var mPosition = 0
        var mModel: RacePlayerInfoModel? = null

        fun bindData(pos: Int, model: RacePlayerInfoModel) {
            this.mPosition = pos
            this.mModel = model

            //TODO 描述怎么说？？
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.userInfo.avatar)
                    .setBorderColor(U.getColor(R.color.white))
                    .setBorderWidth(2.dp().toFloat())
                    .setCircle(true)
                    .build())
            statusTv.text = "等待中"
            nameTv.text = UserInfoManager.getInstance().getRemarkName(model.userInfo.userId, model.userInfo.nickname)

        }
    }
}
