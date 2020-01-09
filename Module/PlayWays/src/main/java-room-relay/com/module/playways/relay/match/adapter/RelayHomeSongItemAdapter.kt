package com.module.playways.relay.match.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.room.song.model.SongModel

class RelayHomeSongItemAdapter(var listener: RelaySongListener?) : RecyclerView.Adapter<RelayHomeSongItemAdapter.RelaySongItemViewHolder>() {

    var mDataList = ArrayList<SongModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelaySongItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.relay_home_song_item_layout, parent, false)
        return RelaySongItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RelaySongItemViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class RelaySongItemViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val songSelectTv: ExTextView = item.findViewById(R.id.song_select_tv)
        private val songNameTv: ExTextView = item.findViewById(R.id.song_name_tv)
        private val songDesc: TextView = item.findViewById(R.id.song_desc)
        private val divider: View = item.findViewById(R.id.song_desc)

        var mPos = -1
        var mModel: SongModel? = null

        init {
            songSelectTv.setAnimateDebounceViewClickListener {
                listener?.selectSong(mPos, mModel)
            }
            songNameTv.setDebounceViewClickListener {
                listener?.selectSongDetail(mPos, mModel)
            }
            songDesc.setDebounceViewClickListener {
                listener?.selectSongDetail(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: SongModel) {
            this.mModel = model
            this.mPos = position

            songNameTv.text = model.itemName
            if (TextUtils.isEmpty(model.songDesc)) {
                songDesc.visibility = View.GONE
            } else {
                songDesc.visibility = View.VISIBLE
                songDesc.text = model.songDesc
            }
        }

    }

    interface RelaySongListener {
        fun selectSong(position: Int, model: SongModel?)
        fun selectSongDetail(position: Int, model: SongModel?)
    }
}
