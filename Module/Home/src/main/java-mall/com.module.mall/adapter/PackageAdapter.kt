package com.module.mall.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.home.R

class PackageAdapter : RecyclerView.Adapter<PackageAdapter.PackageHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.package_product_item_layout, parent, false)
        return PackageHolder(view)
    }

    override fun getItemCount(): Int {
        return 10
    }

    override fun onBindViewHolder(holder: PackageHolder, position: Int) {
        holder.bindData()
    }

    inner class PackageHolder : RecyclerView.ViewHolder {
        var bg: ExImageView
        var productName: ExTextView
        var dataTv: ExTextView
        var btnIv: ExTextView
        var strokeIv: ExImageView
        var avaterIv: BaseImageView

        constructor(itemView: View) : super(itemView) {
            bg = itemView.findViewById(R.id.bg)
            productName = itemView.findViewById(R.id.product_name)
            dataTv = itemView.findViewById(R.id.data_tv)
            btnIv = itemView.findViewById(R.id.btn_iv)
            strokeIv = itemView.findViewById(R.id.stroke_iv)
            avaterIv = itemView.findViewById(R.id.avatar_iv)
        }

        fun bindData() {
            AvatarUtils.loadAvatarByUrl(avaterIv,
                    AvatarUtils.newParamsBuilder(MyUserInfoManager.myUserInfo?.avatar)
                            .setCircle(true)
                            .build())
        }
    }
}