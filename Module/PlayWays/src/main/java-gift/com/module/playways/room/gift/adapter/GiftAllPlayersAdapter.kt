package com.module.playways.room.gift.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.recyclerview.DiffAdapter
import com.module.playways.R
import com.module.playways.room.prepare.model.PlayerInfoModel

class GiftAllPlayersAdapter : DiffAdapter<PlayerInfoModel, RecyclerView.ViewHolder>() {
    internal var mSelectedPlayerInfoModel: PlayerInfoModel? = null

    internal var mOnClickPlayerListener: OnClickPlayerListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_item_view_layout, parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = mDataList[position]

        val reportItemHolder = holder as ItemHolder
        reportItemHolder.bind(model)
    }

    fun setSelectedGrabPlayerInfoModel(selectedPlayerInfoModel: PlayerInfoModel?) {
        mSelectedPlayerInfoModel = selectedPlayerInfoModel
    }

    fun setOnClickPlayerListener(onClickPlayerListener: OnClickPlayerListener) {
        mOnClickPlayerListener = onClickPlayerListener
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    private inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var mIvPlayerIcon: BaseImageView
        internal var mIvSelectedIcon: ImageView

        internal var mPlayerInfoModel: PlayerInfoModel?=null

        init {
            mIvPlayerIcon = itemView.findViewById<View>(R.id.iv_player_icon) as BaseImageView
            mIvSelectedIcon = itemView.findViewById<View>(R.id.iv_selected_icon) as ImageView
            mIvPlayerIcon.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View) {
                    val grabPlayerInfoModel = mSelectedPlayerInfoModel
                    mSelectedPlayerInfoModel = mPlayerInfoModel
                    update(mSelectedPlayerInfoModel)

                    if (grabPlayerInfoModel != null) {
                        update(grabPlayerInfoModel)
                    }

                    if (mOnClickPlayerListener != null) {
                        mOnClickPlayerListener!!.onClick(mPlayerInfoModel)
                    }
                }
            })
        }

        fun bind(model: PlayerInfoModel) {
            this.mPlayerInfoModel = model
            AvatarUtils.loadAvatarByUrl(mIvPlayerIcon,
                    AvatarUtils.newParamsBuilder(model.userInfo.avatar)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())

            if (mSelectedPlayerInfoModel != null && mSelectedPlayerInfoModel!!.userID == mPlayerInfoModel?.userID) {
                mIvSelectedIcon.visibility = View.VISIBLE
            } else {
                mIvSelectedIcon.visibility = View.GONE
            }
        }
    }

    interface OnClickPlayerListener {
        fun onClick(playerInfoModel: PlayerInfoModel?)
    }
}
