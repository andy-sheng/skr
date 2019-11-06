package com.module.playways.friendroom


import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.busilib.view.VoiceChartView
import com.component.level.utils.LevelConfigUtils
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class FriendRoomGrabViewHolder(itemView: View, var mOnItemClickListener: FriendRoomAdapter.FriendRoomClickListener) : RecyclerView.ViewHolder(itemView) {

    val TAG = "FriendRoomVerticalViewHolder"

    private var mFriendRoomModel: GrabRecommendModel? = null
    private var mPos: Int = 0

    private val mRecommendTagSdv: SimpleDraweeView = itemView.findViewById(R.id.recommend_tag_sdv)
    private val mMediaTagSdv: SimpleDraweeView = itemView.findViewById(R.id.media_tag_sdv)
    private val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    private val mLevelBg: ImageView = itemView.findViewById(R.id.level_bg)
    private val mLevelDesc: TextView = itemView.findViewById(R.id.level_desc)
    private val mNameTv: ExTextView = itemView.findViewById(R.id.name_tv)
    private val mRoomPlayerNumTv: ExTextView = itemView.findViewById(R.id.room_player_num_tv)
    private val mRoomInfoTv: ExTextView = itemView.findViewById(R.id.room_info_tv)

    private val mVoiceArea: ExConstraintLayout = itemView.findViewById(R.id.voice_area)
    private val mPlayIv: ImageView = itemView.findViewById(R.id.play_iv)
    private val mVoiceName: TextView = itemView.findViewById(R.id.voice_name)
    private val mVoiceChartView: VoiceChartView = itemView.findViewById(R.id.voice_chart_view)

    init {
        itemView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                mOnItemClickListener?.onClickGrabRoom(mPos, mFriendRoomModel)
            }
        })

        mVoiceArea.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                mOnItemClickListener?.onClickGrabVoice(mPos, mFriendRoomModel)
            }
        })
    }

    fun bindData(friendRoomModel: GrabRecommendModel, position: Int) {
        this.mFriendRoomModel = friendRoomModel
        this.mPos = position

        if (friendRoomModel.userInfo != null) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(friendRoomModel.userInfo!!.avatar)
                    .setCircle(true)
                    .build())
            if (friendRoomModel.userInfo!!.ranking != null && LevelConfigUtils.getAvatarLevelBg(friendRoomModel.userInfo!!.ranking.mainRanking) != 0) {
                mLevelBg.visibility = View.VISIBLE
                mLevelDesc.visibility = View.VISIBLE
                mLevelBg.background = U.getDrawable(LevelConfigUtils.getAvatarLevelBg(friendRoomModel.userInfo!!.ranking.mainRanking))
                mLevelDesc.text = friendRoomModel.userInfo!!.ranking.rankingDesc
            } else {
                mLevelBg.visibility = View.GONE
                mLevelDesc.visibility = View.GONE
            }
        }

        mVoiceChartView.stop()
        if (friendRoomModel.voiceInfo != null) {
            mVoiceArea.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(friendRoomModel.voiceInfo!!.songName)) {
                mVoiceName.text = friendRoomModel.voiceInfo!!.songName
            } else {
                if (friendRoomModel.voiceInfo!!.duration > friendRoomModel.voiceInfo!!.duration / 1000 * 1000) {
                    mVoiceName.text = (friendRoomModel.voiceInfo!!.duration / 1000 + 1).toString() + "s"
                } else {
                    mVoiceName.text = (friendRoomModel.voiceInfo!!.duration / 1000).toString() + "s"
                }
            }
        } else {
            mVoiceArea.visibility = View.GONE
        }

        if (mFriendRoomModel != null && mFriendRoomModel!!.userInfo != null && mFriendRoomModel!!.roomInfo != null) {
            if (!TextUtils.isEmpty(mFriendRoomModel!!.roomInfo!!.roomTagURL)) {
                mRecommendTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(mRecommendTagSdv, ImageFactory.newPathImage(mFriendRoomModel!!.roomInfo!!.roomTagURL)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                mRecommendTagSdv.visibility = View.GONE
            }

            val nickName = UserInfoManager.getInstance().getRemarkName(mFriendRoomModel!!.userInfo!!.userId, mFriendRoomModel!!.userInfo!!.nickname)
            if (!TextUtils.isEmpty(nickName)) {
                mNameTv.visibility = View.VISIBLE
                mNameTv.text = nickName
            } else {
                mNameTv.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(mFriendRoomModel!!.roomInfo!!.mediaTagURL)) {
                mMediaTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(mMediaTagSdv, ImageFactory.newPathImage(mFriendRoomModel!!.roomInfo!!.mediaTagURL)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                mMediaTagSdv.visibility = View.GONE
            }

            mRoomPlayerNumTv.text = mFriendRoomModel!!.roomInfo!!.inPlayersNum.toString() + "/" + mFriendRoomModel!!.roomInfo!!.totalPlayersNum

            if (!TextUtils.isEmpty(mFriendRoomModel!!.roomInfo!!.roomName)) {
                mRoomInfoTv.visibility = View.VISIBLE
                mRoomInfoTv.text = mFriendRoomModel!!.roomInfo!!.roomName
            } else {
                if (mFriendRoomModel!!.tagInfo != null) {
                    // 只显示专场名称
                    val stringBuilder = SpanUtils()
                            .append(mFriendRoomModel!!.tagInfo!!.tagName)
                            .create()
                    mRoomInfoTv.visibility = View.VISIBLE
                    mRoomInfoTv.text = stringBuilder
                } else {
                    mRoomInfoTv.visibility = View.GONE
                    MyLog.w(TAG, "服务器数据有问题 friendRoomModel=$friendRoomModel position=$position")
                }
            }
        } else {
            MyLog.w(TAG, "bindData friendRoomModel=$friendRoomModel position=$position")
        }
    }

    fun startPlay() {
        mPlayIv.isSelected = true
        mVoiceName.visibility = View.INVISIBLE
        mVoiceChartView.visibility = View.VISIBLE
        mVoiceChartView.start()
    }

    fun stopPlay() {
        mPlayIv.isSelected = false
        mVoiceName.visibility = View.VISIBLE
        mVoiceChartView.visibility = View.GONE
        mVoiceChartView.stop()
    }
}
