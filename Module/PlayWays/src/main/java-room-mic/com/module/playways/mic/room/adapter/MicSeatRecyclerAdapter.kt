package com.module.playways.mic.room.adapter

import android.databinding.BindingAdapter
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.BaseImageView
import com.module.playways.R
import com.module.playways.databinding.MicSeatStateItemLayoutBinding
import com.module.playways.mic.room.model.MicSeatModel

class MicSeatRecyclerAdapter : RecyclerView.Adapter<MicSeatRecyclerAdapter.MicSeatHolder>() {
    val mDataList: ArrayList<MicSeatModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MicSeatHolder {
        val inflater = LayoutInflater.from(parent.context)
        val micSeatStateItemLayoutBinding: MicSeatStateItemLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.mic_seat_state_item_layout, parent, false)
        return MicSeatHolder(View(parent.context))
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: MicSeatHolder, position: Int) {
        val model = mDataList.get(position)
        val micSeatStateItemLayoutBinding = DataBindingUtil.getBinding<MicSeatStateItemLayoutBinding>(holder.itemView)
        holder.bindData(model, micSeatStateItemLayoutBinding!!, position)
    }

    inner class MicSeatHolder : RecyclerView.ViewHolder {
        var model: MicSeatModel? = null
        var micSeatStateItemLayoutBinding: MicSeatStateItemLayoutBinding? = null
        var position: Int? = null

        constructor(itemView: View?) : super(itemView)

        fun bindData(model: MicSeatModel, micSeatStateItemLayoutBinding: MicSeatStateItemLayoutBinding, position: Int) {
            micSeatStateItemLayoutBinding!!.model = model
            micSeatStateItemLayoutBinding!!.holder = this
            micSeatStateItemLayoutBinding!!.executePendingBindings()
        }

        fun getRemarkName(): String {
            return UserInfoManager.getInstance().getRemarkName(model?.user?.userId!!, model?.user?.nickname)
        }

        fun getSongNameList(): String {
            var text: String = ""
            model?.music?.forEach {
                text = text + it.itemName
            }

            return text
        }
    }

    companion object {
        @BindingAdapter("bind:MicSeatHolderImageUrl")
        fun loadImage(imageView: BaseImageView, picUrl: String) {
            AvatarUtils.loadAvatarByUrl(imageView, AvatarUtils.newParamsBuilder(picUrl)
                    .setCircle(true)
                    .build())
        }
    }
}