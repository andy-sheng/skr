package com.module.playways.friendroom

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.module.playways.R
import com.module.playways.mic.home.RecommendMicViewHolder
import com.module.playways.mic.home.RecommendUserInfo

import java.util.ArrayList

class FriendRoomAdapter(var mOnItemClickListener: FriendRoomClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<RecommendRoomModel>()

    private val ROOM_GRAB_TYPE = 1
    private val ROOM_MIC_TYPE = 2

    private val REFRESH_PLAY = 1

    var mCurrPlayModel: RecommendRoomModel? = null  //记录当前播放的
    var mCurrPlayPosition = -1
    var isPlay = false

    private val uiHanlder = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ROOM_MIC_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recommend_item_layout, parent, false)
            RecommendMicViewHolder(view, mOnItemClickListener)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_room_grab_item_layout, parent, false)
            FriendRoomGrabViewHolder(view, mOnItemClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataList[position].gameSceneType == RecommendRoomModel.EGST_MIC) {
            ROOM_MIC_TYPE
        } else {
            ROOM_GRAB_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<*>) {
        if (payloads.isEmpty()) {
            val friendRoomModel = mDataList[position]
            if (holder is FriendRoomGrabViewHolder) {
                holder.bindData(friendRoomModel, position)
                if (isPlay && mCurrPlayModel == mDataList[position]) {
                    holder.startPlay()
                } else {
                    holder.stopPlay()
                }
            }
            if (holder is RecommendMicViewHolder) {
                holder.bindRoomData(friendRoomModel, position)
//                if (isPlay && mCurrPlayModel == mDataList[position - 1]) {
//                    holder.startPlay(playChildPosition)
//                } else {
//                    holder.stopPlay()
//                }
            }
        } else {
            // 只有播放
            if (holder is FriendRoomGrabViewHolder) {
                if (isPlay && mCurrPlayModel == mDataList!![position]) {
                    holder.startPlay()
                } else {
                    holder.stopPlay()
                }
            }
        }
    }

    fun update(model: RecommendRoomModel, position: Int) {
        if (mDataList.size > position && position >= 0) {
            mDataList!![position] = model
            notifyItemChanged(position)
        }
    }

    fun remove(position: Int) {
        if (mDataList != null && mDataList!!.size > position && position >= 0) {
            mDataList!!.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun startOrPauseAudio(pos: Int, model: RecommendRoomModel?) {
        if (mCurrPlayModel != null && mCurrPlayModel == model) {
            // 数据和播放类型一致
            stopPlay()
        } else {
            // 数据改变或者播放的类型不一致了
            isPlay = true
            var lastPos = -1
            if (mCurrPlayModel != model) {
                mCurrPlayModel = model
                lastPos = mCurrPlayPosition
                mCurrPlayPosition = pos
            }
            notifyItemChanged(pos, REFRESH_PLAY)
            if (lastPos != -1) {
                val finalLastPos = lastPos
                uiHanlder.post { notifyItemChanged(finalLastPos, REFRESH_PLAY) }
            }
        }
    }

    fun stopPlay() {
        isPlay = false
        update(mCurrPlayPosition, mCurrPlayModel, REFRESH_PLAY)
        // 重置数据
        mCurrPlayPosition = -1
        mCurrPlayModel = null
    }

    private fun update(position: Int, model: RecommendRoomModel?, refreshType: Int) {
        if (position >= 0 && position < mDataList.size && mDataList[position] == model) {
            // 位置是对的
            notifyItemChanged(position, refreshType)
            return
        } else {
            // 位置是错的
            for (i in mDataList!!.indices) {
                if (mDataList[i] == model) {
                    notifyItemChanged(i, refreshType)
                    return
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    interface FriendRoomClickListener {
        fun onClickGrabRoom(position: Int, model: RecommendRoomModel?)

        fun onClickGrabVoice(position: Int, model: RecommendRoomModel?)

        fun onClickMicRoom(model: RecommendRoomModel?, position: Int)

        fun onClickMicVoice(model: RecommendRoomModel?, position: Int, userInfoModel: RecommendUserInfo?, childPos: Int)
    }
}
