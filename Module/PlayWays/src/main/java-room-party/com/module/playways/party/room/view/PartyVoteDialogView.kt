package com.module.playways.party.room.view

import android.content.Context
import android.view.Gravity
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.anim.svga.SvgaParserAdapter
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PartyVoteResultModel
import com.module.playways.room.data.H
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.PartyRoom.EVoteScope
import com.zq.live.proto.PartyRoom.PBeginVote
import com.zq.live.proto.PartyRoom.PResponseVote
import com.zq.live.proto.PartyRoom.PResultVote
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *  下麦，关麦，查看信息，取消
 *  关闭座位，邀请上麦，取消
 *  打开座位，取消
 */
class PartyVoteDialogView(context: Context, val event: PBeginVote) : ExConstraintLayout(context) {
    private var mDialogPlus: DialogPlus? = null
    val leftBg: ExImageView
    val rightBg: ExImageView
    val leftAvatarIv: BaseImageView
    val leftNameTv: ExTextView
    val leftTicketTv: ExTextView
    val leftTicketName: ExTextView
    val leftButtom: ExTextView
    val leftSVGA: SVGAImageView
    val rightAvatarIv: BaseImageView
    val rightNameTv: ExTextView
    val rightTicketTv: ExTextView
    val rightTicketName: ExTextView
    val rightButtom: ExTextView
    val rightSVGA: SVGAImageView
    val countDownTv: ExTextView

    var isVoting = true

    var countDownJob: Job? = null
    //确保消失
    var dismissJob: Job? = null

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    init {
        View.inflate(context, R.layout.party_vote_view_layout, this)
        leftBg = this.findViewById(R.id.left_bg)
        rightBg = this.findViewById(R.id.right_bg)
        leftAvatarIv = this.findViewById(R.id.left_avatar_iv)
        leftNameTv = this.findViewById(R.id.left_name_tv)
        leftTicketTv = this.findViewById(R.id.left_ticket_tv)
        leftTicketName = this.findViewById(R.id.left_ticket_name)
        leftButtom = this.findViewById(R.id.left_buttom)
        leftSVGA = this.findViewById(R.id.left_svga_iv)
        rightAvatarIv = this.findViewById(R.id.right_avatar_iv)
        rightNameTv = this.findViewById(R.id.right_name_tv)
        rightTicketTv = this.findViewById(R.id.right_ticket_tv)
        rightTicketName = this.findViewById(R.id.right_ticket_name)
        rightButtom = this.findViewById(R.id.right_buttom)
        rightSVGA = this.findViewById(R.id.right_svga_iv)
        countDownTv = this.findViewById(R.id.count_down_tv)
        setData()

        //确保能关闭
        dismissJob = launch {
            delay(event.endTimeMs - event.beginTimeMs + 6000)
            dismiss(false)
        }
    }

