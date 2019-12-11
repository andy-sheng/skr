package com.module.playways.party.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.model.PartyGameInfoModel
import com.zq.live.proto.PartyRoom.EPGameType

class PartyGameMainView(viewStub: ViewStub, protected var mRoomData: PartyRoomData) : ExViewStub(viewStub) {
    lateinit var contentBg: ExImageView
    lateinit var gameTv: ExTextView
    lateinit var handCardTv: ExTextView
    lateinit var ruleTv: ExTextView
    lateinit var attentionTv: ExTextView
    lateinit var textScrollView: ScrollView
    lateinit var commonTextView: ExTextView
    lateinit var partyGameTabView: PartyGameTabView

    var seq: Int = 0
    var tagType: TagType = TagType.GAME
    var partyGameInfoModel: PartyGameInfoModel? = null

    override fun init(parentView: View) {
        contentBg = parentView.findViewById(R.id.content_bg)
        gameTv = parentView.findViewById(R.id.game_tv)
        handCardTv = parentView.findViewById(R.id.hand_card_tv)
        ruleTv = parentView.findViewById(R.id.rule_tv)
        textScrollView = parentView.findViewById(R.id.text_scrollView)
        attentionTv = parentView.findViewById(R.id.attention_tv)
        commonTextView = parentView.findViewById(R.id.text_game_tv)
        partyGameTabView = parentView.findViewById(R.id.party_game_tab_view)
        partyGameTabView.roomData = mRoomData

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
        hideAllView()
        gameTv.isSelected = true
        tagType = TagType.GAME

        partyGameTabView.visibility = View.VISIBLE
        setGameTabText()
    }

    private fun toHandCardTab() {
        hideAllView()
        handCardTv.isSelected = true
        tagType = TagType.CARD

        setHandCardText()
    }

    private fun setHandCardText() {
        partyGameInfoModel?.let {
            if (it.gameType == EPGameType.PGT_Play.value) {
                handCardTv.text = "手卡"
                setMainText("", it.play?.playCard)
            } else if (it.gameType == EPGameType.PGT_Question.value) {
                setMainText("", it.question?.questionContent)
                handCardTv.text = "答案"
            }
        }
    }

    private fun setGameTabText() {
        partyGameInfoModel?.let {
            if (it.gameType == EPGameType.PGT_Free.value) {
                gameTv.text = "聊天"
            } else {
                gameTv.text = "游戏"
            }
        }
    }

    private fun toRuleTab() {
        hideAllView()
        ruleTv.isSelected = true
        tagType = TagType.RULE

        partyGameInfoModel?.let {
            setMainText("游戏规则\n", it.gameRule?.ruleDesc)
        }
    }

    private fun toAttentionTab() {
        hideAllView()
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

        commonTextView.text = stringBuilder
    }

    private fun hideAllView() {
        gameTv.isSelected = false
        handCardTv.isSelected = false
        ruleTv.isSelected = false
        attentionTv.isSelected = false
        textScrollView.visibility = View.GONE
        partyGameTabView.visibility = View.GONE
    }

    override fun layoutDesc(): Int {
        return R.layout.party_game_main_view_layout
    }

    fun updateIdentify() {
        partyGameTabView.updateIdentity()
    }

    //轮次切换的时候调用
    fun updateRound(partyGameInfoModel: PartyGameInfoModel?) {
        if (partyGameInfoModel == null) {
            return
        }

        tryInflate()

        this.partyGameInfoModel = partyGameInfoModel
        partyGameTabView.bindData()
        toGameTab()

        setGameTabText()
        setHandCardText()

        if (mRoomData.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isHost() == true) {
            //主持人
            if (partyGameInfoModel?.gameType == EPGameType.PGT_Play.ordinal
                    || partyGameInfoModel?.gameType == EPGameType.PGT_Question.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.VISIBLE
                ruleTv.visibility = View.VISIBLE
                attentionTv.visibility = View.VISIBLE
            } else if (partyGameInfoModel?.gameType == EPGameType.PGT_Free.ordinal
                    || partyGameInfoModel?.gameType == EPGameType.PGT_KTV.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.GONE
                ruleTv.visibility = View.GONE
                attentionTv.visibility = View.VISIBLE
            }
        } else {
            //其他人
            if (partyGameInfoModel?.gameType == EPGameType.PGT_Play.ordinal
                    || partyGameInfoModel?.gameType == EPGameType.PGT_Question.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.GONE
                ruleTv.visibility = View.VISIBLE
                attentionTv.visibility = View.VISIBLE
            } else if (partyGameInfoModel?.gameType == EPGameType.PGT_Free.ordinal
                    || partyGameInfoModel?.gameType == EPGameType.PGT_KTV.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.GONE
                ruleTv.visibility = View.GONE
                attentionTv.visibility = View.VISIBLE
            }
        }
    }

    fun toWaitingState() {
        gameTv.isSelected = true
        partyGameTabView.toWaitingState()

        gameTv.visibility = View.VISIBLE
        handCardTv.visibility = View.GONE
        ruleTv.visibility = View.GONE
        attentionTv.visibility = View.VISIBLE
    }

    enum class TagType {
        GAME, CARD, RULE, ATTENTION
    }
}