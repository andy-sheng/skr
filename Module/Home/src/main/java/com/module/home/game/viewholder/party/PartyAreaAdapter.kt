package com.module.home.game.viewholder.party

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.model.PartyRoomInfoModel
import com.component.busilib.model.PartyRoomTagMode
import com.facebook.drawee.view.SimpleDraweeView
import com.module.home.R

class PartyAreaAdapter : RecyclerView.Adapter<PartyAreaAdapter.PartyAreaItemHolder>() {

    var mDataList = ArrayList<PartyRoomInfoModel>()

    var clickListener: ((position: Int, model: PartyRoomInfoModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyAreaItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.game_party_area_item_layout, parent, false)
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
        val tagTv: ExTextView = item.findViewById(R.id.tag_tv)
        val descTv: TextView = item.findViewById(R.id.desc_tv)

        private var mModl: PartyRoomInfoModel? = null
        private var mPos: Int = -1

        init {
            item.setDebounceViewClickListener {
                clickListener?.invoke(mPos, mModl)
            }
        }

        fun bindData(position: Int, model: PartyRoomInfoModel) {
            this.mModl = model
            this.mPos = position

            FrescoWorker.loadImage(coverSdv, ImageFactory.newPathImage(model.avatarUrl)
                    .setCornerRadius(8.dp().toFloat())
                    .build())
            descTv.text = model.topicName
            
            tagTv.visibility = View.VISIBLE
            when (model.gameMode) {
                PartyRoomTagMode.ERM_GAME_PK -> tagTv.text = "游戏"
                PartyRoomTagMode.ERM_MAKE_FRIEND -> tagTv.text = "相亲"
                PartyRoomTagMode.ERM_SING_PK -> tagTv.text = "K歌"
                PartyRoomTagMode.ERM_ALL -> tagTv.text = "推荐"
                else -> tagTv.visibility = View.GONE
            }
        }
    }
}