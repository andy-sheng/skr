package com.module.playways.songmanager.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.log.MyLog

import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.DiffAdapter
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.module.playways.R
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.SongManagerActivity
import com.module.playways.songmanager.utils.SongTagDrawableUtils
import com.zq.live.proto.Common.StandPlayType

class RecommendSongAdapter(internal var isOwner: Boolean, var type: Int, internal var mListener: RecyclerOnItemClickListener<SongModel>?) : DiffAdapter<SongModel, RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recommend_song_item_layout, parent, false)
        return ItemHolder(view, isOwner, type)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = mDataList[position]

        val reportItemHolder = holder as ItemHolder
        reportItemHolder.bind(model, position)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    private inner class ItemHolder(itemView: View, isOwner: Boolean, type: Int) : RecyclerView.ViewHolder(itemView) {

        var mSelectTv: ExTextView = itemView.findViewById(R.id.select_tv)
        var mSongTag: ExTextView = itemView.findViewById(R.id.song_tag)
        var mSongDesc: TextView = itemView.findViewById(R.id.song_desc)
        var mSongNameTv: ExTextView = itemView.findViewById(R.id.song_name_tv)

        private var mSongModel: SongModel? = null

        init {
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
            if (type == SongManagerActivity.TYPE_FROM_MIC) {
                MyLog.d(TAG, "bind ItemHolder = type = $type")
                if (model.playType == StandPlayType.PT_COMMON_TYPE.value) {
                    mSelectTv.text = "点歌"
                } else {
                    mSelectTv.text = "发起"
                }
            } else {
                if (isOwner) {
                    mSelectTv.text = "点歌"
                } else {
                    mSelectTv.text = "想唱"
                }
            }
            mSongNameTv.text = "《" + model.displaySongName + "》"
            mSongTag.visibility = View.VISIBLE
            if (TextUtils.isEmpty(mSongModel?.songDesc)) {
                mSongDesc.visibility = View.GONE
            } else {
                mSongDesc.visibility = View.VISIBLE
                mSongDesc.text = mSongModel?.songDesc
            }
            when {
                model.playType == StandPlayType.PT_SPK_TYPE.value -> {
                    mSongTag.background = SongTagDrawableUtils.pkDrawable
                    mSongTag.text = "PK"
                }
                model.playType == StandPlayType.PT_CHO_TYPE.value -> {
                    mSongTag.background = SongTagDrawableUtils.chorusDrawable
                    mSongTag.text = "合唱"
                }
                model.playType == StandPlayType.PT_MINI_GAME_TYPE.value -> {
                    mSongTag.background = SongTagDrawableUtils.miniGameDrawable
                    mSongTag.text = "双人游戏"
                }
                model.playType == StandPlayType.PT_FREE_MICRO.value -> {
                    mSongTag.background = SongTagDrawableUtils.freeMicDrawable
                    mSongTag.text = "多人游戏"
                }
                else -> mSongTag.visibility = View.GONE
            }
        }
    }
}