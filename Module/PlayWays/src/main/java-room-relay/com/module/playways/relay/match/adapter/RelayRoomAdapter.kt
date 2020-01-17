package com.module.playways.relay.match.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.player.SinglePlayer
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import com.component.busilib.view.SpeakingTipsAnimationView
import com.component.busilib.view.recyclercardview.CardAdapterHelper
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.relay.match.model.RelayMatchItemInfo
import com.module.playways.relay.match.model.RelaySelectItemInfo
import com.zq.live.proto.Common.ESex
import kotlin.contracts.contract

class RelayRoomAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var mDataList = ArrayList<RelaySelectItemInfo>()
    var listener: RelayRoomListener? = null
    private val cardAdapterHelper = CardAdapterHelper(8, 12)

    var inviteModel: RelaySelectItemInfo? = null

    companion object {
        const val ITEM_TYPE_ROOM = 1
        const val ITEM_TYPE_RED_PACKET = 2

        const val REFRESH_TYPE_HAS_INVITE = 1
        const val REFRESH_TYPE_NO_INVITE = 2
        const val REFRESH_TYPE_RESET_VOICE_ANIMATION = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_ROOM) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.relay_room_card_item_layout, parent, false)
            cardAdapterHelper.onCreateViewHolder(parent, view)
            RelayRoomViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.relay_red_packet_card_item_layout, parent, false)
            cardAdapterHelper.onCreateViewHolder(parent, view)
            RelayRedPacketViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            cardAdapterHelper.onBindViewHolder(holder.itemView, position, itemCount, listener?.getRecyclerViewPosition() == position || mDataList.size == 1)
            if (holder is RelayRoomViewHolder) {
                holder.bindData(position, mDataList[position])
            } else if (holder is RelayRedPacketViewHolder) {
                if (inviteModel?.redpacketItem?.user?.userId == mDataList[position].redpacketItem?.user?.userId) {
                    holder.bindData(position, mDataList[position])
                    holder.setInvited(true)
                } else {
                    holder.bindData(position, mDataList[position])
                    holder.setInvited(false)
                }
            }
        } else {
            payloads.forEach { refreshType ->
                if (refreshType is Int) {
                    when (refreshType) {
                        REFRESH_TYPE_HAS_INVITE -> {
                            if (holder is RelayRedPacketViewHolder) {
                                holder.setInvited(true)
                            }
                        }
                        REFRESH_TYPE_NO_INVITE -> {
                            if (holder is RelayRedPacketViewHolder) {
                                holder.setInvited(false)
                            }
                        }
                        REFRESH_TYPE_RESET_VOICE_ANIMATION -> {
                            if (holder is RelayRedPacketViewHolder) {
                                holder.resetVoiceAnimation()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (mDataList[position].type == RelaySelectItemInfo.ST_MATCH_ITEM) {
            ITEM_TYPE_ROOM
        } else {
            ITEM_TYPE_RED_PACKET
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    fun updateInviteStatus(model: RelaySelectItemInfo, hasInvited: Boolean) {
        // 可能重新刷的数据里面有
        inviteModel = if (hasInvited) {
            model
        } else {
            null
        }
        mDataList.forEachIndexed { index, relaySelectItemInfo ->
            if (relaySelectItemInfo.type == RelaySelectItemInfo.ST_REDPACKET_ITEM && relaySelectItemInfo.redpacketItem?.user?.userId == model.redpacketItem?.user?.userId) {
                if (hasInvited) {
                    notifyItemChanged(index, REFRESH_TYPE_HAS_INVITE)
                } else {
                    notifyItemChanged(index, REFRESH_TYPE_NO_INVITE)
                }
                return@forEachIndexed
            }
        }
    }

    fun addData(list: List<RelaySelectItemInfo>) {
        if (list.isNotEmpty()) {
            val startNotifyIndex = if (mDataList.size > 0) mDataList.size - 1 else 0
            mDataList.addAll(list)
            notifyItemRangeChanged(startNotifyIndex, mDataList.size - startNotifyIndex)
        }
    }

    inner class RelayRedPacketViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        val TAG = "RelayRedPacketViewHolder"
        private val audioBg: View = item.findViewById(R.id.audio_bg)
        private val speakerAnimationIv: SpeakingTipsAnimationView = item.findViewById(R.id.speaker_animation_iv)
        private val imageBg: ImageView = item.findViewById(R.id.image_bg)
        private val avatarLevel: AvatarView = item.findViewById(R.id.avatar_level)
        private val nicknameView: NickNameView = item.findViewById(R.id.nickname_view)
        private val levelTv: ExTextView = item.findViewById(R.id.level_tv)
        private val ageTv: ExTextView = item.findViewById(R.id.age_tv)
        private val sexTv: ExTextView = item.findViewById(R.id.sex_tv)
        private val bottomArea: ImageView = item.findViewById(R.id.bottom_area)

        private val inviteTv: TextView = item.findViewById(R.id.invite_tv)
        private val costTv: ExTextView = item.findViewById(R.id.cost_tv)

        var mPos = -1
        var mModel: RelaySelectItemInfo? = null

        init {
            inviteTv.setDebounceViewClickListener {
                listener?.selectRedPacket(mPos, mModel)
            }
            audioBg.setDebounceViewClickListener {
                if (listener?.clickVoiceInfo(mPos, mModel) == true) {
                    speakerAnimationIv.show(mModel?.redpacketItem?.voiceInfo?.duration?.toInt()
                            ?: 3000, true)
                } else {
                    speakerAnimationIv.reset()
                }
            }

        }

        fun bindData(position: Int, model: RelaySelectItemInfo) {
            this.mPos = position
            this.mModel = model
            initBackground(model.redpacketItem?.user?.sex, imageBg, bottomArea, sexTv)
            costTv.text = "${model.redpacketItem?.costZS.toString()}/次"
            val maleDrawable = DrawableCreator.Builder()
                    .setCornersRadius(16.dp().toFloat())
                    .setSolidColor(Color.parseColor("#C1D6F7"))
                    .build()
            val femaleDrawable = DrawableCreator.Builder()
                    .setCornersRadius(16.dp().toFloat())
                    .setSolidColor(Color.parseColor("#FFD0DC"))
                    .build()
            when (model.redpacketItem?.user?.sex) {
                ESex.SX_MALE.value -> {
                    costTv.background = maleDrawable
                    costTv.setTextColor(Color.parseColor("#4F7CBD"))
                }
                else -> {
                    costTv.background = femaleDrawable
                    costTv.setTextColor(Color.parseColor("#B65D77"))
                }
            }
            levelTv.text = model.redpacketItem?.user?.ranking?.rankingDesc
            avatarLevel.bindData(model.redpacketItem?.user)
            nicknameView.setHonorText(model.redpacketItem?.user?.nicknameRemark, model.redpacketItem?.user?.honorInfo)
            if (!TextUtils.isEmpty(model.redpacketItem?.user?.ageStageString)) {
                ageTv.visibility = View.VISIBLE
                ageTv.text = model.redpacketItem?.user?.ageStageString
            } else {
                ageTv.visibility = View.GONE
            }

            speakerAnimationIv.reset()
            if (model?.redpacketItem?.voiceInfo?.voiceURL?.isNotEmpty() == true) {
                speakerAnimationIv.visibility = View.VISIBLE
                audioBg.visibility = View.VISIBLE
            } else {
                speakerAnimationIv.visibility = View.GONE
                audioBg.visibility = View.GONE
            }
        }

        fun setInvited(hasInvited: Boolean) {
            if (hasInvited) {
                inviteTv.text = "已邀请"
                inviteTv.isClickable = false
            } else {
                inviteTv.text = "邀请合唱"
                inviteTv.isClickable = true
            }
        }

        fun resetVoiceAnimation() {
            if (speakerAnimationIv.visibility == View.VISIBLE) {
                speakerAnimationIv.reset()
            }
        }

    }

    inner class RelayRoomViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val imageBg: ImageView = item.findViewById(R.id.image_bg)
        private val avatarLevel: AvatarView = item.findViewById(R.id.avatar_level)
        private val nicknameView: NickNameView = item.findViewById(R.id.nickname_view)
        private val levelTv: ExTextView = item.findViewById(R.id.level_tv)
        private val ageTv: ExTextView = item.findViewById(R.id.age_tv)
        private val sexTv: ExTextView = item.findViewById(R.id.sex_tv)
        private val bottomArea: ImageView = item.findViewById(R.id.bottom_area)

        private val songNameTv: TextView = item.findViewById(R.id.song_name_tv)
        private val recommendTagSdv: SimpleDraweeView = item.findViewById(R.id.recommend_tag_sdv)
        private val joinTv: TextView = item.findViewById(R.id.join_tv)

        var mPos = -1
        var mModel: RelaySelectItemInfo? = null

        init {
            joinTv.setDebounceViewClickListener {
                listener?.selectRoom(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: RelaySelectItemInfo) {
            this.mPos = position
            this.mModel = model

            initBackground(model.matchItem?.user?.sex, imageBg, bottomArea, sexTv)

            levelTv.text = model.matchItem?.user?.ranking?.rankingDesc
            avatarLevel.bindData(model.matchItem?.user)
            nicknameView.setHonorText(model.matchItem?.user?.nicknameRemark, model.matchItem?.user?.honorInfo)
            songNameTv.text = "《${model.matchItem?.item?.itemName}》"
            if (!TextUtils.isEmpty(model.matchItem?.user?.ageStageString)) {
                ageTv.visibility = View.VISIBLE
                ageTv.text = model.matchItem?.user?.ageStageString
            } else {
                ageTv.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(model.matchItem?.recommendTag?.url)) {
                recommendTagSdv.visibility = View.VISIBLE
                FrescoWorker.loadImage(recommendTagSdv, ImageFactory.newPathImage(model.matchItem?.recommendTag?.url)
                        .setScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .build())
            } else {
                recommendTagSdv.visibility = View.GONE
            }

        }
    }

    fun initBackground(sex: Int?, imageBg: ImageView, bottomArea: ImageView, sexTv: ExTextView) {
        val blueDrawable = DrawableCreator.Builder()
                .setCornersRadius(16.dp().toFloat())
                .setGradientColor(Color.parseColor("#DEF1FF"), Color.parseColor("#C4D7FF"))
                .build()

        val blueBottomDrawable = DrawableCreator.Builder()
                .setCornersRadius(16.dp().toFloat(), 16.dp().toFloat(), 0f, 0f)
                .setGradientColor(Color.parseColor("#DEF1FF"), Color.parseColor("#D0E3FF"))
                .build()

        val boyDrawable = DrawableCreator.Builder()
                .setCornersRadius(4.dp().toFloat())
                .setSolidColor(Color.parseColor("#6AB1DC"))
                .build()

        val redDrawable = DrawableCreator.Builder()
                .setCornersRadius(16.dp().toFloat())
                .setGradientColor(Color.parseColor("#FFDEDE"), Color.parseColor("#FFC4DB"))
                .build()

        val redBottomDrawable = DrawableCreator.Builder()
                .setCornersRadius(16.dp().toFloat(), 16.dp().toFloat(), 0f, 0f)
                .setGradientColor(Color.parseColor("#FFE4E4"), Color.parseColor("#FFE4EE"))
                .build()

        val girlDrawable = DrawableCreator.Builder()
                .setCornersRadius(4.dp().toFloat())
                .setSolidColor(Color.parseColor("#FFA2D5"))
                .build()

        when (sex) {
            ESex.SX_MALE.value -> {
                imageBg.background = blueDrawable
                bottomArea.background = blueBottomDrawable
                sexTv.visibility = View.VISIBLE
                sexTv.text = "男生"
                sexTv.background = boyDrawable
            }
            ESex.SX_FEMALE.value -> {
                imageBg.background = redDrawable
                bottomArea.background = redBottomDrawable
                sexTv.visibility = View.VISIBLE
                sexTv.text = "女生"
                sexTv.background = girlDrawable
            }
            else -> {
                // 没有性别，默认给个女生
                imageBg.background = redDrawable
                bottomArea.background = redBottomDrawable
                sexTv.visibility = View.GONE
            }
        }
    }

    interface RelayRoomListener {
        fun getRecyclerViewPosition(): Int
        fun selectRoom(position: Int, model: RelaySelectItemInfo?)
        fun selectRedPacket(position: Int, model: RelaySelectItemInfo?)
        fun clickVoiceInfo(position: Int, model: RelaySelectItemInfo?): Boolean
    }
}