package com.module.playways.grab.room.view


import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.image.fresco.BaseImageView
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.component.level.utils.LevelConfigUtils
import com.module.playways.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * 进场的动画
 */
class VIPEnterView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "VIPEnterView"

    var normalArea: ConstraintLayout? = null
    var bg: ExImageView? = null
    var vipLevelIv: ExImageView? = null
    var nameTv: ExTextView? = null
    var honorArea: ConstraintLayout? = null
    var honorName: ExTextView? = null
    var honorVipIv: ExImageView? = null
    var honorAvatar: BaseImageView? = null

    var enterJob: Job? = null

    override fun init(parentView: View) {
        parentView?.let {
            it.visibility = View.GONE
            normalArea = parentView.findViewById(R.id.normal_area)
            bg = parentView.findViewById(R.id.bg)
            vipLevelIv = parentView.findViewById(R.id.vip_level_iv)
            nameTv = parentView.findViewById(R.id.name_tv)
            honorArea = parentView.findViewById(R.id.honor_area)
            honorName = parentView.findViewById(R.id.honor_name)
            honorVipIv = parentView.findViewById(R.id.honor_vip_iv)
            honorAvatar = parentView.findViewById(R.id.honor_avatar)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.vip_enter_view_layout
    }

    fun switchRoom() {
        enterJob?.cancel()
        mParentView?.clearAnimation()
    }

    fun enter(playerInfoModel: UserInfoModel, finishCall: (() -> Unit)?) {
        enterJob = launch(Dispatchers.Main) {
            tryInflate()
            normalArea?.visibility = View.GONE
            honorArea?.visibility = View.GONE

            if (playerInfoModel.honorInfo != null && playerInfoModel.honorInfo.isHonor()) {
                //是VIP
                AvatarUtils.loadAvatarByUrl(honorAvatar, AvatarUtils.newParamsBuilder(playerInfoModel.getAvatar())
                        .setCircle(true)
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setBorderColor(Color.WHITE)
                        .build())

                val spanUtils = SpanUtils()
                        .append(UserInfoManager.getInstance().getRemarkName(playerInfoModel.userId, playerInfoModel.nickname)).setForegroundColor(Color.parseColor("#FFCF80"))
                        .append(" 进入了房间").setForegroundColor(U.getColor(R.color.white))
                val stringBuilder = spanUtils.create()
                honorName?.text = stringBuilder

                if (LevelConfigUtils.getRaceCenterAvatarBg(playerInfoModel.ranking.mainRanking) != 0) {
                    honorVipIv?.background = U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(playerInfoModel.ranking.mainRanking))
                } else {
                    honorVipIv?.background = null
                }

                honorArea?.visibility = View.VISIBLE
            } else {
                //不是VIP
                nameTv?.text = UserInfoManager.getInstance().getRemarkName(playerInfoModel.userId, playerInfoModel.nickname)
                if (LevelConfigUtils.getImageResoucesLevel(playerInfoModel.ranking.mainRanking) != 0) {
                    vipLevelIv?.visibility = View.VISIBLE
                    vipLevelIv?.background = U.getDrawable(LevelConfigUtils.getImageResoucesLevel(playerInfoModel.ranking.mainRanking))
                } else {
                    vipLevelIv?.visibility = View.GONE
                }

                normalArea?.visibility = View.VISIBLE
            }

            mParentView?.clearAnimation()
            mParentView?.visibility = View.VISIBLE
            val animationEnter = TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)

            animationEnter.duration = 500
            animationEnter.interpolator = OvershootInterpolator()
            mParentView?.startAnimation(animationEnter)

            delay(1000)

            val animationExit = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                    Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f)
            animationExit.interpolator = LinearInterpolator()
            animationExit.duration = 500
            mParentView?.startAnimation(animationExit)
            delay(500)
            mParentView?.clearAnimation()
            finishCall?.invoke()
            mParentView?.visibility = View.GONE
        }
    }
}

