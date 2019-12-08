package com.module.playways.party.room.view

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.model.PartyGameInfoModel
import com.zq.live.proto.PartyRoom.EPGameType

class PartyGameMainView(viewStub: ViewStub, protected var mRoomData: PartyRoomData?) : ExViewStub(viewStub) {
    lateinit var contentBg: ExImageView
    lateinit var gameTv: ExTextView
    lateinit var handCardTv: ExTextView
    lateinit var ruleTv: ExTextView
    lateinit var attentionTv: ExTextView
    lateinit var gamePicImg: BaseImageView
    lateinit var textScrollView: ScrollView
    lateinit var textGameTv: ExTextView
    lateinit var bottomLeftOpTv: ExTextView
    lateinit var bottomRightOpTv: ExTextView

    var seq: Int = 0
    var tagType: TagType = TagType.GAME
    var partyGameInfoModel: PartyGameInfoModel? = null

    override fun init(parentView: View) {
        contentBg = parentView.findViewById(R.id.content_bg)
        gameTv = parentView.findViewById(R.id.game_tv)
        handCardTv = parentView.findViewById(R.id.hand_card_tv)
        ruleTv = parentView.findViewById(R.id.rule_tv)
        attentionTv = parentView.findViewById(R.id.attention_tv)
        gamePicImg = parentView.findViewById(R.id.game_pic_img)
        textScrollView = parentView.findViewById(R.id.text_scrollView)
        textGameTv = parentView.findViewById(R.id.text_game_tv)
        bottomLeftOpTv = parentView.findViewById(R.id.bottom_left_op_tv)
        bottomRightOpTv = parentView.findViewById(R.id.bottom_right_op_tv)

        gameTv.setDebounceViewClickListener {
            toGameTab()
        }

        handCardTv.setDebounceViewClickListener {
            toHandCardTab()
        }

        ruleTv.setDebounceViewClickListener {
            toRuleTab()
        }

        attentionTv.setDebounceViewClickListener {
            toAttentionTab()
        }

        toGameTab()
    }

    private fun toGameTab() {
        resetMainView()
        gameTv.isSelected = true
        tagType = TagType.GAME

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

        bottomLeftOpTv.visibility = View.VISIBLE
        bottomRightOpTv.visibility = View.VISIBLE
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
            } else {
                return ""
            }
        }

        return ""
    }

    private fun toHandCardTab() {
        resetMainView()
        handCardTv.isSelected = true
        tagType = TagType.CARD

        partyGameInfoModel?.let {
            if (it.gameType == EPGameType.PGT_Play.value) {
                setMainText("", it.play?.playCard)
                handCardTv.text = "手卡"
            } else if (it.gameType == EPGameType.PGT_Question.value) {
                setMainText("", it.question?.questionContent)
                handCardTv.text = "答案"
            }
        }
    }

    private fun toRuleTab() {
        resetMainView()
        ruleTv.isSelected = true
        tagType = TagType.RULE

        partyGameInfoModel?.let {
            setMainText("游戏规则\n", it.gameRule?.ruleDesc)
        }
    }

    private fun toAttentionTab() {
        resetMainView()
        attentionTv.isSelected = true
        tagType = TagType.ATTENTION

        partyGameInfoModel?.let {
            setMainText("房间公告\n", mRoomData?.notice)
        }
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

    private fun resetMainView() {
        gameTv.isSelected = false
        handCardTv.isSelected = false
        ruleTv.isSelected = false
        attentionTv.isSelected = false
        gamePicImg.visibility = View.GONE
        bottomLeftOpTv.visibility = View.GONE
        bottomRightOpTv.visibility = View.GONE
    }

    override fun layoutDesc(): Int {
        return R.layout.party_game_main_view_layout
    }

    //更新
    fun updateGameContent(partyGameInfoModel: PartyGameInfoModel?) {
        if (partyGameInfoModel == null) {
            return
        }

        tryInflate()

        this.partyGameInfoModel = partyGameInfoModel
        if ((mRoomData?.realRoundSeq ?: 0) != seq) {
            seq = (mRoomData?.realRoundSeq ?: 0)
            toGameTab()
        } else {
            if (tagType == TagType.GAME) {
                toGameTab()
            } else if (tagType == TagType.CARD) {
                toHandCardTab()
            } else if (tagType == TagType.RULE) {
                toRuleTab()
            } else if (tagType == TagType.ATTENTION) {
                toAttentionTab()
            }
        }

        if (partyGameInfoModel.gameType == EPGameType.PGT_Play.value) {
            handCardTv.text = "手卡"
        } else if (partyGameInfoModel.gameType == EPGameType.PGT_Question.value) {
            handCardTv.text = "答案"
        }
    }

    fun toWaitingState() {
        resetMainView()
        gameTv.isSelected = true
        textGameTv.text = "等待中"
    }

    enum class TagType {
        GAME, CARD, RULE, ATTENTION
    }
}