package com.module.playways.battle.room.view

import android.graphics.Color
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewStub
import com.common.core.avatar.AvatarUtils
import com.common.image.fresco.BaseImageView
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.grab.room.view.SingCountDownView2

class BattleOtherSingCardView(viewStub: ViewStub, protected var mRoomData: BattleRoomData) : BaseSceneView(viewStub) {
    lateinit var singCountDownView: SingCountDownView2
    lateinit var cardIv: ExImageView
    lateinit var singerAvatarIv: BaseImageView
    lateinit var songNameTv: ExTextView
    lateinit var singerInfoTv: ExTextView

    override fun init(parentView: View) {
        singCountDownView = parentView.findViewById(R.id.sing_count_down_view)
        cardIv = parentView.findViewById(R.id.card_iv)
        singerAvatarIv = parentView.findViewById(R.id.singer_avatar_iv)
        songNameTv = parentView.findViewById(R.id.item_name_tv)
        singerInfoTv = parentView.findViewById(R.id.singer_info_tv)
    }

    fun show() {
        var battleRoundInfoModel = mRoomData?.realRoundInfo
        if (battleRoundInfoModel == null) {
            battleRoundInfoModel = mRoomData?.expectRoundInfo
        }

        battleRoundInfoModel?.let {
            enterAnimation()
            songNameTv.text = "《${it.music?.itemName}》"

            var messageTips: SpannableStringBuilder? = null
            if (mRoomData.getFirstTeammate()?.userID == it.userID) {
                //队友
                messageTips = SpanUtils().append("队友 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                        .append(mRoomData.getPlayerInfoById(it.userID)?.userInfo?.nicknameRemark
                                ?: "").setForegroundColor(U.getColor(R.color.white))
                        .append("演唱中...").setForegroundColor(U.getColor(R.color.white_trans_50))
                        .create()
            } else {
                //对手
                messageTips = SpanUtils().append("对手 ").setForegroundColor(U.getColor(R.color.white_trans_50))
                        .append(mRoomData.getPlayerInfoById(it.userID)?.userInfo?.nicknameRemark
                                ?: "").setForegroundColor(U.getColor(R.color.white))
                        .append("演唱中...").setForegroundColor(U.getColor(R.color.white_trans_50))
                        .create()
            }

            singerInfoTv.text = messageTips

            val totalMs = it.music?.totalMs ?: 0
            singCountDownView.startPlay(0, totalMs, true)

            AvatarUtils.loadAvatarByUrl(singerAvatarIv, AvatarUtils.newParamsBuilder(mRoomData.getPlayerInfoById(it.userID)?.userInfo?.avatar)
                    .setCircle(true)
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(1.dp().toFloat())
                    .build())
        }
    }

    fun hide() {
        leaveAnimation()
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_other_sing_view_layout
    }
}