    fun setData() {
        AvatarUtils.loadAvatarByUrl(leftAvatarIv,
                AvatarUtils.newParamsBuilder(event.usersList[0].userInfo.avatar)
                        .setCircle(false)
                        .setBorderWidth(1.dp().toFloat())
                        .setBorderColor(U.getColor(R.color.white))
                        .setCornerRadius(40.dp().toFloat())
                        .build())

        leftNameTv.text = UserInfoManager.getInstance().getRemarkName(event.usersList[0].userInfo.userID, event.usersList[0].userInfo.nickName)

        AvatarUtils.loadAvatarByUrl(rightAvatarIv,
                AvatarUtils.newParamsBuilder(event.usersList[1].userInfo.avatar)
                        .setCircle(false)
                        .setBorderWidth(1.dp().toFloat())
                        .setBorderColor(U.getColor(R.color.white))
                        .setCornerRadius(40.dp().toFloat())
                        .build())

        rightNameTv.text = UserInfoManager.getInstance().getRemarkName(event.usersList[1].userInfo.userID, event.usersList[1].userInfo.nickName)

        leftButtom.setDebounceViewClickListener {
            vote(event.usersList[0].userInfo.userID)
        }

        rightButtom.setDebounceViewClickListener {
            vote(event.usersList[1].userInfo.userID)
        }

        if ((System.currentTimeMillis() - BaseRoomData.shiftTsForRelay) - event.beginTimeMs < 1000) {
            //按没有延迟处理
            countDown((event.endTimeMs - event.beginTimeMs).toInt() / 1000)
        } else {
            countDown(((event.endTimeMs - event.beginTimeMs).toInt() - ((System.currentTimeMillis() - BaseRoomData.shiftTsForRelay) - event.beginTimeMs).toInt()) / 1000)
        }

        if (event.scope == EVoteScope.EVS_HOST_GUEST) {
            val info = H.partyRoomData?.getPlayerInfoById(MyUserInfoManager.uid.toInt())
            if (info == null || !info.isGuest() || !info.isHost()) {
                leftButtom.visibility = View.GONE
                rightButtom.visibility = View.GONE
            }
        }

        var isContainMe = false
        for (user in event.usersList) {
            if (user.userInfo.userID == MyUserInfoManager.uid.toInt()) {
                isContainMe = true
                break
            }
        }

        if (isContainMe) {
            leftButtom.visibility = View.GONE
            rightButtom.visibility = View.GONE
        }
    }

    private fun countDown(count: Int) {
        MyLog.d("PartyVoteDialogView", "countDown count = $count")
        countDownJob?.cancel()
        countDownJob = launch {
            for (c in 0..(count - 1)) {
                countDownTv.text = "${count - c}s"
                MyLog.d("PartyVoteDialogView", "countDown c = $c")
                delay(1000)
            }

            countDownTv.text = "0s"
            delay(2000)
            getVoteResult()
        }
    }

