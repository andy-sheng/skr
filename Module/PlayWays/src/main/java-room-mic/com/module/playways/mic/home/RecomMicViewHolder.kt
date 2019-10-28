package com.module.playways.mic.home

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.level.utils.LevelConfigUtils
import com.module.playways.R

class RecomMicViewHolder(item: View, listener: RecomMicListener) : RecyclerView.ViewHolder(item) {

    val levelIv: ImageView = item.findViewById(R.id.level_iv)
    val levelDescTv: TextView = item.findViewById(R.id.level_desc_tv)
    val recyclerView: RecyclerView = item.findViewById(R.id.recycler_view)
    val divider: View = item.findViewById(R.id.divider)
    val enterRoomTv: ExTextView = item.findViewById(R.id.enter_room_tv)
    val roomNameTv: TextView = item.findViewById(R.id.room_name_tv)

    val childAdapter: RecomMicChildAdapter = RecomMicChildAdapter()

    var mModel: RecomMicInfoModel? = null
    var mPosition: Int = 0

    var playPosition: Int = -1  // 记录下holder的播放位置

    init {
        recyclerView.layoutManager = GridLayoutManager(item.context, 3)
        recyclerView.adapter = childAdapter

        enterRoomTv.setAnimateDebounceViewClickListener {
            // todo 进入房间中
            listener.onClickEnterRoom(mModel, mPosition)
        }

        childAdapter.onClickVoice = { model, position ->
            listener.onClickUserVoice(mModel, mPosition, model, position)
        }
    }

    fun bindData(model: RecomMicInfoModel, position: Int) {
        this.mPosition = position
        this.mModel = model
        this.playPosition = -1

        childAdapter.mDataList.clear()
        if (model.roomInfo?.userList.isNullOrEmpty()) {
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            childAdapter.mDataList.addAll(model.roomInfo?.userList!!)
            childAdapter.notifyDataSetChanged()
        }

        if (LevelConfigUtils.getImageResoucesLevel(model.roomInfo?.roomLevel ?: 0) != 0) {
            levelIv.visibility = View.VISIBLE
            levelIv.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(model.roomInfo?.roomLevel
                    ?: 0))
        } else {
            //todo 全名局 缺个图标
            levelIv.visibility = View.INVISIBLE
        }

        roomNameTv.text = model.roomInfo?.roomName
        levelDescTv.text = LevelConfigUtils.getMicLevelDesc(model.roomInfo?.roomLevel ?: 0)
    }

    fun startPlay(playPosition: Int) {
        // 播放指定playPosition 位置的动画(可能需要停掉其他位置的)
        stopPlay()
        this.playPosition = playPosition

        val holder = recyclerView.findViewHolderForAdapterPosition(playPosition)
        if (holder is RecomMicChildAdapter.RecomChildViewHolder) {
            holder.starPlay()
        }
    }

    fun stopPlay() {
        // 停调所有动画
        if (playPosition >= 0) {
            val holder = recyclerView.findViewHolderForAdapterPosition(playPosition)
            if (holder is RecomMicChildAdapter.RecomChildViewHolder) {
                holder.stopPlay()
            }
        }
        playPosition = -1
    }
}