package com.module.playways.mic.home

import android.graphics.Color
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.common.view.ex.shadow.ShadowConfig
import com.component.level.utils.LevelConfigUtils
import com.module.playways.R

class RecommendMicViewHolder(item: View, listener: RecommendMicListener) : RecyclerView.ViewHolder(item) {

    val background: ExConstraintLayout = item.findViewById(R.id.background)
    val levelIv: ImageView = item.findViewById(R.id.level_iv)
    val levelDescTv: TextView = item.findViewById(R.id.level_desc_tv)
    val recyclerView: RecyclerView = item.findViewById(R.id.recycler_view)
    val divider: View = item.findViewById(R.id.divider)
    val enterRoomTv: ExTextView = item.findViewById(R.id.enter_room_tv)
    val roomNameTv: TextView = item.findViewById(R.id.room_name_tv)

    val childAdapter: RecommendMicChildAdapter = RecommendMicChildAdapter()

    var mModel: RecommendMicInfoModel? = null
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

    fun bindData(model: RecommendMicInfoModel, position: Int) {
        this.mPosition = position
        this.mModel = model
        this.playPosition = -1

        val shadowConfig = ShadowConfig.obtain()
                .color(Color.parseColor("#809F6CDF"))
                .radius(9f)
                .leftBottomCorner(48)
                .rightBottomCorner(48)
                .yOffset(18)
        background.setShadowConfig(shadowConfig)

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
            levelIv.background = U.getDrawable(R.drawable.mic_all_people_icon)
        }

        if (TextUtils.isEmpty(model.roomInfo?.roomName)) {
            roomNameTv.text = "小K房 以歌会友"
        } else {
            roomNameTv.text = model.roomInfo?.roomName
        }

        levelDescTv.text = LevelConfigUtils.getMicLevelDesc(model.roomInfo?.roomLevel ?: 0)
    }

    fun startPlay(playPosition: Int) {
        // 播放指定playPosition 位置的动画(可能需要停掉其他位置的)
        stopPlay()
        this.playPosition = playPosition

        val holder = recyclerView.findViewHolderForAdapterPosition(playPosition)
        if (holder is RecommendMicChildAdapter.RecomChildViewHolder) {
            holder.starPlay()
        }
    }

    fun stopPlay() {
        // 停调所有动画
        if (playPosition >= 0) {
            val holder = recyclerView.findViewHolderForAdapterPosition(playPosition)
            if (holder is RecommendMicChildAdapter.RecomChildViewHolder) {
                holder.stopPlay()
            }
        }
        playPosition = -1
    }
}