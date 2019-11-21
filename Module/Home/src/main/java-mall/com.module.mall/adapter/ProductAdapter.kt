package com.module.mall.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.module.home.R
import com.module.mall.model.ProductModel

class ProductAdapter : DiffAdapter<ProductModel, ProductAdapter.ProductHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mall_product_item_layout, parent, false)
        return ProductHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        holder.bindData(mDataList[position])
    }

    inner class ProductHolder : RecyclerView.ViewHolder {
        var bg: ExImageView
        var productName: ExTextView
        var btnIv: ExTextView
        var strokeIv: ExImageView
        var effectIv: BaseImageView

        var model: ProductModel? = null

        constructor(itemView: View) : super(itemView) {
            bg = itemView.findViewById(R.id.bg)
            productName = itemView.findViewById(R.id.product_name)
            btnIv = itemView.findViewById(R.id.btn_iv)
            strokeIv = itemView.findViewById(R.id.stroke_iv)
            effectIv = itemView.findViewById(R.id.effect_iv)
        }

        fun bindData(model: ProductModel) {
            this.model = model
            AvatarUtils.loadAvatarByUrl(effectIv,
                    AvatarUtils.newParamsBuilder(model.goodsURL)
                            .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                            .build())

            updateText(model)
        }

        fun updateText(model: ProductModel) {
            this.model = model
            productName.text = model.goodsName

        }
    }
}