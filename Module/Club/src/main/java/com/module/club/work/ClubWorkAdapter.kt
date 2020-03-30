package com.module.club.work

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.club.R

class ClubWorkAdapter : RecyclerView.Adapter<ClubWorkAdapter.PartyAreaItemHolder>() {

    var mDataList = ArrayList<WorkModel>()
    var clickListener: ((position: Int, model: WorkModel?) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyAreaItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_work_view_layout, parent, false)
        return PartyAreaItemHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PartyAreaItemHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class PartyAreaItemHolder(item: View) : RecyclerView.ViewHolder(item) {

        val coverSdv: SimpleDraweeView = item.findViewById(R.id.cover_sdv)
        val nameTextView: TextView = item.findViewById(R.id.name_tv)
        val statusTextView: TextView = item.findViewById(R.id.status_tv)
        val tagTextView: TextView = item.findViewById(R.id.tag_tv)
        var imagePlay: ImageView = item.findViewById(R.id.record_play_iv)

        private var mModl: WorkModel? = null
        private var mPos: Int = -1

        init {
            item.setDebounceViewClickListener {
                clickListener?.invoke(mPos, mModl)
            }
            imagePlay.setDebounceViewClickListener {

            }
        }

        fun bindData(position: Int, model: WorkModel) {
            this.mModl = model
            this.mPos = position
            //TODO 字段可能要调整
            FrescoWorker.loadImage(coverSdv, ImageFactory.newPathImage(model?.avatar)
                    .setCornerRadius(8.dp().toFloat())
                    .build())
            nameTextView.text = model?.nickName
            statusTextView.text = model?.songName
            tagTextView.text = model?.artist
        }
    }
}