package com.module.playways.party.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData

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
        unSelectAllTab()
        gameTv.isSelected = true

        val stringBuilder = SpanUtils()
                .append("中华小曲库").setForegroundColor(U.getColor(R.color.white_trans_80)).setFontSize(U.getDisplayUtils().dip2px(14f)).setBold()
                .append("\n演唱一首带“爱”字的歌曲").setForegroundColor(U.getColor(R.color.white_trans_50)).setFontSize(U.getDisplayUtils().dip2px(14f))
                .create()

        textGameTv.text = stringBuilder
    }

    private fun toHandCardTab() {
        unSelectAllTab()
        handCardTv.isSelected = true
    }

    private fun toRuleTab() {
        unSelectAllTab()
        ruleTv.isSelected = true
    }

    private fun toAttentionTab() {
        unSelectAllTab()
        attentionTv.isSelected = true
    }

    private fun unSelectAllTab() {
        gameTv.isSelected = false
        handCardTv.isSelected = false
        ruleTv.isSelected = false
        attentionTv.isSelected = false
    }

    override fun layoutDesc(): Int {
        return R.layout.party_game_main_view_layout
    }

    //更新
    fun updateGameContent() {
        tryInflate()
    }
}