    private fun getVoteResult() {
        if (isVoting) {
            launch {
                var result = subscribe(RequestControl("getVoteResult", ControlType.CancelThis)) {
                    roomServerApi.getVoteResult(H.partyRoomData?.gameId ?: 0, event.voteTag)
                }

                if (result.errno == 0) {
                    if (isVoting) {
                        isVoting = false
                        val voteResultList = JSON.parseArray(result.data.getString("voteInfos"), PartyVoteResultModel::class.java)
                        var leftCnt = 0
                        var rightCnt = 0
                        var leftStr = ""
                        var rightStr = ""
                        voteResultList.forEach {
                            if (it.user?.userID == this@PartyVoteDialogView.event.usersList[0].userInfo.userID) {
                                leftTicketTv.text = it.voteCnt.toString()
                                leftCnt = it.voteCnt ?: 0
                                leftStr = "${UserInfoManager.getInstance().getRemarkName(this@PartyVoteDialogView.event.usersList[0].userInfo.userID, this@PartyVoteDialogView.event.usersList[0].userInfo.nickName)} ${it.voteCnt}"
                            } else if (it.user?.userID == this@PartyVoteDialogView.event.usersList[1].userInfo.userID) {
                                rightTicketTv.text = it.voteCnt.toString()
                                rightCnt = it.voteCnt ?: 0
                                rightStr = "${UserInfoManager.getInstance().getRemarkName(this@PartyVoteDialogView.event.usersList[1].userInfo.userID, this@PartyVoteDialogView.event.usersList[1].userInfo.nickName)} ${it.voteCnt}"
                            }
                        }

                        showWinner(leftCnt, rightCnt)
                        pretendSystemMsg("投票结果：$leftStr  $rightStr")
                    }
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    private fun showWinner(leftCnt: Int, rightCnt: Int) {
        dismissJob?.cancel()
        if (leftCnt == 0 && rightCnt == 0) {
            //什么都不做
        } else if (leftCnt > rightCnt) {
            tryPlayAnima(leftSVGA)
        } else if (leftCnt < rightCnt) {
            tryPlayAnima(rightSVGA)
        } else {
            tryPlayAnima(leftSVGA)
            tryPlayAnima(rightSVGA)
        }

        launch {
            delay(3000)
            dismiss(false)
        }
    }

    private fun tryPlayAnima(svgaImageView: SVGAImageView) {
        svgaImageView.clearAnimation()
        svgaImageView.visibility = View.VISIBLE
        svgaImageView.loops = 1
        SvgaParserAdapter.parse("vote_star.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                svgaImageView.setImageDrawable(drawable)
                svgaImageView.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    private fun vote(userID: Int) {
        val map = HashMap<String, Any?>()
        map["roomID"] = H.partyRoomData?.gameId
        map["beVotedUserID"] = userID
        map["voteTag"] = event.voteTag

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("vote", ControlType.CancelThis)) {
                roomServerApi.vote(body)
            }

            if (result.errno == 0) {
//                dismiss(false)
//                U.getToastUtil().showShort("")
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventBus.getDefault().register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PResponseVote) {
        if (event.voteTag == this.event.voteTag) {
            if (isVoting) {
                if (this.event.usersList[0].userInfo.userID == event.userID) {
                    leftTicketTv.text = event.voteCnt.toString()
                } else if (this.event.usersList[1].userInfo.userID == event.userID) {
                    rightTicketTv.text = event.voteCnt.toString()
                }
            } else {
                MyLog.w("PartyVoteDialogView", "已经不再投票中了，PResponseVote event.voteTag是${event.voteTag}")
            }
        } else {
            MyLog.w("PartyVoteDialogView", "收到别的投票相关消息，PResponseVote event.voteTag是${event.voteTag}")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PResultVote) {
        if (event.voteTag == this.event.voteTag) {
            if (isVoting) {
                isVoting = false
                countDownJob?.cancel()
                countDownTv.text = "0s"
                var leftCnt = 0
                var rightCnt = 0
                event.voteInfosList.forEach {
                    if (it.user.userInfo.userID == this.event.usersList[0].userInfo.userID) {
                        leftTicketTv.text = event.voteInfosList[0].voteCnt.toString()
                        leftCnt = event.voteInfosList[0].voteCnt
                    } else if (it.user.userInfo.userID == this.event.usersList[1].userInfo.userID) {
                        rightTicketTv.text = event.voteInfosList[1].voteCnt.toString()
                        rightCnt = event.voteInfosList[1].voteCnt
                    }
                }

                showWinner(leftCnt, rightCnt)
            } else {
                MyLog.w("PartyVoteDialogView", "已经不再投票中了，PResultVote event.voteTag是${event.voteTag}")
            }
        } else {
            MyLog.w("PartyVoteDialogView", "收到别的投票相关消息，PResultVote event.voteTag是${event.voteTag}")
        }
    }

    fun pretendSystemMsg(text: String) {
        val commentSysModel = CommentSysModel(GameModeType.GAME_MODE_PARTY, text)
        EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
    }

    /**
     * 以后tips dialog 不要在外部单独写 dialog 了。
     * 可以不
     */
    fun showByDialog() {
        showByDialog(true)
    }

    fun showByDialog(canCancel: Boolean) {
        mDialogPlus?.dismiss(false)
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(com.common.base.R.color.transparent)
                .setOverlayBackgroundResource(com.common.base.R.color.black_trans_80)
                .setExpanded(false)
                .setMargin(U.getDisplayUtils().dip2px(10f), 0, U.getDisplayUtils().dip2px(10f), U.getDisplayUtils().dip2px(10f))
                .setCancelable(canCancel)
                .setContentHeight(U.getDisplayUtils().dip2px(350f))
                .create()
        mDialogPlus?.show()
    }

    fun dismiss() {
        mDialogPlus?.dismiss()
    }

    fun dismiss(isAnimation: Boolean) {
        mDialogPlus?.dismiss(isAnimation)
    }
}