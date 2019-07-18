package com.module.home.game.viewholder.grab

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.View
import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.component.busilib.friends.SpecialModel
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.home.R


class GrabTagViewHolder(itemView: View, onClickTagListener: ((model: SpecialModel?) -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    var model: SpecialModel? = null
    var pos: Int = 0

    val mImageSdv: SimpleDraweeView = itemView.findViewById(R.id.image_sdv)
    val mBackground: ConstraintLayout = itemView.findViewById(R.id.background)


    init {
        itemView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View?) {
                onClickTagListener?.invoke(model)
            }
        })
    }

    fun bind(position: Int, model: SpecialModel) {
        this.model = model
        this.pos = position


        var lp = mBackground.layoutParams //此为父布局
        lp.height = (model?.biggest.h) * (U.getDisplayUtils().screenWidth / 2 - U.getDisplayUtils().dip2px(18f)) / (model?.biggest.w)
        mBackground.layoutParams = lp

        FrescoWorker.loadImage(mImageSdv, ImageFactory.newPathImage(model?.biggest.url)
                .setScaleType(ScalingUtils.ScaleType.FIT_XY)
                .build<BaseImage>())
    }
}
