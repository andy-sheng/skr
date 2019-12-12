package com.module.playways.party.room.view

import android.content.Context
import android.graphics.Color
import android.support.constraint.Group
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ex.ExConstraintLayout
import com.common.view.ex.ExTextView
import com.component.lyrics.utils.SongResUtils
import com.module.playways.R
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.event.PartySelectSongEvent
import com.module.playways.party.room.model.PartyGameInfoModel
import com.module.playways.room.data.H
import com.zq.live.proto.PartyRoom.EPGameType
import com.zq.mediaengine.kit.ZqEngineKit
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

class PartyGameTabView : ExConstraintLayout {
    val mTag = "PartyGameTabView"
    var partySelfSingLyricLayoutViewStub: ViewStub
    var gamePicImg: BaseImageView
    var textScrollView: ScrollView
    var textGameTv: ExTextView
    var bottomLeftOpTv: ExTextView
    var bottomRightOpTv: ExTextView
    var avatarIv: BaseImageView
    var singingTv: ExTextView
    var selectSongTv: ExTextView
    var singingGroup: Group

    var partySelfSingLyricView: PartySelfSingLyricView? = null

    val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    var roomData: PartyRoomData? = null
        set(value) {
            field = value
            partySelfSingLyricView = PartySelfSingLyricView(partySelfSingLyricLayoutViewStub, roomData!!)
        }

