package com.module.mall.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.module.home.R
import com.module.mall.model.PackageModel
import com.module.mall.model.ProductModel

class PackageAdapter(val getPacketItemIDMethod: (() -> String), val setSelectedItemFirstMethod: ((packetItemID: String, index: Int) -> Unit)) : DiffAdapter<PackageModel, PackageAdapter.PackageHolder>() {

    var useEffectMethod: ((PackageModel) -> Unit)? = null
    var cancelUseEffectMethod: ((PackageModel) -> Unit)? = null
    var selectItemMethod: ((ProductModel, Int, String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.package_product_item_layout, parent, false)
        return PackageHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PackageHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    override fun onBindViewHolder(holder: PackageHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bindData(mDataList[position], position)
        } else {
            // 局部刷新
            payloads.forEach {
                if (it is Int) {
                    refreshHolder(holder, position, it)
                }
            }
        }
    }

    fun refreshHolder(holder: PackageHolder, position: Int, refreshType: Int) {
        holder.updateText(mDataList[position], position)
    }

    inner class PackageHolder : RecyclerView.ViewHolder {
        var bg: ExImageView
        var productName: ExTextView
        var dataTv: ExTextView
        var btnIv: ExTextView
        var strokeIv: ExImageView
        var effectIv: BaseImageView

        var model: PackageModel? = null
        var index: Int = -1

        constructor(itemView: View) : super(itemView) {
            bg = itemView.findViewById(R.id.bg)
            productName = itemView.findViewById(R.id.product_name)
            dataTv = itemView.findViewById(R.id.data_tv)
            btnIv = itemView.findViewById(R.id.btn_iv)
            strokeIv = itemView.findViewById(R.id.stroke_iv)
            effectIv = itemView.findViewById(R.id.effect_iv)

            btnIv.setDebounceViewClickListener {
                if (model?.useStatus == 2) {
                    cancelUseEffectMethod?.invoke(model!!)
                } else if (model?.useStatus == 1) {
                    useEffectMethod?.invoke(model!!)
                }
            }

            bg.setDebounceViewClickListener {
                selectItemMethod?.invoke(model?.goodsInfo!!, index, model!!.packetItemID)
            }
        }

        fun bindData(model: PackageModel, position: Int) {
            this.model = model
            this.index = position
            AvatarUtils.loadAvatarByUrl(effectIv,
                    AvatarUtils.newParamsBuilder(model.goodsInfo?.goodsURL)
                            .setSizeType(ImageUtils.SIZE.SIZE_160)
                            .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                            .build())


            dataTv.text = model.expireTimeStr
            updateText(model, index)
        }

        fun updateText(model: PackageModel, position: Int) {
            this.model = model
            this.index = position

            productName.text = model.goodsInfo?.goodsName
            if (model.useStatus == 2) {
                btnIv.isSelected = true
                btnIv.text = "取消使用"
            } else if (model.useStatus == 1) {
                btnIv.isSelected = false
                btnIv.text = "使用"
            }

            if (TextUtils.isEmpty(getPacketItemIDMethod.invoke())) {
                if (model.useStatus == 2) {
                    strokeIv.visibility = View.VISIBLE
                } else if (model.useStatus == 1) {
                    strokeIv.visibility = View.GONE
                }

                setSelectedItemFirstMethod.invoke(model.packetItemID, position)
            } else {
                if (getPacketItemIDMethod.invoke().equals(model.packetItemID)) {
                    strokeIv.visibility = View.VISIBLE
                } else {
                    strokeIv.visibility = View.GONE
                }
            }
        }
    }
}