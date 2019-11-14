package com.module.playways.room.record

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.ImageUtils
import com.common.utils.U
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class GiftRecordAdapter : RecyclerView.Adapter<GiftRecordAdapter.GiftRecordViewHolder>() {

    var mDataList = ArrayList<GiftRecordModel>()

    var onClickAvatarListener: ((model: GiftRecordModel?, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftRecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gift_record_item_view_layout, parent, false)
        return GiftRecordViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: GiftRecordViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    inner class GiftRecordViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val avatarView: AvatarView = item.findViewById(R.id.avatar_view)
        val nicknameView: NickNameView = item.findViewById(R.id.nickname_view)
        val timeDescTv: TextView = item.findViewById(R.id.time_desc_tv)
        val giftDescTv: TextView = item.findViewById(R.id.gift_desc_tv)
        val giftSdv: SimpleDraweeView = item.findViewById(R.id.gift_sdv)

        var mModel: GiftRecordModel? = null
        var mPos: Int = -1

        init {
            avatarView.setDebounceViewClickListener { onClickAvatarListener?.invoke(mModel, mPos) }
        }

        fun bindData(model: GiftRecordModel, position: Int) {
            this.mModel = model
            this.mPos = position

            avatarView.bindData(model.userInfo)
            nicknameView.setAllStateText(model.userInfo?.nicknameRemark, model.userInfo?.sex, model.userInfo?.honorInfo)
            timeDescTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(model.gift?.timeMs
                    ?: 0, System.currentTimeMillis())
            giftDescTv.text = "${model.gift?.actionDesc} x${model.gift?.amount}"

            FrescoWorker.loadImage(giftSdv, ImageFactory.newPathImage(model.gift?.giftPic)
                    .setScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                    .setResizeByOssProcessor(ImageUtils.SIZE.SIZE_640)
                    .build<BaseImage>())
        }
    }
}