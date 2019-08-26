package com.module.playways.race.room.view.topContent

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.DebounceViewClickListener
import com.component.person.event.ShowPersonCardEvent
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.race.room.model.RacePlayerInfoModel
import org.greenrobot.eventbus.EventBus

class RaceTopContentAdapter : RecyclerView.Adapter<RaceTopContentAdapter.RaceTopViewHolder>() {

    var mDataList = ArrayList<RacePlayerInfoModel>()

    override fun onBindViewHolder(holder: RaceTopViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RaceTopViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.race_top_item_view_layout, parent, false)
        return RaceTopViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (mDataList.size >= 7) {
            7
        } else {
            mDataList.size
        }
    }

    inner class RaceTopViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)

        var mPostion = 0
        var mModel: RacePlayerInfoModel? = null

        init {
            item.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mModel?.let {
                        EventBus.getDefault().post(ShowPersonCardEvent(it.userInfo.userId))
                    }
                }
            })
        }

        fun bindData(position: Int, model: RacePlayerInfoModel) {
            this.mPostion = position
            this.mModel = model

            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.userInfo.avatar)
                    .setBorderColor(U.getColor(R.color.white))
                    .setBorderWidth(1.dp().toFloat())
                    .setCircle(true)
                    .build())
        }
    }
}