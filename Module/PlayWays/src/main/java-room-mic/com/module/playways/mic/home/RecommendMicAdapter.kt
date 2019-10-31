package com.module.playways.mic.home

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.playways.R

class RecommendMicAdapter(var listener: RecommendMicListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<RecommendMicInfoModel>()

    val VIEW_TYPE_HEAD = 1
    val VIEW_TYPE_ITEM = 2

    var isPlay = false    //标记是否播放
    var playPosition = -1   //标记播放的位置
    var playChildPosition = -1  //标记播放的是holder里面某个位置
    var currPlayModel: RecommendMicInfoModel? = null

    val REFRESH_PLAY = 1 //局部刷新播放
    val REFRESH_STOP = 2 //局部刷新暂停
    private val uiHanlder = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recommend_item_layout, parent, false)
            RecommendMicViewHolder(view, listener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recommend_head_layout, parent, false)
            RecommendMicHeadHolder(view, listener)
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_HEAD
        } else {
            VIEW_TYPE_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (holder is RecommendMicViewHolder) {
            if (payloads.isEmpty()) {
                holder.bindData(mDataList[position - 1], position)
                if (isPlay && currPlayModel == mDataList[position - 1]) {
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
                                if (isPlay && currPlayModel === mDataList[position - 1]) {
                                    holder.startPlay(playChildPosition)
                                } else {
                                    holder.stopPlay()
                                }
                            }
                            REFRESH_STOP -> {
                                // 停止播放
                                holder.stopPlay()
                            }
                        }
                    }
                }
            }
        }
    }

    fun startPlay(model: RecommendMicInfoModel?, position: Int, recomUserInfo: RecommendUserInfo?, childPos: Int) {
        isPlay = true

        when {
            currPlayModel != model -> {
                // 需要更新 2个holder
                val lastPos = playPosition
                currPlayModel = model
                playPosition = position
                playChildPosition = childPos
                notifyItemChanged(position, REFRESH_PLAY)
                if (lastPos >= 0) {
                    // 停掉之前的
                    uiHanlder.post {
                        notifyItemChanged(lastPos, REFRESH_STOP)
                    }
                }

            }
            childPos != playChildPosition -> {
                // 需要更新 1个holder
                currPlayModel = model
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
        update(playPosition, currPlayModel, REFRESH_STOP)
        // 重置数据
        playPosition = -1
        playChildPosition = -1
        currPlayModel = null
    }

    fun update(position: Int, model: RecommendMicInfoModel?, refreshType: Int) {
        if (mDataList != null || mDataList.size > 0) {
            if (position >= 0 && position < mDataList.size && mDataList[position - 1] === model) {
                // 位置是对的
                notifyItemChanged(position, refreshType)
                return
            } else {
                // 位置是错的
                for (i in mDataList.indices) {
                    if (mDataList[i] === model) {
                        notifyItemChanged(i + 1, refreshType)
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

interface RecommendMicListener {
    fun onClickEnterRoom(model: RecommendMicInfoModel?, position: Int)

    fun onClickUserVoice(model: RecommendMicInfoModel?, position: Int, userInfoModel: RecommendUserInfo?, childPos: Int)

    fun onClickQuickEnterRoom()
}
