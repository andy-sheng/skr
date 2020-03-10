package com.module.playways.battle.room.view

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import com.alibaba.fastjson.JSON
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.image.fresco.BaseImageView
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.lyrics.LyricsManager
import com.component.person.event.ShowPersonCardEvent
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.grab.room.model.NewChorusLyricModel
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class BattleSongGuideView(viewStub: ViewStub, protected var mRoomData: BattleRoomData) : BaseSceneView(viewStub) {
    val mTag = "BattleSongGuideView"
    var titleBg: ImageView? = null
    var leftFirstAvatar: BaseImageView? = null
    var leftSecondAvatar: BaseImageView? = null
    var rightFirstAvatar: BaseImageView? = null
    var rightSecondAvatar: BaseImageView? = null
    var songNameTv: ExTextView? = null
    var songLyricTv: ExTextView? = null
    var singerInfoTv: ExTextView? = null
    var leftWinNumTv: ExTextView? = null
    var rightWinNumTv: ExTextView? = null

    override fun init(parentView: View) {
        titleBg = parentView.findViewById(R.id.title_bg)
        leftFirstAvatar = parentView.findViewById(R.id.left_first_avatar)
        leftSecondAvatar = parentView.findViewById(R.id.left_second_avatar)
        rightFirstAvatar = parentView.findViewById(R.id.right_first_avatar)
        rightSecondAvatar = parentView.findViewById(R.id.right_second_avatar)

        leftFirstAvatar?.setOnClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.myTeamInfo[0].userID))
        }
        leftSecondAvatar?.setOnClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.myTeamInfo[1].userID))
        }
        rightFirstAvatar?.setOnClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.opTeamInfo[0].userID))
        }
        rightSecondAvatar?.setOnClickListener {
            EventBus.getDefault().post(ShowPersonCardEvent(mRoomData.opTeamInfo[1].userID))
        }

        songNameTv = parentView.findViewById(R.id.song_name_tv)
        songLyricTv = parentView.findViewById(R.id.song_lyric_tv)
        singerInfoTv = parentView.findViewById(R.id.singer_info_tv)
        leftWinNumTv = parentView.findViewById(R.id.left_win_num_tv)
        rightWinNumTv = parentView.findViewById(R.id.right_win_num_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_song_guide_view_layout
    }

    var countDownJop: Job? = null

    fun show() {
        setVisibility(View.VISIBLE)
        var battleRoundInfoModel = mRoomData?.realRoundInfo
        leftWinNumTv?.text = "${mRoomData.myTeamScore}"
        rightWinNumTv?.text = "${mRoomData.opTeamScore}"
        battleRoundInfoModel?.let {
            enterAnimation()
            loadAvatar(leftFirstAvatar!!, mRoomData.myTeamInfo[0].userInfo.avatar)
            loadAvatar(leftSecondAvatar!!, mRoomData.myTeamInfo[1].userInfo.avatar)
            loadAvatar(rightFirstAvatar!!, mRoomData.opTeamInfo[0].userInfo.avatar)
            loadAvatar(rightSecondAvatar!!, mRoomData.opTeamInfo[1].userInfo.avatar)

            songNameTv?.text = "《${it.music?.itemName}》"

            LyricsManager
                    .loadGrabPlainLyric(it.music?.standLrc)
                    .subscribe(Consumer<String> { o ->
                        songLyricTv?.text = ""
                        if (U.getStringUtils().isJSON(o)) {
                            val newChorusLyricModel = JSON.parseObject(o, NewChorusLyricModel::class.java)
                            var i = 0
                            while (i < newChorusLyricModel.items.size && i < 2) {
                                songLyricTv?.append(newChorusLyricModel.items[i].words)
                                if (i == 0) {
                                    songLyricTv?.append("，")
                                }
                                i++
                            }
                        } else {
                            songLyricTv?.text = o.replace("\n", "，")
                        }
                    }, Consumer<Throwable> { throwable -> MyLog.e(mTag, throwable) })

            countDownJop = launch {
                var countDownTime = (it.waitEndMs - it.waitBeginMs)/1000
                for (i in 0..countDownTime) {
                    var t = countDownTime - i
                    var messageTips: SpannableStringBuilder? = null
                    if (it.userID == MyUserInfoManager.uid.toInt()) {
                        messageTips = SpanUtils().append("轮到 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                                .append("你 ").setForegroundColor(U.getColor(R.color.white))
                                .append("演唱 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                                .append("${t}s ").setForegroundColor(U.getColor(R.color.white))
                                .create()
                    } else {
                        if (mRoomData.getFirstTeammate()?.userID == it.userID) {
                            //队友
                            messageTips = SpanUtils().append("轮到队友 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                                    .append(mRoomData.getPlayerInfoById(it.userID)?.userInfo?.nicknameRemark
                                            ?: "").setForegroundColor(U.getColor(R.color.white))
                                    .append("演唱 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                                    .append("${t}s ").setForegroundColor(U.getColor(R.color.white))
                                    .create()
                        } else {
                            //对手
                            messageTips = SpanUtils().append("轮到对手 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                                    .append(mRoomData.getPlayerInfoById(it.userID)?.userInfo?.nicknameRemark
                                            ?: "").setForegroundColor(U.getColor(R.color.white))
                                    .append("演唱 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                                    .append("${t}s ").setForegroundColor(U.getColor(R.color.white))
                                    .create()
                        }
                    }
                    singerInfoTv?.text = messageTips
                    delay(1000)
                }
            }
        }
    }

    private fun loadAvatar(iv: BaseImageView, avatar: String) {
        AvatarUtils.loadAvatarByUrl(iv, AvatarUtils.newParamsBuilder(avatar)
                .setCircle(true)
                .setBorderColor(Color.WHITE)
                .setBorderWidth(1.dp().toFloat())
                .build())
    }

    fun hide() {
        countDownJop?.cancel()
        mParentView?.clearAnimation()
        setVisibility(View.GONE)
    }
}