package com.module.playways.party.room.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.view.ExViewStub
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.party.room.model.PartyEmojiInfoModel

class PartyEmojiView(viewStub: ViewStub) : ExViewStub(viewStub) {

    private var recyclerView: RecyclerView? = null
    private val adapter = PartyEmojiAdapter()

    override fun init(parentView: View) {
        recyclerView = parentView.findViewById(R.id.recycler_view)

        recyclerView?.layoutManager = LinearLayoutManager(parentView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.adapter = adapter
    }

    override fun layoutDesc(): Int {
        return R.layout.party_emoji_view_layout
    }

    fun bindData() {
        // todo 去获取数据去
    }

    class PartyEmojiAdapter : RecyclerView.Adapter<PartyEmojiAdapter.PartyEmojiViewHolder>() {

        var mDataList = ArrayList<PartyEmojiInfoModel>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyEmojiViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.party_emoji_item_layout, parent, false)
            return PartyEmojiViewHolder(view)
        }

        override fun getItemCount(): Int {
            return mDataList.size
        }

        override fun onBindViewHolder(holder: PartyEmojiViewHolder, position: Int) {
            holder.bindData(position, mDataList[position])
        }


        inner class PartyEmojiViewHolder(item: View) : RecyclerView.ViewHolder(item) {

            val emojiIv: SimpleDraweeView = item.findViewById(R.id.emoji_iv)
            val emojiDesc: TextView = item.findViewById(R.id.emoji_desc)

            var mPos = -1
            var mModel: PartyEmojiInfoModel? = null

            init {
                item.setDebounceViewClickListener { }
            }

            fun bindData(position: Int, model: PartyEmojiInfoModel) {
                this.mPos = position
                this.mModel = model

                FrescoWorker.loadImage(emojiIv, ImageFactory.newPathImage(model.smallEmojiURL)
                        .build())
                // todo  缺少描述信息
                emojiDesc.text = "缺少描述信息"
            }
        }

    }
}