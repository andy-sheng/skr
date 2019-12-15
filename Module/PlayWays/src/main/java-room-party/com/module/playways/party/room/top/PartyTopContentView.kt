package com.module.playways.party.room.top

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.event.PartyOnlineUserCntChangeEvent
import com.module.playways.party.room.model.PartyBeHostConfirmEvent
import com.module.playways.party.room.model.PartyOpHostEvent
import com.module.playways.party.room.model.PartySelfOpHostEvent
import com.module.playways.room.data.H
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
            if (H.partyRoomData?.hostId == null) {
                return@setDebounceViewClickListener
            }

            val self = roomData?.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.userInfo?.clubInfo
            val host = roomData?.getPlayerInfoById(roomData?.hostId ?: 0)?.userInfo?.clubInfo

            if (roomData?.isClubHome() == true) {
                if (self != null) {
                    if (host == null) {
                        if (self.canBeHost()) {
                            EventBus.getDefault().post(PartyBeHostConfirmEvent())
                            return@setDebounceViewClickListener
                        }
                    } else {
                        if (self.canOpHost() && self.isHighLevelThen(host)) {
                            EventBus.getDefault().post(PartyOpHostEvent())
                            return@setDebounceViewClickListener
                        } else if ((roomData?.hostId ?: 0) == MyUserInfoManager.uid.toInt()) {
                            EventBus.getDefault().post(PartySelfOpHostEvent())
                            return@setDebounceViewClickListener
                        }
                    }
                }
            }

            if (H.partyRoomData?.hostId!! > 0) {
                EventBus.getDefault().post(ShowPersonCardEvent(H.partyRoomData?.hostId!!))
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
        onlineNum.text = "在线${H.partyRoomData?.onlineUserCnt}人"
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyOnlineUserCntChangeEvent) {
        onlineNum.text = "在线${H.partyRoomData?.onlineUserCnt}人"
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