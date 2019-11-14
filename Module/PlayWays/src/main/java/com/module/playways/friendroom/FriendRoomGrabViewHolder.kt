package com.module.playways.friendroom


import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setAnimateDebounceViewClickListener
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
import com.component.person.view.CommonAudioView
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R

class FriendRoomGrabViewHolder(itemView: View, var mOnItemClickListener: FriendRoomAdapter.FriendRoomClickListener) : RecyclerView.ViewHolder(itemView) {

    val mTag = "FriendRoomVerticalViewHolder"

    private var mFriendRoomModel: RecommendRoomModel? = null
    private var mPos: Int = 0

    private val mRecommendTagSdv: SimpleDraweeView = itemView.findViewById(R.id.recommend_tag_sdv)
    private val mMediaTagSdv: SimpleDraweeView = itemView.findViewById(R.id.media_tag_sdv)
    private val mAvatarIv: SimpleDraweeView = itemView.findViewById(R.id.avatar_iv)
    private val mLevelBg: ImageView = itemView.findViewById(R.id.level_bg)
    private val mNameTv: ExTextView = itemView.findViewById(R.id.name_tv)
    private val mRoomPlayerNumTv: ExTextView = itemView.findViewById(R.id.room_player_num_tv)
    private val mRoomInfoTv: ExTextView = itemView.findViewById(R.id.room_info_tv)
    private val enterRoomTv: ExTextView = itemView.findViewById(R.id.enter_room_tv)
    private val audioView: CommonAudioView = itemView.findViewById(R.id.audio_view)

    init {
        enterRoomTv.setAnimateDebounceViewClickListener {
            mOnItemClickListener.onClickGrabRoom(mPos, mFriendRoomModel)
        }

        audioView.setOnClickListener(object : AnimateClickListener() {
            override fun click(view: View) {
                mOnItemClickListener.onClickGrabVoice(mPos, mFriendRoomModel)
            }
        })
    }

    fun bindData(model: RecommendRoomModel, position: Int) {
        this.mFriendRoomModel = model
        this.mPos = position

        model.grabRoom?.let {
            bindData(it, position)
        }
    }

    private fun bindData(friendRoomModel: RecommendGrabRoomModel, position: Int) {
        if (friendRoomModel.userInfo != null) {
            AvatarUtils.loadAvatarByUrl(mAvatarIv, AvatarUtils.newParamsBuilder(friendRoomModel.userInfo?.avatar)
                    .setCircle(true)
                    .build())
            if (friendRoomModel.userInfo?.ranking != null && LevelConfigUtils.getRaceCenterAvatarBg(friendRoomModel.userInfo?.ranking?.mainRanking
                            ?: 0) != 0) {
                mLevelBg.visibility = View.VISIBLE
                mLevelBg.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(friendRoomModel.userInfo?.ranking?.mainRanking
                        ?: 0))
            } else {
                mLevelBg.visibility = View.GONE
            }
        }

        if (friendRoomModel.voiceInfo != null) {
            audioView.visibility = View.VISIBLE
            audioView.bindData(friendRoomModel.voiceInfo?.duration ?: 0)
        } else {
            audioView.visibility = View.GONE
        }

        if (friendRoomModel.userInfo != null && friendRoomModel.roomInfo != null) {
            if (!TextUtils.isEmpty(friendRoomModel.roomInfo!!.roomTagURL)) {
                mRecommendTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(mRecommendTagSdv, ImageFactory.newPathImage(friendRoomModel.roomInfo?.roomTagURL)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                mRecommendTagSdv.visibility = View.GONE
            }

            val nickName = UserInfoManager.getInstance().getRemarkName(friendRoomModel.userInfo?.userId
                    ?: 0, friendRoomModel.userInfo?.nickname)
            if (!TextUtils.isEmpty(nickName)) {
                mNameTv.visibility = View.VISIBLE
                mNameTv.text = nickName
            } else {
                mNameTv.visibility = View.GONE
            }

            if (!TextUtils.isEmpty(friendRoomModel.roomInfo?.mediaTagURL)) {
                mMediaTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(mMediaTagSdv, ImageFactory.newPathImage(friendRoomModel.roomInfo?.mediaTagURL)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                mMediaTagSdv.visibility = View.GONE
            }

            mRoomPlayerNumTv.text = friendRoomModel.roomInfo?.inPlayersNum.toString() + "/" + friendRoomModel.roomInfo?.totalPlayersNum

            if (!TextUtils.isEmpty(friendRoomModel.roomInfo?.roomName)) {
                mRoomInfoTv.visibility = View.VISIBLE
                mRoomInfoTv.text = friendRoomModel.roomInfo?.roomName
            } else {
                if (friendRoomModel.tagInfo != null) {
                    // 只显示专场名称
                    val stringBuilder = SpanUtils()
                            .append("${friendRoomModel.tagInfo?.tagName}")
                            .create()
                    mRoomInfoTv.visibility = View.VISIBLE
                    mRoomInfoTv.text = stringBuilder
                } else {
                    mRoomInfoTv.visibility = View.GONE
                    MyLog.w(mTag, "服务器数据有问题 friendRoomModel=$friendRoomModel position=$position")
                }
            }
        } else {
            MyLog.w(mTag, "bindData friendRoomModel=$friendRoomModel position=$position")
        }
    }

    fun startPlay() {
        audioView.setPlay(true)
    }

    fun stopPlay() {
        audioView.setPlay(false)
    }
}
