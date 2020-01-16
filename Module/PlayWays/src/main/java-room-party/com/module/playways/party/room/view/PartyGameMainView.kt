package com.module.playways.party.room.view

import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.engine.EngineEvent
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.event.PartyHostChangeEvent
import com.module.playways.party.room.event.PartyMyUserInfoChangeEvent
import com.module.playways.party.room.event.PartyNoticeChangeEvent
import com.module.playways.party.room.model.PartyGameInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.zq.live.proto.PartyRoom.EPGameType
import com.zq.live.proto.PartyRoom.EPRoundStatus
import com.zq.mediaengine.kit.ZqEngineKit
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PartyGameMainView(viewStub: ViewStub, protected var mRoomData: PartyRoomData) : ExViewStub(viewStub) {
    val TAG = "PartyGameMainView"
    lateinit var contentBg: ExImageView
    lateinit var gameTv: ExTextView
    lateinit var handCardTv: ExTextView
    lateinit var ruleTv: ExTextView
    lateinit var attentionTv: ExTextView
    lateinit var textScrollView: ScrollView
    lateinit var commonTextView: ExTextView
    var partyGameTabView: PartyGameTabView? = null

    var seq: Int = 0
    var tagType: TagType = TagType.GAME
    var partyGameInfoModel: PartyGameInfoModel? = null

    override fun init(parentView: View) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        contentBg = parentView.findViewById(R.id.content_bg)
        gameTv = parentView.findViewById(R.id.game_tv)
        handCardTv = parentView.findViewById(R.id.hand_card_tv)
        ruleTv = parentView.findViewById(R.id.rule_tv)
        textScrollView = parentView.findViewById(R.id.text_scrollView)
        attentionTv = parentView.findViewById(R.id.attention_tv)
        commonTextView = parentView.findViewById(R.id.text_game_tv)
        partyGameTabView = parentView.findViewById(R.id.party_game_tab_view)
        partyGameTabView?.roomData = mRoomData
        partyGameTabView?.iAudioGameListener = object : PartyGameTabView.IAudioGameListener {
            override fun stopPlay() {
                ZqEngineKit.getInstance().stopAudioMixing()
            }

            override fun startPlay(url: String) {
                if (mRoomData.hostId == MyUserInfoManager.uid.toInt()) {
                    ZqEngineKit.getInstance().stopAudioMixing()
                    ZqEngineKit.getInstance().startAudioMixing(url, 1)
                } else {
                    U.getToastUtil().showShort("仅主持人可操作哦～")
                    partyGameTabView?.setAudioPlay(false)
                }
            }
        }

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

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun toGameTab() {
        hideAllView()
        gameTv.isSelected = true
        tagType = TagType.GAME

        partyGameTabView?.visibility = View.VISIBLE
        setGameTabText()
    }

    private fun toHandCardTab() {
        hideAllView()
        handCardTv.isSelected = true
        tagType = TagType.CARD

        textScrollView.visibility = View.VISIBLE
        setHandCardText()
    }

    private fun setHandCardText() {
        partyGameInfoModel?.let {
            if (it.rule?.ruleType == EPGameType.PGT_Play.value) {
                handCardTv.text = " 手卡"
                setMainText("", it.play?.palyInfo?.playCard)
            } else if (it.rule?.ruleType == EPGameType.PGT_Question.value) {
                setMainText("", it.question?.questionInfo?.answerContent)
                handCardTv.text = " 答案"
            }
        }
    }

    private fun setGameTabText() {
        partyGameInfoModel?.let {
            if (it.rule?.ruleType == EPGameType.PGT_Free.value) {
                gameTv.text = " 聊天"
            } else {
                gameTv.text = " 游戏"
            }
        }
    }

    private fun toRuleTab() {
        hideAllView()
        ruleTv.isSelected = true
        tagType = TagType.RULE
        textScrollView.visibility = View.VISIBLE

        partyGameInfoModel?.let {
            setMainText("游戏规则\n", it.rule?.ruleDesc)
        }
    }

    private fun toAttentionTab() {
        hideAllView()
        attentionTv.isSelected = true
        tagType = TagType.ATTENTION
        textScrollView.visibility = View.VISIBLE

        setMainText("房间公告\n", mRoomData?.notice)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyNoticeChangeEvent) {
        if (tagType == TagType.ATTENTION) {
            setMainText("房间公告\n", mRoomData?.notice)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyMyUserInfoChangeEvent) {
        partyGameTabView?.updateIdentity()
        tagChange()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyHostChangeEvent) {
        if (event.hostId > 0) {
            if (mRoomData?.realRoundInfo?.sceneInfo == null) {
                toEmptyState()
            } else {
                partyGameTabView?.updateIdentity()
                tagChange()
            }
        } else {
            toEmptyState()
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_STATE_CHANGE) {
            var state = event.obj as EngineEvent.MusicStateChange
            if (state.isPlayOk && mRoomData.realRoundInfo?.accLoadingOk == false) {
                mRoomData.realRoundInfo?.accLoadingOk = true
                var progress = mRoomData.getSingCurPosition()
                DebugLogView.println(TAG, "伴奏加载ok progress=${progress}")
                if (progress != Long.MAX_VALUE) {
                    if (progress > 0) {
                        DebugLogView.println(TAG, "EngineEvent 超时上车了")
                        ZqEngineKit.getInstance().setAudioMixingPosition(progress.toInt())
                    } else {
                        DebugLogView.println(TAG, "EngineEvent 先暂停 ${-progress}ms后 resume")
                        ZqEngineKit.getInstance().pauseAudioMixing()
                    }
                } else {
                    MyLog.e(TAG, "当前播放进度非法2")
                }
            }
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
            partyGameTabView?.setAudioPlay(false)
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
        partyGameTabView?.visibility = View.GONE
    }

    override fun layoutDesc(): Int {
        return R.layout.party_game_main_view_layout
    }

    //轮次切换的时候调用
    fun updateRound(thisRound: PartyRoundInfoModel?) {
        if (thisRound?.sceneInfo == null) {
            return
        }

        tryInflate()

        this.partyGameInfoModel = thisRound?.sceneInfo!!
        partyGameTabView?.bindData()
        toGameTab()

        setGameTabText()
        setHandCardText()

        tagChange()
    }

    //根据身份调整tag显示和关闭
    fun tagChange() {
        if (mRoomData.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isHost() == true) {
            //主持人
            if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Play.ordinal
                    || partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Question.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.VISIBLE
                ruleTv.visibility = View.VISIBLE
                attentionTv.visibility = View.VISIBLE
            } else if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Free.ordinal
                    || partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.GONE
                ruleTv.visibility = View.GONE
                attentionTv.visibility = View.VISIBLE
            }
        } else {
            //其他人
            if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Play.ordinal
                    || partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Question.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.GONE
                ruleTv.visibility = View.VISIBLE
                attentionTv.visibility = View.VISIBLE
            } else if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Free.ordinal
                    || partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
                gameTv.visibility = View.VISIBLE
                handCardTv.visibility = View.GONE
                ruleTv.visibility = View.GONE
                attentionTv.visibility = View.VISIBLE
            }
        }

        if (mRoomData?.realRoundInfo?.status == EPRoundStatus.PRS_WAITING.value) {
            ruleTv.visibility = View.GONE
            handCardTv.visibility = View.GONE
        }

        if (getCurrentSelectedView().visibility == View.GONE) {
            changeTabByType(TagType.GAME)
        }

    }

    private fun getCurrentSelectedView(): View {
        return when (tagType) {
            TagType.GAME -> gameTv
            TagType.CARD -> handCardTv
            TagType.RULE -> ruleTv
            TagType.ATTENTION -> attentionTv
            else -> gameTv
        }
    }

    private fun changeTabByType(tagType: TagType) {
        when (tagType) {
            TagType.GAME -> toGameTab()
            TagType.CARD -> toHandCardTab()
            TagType.RULE -> toRuleTab()
            TagType.ATTENTION -> toAttentionTab()
            else -> toGameTab()
        }
    }

    fun toEmptyState() {
        gameTv.isSelected = true
        partyGameTabView?.toEmptyState()

        gameTv.visibility = View.VISIBLE
        handCardTv.visibility = View.GONE
        ruleTv.visibility = View.GONE
        attentionTv.visibility = View.VISIBLE
    }

    enum class TagType {
        GAME, CARD, RULE, ATTENTION
    }
}