package com.module.playways.songmanager.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.constans.GameModeType
import com.module.playways.R
import com.module.playways.room.data.H
import com.module.playways.songmanager.model.GrabRoomSongModel
import com.module.playways.songmanager.model.MicExistSongModel
import com.module.playways.songmanager.utils.SongTagDrawableUtils
import com.zq.live.proto.Common.StandPlayType

class MicExistSongAdapter(var gameType: Int, var listener: MicExistListener?) : RecyclerView.Adapter<MicExistSongAdapter.ItemHolde>() {

    // todo 先用之前等接口，有变化再改吧
    var mDataList = ArrayList<MicExistSongModel>()
    var hasSing: Boolean = false  // 标记是是否在演唱中

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

        var mModel: MicExistSongModel? = null
        var mPosition: Int = 0

        init {
            tvManage.setAnimateDebounceViewClickListener { listener?.onClickDelete(mModel, mPosition) }
            stickIv.setAnimateDebounceViewClickListener { listener?.onStick(mModel, mPosition) }
        }

        fun bindData(model: MicExistSongModel, position: Int) {
            this.mModel = model
            this.mPosition = position

            if (model.status == MicExistSongModel.EUSI_IN_PLAY) {
                hasSing = true
                tvManage.isClickable = false
                tvManage.text = "演唱中"
                stickIv.visibility = View.GONE
                tvManage.background = SongTagDrawableUtils.grayDrawable
            } else {
                if (position == 0) {
                    stickIv.visibility = View.GONE
                } else if (hasSing && position == 1) {
                    // 除去演唱中，第一个不需要置顶
                    stickIv.visibility = View.GONE
                } else {
                    stickIv.visibility = View.VISIBLE
                }
                tvManage.isClickable = true
                tvManage.text = "删除"

                tvManage.background = SongTagDrawableUtils.redDrawable

                if (gameType == GameModeType.GAME_MODE_PARTY) {
                    // 显示的是所有人的已点
                    if (H.partyRoomData?.myUserInfo?.isHost() == true) {
                        // 置顶与之前保持一致即可
                        tvManage.visibility = View.VISIBLE
                    } else {
                        stickIv.visibility = View.GONE
                        if (model.userID == MyUserInfoManager.uid.toInt()) {
                            // 我点的歌
                            tvManage.visibility = View.VISIBLE
                        } else {
                            tvManage.visibility = View.GONE
                        }
                    }
                }
            }
            tvSongDesc.visibility = View.GONE

            when (model.songModel?.playType) {
                StandPlayType.PT_SPK_TYPE.value -> {
                    songTagTv.text = "PK"
                    songTagTv.visibility = View.VISIBLE
                    songTagTv.background = SongTagDrawableUtils.pkDrawable
                    tvSongName.text = "《" + model.songModel?.displaySongName + "》"
                }
                StandPlayType.PT_CHO_TYPE.value -> {
                    songTagTv.text = "合唱"
                    songTagTv.visibility = View.VISIBLE
                    songTagTv.background = SongTagDrawableUtils.chorusDrawable
                    tvSongName.text = "《" + model.songModel?.displaySongName + "》"
                }
                StandPlayType.PT_MINI_GAME_TYPE.value -> {
                    songTagTv.text = "双人游戏"
                    songTagTv.visibility = View.VISIBLE
                    songTagTv.background = SongTagDrawableUtils.miniGameDrawable
                    tvSongName.text = "【" + model.songModel?.itemName + "】"
                }
                StandPlayType.PT_FREE_MICRO.value -> {
                    songTagTv.text = "多人游戏"
                    songTagTv.visibility = View.VISIBLE
                    songTagTv.background = SongTagDrawableUtils.freeMicDrawable
                    tvSongName.text = "【" + model.songModel?.itemName + "】"
                }
                else -> {
                    songTagTv.visibility = View.GONE
                    tvSongName.text = "《" + model.songModel?.displaySongName + "》"
                }
            }
        }
    }
}

interface MicExistListener {
    fun onClickDelete(model: MicExistSongModel?, position: Int)
    fun onStick(model: MicExistSongModel?, position: Int)
}