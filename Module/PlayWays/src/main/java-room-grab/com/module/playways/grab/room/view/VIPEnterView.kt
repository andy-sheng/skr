package com.module.playways.grab.room.view


import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.view.animation.TranslateAnimation
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * 转场时的歌曲信息页
 */
class VIPEnterView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "VIPEnterView"

    var vipLevelIv: ExImageView? = null
    var nameTv: ExTextView? = null

    var enterJob: Job? = null

    override fun init(parentView: View) {
        parentView?.let {
            it.visibility = View.GONE
            vipLevelIv = parentView.findViewById(R.id.vip_level_iv)
            nameTv = parentView.findViewById(R.id.name_tv)
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
            nameTv?.text = UserInfoManager.getInstance().getRemarkName(playerInfoModel.userId, playerInfoModel.nickname)
            when (playerInfoModel.mainLevel) {
                //TODO 头像设置
//                vipLevelIv
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

