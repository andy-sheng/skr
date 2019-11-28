package com.module.playways.mic.room.top

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.permission.SkrAudioPermission
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.common.view.ex.drawable.DrawableCreator
import com.component.busilib.constans.GameModeType
import com.component.busilib.view.AvatarView
import com.module.playways.R
import com.module.playways.mic.room.MicRoomServerApi
import com.module.playways.mic.room.model.RoomInviteMusicModel
import com.module.playways.relay.room.RelayRoomServerApi
import com.module.playways.room.data.H
import com.zq.live.proto.Common.StandPlayType
import com.zq.live.proto.MicRoom.EMWantSingType
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody

// 合唱或者PK的邀请view (包含邀请和邀请结果) 接唱房间的邀请
class RoomInviteView(viewStub: ViewStub) : ExViewStub(viewStub) {

    private var triangleArrow: ImageView? = null
    private var inviteGroup: Group? = null
    private var nameTv: TextView? = null
    private var descTv: TextView? = null
    private var songNameTv: TextView? = null
    private var agreeTv: ExTextView? = null

    private var resultGroup: Group? = null
    private var resultAvatar: AvatarView? = null
    private var resultName: TextView? = null
    private var resultDesc: TextView? = null

    private var inviteJob: Job? = null
    private var resultJob: Job? = null

    private var gameType: Int = 0
    private var userMusicModel: RoomInviteMusicModel? = null
    private var leftMargin: Int = 0

