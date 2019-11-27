package com.module.playways.grab.room.view.pk

import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView
import com.module.playways.room.data.H
import com.zq.live.proto.GrabRoom.EQRoundStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PKSelfSingCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

    val TAG = "PKSelfSingCardView"

    internal var mPkSelfSingLyricView: SelfSingLyricView? = null
//    internal var mPkSingCardView: PKSingCardView? = null
//    internal var mSingCountDownView: SingCountDownView2? = null

    internal var mLeftUserInfoModel: UserInfoModel? = null
    internal var mRightUserInfoModel: UserInfoModel? = null

    internal var mEnterTranslateAnimation: TranslateAnimation? = null // 飞入的进场动画
    //    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    var mOverListener: (() -> Unit)? = null

    override fun init(parentView: View) {
        val viewStub = mParentView!!.findViewById<ViewStub>(R.id.pk_self_sing_lyric_view_stub)
        mPkSelfSingLyricView = SelfSingLyricView(viewStub)
//        mPkSingCardView = mParentView!!.findViewById(R.id.pk_sing_card_view)
//        mSingCountDownView = mParentView!!.findViewById(R.id.sing_count_down_view)
//        mSingCountDownView!!.setListener(mOverListener)
//        mParentView!!.findViewById<View>(R.id.iv_bg).setDebounceViewClickListener { }
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_pk_self_sing_card_stub_layout
    }

    fun playLyric() {
        tryInflate()
        mLeftUserInfoModel = null
        mRightUserInfoModel = null

        var firstRound = true

        if (H.isGrabRoom()) {
            val grabRoundInfoModel = H.grabRoomData!!.realRoundInfo ?: return
            val list = grabRoundInfoModel.getsPkRoundInfoModels()
            if (list != null && list.size >= 2) {
                mLeftUserInfoModel = H.grabRoomData!!.getPlayerOrWaiterInfo(list[0].userID)
                mRightUserInfoModel = H.grabRoomData!!.getPlayerOrWaiterInfo(list[1].userID)
            }
            if (grabRoundInfoModel.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                firstRound = false
            }
        } else if (H.isMicRoom()) {
            val grabRoundInfoModel = H.micRoomData!!.realRoundInfo ?: return
            val list = grabRoundInfoModel.getsPkRoundInfoModels()
            if (list != null && list.size >= 2) {
                mLeftUserInfoModel = H.micRoomData!!.getPlayerOrWaiterInfo(list[0].userID)
                mRightUserInfoModel = H.micRoomData!!.getPlayerOrWaiterInfo(list[1].userID)
            }
            if (grabRoundInfoModel.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                firstRound = false
            }
        }

        setVisibility(View.VISIBLE)
        // 绑定数据
//        mPkSingCardView!!.bindData()
        if (firstRound) {
            // pk第一个人唱
//            mPkSingCardView!!.visibility = View.VISIBLE
            playCardEnterAnimation()
        } else {
//            mPkSingCardView!!.visibility = View.VISIBLE
            if (mRightUserInfoModel != null) {
                playIndicateAnimation(mRightUserInfoModel!!.userId)
            }
        }
    }

    /**
     * 播放指示谁唱的animation
     *
     * @param userId
     */
    private fun playIndicateAnimation(userId: Int) {
//        if (H.isGrabRoom()) {
//            mSingCountDownView!!.startPlay(0, H.grabRoomData?.realRoundInfo?.singTotalMs
//                    ?: 0, false)
//        } else if (H.isMicRoom()) {
//            mSingCountDownView!!.startPlay(0, H.micRoomData?.realRoundInfo?.singTotalMs ?: 0, false)
//        }

//        mPkSingCardView!!.playScaleAnimation(userId, false, object : PKSingCardView.AnimationListerner {
//            override fun onAnimationEndExcludeSvga() {
////                mPkSingCardView!!.playWithDraw()
//                playRealLyric()
//            }
//
//            override fun onAnimationEndWithDraw() {
//
//            }
//        })

        launch {
            delay(1000)
            playRealLyric()
        }
    }

    private fun playRealLyric() {
        if (H.isGrabRoom()) {
            mPkSelfSingLyricView!!.playWithAcc(H.getSongModel(), H.grabRoomData?.realRoundInfo?.singTotalMs
                    ?: 0)
//            mSingCountDownView!!.startPlay(0, H.grabRoomData?.realRoundInfo?.singTotalMs ?: 0, true)
        } else if (H.isMicRoom()) {
            mPkSelfSingLyricView!!.playWithAcc(H.getSongModel(), H.micRoomData?.realRoundInfo?.singTotalMs
                    ?: 0)
//            mSingCountDownView!!.startPlay(0, H.micRoomData?.realRoundInfo?.singTotalMs ?: 0, true)
        }
    }

    /**
     * 入场动画
     */
    private fun playCardEnterAnimation() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation((-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f, 0.0f)
            mEnterTranslateAnimation!!.duration = 200
        }
        mEnterTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                if (mLeftUserInfoModel != null) {
                    playIndicateAnimation(mLeftUserInfoModel!!.userId)
                }
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        mParentView!!.startAnimation(mEnterTranslateAnimation)
    }

    //
    //    /**
    //     * 离场动画，整个pk结束才执行
    //     */
    //    public void hide() {
    //        if (this != null && this.getVisibility() == VISIBLE) {
    //            if (mLeaveTranslateAnimation == null) {
    //                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
    //                mLeaveTranslateAnimation.setDuration(200);
    //            }
    //            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
    //                @Override
    //                public void onAnimationStart(Animation animation) {
    //
    //                }
    //
    //                @Override
    //                public void onAnimationEnd(Animation animation) {
    //                    mSingCountDownView.reset();
    //                    clearAnimation();
    //                    setVisibility(GONE);
    //                }
    //
    //                @Override
    //                public void onAnimationRepeat(Animation animation) {
    //
    //                }
    //            });
    //            this.startAnimation(mLeaveTranslateAnimation);
    //        } else {
    //            mSingCountDownView.reset();
    //            clearAnimation();
    //            setVisibility(GONE);
    //        }
    //        mPkSelfSingLyricView.reset();
    //    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
//            if (mSingCountDownView != null) {
//                mSingCountDownView!!.reset()
//            }
            if (mPkSelfSingLyricView != null) {
                mPkSelfSingLyricView!!.reset()
            }
//            if (mPkSingCardView != null) {
//                mPkSingCardView!!.reset()
//            }
        }
    }

    fun destroy() {
        if (mPkSelfSingLyricView != null) {
            mPkSelfSingLyricView!!.destroy()
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation!!.setAnimationListener(null)
            mEnterTranslateAnimation!!.cancel()
        }

        //        if (mLeaveTranslateAnimation != null) {
        //            mLeaveTranslateAnimation.setAnimationListener(null);
        //            mLeaveTranslateAnimation.cancel();
        //        }
    }

}
