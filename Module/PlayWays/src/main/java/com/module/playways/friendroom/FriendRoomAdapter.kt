package com.module.playways.friendroom

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.module.playways.R
import com.module.playways.mic.home.FriendInviteViewHolder
import com.module.playways.mic.home.RecommendMicViewHolder
import com.module.playways.mic.home.RecommendUserInfo

import java.util.ArrayList

class FriendRoomAdapter(var mOnItemClickListener: FriendRoomClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<RecommendRoomModel>()

    private val INVITE_FRIEND_TYPE = 0
    private val ROOM_GRAB_TYPE = 1
    private val ROOM_MIC_TYPE = 2
    private val ROOM_PARTY_TYPE = 3

    private val REFRESH_PLAY = 1
    private val REFRESH_STOP = 2

    var mCurrPlayModel: RecommendRoomModel? = null  //记录当前播放的
    var mCurrPlayPosition = -1
    var mCurrChildPosition = -1  //标记播放的是holder里面某个位置，抢唱房为-1
    var isPlay = false  // 标记播放

    private val uiHanlder = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            INVITE_FRIEND_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_room_invite_item_layout, parent, false)
                FriendInviteViewHolder(view, mOnItemClickListener)
            }
            ROOM_MIC_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_recommend_item_layout, parent, false)
                RecommendMicViewHolder(view, mOnItemClickListener)
            }
            ROOM_PARTY_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.party_recommend_item_layout, parent, false)
                RecommendPartyViewHolder(view, mOnItemClickListener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_room_grab_item_layout, parent, false)
                FriendRoomGrabViewHolder(view, mOnItemClickListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position == 0 -> INVITE_FRIEND_TYPE
            getDataByPosition(position).gameSceneType == RecommendRoomModel.EGST_MIC -> ROOM_MIC_TYPE
            getDataByPosition(position).gameSceneType == RecommendRoomModel.EGST_PARTY -> ROOM_PARTY_TYPE
            else -> ROOM_GRAB_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<*>) {
        if (payloads.isEmpty()) {
            if (position > 0) {
                val friendRoomModel = getDataByPosition(position)
                if (holder is FriendRoomGrabViewHolder) {
                    holder.bindData(friendRoomModel, position)
                    if (isPlay && mCurrPlayModel == getDataByPosition(position)) {
                        holder.startPlay()
                    } else {
                        holder.stopPlay()
                    }
                }
                if (holder is RecommendMicViewHolder) {
                    holder.bindRoomData(friendRoomModel, position)
                    if (isPlay && mCurrPlayModel == getDataByPosition(position)) {
                        holder.startPlay(mCurrChildPosition)
                    } else {
                        holder.stopPlay()
                    }
                }
                if (holder is RecommendPartyViewHolder) {
                    holder.bindData(friendRoomModel, position)
                }
            }
        } else {
            // 局部更新
            payloads.forEach { refreshType ->
                if (refreshType is Int) {
                    when (refreshType) {
                        REFRESH_PLAY -> {
                            if (isPlay && mCurrPlayModel === getDataByPosition(position)) {
                                if (holder is FriendRoomGrabViewHolder) {
                                    holder.startPlay()
                                } else if (holder is RecommendMicViewHolder) {
                                    holder.startPlay(mCurrChildPosition)
                                }
                            } else {
                                if (holder is FriendRoomGrabViewHolder) {
                                    holder.stopPlay()
                                } else if (holder is RecommendMicViewHolder) {
                                    holder.stopPlay()
                                }
                            }
                        }
                        REFRESH_STOP -> {
                            if (holder is FriendRoomGrabViewHolder) {
                                holder.stopPlay()
                            } else if (holder is RecommendMicViewHolder) {
                                holder.stopPlay()
                            }
                        }
                    }
                }
            }
        }
    }

    fun update(model: RecommendRoomModel, position: Int) {
        if (mDataList.size > position && position >= 0) {
            setDataByPosition(position, model)
            notifyItemChanged(position)
        }
    }

    fun remove(position: Int) {
        if (mDataList.size > position && position >= 0) {
            mDataList.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun startPlay(position: Int, model: RecommendRoomModel?, childPos: Int) {
        if (model?.gameSceneType == RecommendRoomModel.EGST_MIC) {
            // 排麦房的播放
            isPlay = true
            when {
                mCurrPlayModel != model -> {
                    // 需要更新 2个holder
                    val lastPos = mCurrPlayPosition
                    mCurrPlayModel = model
                    mCurrPlayPosition = position
                    mCurrChildPosition = childPos
                    notifyItemChanged(position, REFRESH_PLAY)
                    if (lastPos >= 0) {
                        // 停掉之前的
                        uiHanlder.post {
                            notifyItemChanged(lastPos, REFRESH_STOP)
                        }
                    }

                }
                childPos != mCurrChildPosition -> {
                    // 需要更新 1个holder
                    mCurrPlayModel = model
                    mCurrChildPosition = childPos
                    notifyItemChanged(position, REFRESH_PLAY)
                }
                else -> {
                    // todo donothing 算错误
                }
            }
        } else {
            // 抢唱房的播放
            isPlay = true
            mCurrChildPosition = childPos
            var lastPos = -1
            if (mCurrPlayModel != model) {
                mCurrPlayModel = model
                lastPos = mCurrPlayPosition
                mCurrPlayPosition = position
            }
            notifyItemChanged(position, REFRESH_PLAY)
            if (lastPos != -1) {
                val finalLastPos = lastPos
                uiHanlder.post { notifyItemChanged(finalLastPos, REFRESH_STOP) }
            }
        }
    }

    private fun getDataByPosition(position: Int): RecommendRoomModel {
        return mDataList[position - 1]
    }

    private fun setDataByPosition(position: Int, model: RecommendRoomModel) {
        if (position > 0) {
            mDataList[position - 1] = model
        }
    }

    fun stopPlay() {
        isPlay = false
        update(mCurrPlayPosition, mCurrPlayModel, REFRESH_STOP)
        // 重置数据
        mCurrPlayPosition = -1
        mCurrPlayModel = null
        mCurrChildPosition = -1
    }

    private fun update(position: Int, model: RecommendRoomModel?, refreshType: Int) {
        if (position >= 0 && position < mDataList.size && getDataByPosition(position) == model) {
            // 位置是对的
            notifyItemChanged(position, refreshType)
            return
        } else {
            // 位置是错的
            for (i in mDataList.indices) {
                if (mDataList[i] == model) {
                    notifyItemChanged(i, refreshType)
                    return
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size + 1
    }

    interface FriendRoomClickListener {
        fun onClickGrabRoom(position: Int, model: RecommendRoomModel?)

        fun onClickGrabVoice(position: Int, model: RecommendRoomModel?)

        fun onClickMicRoom(model: RecommendRoomModel?, position: Int)

        fun onClickMicVoice(model: RecommendRoomModel?, position: Int, userInfoModel: RecommendUserInfo?, childPos: Int)

        fun onClickInvite()

        fun onClickPartyRoom(position: Int, model: RecommendRoomModel?)
    }
}
