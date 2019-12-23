package com.module.playways.party.room.top

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.component.person.event.ShowPersonCardEvent
import com.module.RouterConstants
import com.module.club.IClubModuleService
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.event.PartyOnlineUserCntChangeEvent
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
    val clubIconIv: ImageView

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
        clubIconIv = this.findViewById(R.id.club_icon_iv)
        (this.findViewById(R.id.avatar_iv_bg) as View).setOnClickListener {
            avatarIv.callOnClick()
        }

        avatarIv.setDebounceViewClickListener {
            if (H.partyRoomData?.hostId == null) {
                return@setDebounceViewClickListener
            }


            if (roomData?.isClubHome() == true) {
                val host = roomData?.getPlayerInfoById(roomData?.hostId ?: 0)?.userInfo?.clubInfo
                getClubIdentify(roomData?.clubInfo?.clubID ?: 0) {
                    if (it != null) {
                        if (host == null) {
                            if (it.canBeHost()) {
                                listener?.showPartyBeHostConfirm()
                                return@getClubIdentify
                            }
                        } else {
                            if (it.canOpHost() && it.isHighLevelThen(host)) {
                                listener?.showPartyOpHost()
                                return@getClubIdentify
                            } else if ((roomData?.hostId ?: 0) == MyUserInfoManager.uid.toInt()) {
                                listener?.showPartySelfOpHost()
                                return@getClubIdentify
                            }
                        }
                    }

                    if (H.partyRoomData?.hostId!! > 0) {
                        EventBus.getDefault().post(ShowPersonCardEvent(H.partyRoomData?.hostId!!))
                    }
                }
            } else {
                if (H.partyRoomData?.hostId!! > 0) {
                    EventBus.getDefault().post(ShowPersonCardEvent(H.partyRoomData?.hostId!!))
                }
            }

        }
        arrowIv.setDebounceViewClickListener { listener?.clickArrow(!mIsOpen) }
        moreArrow.setDebounceViewClickListener { listener?.showRoomMember() }
        onlineNum.setDebounceViewClickListener { listener?.showRoomMember() }
        audienceIv.setDebounceViewClickListener { listener?.showRoomMember() }
        clubIconIv.setDebounceViewClickListener { listener?.showClubInfoCard() }
    }

    fun getClubIdentify(clubID: Int, call: ((ClubMemberInfo?) -> Unit)) {
        val userServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
        ApiMethods.subscribe(userServerApi.getClubMemberInfo(MyUserInfoManager.uid.toInt(), clubID), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val clubMemberInfo = JSON.parseObject(result.data.getString("info"), ClubMemberInfo::class.java)
                    call.invoke(clubMemberInfo)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                    call.invoke(null)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                call.invoke(null)
                U.getToastUtil().showShort("网络错误")
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                U.getToastUtil().showShort(e.message)
                call.invoke(null)
            }
        })
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
        if (hostUser != null) {
            avatarIv.visibility = View.VISIBLE
            AvatarUtils.loadAvatarByUrl(avatarIv,
                    AvatarUtils.newParamsBuilder(hostUser?.userInfo?.avatar)
                            .setBorderColor(Color.WHITE)
                            .setBorderWidth(1.dp().toFloat())
                            .setCircle(true)
                            .build())
            nameTv.text = hostUser?.userInfo?.nicknameRemark
        } else {
            nameTv.text = "无房主"
            avatarIv.visibility = View.INVISIBLE

            if (H.partyRoomData?.isClubHome() == true) {
                if (MyUserInfoManager.myUserInfo?.clubInfo?.club?.clubID == H.partyRoomData?.clubInfo?.clubID
                        && MyUserInfoManager.myUserInfo?.clubInfo?.canBeHost() == true) {
                    val spanUtils = SpanUtils()
                            .append("上麦主持").setForegroundColor(Color.parseColor("#DEA243")).create()
                    nameTv.text = spanUtils
                } else {
                    nameTv.text = "暂无主持人"
                }
            }
        }

        compereTv.text = "房间号:${H.partyRoomData?.gameId}"
        onlineNum.text = "在线${H.partyRoomData?.onlineUserCnt}人"

        if (H.partyRoomData?.isClubHome() == true) {
            clubIconIv.visibility = View.VISIBLE
        } else {
            clubIconIv.visibility = View.GONE
        }
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
        fun showPartyBeHostConfirm()
        fun showPartyOpHost()
        fun showPartySelfOpHost()
        fun showClubInfoCard()
    }
}