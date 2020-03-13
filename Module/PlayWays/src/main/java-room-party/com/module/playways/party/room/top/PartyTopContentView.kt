package com.module.playways.party.room.top

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rxretrofit.*
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.view.SpeakingTipsAnimationView
import com.component.person.event.ShowPersonCardEvent
import com.engine.EngineEvent
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.event.PartyOnlineUserCntChangeEvent
import com.module.playways.party.room.model.PartyPunishInfoModel
import com.module.playways.party.room.model.PartyRankModel
import com.module.playways.room.data.H
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

// 顶部头像栏
class PartyTopContentView : ExConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val arrowIv: ImageView
    private val avatarIv: BaseImageView
    private val nameTv: TextView
    private val compereTv: TextView
    private val onlineNum: ExTextView
    private val clubIconIv: ImageView

    private val speakerAnimationIv: SpeakingTipsAnimationView

    private val userRankArea: ExConstraintLayout
    private val avatarRank3: SimpleDraweeView
    private val avatarRank2: SimpleDraweeView
    private val avatarRank1: SimpleDraweeView
    private val rankTv3: ExTextView
    private val rankTv2: ExTextView
    private val rankTv1: ExTextView

    private val emptyTv: TextView

    var listener: Listener? = null
    var mIsOpen = true

    var roomData: PartyRoomData? = null

    var updateJob: Job? = null

    init {
        View.inflate(context, R.layout.party_top_content_view_layout, this)

        arrowIv = this.findViewById(R.id.arrow_iv)
        avatarIv = this.findViewById(R.id.avatar_iv)
        nameTv = this.findViewById(R.id.name_tv)
        compereTv = this.findViewById(R.id.compere_tv)
        onlineNum = this.findViewById(R.id.online_num)
        clubIconIv = this.findViewById(R.id.club_icon_iv)
        speakerAnimationIv = this.findViewById(R.id.speaker_animation_iv)

        userRankArea = this.findViewById(R.id.user_rank_area)
        avatarRank3 = this.findViewById(R.id.avatar_rank_3)
        avatarRank2 = this.findViewById(R.id.avatar_rank_2)
        avatarRank1 = this.findViewById(R.id.avatar_rank_1)
        rankTv3 = this.findViewById(R.id.rank_tv_3)
        rankTv2 = this.findViewById(R.id.rank_tv_2)
        rankTv1 = this.findViewById(R.id.rank_tv_1)

        emptyTv = this.findViewById(R.id.empty_tv)

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
                            if ((roomData?.hostId ?: 0) == MyUserInfoManager.uid.toInt()) {
                                listener?.showPartySelfOpHost()
                                return@getClubIdentify
                            } else if (it.canOpHost() && it.isHighLevelThen(host)) {
                                listener?.showPartyOpHost()
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
        onlineNum.setDebounceViewClickListener { listener?.showRoomMember() }
        clubIconIv.setDebounceViewClickListener { listener?.showClubInfoCard() }
        userRankArea.setDebounceViewClickListener { listener?.showPartyRankList() }

        getPartyRankList()
    }

    private fun getPartyRankList() {
        updateJob?.cancel()
        val partyRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)
        updateJob = launch {
            repeat(Int.MAX_VALUE) {
                val result = subscribe(RequestControl("getPartyRankList", ControlType.CancelThis)) {
                    partyRoomServerApi.getPartyRankList(0, 10, MyUserInfoManager.uid.toInt(), H.partyRoomData?.gameId
                            ?: 0, 1)
                }

                if (result.errno == 0) {
                    val list = JSON.parseArray(result.data.getString("rankInfos"), PartyRankModel::class.java)
                    bindRankData(list)
                } else {

                }

                delay(30 * 1000)
            }
        }
    }

    private fun bindRankData(list: List<PartyRankModel>?) {
        if (!list.isNullOrEmpty()) {
            emptyTv.visibility = View.GONE
            if (getPartyRankModel(1, list) != null) {
                avatarRank1.visibility = View.VISIBLE
                rankTv1.visibility = View.VISIBLE
                AvatarUtils.loadAvatarByUrl(avatarRank1, AvatarUtils.newParamsBuilder(getPartyRankModel(1, list)?.model?.avatar)
                        .setCircle(true)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(1.dp().toFloat())
                        .build())
            } else {
                avatarRank1.visibility = View.GONE
                rankTv1.visibility = View.GONE
            }

            if (getPartyRankModel(2, list) != null) {
                avatarRank2.visibility = View.VISIBLE
                rankTv2.visibility = View.VISIBLE
                AvatarUtils.loadAvatarByUrl(avatarRank2, AvatarUtils.newParamsBuilder(getPartyRankModel(2, list)?.model?.avatar)
                        .setCircle(true)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(1.dp().toFloat())
                        .build())
            } else {
                avatarRank2.visibility = View.GONE
                rankTv2.visibility = View.GONE
            }

            if (getPartyRankModel(3, list) != null) {
                avatarRank3.visibility = View.VISIBLE
                rankTv3.visibility = View.VISIBLE
                AvatarUtils.loadAvatarByUrl(avatarRank3, AvatarUtils.newParamsBuilder(getPartyRankModel(3, list)?.model?.avatar)
                        .setCircle(true)
                        .setBorderColor(Color.WHITE)
                        .setBorderWidth(1.dp().toFloat())
                        .build())
            } else {
                avatarRank3.visibility = View.GONE
                rankTv3.visibility = View.GONE
            }
        } else {
            emptyTv.visibility = View.VISIBLE
            avatarRank1.visibility = View.GONE
            avatarRank2.visibility = View.GONE
            avatarRank3.visibility = View.GONE
            rankTv1.visibility = View.GONE
            rankTv2.visibility = View.GONE
            rankTv3.visibility = View.GONE
        }
    }

    private fun getPartyRankModel(seq: Int, list: List<PartyRankModel>?): PartyRankModel? {
        if (list.isNullOrEmpty()) {
            return null
        }
        list.forEach {
            if (it.rankSeq == seq) {
                return it
            }
        }
        return null
    }

    private fun getClubIdentify(clubID: Int, call: ((ClubMemberInfo?) -> Unit)) {
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

    fun switchRoom() {
        bindData()
        getPartyRankList()
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
            var list = event.getObj<List<EngineEvent.UserVolumeInfo>>()
            for (uv in list) {
                //    MyLog.d(TAG, "UserVolumeInfo uv=" + uv);
                if (uv != null) {
                    var uid = uv.uid
                    if (uid == 0) {
                        uid = MyUserInfoManager.uid.toInt()
                    }
                    var volume = uv.volume
                    if (volume > 20) {
                        if (uid == H.partyRoomData?.hostId) {
                            speakerAnimationIv.show(1000)
                        }
                    }
                }
            }
        }
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
        updateJob?.cancel()
    }

    interface Listener {
        fun clickArrow(open: Boolean)
        fun showRoomMember()
        fun showPartyBeHostConfirm()
        fun showPartyOpHost()
        fun showPartySelfOpHost()
        fun showClubInfoCard()
        fun showPartyRankList()
    }
}