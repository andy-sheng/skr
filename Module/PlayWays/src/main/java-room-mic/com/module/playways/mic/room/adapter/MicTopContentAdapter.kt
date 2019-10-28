package com.module.playways.mic.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.common.core.account.UserAccountManager
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.VoiceChartView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.mic.room.model.MicPlayerInfoModel
import com.zq.live.proto.MicRoom.EMUserRole
import org.greenrobot.eventbus.EventBus

class MicTopContentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mDataList = ArrayList<MicPlayerInfoModel>()
    val SEAT_TYPE = 0
    val INVITE_TYPE = 1

    var maxUserCount = 1
        set(value) {
            if (value > 0) {
                field = value
            }
        }

    var inviteCall: (() -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MicAvatarTopViewHolder) {
            holder.bindData(position, if (position < mDataList.size) mDataList[position] else null)
        } else if (holder is MicInviteViewHolder) {
            holder.bindData(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == SEAT_TYPE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_top_avatar_item_layout, parent, false)
            return MicAvatarTopViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.mic_top_invite_item_layout, parent, false)
            return MicInviteViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (mDataList.size >= maxUserCount) {
            return SEAT_TYPE
        } else if (position < maxUserCount) {
            return SEAT_TYPE
        } else {
            return INVITE_TYPE
        }
    }

    override fun getItemCount(): Int {
        if (mDataList.size >= maxUserCount) {
            return mDataList.size
        } else {
            return maxUserCount + 1
        }
    }

    inner class MicAvatarTopViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        var mPostion = 0
        var mModel: MicPlayerInfoModel? = null

        var circleBgIv: ExImageView
        var avatarIv: BaseImageView
        var waitingTv: ExTextView
        var voiceChartView: VoiceChartView
        var homeownerIv: ImageView
        var emptyIv: ImageView

        init {
            item.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    mModel?.let {
                        if (it.userID != UserAccountManager.SYSTEM_ID && it.userID != UserAccountManager.SYSTEM_GRAB_ID && it.userID != UserAccountManager.SYSTEM_RANK_AI) {
                            EventBus.getDefault().post(ShowPersonCardEvent(it.userInfo.userId))
                        }
                    }
                }
            })

            circleBgIv = itemView.findViewById(R.id.circle_bg_iv)
            avatarIv = itemView.findViewById(R.id.avatar_iv)
            waitingTv = itemView.findViewById(R.id.waiting_tv)
            voiceChartView = itemView.findViewById(R.id.voice_chart_view)
            homeownerIv = itemView.findViewById(R.id.homeowner_iv)
            emptyIv = item.findViewById(R.id.empty_iv)
        }

        fun bindData(position: Int, model: MicPlayerInfoModel?) {
            this.mPostion = position
            this.mModel = model

            if (mModel == null) {
                circleBgIv.visibility = View.GONE
                avatarIv.visibility = View.GONE
                waitingTv.visibility = View.GONE
                voiceChartView.visibility = View.GONE
                homeownerIv.visibility = View.GONE
                emptyIv.visibility = View.VISIBLE
                return
            } else {
                avatarIv.visibility = View.VISIBLE
                emptyIv.visibility = View.GONE
            }

            if (model!!.isCurSing) {
                circleBgIv.visibility = View.VISIBLE
                voiceChartView.visibility = View.VISIBLE
                voiceChartView.start()
            } else {
                circleBgIv.visibility = View.GONE
                voiceChartView.visibility = View.GONE
                voiceChartView.stop()
            }

            if (model!!.isNextSing) {
                waitingTv.visibility = View.VISIBLE
            } else {
                waitingTv.visibility = View.GONE
            }

            if (model?.role == EMUserRole.MQUR_ROOM_OWNER.value) {
                homeownerIv.visibility = View.VISIBLE
            } else {
                homeownerIv.visibility = View.GONE
            }

            AvatarUtils.loadAvatarByUrl(avatarIv,
                    AvatarUtils.newParamsBuilder(mModel?.userInfo?.avatar)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())
        }
    }

    inner class MicInviteViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var mPostion = 0

        init {
            item.setOnClickListener(object : DebounceViewClickListener() {
                override fun clickValid(v: View?) {
                    inviteCall?.invoke()
                }
            })
        }

        fun bindData(position: Int) {
            this.mPostion = position
        }
    }
}