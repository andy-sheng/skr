package com.module.home.game.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View

import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.friends.SpecialModel
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.home.R

class GrabSelectViewHolder(itemView: View, mItemClickListener: RecyclerOnItemClickListener<SpecialModel>?) : RecyclerView.ViewHolder(itemView) {

    val TAG = "GrabSelectViewHolder"

    private val mBackground: SimpleDraweeView = itemView.findViewById(R.id.background)

    internal var mSpecialModel: SpecialModel? = null
    internal var mPosition: Int = 0

    init {
        itemView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                mItemClickListener?.onItemClicked(view, mPosition, mSpecialModel)
            }
        })
    }

    fun bindData(specialModel: SpecialModel, postion: Int) {
        this.mSpecialModel = specialModel
        this.mPosition = postion

        FrescoWorker.loadImage(mBackground, ImageFactory.newPathImage(mSpecialModel?.bgImage1)
                .setLoadingDrawable(U.getDrawable(R.drawable.grab_img_btn_loading1))
                .setLoadingScaleType(ScalingUtils.ScaleType.FIT_XY)
                .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                .build<BaseImage>())
    }
}
