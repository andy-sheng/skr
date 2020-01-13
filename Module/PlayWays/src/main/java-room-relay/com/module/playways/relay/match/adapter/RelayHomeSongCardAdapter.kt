package com.module.playways.relay.match.adapter

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.module.playways.R
import com.module.playways.room.song.model.SongCardModel
import com.module.playways.room.song.model.SongModel

class RelayHomeSongCardAdapter(val maxSize: Int) : RecyclerView.Adapter<RelayHomeSongCardAdapter.RelaySongCardViewHolder>() {

    var listener: RelayHomeListener? = null
    var mDataList = ArrayList<SongCardModel>()
    private val cardAdapterHelper = CardAdapterHelper(8, 12)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelaySongCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.relay_song_card_item_layout, parent, false)
        cardAdapterHelper.onCreateViewHolder(parent, view)
        return RelaySongCardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RelaySongCardViewHolder, position: Int) {
        cardAdapterHelper.onBindViewHolder(holder.itemView, position, itemCount, listener?.getRecyclerViewPosition() == position
                || mDataList.size == 1)
        holder.bindData(position, mDataList[position])
    }

    fun addData(list: List<SongModel>?) {
        if (list?.isNotEmpty() == true) {
            val oldSize = mDataList.size
            val lastDiff = if (oldSize > 0) maxSize - mDataList[oldSize - 1].list.size else 0
            var songCardModel: SongCardModel? = null
            for (i in list.indices) {
                if (oldSize > 0 && mDataList[oldSize - 1].list.size != maxSize) {
                    mDataList[oldSize - 1].list.add(list[i])
                } else {
                    if (songCardModel == null) {
                        songCardModel = SongCardModel()
                    }
                    songCardModel.list.add(list[i])

                    if ((i - lastDiff + 1) % maxSize == 0) {
                        mDataList.add(songCardModel)
                        songCardModel = null
                    }
                }
            }
            if (songCardModel != null) {
                mDataList.add(songCardModel)
            }

            // 必须得从oldSize - 1开始change，数据得间隔会不对
            if (oldSize > 0) {
                notifyItemRangeChanged(oldSize - 1, mDataList.size - oldSize + 1)
            } else {
                notifyItemRangeChanged(0, mDataList.size)
            }

        }
    }

    inner class RelaySongCardViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val songView: RecyclerView = item.findViewById(R.id.song_view)
        private var adapter: RelayHomeSongItemAdapter

        var mPos = -1

        init {
            songView.layoutManager = LinearLayoutManager(item.context, LinearLayoutManager.VERTICAL, false)
            adapter = RelayHomeSongItemAdapter(object : RelayHomeSongItemAdapter.RelaySongListener {
                override fun selectSong(position: Int, model: SongModel?) {
                    listener?.selectSong(mPos, position, model)
                }

                override fun selectSongDetail(position: Int, model: SongModel?) {
                    listener?.selectSongDetail(mPos, position, model)
                }

            }, maxSize, false)
            songView.adapter = adapter
        }

        fun bindData(position: Int, model: SongCardModel) {
            this.mPos = position

            adapter.mDataList.clear()
            adapter.mDataList.addAll(model.list)
            adapter.notifyDataSetChanged()
        }
    }

    interface RelayHomeListener {
        fun getRecyclerViewPosition(): Int
        fun selectSong(position: Int, childPosition: Int, model: SongModel?)
        fun selectSongDetail(position: Int, childPosition: Int, model: SongModel?)
    }
}