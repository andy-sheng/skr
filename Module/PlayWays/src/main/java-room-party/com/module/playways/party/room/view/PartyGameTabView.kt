package com.module.playways.party.room.view

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.support.constraint.Group
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.model.PartyGameInfoModel
import com.zq.live.proto.PartyRoom.EPGameType

class PartyGameTabView : ConstraintLayout {
    var partySelfSingLyricLayoutViewStub: ViewStub
    var gamePicImg: BaseImageView
    var textScrollView: ScrollView
    var textGameTv: ExTextView
    var bottomLeftOpTv: ExTextView
    var bottomRightOpTv: ExTextView
    var avatarIv: BaseImageView
    var singingTv: ExTextView
    var singingGroup: Group

    var partySelfSingLyricView: PartySelfSingLyricView? = null

    var roomData: PartyRoomData? = null
        set(value) {
            field = value
            partySelfSingLyricView = PartySelfSingLyricView(partySelfSingLyricLayoutViewStub, roomData!!)
        }

    var partyGameInfoModel: PartyGameInfoModel? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.party_game_tab_view_layout, this)
        partySelfSingLyricLayoutViewStub = rootView.findViewById(R.id.party_self_sing_lyric_layout_view_stub)

        bottomLeftOpTv = rootView.findViewById(R.id.bottom_left_op_tv)
        gamePicImg = rootView.findViewById(R.id.game_pic_img)
        textScrollView = rootView.findViewById(R.id.text_scrollView)
        textGameTv = rootView.findViewById(R.id.text_game_tv)
        bottomRightOpTv = rootView.findViewById(R.id.bottom_right_op_tv)
        avatarIv = rootView.findViewById(R.id.avatar_iv)
        singingTv = rootView.findViewById(R.id.singing_tv)
        singingGroup = rootView.findViewById(R.id.singing_group)
        bottomLeftOpTv.text = "切游戏"
        bottomRightOpTv.text = "下一题"
    }

    //身份更新
    fun updateIdentity() {
        if (roomData?.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isHost() == true) {
            //主持人
            bottomLeftOpTv.visibility = View.VISIBLE
            bottomRightOpTv.visibility = View.VISIBLE
        } else {
            //其他
            bottomLeftOpTv.visibility = View.GONE
            bottomRightOpTv.visibility = View.GONE
        }
    }

    //题目更新，只有换轮次的时候调用
    fun bindData() {
        hideAllTypeView()

        updateIdentity()

        partyGameInfoModel = roomData?.realRoundInfo?.itemInfo
        if (partyGameInfoModel?.gameType == EPGameType.PGT_Play.ordinal
                || partyGameInfoModel?.gameType == EPGameType.PGT_Question.ordinal) {
            textScrollView.visibility = View.VISIBLE

            setMainText(getGameTagTitle(), getGameTagContent())
            partyGameInfoModel?.let {
                if (it.gameType == EPGameType.PGT_Question.value && (it.question?.questionPic?.size
                                ?: 0) > 0) {
                    gamePicImg.visibility = View.VISIBLE

                    AvatarUtils.loadAvatarByUrl(gamePicImg, AvatarUtils.newParamsBuilder(it.question?.questionPic?.get(0))
                            .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setBorderColor(Color.WHITE)
                            .build())
                } else {
                    gamePicImg.visibility = View.GONE
                }
            }
        } else if (partyGameInfoModel?.gameType == EPGameType.PGT_Free.ordinal) {
            textScrollView.visibility = View.VISIBLE
            setMainText("", "自由麦模式，大家畅所欲言吧～")
        } else if (partyGameInfoModel?.gameType == EPGameType.PGT_KTV.ordinal) {
            partySelfSingLyricView?.setVisibility(View.VISIBLE)

            partySelfSingLyricView?.startFly {

            }
        }
    }

    private fun hideAllTypeView() {
        partySelfSingLyricView?.setVisibility(View.GONE)
        bottomLeftOpTv.visibility = View.GONE
        gamePicImg.visibility = View.GONE
        textScrollView.visibility = View.GONE
        bottomRightOpTv.visibility = View.GONE
        avatarIv.visibility = View.GONE
        singingGroup.visibility = View.GONE
        bottomLeftOpTv.visibility = View.GONE
        bottomRightOpTv.visibility = View.GONE
    }

    private fun getGameTagTitle(): String {
        var gameTagTitle = ""
        gameTagTitle = partyGameInfoModel?.gameRule?.ruleName ?: ""

        return if (TextUtils.isEmpty(gameTagTitle)) "" else "$gameTagTitle\n"
    }

    private fun getGameTagContent(): String {
        partyGameInfoModel?.let {
            if (it.gameType == EPGameType.PGT_Play.value) {
                return it.play?.playContent ?: ""
            } else if (it.gameType == EPGameType.PGT_Question.value) {
                return it.question?.answerContent ?: ""
            }
        }

        return ""
    }

    private fun setMainText(title: String?, content: String?) {
        val stringBuilder = SpanUtils()
                .append(title
                        ?: "").setForegroundColor(U.getColor(R.color.white_trans_80)).setFontSize(U.getDisplayUtils().dip2px(14f)).setBold()
                .append(content
                        ?: "").setForegroundColor(U.getColor(R.color.white_trans_50)).setFontSize(U.getDisplayUtils().dip2px(14f))
                .create()

        textGameTv.text = stringBuilder
    }

    fun toWaitingState() {
        setMainText("", "还没有歌曲，大家快去点歌吧～")
    }
}