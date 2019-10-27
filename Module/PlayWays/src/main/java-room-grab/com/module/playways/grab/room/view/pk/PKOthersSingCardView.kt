package com.module.playways.grab.room.view.pk


import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.TranslateAnimation

import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ExViewStub
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.grab.room.view.pk.view.PKSingCardView
import com.module.playways.R
import com.module.playways.room.data.H
import com.zq.live.proto.GrabRoom.EQRoundStatus
import com.zq.live.proto.MicRoom.EMRoundStatus


/**
 * 别人唱歌PK时，自己看到的板子
 */
class PKOthersSingCardView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "PKOthersSingCardView"

    internal var mCountDownStatus = COUNT_DOWN_STATUS_WAIT

    internal var mPkCardView: PKSingCardView? = null
    internal var mSingCountDownView: SingCountDownView2? = null

    internal var mEnterTranslateAnimation: TranslateAnimation? = null // 飞入的进场动画
    internal var mLeaveTranslateAnimation: TranslateAnimation? = null // 飞出的离场动画

    internal var mLeftUserInfoModel: UserInfoModel? = null
    internal var mRightUserInfoModel: UserInfoModel? = null
    internal var mHasPlayFullAnimation = false

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {}
    }

    override fun init(parentView: View) {
        mPkCardView = mParentView!!.findViewById<View>(R.id.pk_card_view) as PKSingCardView
        mSingCountDownView = mParentView!!.findViewById(R.id.sing_count_down_view)
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_pk_other_sing_card_stub_layout
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        destroyAnimation()
    }

    fun bindData() {
        tryInflate()
        mLeftUserInfoModel = null
        mRightUserInfoModel = null
        var firstRound = true
        if (H.isGrabRoom()) {
            val grabRoundInfoModel = H.grabRoomData?.realRoundInfo
            val list = grabRoundInfoModel?.getsPkRoundInfoModels()
            if (list != null && list!!.size >= 2) {
                mLeftUserInfoModel = H.grabRoomData?.getPlayerOrWaiterInfo(list!!.get(0).userID)
                mRightUserInfoModel = H.grabRoomData?.getPlayerOrWaiterInfo(list!!.get(1).userID)
            }
            if (grabRoundInfoModel?.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                firstRound = false
            }
        } else if (H.isMicRoom()) {
            val grabRoundInfoModel = H.micRoomData?.realRoundInfo
            val list = grabRoundInfoModel?.getsPkRoundInfoModels()
            if (list != null && list!!.size >= 2) {
                mLeftUserInfoModel = H.micRoomData?.getPlayerOrWaiterInfo(list!!.get(0).userID)
                mRightUserInfoModel = H.micRoomData?.getPlayerOrWaiterInfo(list!!.get(1).userID)
            }
            if (grabRoundInfoModel?.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
                firstRound = false
            }
        }

        mHasPlayFullAnimation = false
        mUiHandler.removeCallbacksAndMessages(null)
        mParentView!!.visibility = View.VISIBLE
        // 绑定数据
        mPkCardView?.bindData()

        if (firstRound) {
            // pk第一个人唱
            playCardEnterAniamtion()
        } else {
            if (mRightUserInfoModel != null) {
                playIndicateAnimation(mRightUserInfoModel!!.userId)
            }
        }
    }

    private fun playIndicateAnimation(userId: Int) {
        if (H.isGrabRoom()) {
            val infoModel = H.grabRoomData?.realRoundInfo
            val totalMs = infoModel?.singTotalMs
            mSingCountDownView?.startPlay(0, totalMs ?: 0, false)
            if (infoModel?.isParticipant == false && infoModel?.isEnterInSingStatus) {
                // 开始倒计时
                // 直接播放svga 保证声纹动画
                mPkCardView?.playScaleWithoutAnimation(userId)
                mPkCardView?.playSingAnimation(userId)
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                countDown("中途进来")
            } else {
                mPkCardView?.playScaleAnimation(userId, true, object : PKSingCardView.AnimationListerner {
                    override fun onAnimationEndExcludeSvga() {
                        // 开始倒计时
                        mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                        countDown("动画播放完毕")
                    }

                    override fun onAnimationEndWithDraw() {
                        // TODO: 2019/4/26 不会调用
                    }
                })
            }
        } else if (H.isMicRoom()) {
            val infoModel = H.micRoomData?.realRoundInfo
            val totalMs = infoModel?.singTotalMs
            mSingCountDownView?.startPlay(0, totalMs ?: 0, false)
            if (infoModel?.isParticipant == false && infoModel?.isEnterInSingStatus) {
                // 开始倒计时
                // 直接播放svga 保证声纹动画
                mPkCardView?.playScaleWithoutAnimation(userId)
                mPkCardView?.playSingAnimation(userId)
                mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                countDown("中途进来")
            } else {
                mPkCardView?.playScaleAnimation(userId, true, object : PKSingCardView.AnimationListerner {
                    override fun onAnimationEndExcludeSvga() {
                        // 开始倒计时
                        mCountDownStatus = COUNT_DOWN_STATUS_PLAYING
                        countDown("动画播放完毕")
                    }

                    override fun onAnimationEndWithDraw() {
                        // TODO: 2019/4/26 不会调用
                    }
                })
            }
        }

    }

    // pk 他人的为什么有动画
    private fun playCardEnterAniamtion() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation((-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f, 0.0f)
            mEnterTranslateAnimation!!.duration = 200
        }
        mEnterTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                // TODO: 2019/4/23 先播放左边的动画，后面都是一体的
                if (mLeftUserInfoModel != null) {
                    playIndicateAnimation(mLeftUserInfoModel!!.userId)
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        if (mParentView != null) {
            mParentView!!.startAnimation(mEnterTranslateAnimation)
        }
    }

    /**
     * 离场动画
     */
    fun hide() {
        if (mParentView != null) {
            if (mParentView!!.visibility == View.VISIBLE) {
                if (mLeaveTranslateAnimation == null) {
                    mLeaveTranslateAnimation = TranslateAnimation(0.0f, U.getDisplayUtils().screenWidth.toFloat(), 0.0f, 0.0f)
                    mLeaveTranslateAnimation!!.duration = 200
                }
                mLeaveTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        destroyAnimation()
                        mSingCountDownView?.reset()
                        mParentView!!.clearAnimation()
                        mParentView!!.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                mParentView!!.startAnimation(mLeaveTranslateAnimation)
            } else {
                destroyAnimation()
                mSingCountDownView?.reset()
                mParentView!!.clearAnimation()
                mParentView!!.visibility = View.GONE
            }
        }
    }

    private fun countDown(from: String) {
        MyLog.d(TAG, "countDown from=$from")
        if (H.isGrabRoom()) {
            val infoModel = H.grabRoomData?.realRoundInfo
            val totalMs = infoModel?.singTotalMs ?: 20 * 1000
            val progress: Int  //当前进度条
            val leaveTime: Int //剩余时间
            MyLog.d(TAG, "countDown isParticipant:" + infoModel?.isParticipant + " enterStatus=" + infoModel?.enterStatus)
            if (infoModel?.isParticipant == false && infoModel.status == infoModel.enterStatus) {
                MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
                progress = infoModel.elapsedTimeMs * 100 / totalMs
                leaveTime = totalMs - infoModel.elapsedTimeMs
            } else {
                progress = 1
                leaveTime = totalMs
            }
            mSingCountDownView?.startPlay(progress, leaveTime, true)
        } else if (H.isMicRoom()) {
                val infoModel = H.micRoomData?.realRoundInfo
                val totalMs = infoModel?.singTotalMs ?: 20 * 1000
                val progress: Int  //当前进度条
                val leaveTime: Int //剩余时间
                MyLog.d(TAG, "countDown isParticipant:" + infoModel?.isParticipant + " enterStatus=" + infoModel?.enterStatus)
                if (infoModel?.isParticipant == false && infoModel.status == infoModel.enterStatus) {
                    MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
                    progress = infoModel.elapsedTimeMs * 100 / totalMs
                    leaveTime = totalMs - infoModel.elapsedTimeMs
                } else {
                    progress = 1
                    leaveTime = totalMs
                }
                mSingCountDownView?.startPlay(progress, leaveTime, true)
            }
    }

    private fun destroyAnimation() {
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation!!.setAnimationListener(null)
            mEnterTranslateAnimation!!.cancel()
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation!!.setAnimationListener(null)
            mLeaveTranslateAnimation!!.cancel()
        }
    }

    companion object {

        internal val COUNT_DOWN_STATUS_WAIT = 2
        internal val COUNT_DOWN_STATUS_PLAYING = 3
    }
}
