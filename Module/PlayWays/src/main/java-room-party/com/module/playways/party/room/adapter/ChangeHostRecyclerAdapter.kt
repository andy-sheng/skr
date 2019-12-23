package com.module.playways.party.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.model.PartyPlayerInfoModel

class ChangeHostRecyclerAdapter : RecyclerView.Adapter<ChangeHostRecyclerAdapter.ChangeHostHolder>() {
    val mRaceGamePlayInfoList = ArrayList<PartyPlayerInfoModel>()
    var mOpMethod: ((Int, PartyPlayerInfoModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChangeHostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.change_host_adapter_item_layout, parent, false)
        return ChangeHostHolder(view)
    }

    override fun getItemCount(): Int {
        return mRaceGamePlayInfoList.size
    }

    override fun onBindViewHolder(holder: ChangeHostHolder, position: Int) {
        holder.bindData(position, mRaceGamePlayInfoList.get(position))
    }

    fun addData(list: List<PartyPlayerInfoModel>) {
        list?.let {
            if (it.size > 0) {
                val startNotifyIndex = if (mRaceGamePlayInfoList.size > 0) mRaceGamePlayInfoList.size - 1 else 0
                mRaceGamePlayInfoList.addAll(list)
                notifyItemRangeChanged(startNotifyIndex, mRaceGamePlayInfoList.size - startNotifyIndex)
            }
        }
    }

    inner class ChangeHostHolder : RecyclerView.ViewHolder {
        val TAG = "ChangeHostHolder"
        var avatarIv: BaseImageView
        var nameTv: ExTextView
        var opTv: ExTextView
        var pos = -1
        var model: PartyPlayerInfoModel? = null

        constructor(itemView: View) : super(itemView) {
            avatarIv = itemView.findViewById(R.id.avatar_iv)
            nameTv = itemView.findViewById(R.id.name_tv)
            opTv = itemView.findViewById(R.id.op_tv)

            opTv.setDebounceViewClickListener {
                mOpMethod?.invoke(pos, model!!)
            }
        }

        fun bindData(position: Int, model: PartyPlayerInfoModel) {
            pos = position
            this.model = model
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.userInfo.avatar)
                    .setCircle(true)
                    .setBorderColor(U.getColor(R.color.white))
                    .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                    .build())

            nameTv.text = UserInfoManager.getInstance().getRemarkName(model.userID, model.userInfo.nickname)
        }
    }
}