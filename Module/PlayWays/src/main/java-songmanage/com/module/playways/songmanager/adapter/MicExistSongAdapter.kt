package com.module.playways.songmanager.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.songmanager.model.GrabRoomSongModel

class MicExistSongAdapter(var listener: MicExistListener?) : RecyclerView.Adapter<MicExistSongAdapter.ItemHolde>() {

    // todo 先用之前等接口，有变化再改吧
    var mDataList = ArrayList<GrabRoomSongModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolde {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_exist_song_item_layout, parent, false)
        return ItemHolde(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ItemHolde, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    inner class ItemHolde(item: View) : RecyclerView.ViewHolder(item) {

        val tvSongName: ExTextView = item.findViewById(R.id.tv_song_name)
        val songTagTv: TextView = item.findViewById(R.id.song_tag_tv)
        val stickIv: ImageView = item.findViewById(R.id.stick_iv)
        val tvSongDesc: TextView = item.findViewById(R.id.tv_song_desc)
        val tvManage: ExTextView = item.findViewById(R.id.tv_manage)

        var mModel: GrabRoomSongModel? = null
        var mPosition: Int = 0

        init {
            tvManage.setAnimateDebounceViewClickListener { listener?.onClickDelete(mModel, mPosition) }
            stickIv.setAnimateDebounceViewClickListener { listener?.onStick(mModel, mPosition) }
        }

        fun bindData(model: GrabRoomSongModel, position: Int) {

        }
    }
}

public interface MicExistListener {
    fun onClickDelete(model: GrabRoomSongModel?, position: Int)
    fun onStick(model: GrabRoomSongModel?, position: Int)
}