package com.module.posts.publish.redpkg

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.module.posts.R
import java.io.Serializable

class PostsRedPkgAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var dataList = ArrayList<RedPkgModel>()
    var selectModel: RedPkgModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_publish_item_red_pkg_layout, parent, false)
        return RedPkgItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = dataList.get(position)
        val reportItemHolder = holder as RedPkgItemHolder
        reportItemHolder.bindData(model)
    }


    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class RedPkgItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val coinIv: ImageView
        val coinTv: TextView

        init {
            coinIv = itemView.findViewById(R.id.coin_iv)
            coinTv = itemView.findViewById(R.id.coin_tv)
            itemView.setOnClickListener {
                selectModel = model
                notifyDataSetChanged()
            }
        }

        var model: RedPkgModel? = null
        fun bindData(model: RedPkgModel) {
            this.model = model
            if (this.model?.redpacketType == 1) {
                coinIv.setImageResource(R.drawable.hongbao_jinbi)
            } else if (this.model?.redpacketType == 2) {
                coinIv.setImageResource(R.drawable.hongbao_zuanshi)
            }
            coinTv.text = this.model?.redpacketDesc
            itemView.isSelected = this.model!! == selectModel
        }
    }

}

