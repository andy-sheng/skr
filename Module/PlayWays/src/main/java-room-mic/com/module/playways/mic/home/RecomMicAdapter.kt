package com.module.playways.mic.home

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.playways.R

class RecomMicAdapter(var listener: RecomMicListener) : RecyclerView.Adapter<RecomMicViewHolder>() {

    var mDataList = ArrayList<RecomMicInfoModel>()

    var isPlay = false    //标记是否播放
    var playPosition = -1   //标记播放的位置
    var playChildPosition = -1  //标记播放的是holder里面某个位置
    var currPlayModel: RecomMicInfoModel? = null

    val REFRESH_PLAY = 1 //局部刷新播放
    private val uiHanlder = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecomMicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recom_item_layout, parent, false)
        return RecomMicViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RecomMicViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecomMicViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bindData(mDataList[position], position)
            if (isPlay && currPlayModel == mDataList[position]) {
                holder.startPlay(playChildPosition)
            } else {
                holder.stopPlay()
            }
        } else {
            // 局部更新
            payloads.forEach { refreshType ->
                if (refreshType is Int) {
                    when (refreshType) {
                        REFRESH_PLAY -> {
                            // 只有播放
                            if (isPlay && currPlayModel === mDataList[position]) {
                                holder.startPlay(playChildPosition)
                            } else {
                                holder.stopPlay()
                            }
                        }
                    }
                }
            }
        }
    }

    fun startPlay(model: RecomMicInfoModel?, position: Int, recomUserInfo: RecomUserInfo?, childPos: Int) {
        isPlay = true

        when {
            currPlayModel != model -> {
                // 需要更新 2个holder
                val lastPos = playPosition
                currPlayModel = model
                playChildPosition = childPos
                notifyItemChanged(position, REFRESH_PLAY)
                if (lastPos >= 0) {
                    // 停掉之前的
                    uiHanlder.post {
                        notifyItemChanged(lastPos, REFRESH_PLAY)
                    }
                }

            }
            childPos != playChildPosition -> {
                // 需要更新 1个holder
                playChildPosition = childPos
                notifyItemChanged(position, REFRESH_PLAY)
            }
            else -> {
                // todo donothing 算错误
            }
        }
    }

    fun stopPlay() {
        isPlay = false
        update(playPosition, currPlayModel, REFRESH_PLAY)
        // 重置数据
        playPosition = -1
        playChildPosition = -1
        currPlayModel = null
    }

    fun update(position: Int, model: RecomMicInfoModel?, refreshType: Int) {
        if (mDataList != null || mDataList.size > 0) {
            if (position >= 0 && position < mDataList.size && mDataList[position] === model) {
                // 位置是对的
                notifyItemChanged(position, refreshType)
                return
            } else {
                // 位置是错的
                for (i in mDataList.indices) {
                    if (mDataList[i] === model) {
                        notifyItemChanged(i, refreshType)
                        return
                    }
                }
            }
        } else {
            playPosition = -1
            playChildPosition = -1
            currPlayModel = null
        }
    }

}

interface RecomMicListener {
    fun onClickEnterRoom(model: RecomMicInfoModel?, position: Int)

    fun onClickUserVoice(model: RecomMicInfoModel?, position: Int, userInfoModel: RecomUserInfo?, childPos: Int)
}