    private val micRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)
    private val relayRoomServerApi = ApiManager.getInstance().createService(RelayRoomServerApi::class.java)

    var agreeInviteListener: (() -> Unit)? = null

    val grayDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#B1AC99"))
            .setCornersRadius(21.dp().toFloat())
            .build()

    val yellowDrawable: Drawable = DrawableCreator.Builder()
            .setSolidColor(Color.parseColor("#FFC15B"))
            .setCornersRadius(21.dp().toFloat())
            .build()

    val mSkrAudioPermission = SkrAudioPermission()

    override fun init(parentView: View) {
        // 指向某个view的三角形
        triangleArrow = parentView.findViewById(R.id.triangle_arrow)

        // 邀请
        inviteGroup = parentView.findViewById(R.id.invite_group)
        nameTv = parentView.findViewById(R.id.name_tv)
        descTv = parentView.findViewById(R.id.desc_tv)
        songNameTv = parentView.findViewById(R.id.song_name_tv)
        agreeTv = parentView.findViewById(R.id.agree_tv)

        // 结果
        resultGroup = parentView.findViewById(R.id.result_group)
        resultAvatar = parentView.findViewById(R.id.result_avatar)
        resultName = parentView.findViewById(R.id.result_name)
        resultDesc = parentView.findViewById(R.id.result_desc)

        agreeTv?.setAnimateDebounceViewClickListener {
            agreeInviteListener?.invoke()
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.mic_invite_view_stub_layout
    }

    fun startCheckSelfJob(micUserMusicModel: RoomInviteMusicModel?) {
        this.userMusicModel = micUserMusicModel
        cancelJob()
        inviteJob = launch {
            delay(8000)
            // 去拉一下演唱的状态
            syncInviteResult(micUserMusicModel?.uniqTag)
        }
    }

    @SuppressLint("SetTextI18n")
    fun showInvite(micUserMusicModel: RoomInviteMusicModel?, left: Int, isInvite: Boolean, gameType: Int) {
        tryInflate()

        this.userMusicModel = micUserMusicModel
        this.gameType = gameType
        setVisibility(View.VISIBLE)
        // 自适应一下箭头的位置
        if (left == -1) {
            triangleArrow?.visibility = View.GONE
        } else {
            triangleArrow?.visibility = View.VISIBLE
            if (left >= 0 && left != leftMargin) { // 外面用-1的时候不改变他的位置,且位置改变了
                val contentLayoutParams = triangleArrow?.layoutParams as ConstraintLayout.LayoutParams?
                contentLayoutParams?.leftMargin = left
                triangleArrow?.layoutParams = contentLayoutParams
                leftMargin = left
            }
        }


        cancelJob()
        if (isInvite) {
            resultGroup?.visibility = View.GONE
            inviteGroup?.visibility = View.VISIBLE

            val model = H.micRoomData?.getPlayerOrWaiterInfo(micUserMusicModel?.userID)
            nameTv?.text = model?.nicknameRemark
            when {
                micUserMusicModel?.music?.playType == StandPlayType.PT_CHO_TYPE.value -> descTv?.text = "邀请合唱"
                micUserMusicModel?.music?.playType == StandPlayType.PT_SPK_TYPE.value -> descTv?.text = "邀请PK"
                micUserMusicModel?.music?.playType == StandPlayType.PT_RELAY_TYPE.value -> descTv?.text = "邀请接唱"
                else -> descTv?.text = "邀请"
            }
            songNameTv?.text = "《${micUserMusicModel?.music?.displaySongName}》"
            agreeTv?.isClickable = true
            agreeTv?.background = yellowDrawable

            var agreeText = "抢唱"
            if (gameType == GameModeType.GAME_MODE_RELAY) {
                agreeText = "同意"
            }
            inviteJob = launch {
                repeat(8) {
                    agreeTv?.text = "$agreeText ${8 - it}s"
                    delay(1000)
                }
                agreeTv?.text = "$agreeText 0s"
                // 去拉一下演唱的状态
                syncInviteResult(micUserMusicModel?.uniqTag)
            }
        } else {
            if (micUserMusicModel?.peerID != 0) {
                if (micUserMusicModel?.peerID == MyUserInfoManager.uid.toInt()) {
                    resultGroup?.visibility = View.GONE
                    inviteGroup?.visibility = View.VISIBLE
                    agreeTv?.text = "加入成功"
                    agreeTv?.isClickable = false
                    agreeTv?.background = grayDrawable
                } else {
                    resultGroup?.visibility = View.VISIBLE
                    inviteGroup?.visibility = View.GONE
                    val model = H.micRoomData?.getPlayerOrWaiterInfo(micUserMusicModel?.userID)  // 发起人
                    val peerModel = H.micRoomData?.getPlayerOrWaiterInfo(micUserMusicModel?.peerID)  // 接收人

                    resultAvatar?.bindData(peerModel)
                    resultName?.text = peerModel?.nicknameRemark
                    if (gameType == GameModeType.GAME_MODE_MIC) {
                        if (micUserMusicModel?.wantSingType == EMWantSingType.MWST_SPK.value) {
                            resultDesc?.text = "已接受${model?.nicknameRemark}的PK"
                        } else {
                            resultDesc?.text = "已加入${model?.nicknameRemark}的合唱"
                        }
                    } else {
                        resultDesc?.text = "已加入接唱"
                    }
                }
                resultJob = launch {
                    delay(2000)
                    setVisibility(View.GONE)
                }
            } else {
                // 没人和你合唱 只给发起人
                setVisibility(View.GONE)
                if (micUserMusicModel.userID == MyUserInfoManager.uid.toInt()) {
                    when {
                        micUserMusicModel.music?.playType == StandPlayType.PT_CHO_TYPE.value -> U.getToastUtil().showShort("发起${micUserMusicModel?.music?.displaySongName}合唱失败")
                        micUserMusicModel.music?.playType == StandPlayType.PT_SPK_TYPE.value -> U.getToastUtil().showShort("发起${micUserMusicModel?.music?.displaySongName}PK失败")
                        micUserMusicModel.music?.playType == StandPlayType.PT_RELAY_TYPE.value -> U.getToastUtil().showShort("发起${micUserMusicModel?.music?.displaySongName}接唱失败")
                        else -> U.getToastUtil().showShort("${micUserMusicModel.music?.displaySongName}发起失败")
                    }
                }
            }
        }
    }

    private fun cancelJob() {
        inviteJob?.cancel()
        resultJob?.cancel()
    }

    fun agreeInvite() {
        if (gameType == GameModeType.GAME_MODE_MIC) {
            launch {
                val map = mutableMapOf(
                        "roomID" to (H.micRoomData?.gameId ?: 0),
                        "uniqTag" to (userMusicModel?.uniqTag ?: ""))
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("agreeInvite", ControlType.CancelLast)) {
                    micRoomServerApi.agreeSing(body)
                }
                if (result.errno == 0) {
                    userMusicModel?.peerID = MyUserInfoManager.uid.toInt()
                    showInvite(userMusicModel, -1, false, GameModeType.GAME_MODE_MIC)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        } else if (gameType == GameModeType.GAME_MODE_RELAY) {
            launch {
                val map = mutableMapOf(
                        "roomID" to (H.micRoomData?.gameId ?: 0),
                        "uniqTag" to (userMusicModel?.uniqTag ?: ""))
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("agreeInvite", ControlType.CancelLast)) {
                    relayRoomServerApi.agreeSing(body)
                }
                if (result.errno == 0) {
                    userMusicModel?.peerID = MyUserInfoManager.uid.toInt()
                    showInvite(userMusicModel, -1, false, GameModeType.GAME_MODE_MIC)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    private fun syncInviteResult(uniqTag: String?) {
        if (gameType == GameModeType.GAME_MODE_MIC) {
            launch {
                val result = subscribe(RequestControl("syncInviteResult", ControlType.CancelLast)) {
                    micRoomServerApi.getAgreeSingResult(H.micRoomData?.gameId
                            ?: 0, uniqTag ?: "")
                }
                if (result.errno == 0) {
                    val userMusicModel = JSON.parseObject(result.data.getString("music"), RoomInviteMusicModel::class.java)
                    showInvite(userMusicModel, -1, false, GameModeType.GAME_MODE_MIC)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        } else if (gameType == GameModeType.GAME_MODE_RELAY){
            launch {
                val result = subscribe(RequestControl("syncInviteResult", ControlType.CancelLast)) {
                    relayRoomServerApi.getAgreeSingResult(H.micRoomData?.gameId
                            ?: 0, uniqTag ?: "")
                }
                if (result.errno == 0) {
                    val userMusicModel = JSON.parseObject(result.data.getString("music"), RoomInviteMusicModel::class.java)
                    showInvite(userMusicModel, -1, false, GameModeType.GAME_MODE_RELAY)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            cancelJob()
        }
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        cancelJob()
    }
}