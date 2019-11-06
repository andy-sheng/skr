package com.module.playways.friendroom

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.module.playways.R
import com.module.playways.mic.home.RecommendMicInfoModel
import com.module.playways.mic.home.RecommendUserInfo

import java.util.ArrayList

class FriendRoomAdapter(var mOnItemClickListener: FriendRoomClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    internal var mDataList: MutableList<GrabRecommendModel>? = ArrayList()

    private val REFRESH_PLAY = 1

    var dataList = ArrayList<GrabRecommendModel>()

    var mCurrPlayModel: GrabRecommendModel? = null  //记录当前播放的
    var mCurrPlayPosition = -1
    var isPlay = false

    private val uiHanlder = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_room_verit_item_layout, parent, false)
        val itemHolder = FriendRoomGrabViewHolder(view, mOnItemClickListener)
        return itemHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<*>) {
        if (payloads.isEmpty()) {
            val friendRoomModel = mDataList!![position]
            (holder as FriendRoomGrabViewHolder).bindData(friendRoomModel, position)
            if (isPlay && mCurrPlayModel == mDataList!![position]) {
                holder.startPlay()
            } else {
                holder.stopPlay()
            }
        } else {
            // 只有播放
            if (isPlay && mCurrPlayModel == mDataList!![position]) {
                (holder as FriendRoomGrabViewHolder).startPlay()
            } else {
                (holder as FriendRoomGrabViewHolder).stopPlay()
            }
        }
    }

    fun update(model: GrabRecommendModel, position: Int) {
        if (mDataList != null && mDataList!!.size > position && position >= 0) {
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

    fun startOrPauseAudio(pos: Int, model: GrabRecommendModel?) {
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

    fun update(position: Int, model: GrabRecommendModel?, refreshType: Int) {
        if (mDataList != null || mDataList!!.size > 0) {
            if (position >= 0 && position < mDataList!!.size && mDataList!![position] == model) {
                // 位置是对的
                notifyItemChanged(position, refreshType)
                return
            } else {
                // 位置是错的
                for (i in mDataList!!.indices) {
                    if (mDataList!![i] == model) {
                        notifyItemChanged(i, refreshType)
                        return
                    }
                }
            }
        } else {
            mCurrPlayModel = null
            mCurrPlayPosition = -1
        }
    }

    override fun getItemCount(): Int {
        return mDataList!!.size
    }

    interface FriendRoomClickListener {
        fun onClickGrabRoom(position: Int, model: GrabRecommendModel?)

        fun onClickGrabVoice(position: Int, model: GrabRecommendModel?)

        fun onClickMicRoom(model: RecommendMicInfoModel?, position: Int)

        fun onClickMicVoice(model: RecommendMicInfoModel?, position: Int, userInfoModel: RecommendUserInfo?, childPos: Int)
    }
}