    var partyGameInfoModel: PartyGameInfoModel? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)


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
        selectSongTv = rootView.findViewById(R.id.select_song_tv)
        singingGroup = rootView.findViewById(R.id.singing_group)
        bottomLeftOpTv.text = "切游戏"
        bottomRightOpTv.text = "下一题"

        bottomLeftOpTv.setDebounceViewClickListener {
            endRound()
        }

        bottomRightOpTv.setDebounceViewClickListener {
            endQuestion()
        }

        selectSongTv.setDebounceViewClickListener {
            EventBus.getDefault().post(PartySelectSongEvent())
        }
    }

    fun endQuestion() {
        partyGameInfoModel?.let {
            launch {
                val map = mutableMapOf(
                        "roomID" to H.partyRoomData?.gameId,
                        "roundSeq" to H.partyRoomData?.realRoundSeq
                )

                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe(RequestControl("${mTag} endQuestion", ControlType.CancelThis)) {
                    if (it.rule?.ruleType == EPGameType.PGT_KTV.value) {
                        roomServerApi.endMusic(body)
                    } else {
                        roomServerApi.endQuestion(body)
                    }
                }

                if (result.errno == 0) {

                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }
        }
    }

    fun endRound() {
        launch {
            val map = mutableMapOf(
                    "roomID" to H.partyRoomData?.gameId,
                    "roundSeq" to H.partyRoomData?.realRoundSeq
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("${mTag} endRound", ControlType.CancelThis)) {
                roomServerApi.endRound(body)
            }

            if (result.errno == 0) {

            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    //身份更新
    fun updateIdentity() {
        singingGroup.visibility = View.GONE
        selectSongTv.visibility = View.GONE
        bottomLeftOpTv.visibility = View.GONE
        bottomRightOpTv.visibility = View.GONE

        if (roomData?.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isHost() == true) {
            //主持人
            bottomLeftOpTv.visibility = View.VISIBLE

            partyGameInfoModel?.let {
                if (it.rule?.ruleType == EPGameType.PGT_Play.ordinal) {
                    bottomRightOpTv.visibility = View.GONE
                } else if (it.rule?.ruleType == EPGameType.PGT_Question.ordinal) {
                    if (it.question?.hasNextquestion == true) {
                        bottomRightOpTv.visibility = View.VISIBLE
                        bottomRightOpTv.text = "下一题"
                    } else {
                        bottomRightOpTv.visibility = View.GONE
                    }
                } else if (it.rule?.ruleType == EPGameType.PGT_Free.ordinal) {
                    bottomRightOpTv.visibility = View.GONE
                } else if (it.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
                    //不管有没有歌都得可以切歌
                    if ((it.ktv?.userID ?: 0) > 0) {
                        bottomRightOpTv.text = "切歌"
                        bottomRightOpTv.visibility = View.VISIBLE
                    } else {
                        bottomRightOpTv.text = ""
                        bottomRightOpTv.visibility = View.GONE
                    }
                }
            }
        } else {
            //其他
            partyGameInfoModel?.let {
                if (it.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
                    if (it.ktv?.userID ?: 0 > 0) {
                        if (it.ktv?.userID == MyUserInfoManager.uid.toInt()) {
                            bottomRightOpTv.visibility = View.VISIBLE
                            bottomRightOpTv.text = "切歌"
                        }

                        singingGroup.visibility = View.VISIBLE
                        AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(roomData?.getPlayerInfoById(it.ktv?.userID
                                ?: 0)?.userInfo?.avatar)
                                .setBorderWidth(U.getDisplayUtils().dip2px(1f).toFloat())
                                .setBorderColor(U.getColor(R.color.white))
                                .setCircle(true)
                                .build())
                    } else {
                        singingGroup.visibility = View.GONE
                    }
                } else {
                    singingGroup.visibility = View.GONE
                }
            }
        }

        partyGameInfoModel?.let {
            if (it.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
                if (roomData?.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isGuest() == true
                        || roomData?.getPlayerInfoById(MyUserInfoManager.uid.toInt())?.isHost() == true) {
                    selectSongTv.visibility = View.VISIBLE
                } else {
                    selectSongTv.visibility = View.GONE
                }
            } else {
                selectSongTv.visibility = View.GONE
            }
        }
    }

    //题目更新，只有换轮次的时候调用
    fun bindData() {
        if (roomData?.realRoundInfo?.sceneInfo == null) {
            return
        }

        ZqEngineKit.getInstance().stopAudioMixing()

        partySelfSingLyricView?.reset()

        partyGameInfoModel = roomData?.realRoundInfo?.sceneInfo

        hideAllTypeView()

        updateIdentity()

        if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Play.ordinal
                || partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Question.ordinal) {
            textScrollView.visibility = View.VISIBLE

            setMainText(getGameTagTitle(), getGameTagContent())
            partyGameInfoModel?.let {
                if (it.rule?.ruleType == EPGameType.PGT_Question.value && (it.question?.questionInfo?.questionPic?.size
                                ?: 0) > 0) {
                    gamePicImg.visibility = View.VISIBLE

                    AvatarUtils.loadAvatarByUrl(gamePicImg, AvatarUtils.newParamsBuilder(it.question?.questionInfo?.questionPic?.get(0))
                            .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setBorderColor(Color.WHITE)
                            .build())
                } else {
                    gamePicImg.visibility = View.GONE
                }
            }
        } else if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_Free.ordinal) {
            textScrollView.visibility = View.VISIBLE
            setMainText("", "自由麦模式，大家畅所欲言吧～")
        } else if (partyGameInfoModel?.rule?.ruleType == EPGameType.PGT_KTV.ordinal) {
            if (partyGameInfoModel?.ktv?.userID ?: 0 > 0) {
                textScrollView.visibility = View.GONE
                partySelfSingLyricView?.setVisibility(View.VISIBLE)
                if (partyGameInfoModel?.ktv?.userID == MyUserInfoManager.uid.toInt()) {
                    partySelfSingLyricView?.startFly(true) {
                        MyLog.d(mTag, "partySelfSingLyricView?.startFly end")
                        endQuestion()
                        ZqEngineKit.getInstance().stopAudioMixing()
                    }

                    val songModel = partyGameInfoModel?.ktv?.music
                    // 开始开始混伴奏，开始解除引擎mute
                    val accFile = SongResUtils.getAccFileByUrl(songModel?.acc)
                    // midi不需要在这下，只要下好，native就会解析，打分就能恢复
                    val midiFile = SongResUtils.getMIDIFileByUrl(songModel?.midi)

                    val songBeginTs = songModel?.beginMs ?: 0
                    if (accFile != null && accFile.exists()) {
                        // 伴奏文件存在
                        ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), accFile.absolutePath, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
                    } else {
                        ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), songModel?.acc, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
                    }
                } else {
                    partySelfSingLyricView?.startFly(false) {

                    }
                }
            } else {
                //还没开始
                partySelfSingLyricView?.setVisibility(View.GONE)
                textScrollView.visibility = View.VISIBLE
                setMainText("", "还没有歌曲，大家快去点歌吧～")
            }
        }
    }

    private fun hideAllTypeView() {
        partySelfSingLyricView?.setVisibility(View.GONE)
        bottomLeftOpTv.visibility = View.GONE
        gamePicImg.visibility = View.GONE
        textScrollView.visibility = View.GONE
        selectSongTv.visibility = View.GONE
        bottomRightOpTv.visibility = View.GONE
        avatarIv.visibility = View.GONE
        singingGroup.visibility = View.GONE
        bottomLeftOpTv.visibility = View.GONE
        bottomRightOpTv.visibility = View.GONE
    }

    private fun getGameTagTitle(): String {
        var gameTagTitle = ""
        partyGameInfoModel?.let {
            if (it.rule?.ruleType == EPGameType.PGT_Play.value) {
                gameTagTitle = it.play?.palyInfo?.playName ?: ""
            } else if (it.rule?.ruleType == EPGameType.PGT_Question.value) {
                gameTagTitle = ""
            }
        }

        return if (TextUtils.isEmpty(gameTagTitle)) "" else "$gameTagTitle\n"
    }

    private fun getGameTagContent(): String {
        partyGameInfoModel?.let {
            if (it.rule?.ruleType == EPGameType.PGT_Play.value) {
                return it.play?.palyInfo?.playContent ?: ""
            } else if (it.rule?.ruleType == EPGameType.PGT_Question.value) {
                return it.question?.questionInfo?.questionContent ?: ""
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
        hideAllTypeView()
        textScrollView.visibility = View.VISIBLE
        setMainText("", "房主还没有添加游戏，先聊聊天吧～")
        ZqEngineKit.getInstance().stopAudioMixing()
        partySelfSingLyricView?.reset()
    }
}