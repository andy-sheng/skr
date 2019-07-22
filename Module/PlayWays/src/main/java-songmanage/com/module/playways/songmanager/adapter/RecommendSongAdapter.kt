package com.module.playways.songmanager.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.common.view.recyclerview.DiffAdapter
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.module.playways.R
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.Common.StandPlayType

class RecommendSongAdapter(internal var isOwner: Boolean, internal var mListener: RecyclerOnItemClickListener<SongModel>?) : DiffAdapter<SongModel, RecyclerView.ViewHolder>() {

    val pk: Drawable = DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setSolidColor(Color.parseColor("#CB5883"))
            .build()

    val togather: Drawable = DrawableCreator.Builder()
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setSolidColor(Color.parseColor("#7088FF"))
            .build()

    val game: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#61B14F"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()

    val freeMic: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#C856E0"))
            .setCornersRadius(U.getDisplayUtils().dip2px(10f).toFloat())
            .setStrokeWidth(U.getDisplayUtils().dip2px(1.5f).toFloat())
            .setStrokeColor(U.getColor(R.color.white_trans_70))
            .build()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recommend_song_item_layout, parent, false)
        return ItemHolder(view, isOwner)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = mDataList[position]

        val reportItemHolder = holder as ItemHolder
        reportItemHolder.bind(model, position)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    private inner class ItemHolder(itemView: View, isOwner: Boolean) : RecyclerView.ViewHolder(itemView) {

        var mSelectTv: ExTextView = itemView.findViewById(R.id.select_tv)
        var mSongTag: ExTextView = itemView.findViewById(R.id.song_tag)
        var mSongDesc: TextView = itemView.findViewById(R.id.song_desc)
        var mSongNameTv: ExTextView = itemView.findViewById(R.id.song_name_tv)

        private var mSongModel: SongModel? = null

        init {
            if (isOwner) {
                mSelectTv.text = "点歌"
            } else {
                mSelectTv.text = "想唱"
            }

            mSelectTv.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View) {
                    if (mListener != null) {
                        mListener!!.onItemClicked(v, -1, mSongModel)
                    }
                }
            })
        }

        fun bind(model: SongModel, position: Int) {
            mSongModel = model
            mSongNameTv.text = "《" + model.displaySongName + "》"
            mSongTag.visibility = View.VISIBLE
            if (TextUtils.isEmpty(mSongModel?.songDesc)) {
                mSongDesc.visibility = View.GONE
            } else {
                mSongDesc.visibility = View.VISIBLE
                mSongDesc.text = mSongModel?.songDesc
            }
            if (model.playType == StandPlayType.PT_SPK_TYPE.value) {
                mSongTag.background = pk
                mSongTag.text = "PK"
            } else if (model.playType == StandPlayType.PT_CHO_TYPE.value) {
                mSongTag.background = togather
                mSongTag.text = "合唱"
            } else if (model.playType == StandPlayType.PT_MINI_GAME_TYPE.value) {
                mSongTag.background = game
                mSongTag.text = "双人游戏"
            } else if (model.playType == StandPlayType.PT_FREE_MICRO.value) {
                mSongTag.background = freeMic
                mSongTag.text = "多人游戏"
            } else {
                mSongTag.visibility = View.GONE
            }
        }
    }
}