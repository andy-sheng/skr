package com.module.playways.race.room.view.actor

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.component.person.event.ShowPersonCardEvent
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.race.room.model.RacePlayerInfoModel
import org.greenrobot.eventbus.EventBus

class RaceActorAdapter : RecyclerView.Adapter<RaceActorAdapter.RaceActorViewHolder>() {

    var mDataList = ArrayList<RaceActorInfoModel>()

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
        var mModel: RaceActorInfoModel? = null

        init {
            item.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mModel?.let {
                        EventBus.getDefault().post(ShowPersonCardEvent(it.plyer.userID))
                    }
                }
            })
        }

        fun bindData(pos: Int, model: RaceActorInfoModel) {
            this.mPosition = pos
            this.mModel = model

            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.plyer?.userInfo?.avatar)
                    .setBorderColor(U.getColor(R.color.white))
                    .setBorderWidth(2.dp().toFloat())
                    .setCircle(true)
                    .build())
            when {
                model.status == 1 -> {
                    statusTv.visibility = View.VISIBLE
                    statusTv.setTextColor(Color.parseColor("#FFC15B"))
                    statusTv.text = "演唱中"
                }
                model.status == 2 -> {
                    statusTv.visibility = View.VISIBLE
                    statusTv.setTextColor(U.getColor(R.color.white_trans_50))
                    statusTv.text = "等待中"
                }
                else -> {
                    statusTv.visibility = View.GONE
                }
            }
            descTv.text = model.scoreState?.rankingDesc
            nameTv.text = UserInfoManager.getInstance().getRemarkName(model.plyer?.userInfo?.userId
                    ?: 0, model.plyer?.userInfo?.nickname)

        }
    }
}
