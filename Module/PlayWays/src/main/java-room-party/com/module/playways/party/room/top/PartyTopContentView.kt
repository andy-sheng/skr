package com.module.playways.party.room.top

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.component.busilib.view.AvatarView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.event.PartyOnlineUserCntChangeEvent
import com.module.playways.relay.room.event.RelayLockChangeEvent
import com.module.playways.room.data.H
import com.zq.live.proto.RelayRoom.RMuteMsg
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// 顶部头像栏
class PartyTopContentView : ExConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val arrowIv: ImageView
    val avatarIv: BaseImageView
    val nameTv: TextView
    val compereTv: TextView
    val moreArrow: ExImageView
    val onlineNum: TextView
    val audienceIv: ImageView

    var listener: Listener? = null
    var mIsOpen = true

    var roomData: PartyRoomData? = null

    init {
        View.inflate(context, R.layout.party_top_content_view_layout, this)

        arrowIv = this.findViewById(R.id.arrow_iv)
        avatarIv = this.findViewById(R.id.avatar_iv)
        nameTv = this.findViewById(R.id.name_tv)
        compereTv = this.findViewById(R.id.compere_tv)
        moreArrow = this.findViewById(R.id.more_arrow)
        onlineNum = this.findViewById(R.id.online_num)
        audienceIv = this.findViewById(R.id.audience_iv)

        avatarIv.setDebounceViewClickListener {
            H.partyRoomData?.hostId?.let {
                EventBus.getDefault().post(ShowPersonCardEvent(it))
            }
        }
        arrowIv.setDebounceViewClickListener { listener?.clickArrow(!mIsOpen) }
        moreArrow.setDebounceViewClickListener { listener?.showRoomMember() }
        onlineNum.setDebounceViewClickListener { listener?.showRoomMember() }
        audienceIv.setDebounceViewClickListener { listener?.showRoomMember() }
    }

    fun setArrowIcon(open: Boolean) {
        if (open) {
            // 展开状态
            mIsOpen = true
            arrowIv.setImageResource(R.drawable.race_expand_icon)
        } else {
            // 折叠状态
            mIsOpen = false
            arrowIv.setImageResource(R.drawable.race_shrink_icon)
        }
    }

    fun bindData() {
        val hostUser = H.partyRoomData?.getPlayerInfoById(H.partyRoomData?.hostId ?: 0)
        AvatarUtils.loadAvatarByUrl(avatarIv,
                AvatarUtils.newParamsBuilder(hostUser?.userInfo?.avatar)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(1.dp().toFloat())
                        .setCircle(true)
                        .build())
        nameTv.text = hostUser?.userInfo?.nicknameRemark
        compereTv.text = "房间号:${H.partyRoomData?.gameId}"
        onlineNum.text = "在线${H.partyRoomData?.applyUserCnt}人"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyOnlineUserCntChangeEvent) {
        onlineNum.text = "在线${H.partyRoomData?.applyUserCnt}人"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyHostChangeEvent) {
        bindData()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    interface Listener {
        fun clickArrow(open: Boolean)
        fun showRoomMember()
    }
}