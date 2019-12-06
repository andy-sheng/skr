package com.module.playways.party.room.seat

import android.support.constraint.Group
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.person.event.ShowPersonCardEvent
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.party.room.model.PartyActorInfoModel
import org.greenrobot.eventbus.EventBus

// 正常位置
class SeatViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    private val avatarSdv: SimpleDraweeView = item.findViewById(R.id.avatar_sdv)
    private val hotTv: ExTextView = item.findViewById(R.id.hot_tv)
    private val nameTv: TextView = item.findViewById(R.id.name_tv)
    private val muteArea: Group = item.findViewById(R.id.mute_area)
    private val muteBg: ExImageView = item.findViewById(R.id.mute_bg)
    private val muteIv: ImageView = item.findViewById(R.id.mute_iv)
    private val emojiSdv: SimpleDraweeView = item.findViewById(R.id.emoji_sdv)

    var mModel: PartyActorInfoModel? = null
    var mPos: Int = -1

    init {
        avatarSdv.setDebounceViewClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(mModel?.player?.userID ?: 0))
        }
    }

    fun bindData(position: Int, model: PartyActorInfoModel?) {
        this.mModel = model
        this.mPos = position

    }

    fun refreshMute() {

    }
}


// 空座位
class EmptySeatViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    private val emptyBg: ExImageView = item.findViewById(R.id.empty_bg)
    private val emptyTv: TextView = item.findViewById(R.id.empty_tv)

    var mPos: Int = -1

    fun bindData(position: Int, model: PartyActorInfoModel?) {
        this.mPos = position

        if (model?.seat?.seatStatus == 2) {
            // 关闭席位的UI
        } else {
            emptyTv.text = "${position + 1}"
        }
    }
}