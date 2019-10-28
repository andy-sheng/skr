package com.module.playways.grab.room.view.pk

import android.os.Handler
import android.view.View
import android.view.ViewStub
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.common.anim.svga.SvgaParserAdapter
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.component.busilib.view.BitmapTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.listener.SVGAListener
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.model.SPkRoundInfoModel
import com.common.view.ExViewStub
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.module.playways.room.data.H
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.zq.live.proto.GrabRoom.EQRoundOverReason


/**
 * PK 结果卡片
 */
class PKRoundOverCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

    val TAG = "PKRoundOverCardView"

    internal var mPkArea: LinearLayout? = null
    internal var mLeftAvatarBg: ImageView? = null
    internal var mLeftAvatarIv: SimpleDraweeView? = null
    internal var mLeftName: ExTextView? = null
    internal var mLeftOverReasonIv: ImageView? = null

    internal var mRightAvatarBg: ImageView? = null
    internal var mRightAvatarIv: SimpleDraweeView? = null
    internal var mRightName: ExTextView? = null
    internal var mRightOverReasonIv: ImageView? = null

    internal var mSroreArea: LinearLayout? = null
    internal var mLeftTipsTv: TextView? = null
    internal var mLeftScoreBtv: BitmapTextView? = null
    internal var mRightScoreBtv: BitmapTextView? = null
    internal var mRightTipsTv: TextView? = null

    internal var mLeftWinIv: ImageView? = null
    internal var mRightWinIv: ImageView? = null
    internal var mLeftSvga: SVGAImageView? = null
    internal var mRightSvga: SVGAImageView? = null

    internal var mUiHandler = Handler()

    internal var mEnterTranslateAnimation: TranslateAnimation? = null // 飞入的进场动画
    internal var mLeaveTranslateAnimation: TranslateAnimation? = null // 飞出的离场动画

    internal var mLeftUserInfoModel: UserInfoModel? = null
    internal var mRightUserInfoModel: UserInfoModel? = null
    internal var mLeftOverReason: Int = 0
    internal var mRightOverReason: Int = 0
    internal var mLeftScore: String? = null
    internal var mRightScore: String? = null
    internal var mLeftWin = false     // 标记左边是否胜利
    internal var mRightWin = false    // 标记右边是否胜利
    internal var mSVGAListener: SVGAListener? = null

    override fun init(parentView: View) {
        mPkArea = parentView.findViewById<View>(R.id.pk_area) as LinearLayout
        mLeftAvatarBg = parentView.findViewById<View>(R.id.left_avatar_bg) as ImageView
        mLeftAvatarIv = parentView.findViewById<View>(R.id.left_avatar_iv) as SimpleDraweeView
        mLeftName = parentView.findViewById<View>(R.id.left_name) as ExTextView
        mLeftOverReasonIv = parentView.findViewById<View>(R.id.left_over_reason_iv) as ImageView
        mRightAvatarBg = parentView.findViewById<View>(R.id.right_avatar_bg) as ImageView
        mRightAvatarIv = parentView.findViewById<View>(R.id.right_avatar_iv) as SimpleDraweeView
        mRightName = parentView.findViewById<View>(R.id.right_name) as ExTextView
        mRightOverReasonIv = parentView.findViewById<View>(R.id.right_over_reason_iv) as ImageView
        mSroreArea = parentView.findViewById<View>(R.id.srore_area) as LinearLayout
        mLeftTipsTv = parentView.findViewById<View>(R.id.left_tips_tv) as TextView
        mLeftScoreBtv = parentView.findViewById<View>(R.id.left_score_btv) as BitmapTextView
        mRightScoreBtv = parentView.findViewById<View>(R.id.right_score_btv) as BitmapTextView
        mRightTipsTv = parentView.findViewById<View>(R.id.right_tips_tv) as TextView

        mLeftWinIv = parentView.findViewById<View>(R.id.left_win_iv) as ImageView
        mRightWinIv = parentView.findViewById<View>(R.id.right_win_iv) as ImageView
        mLeftSvga = parentView.findViewById<View>(R.id.left_svga) as SVGAImageView
        mRightSvga = parentView.findViewById<View>(R.id.right_svga) as SVGAImageView
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_pk_round_over_card_stub_layout
    }

    fun bindData(roundInfoModel: GrabRoundInfoModel?, svgaListener: SVGAListener) {
        tryInflate()
        reset()
        this.mSVGAListener = svgaListener
        val list = roundInfoModel?.getsPkRoundInfoModels()
        if (list != null && list.size >= 2) {
            mLeftUserInfoModel = H.grabRoomData?.getPlayerOrWaiterInfo(list[0].userID)
            this.mLeftScore = String.format("%.1f", list[0].score)
            this.mLeftOverReason = list[0].overReason
            this.mLeftWin = list[0].isWin

            mRightUserInfoModel = H.grabRoomData?.getPlayerOrWaiterInfo(list[1].userID)
            this.mRightScore = String.format("%.1f", list[1].score)
            this.mRightOverReason = list[1].overReason
            this.mRightWin = list[1].isWin
        }
        bindInner()
    }

    fun bindData(roundInfoModel: MicRoundInfoModel?, svgaListener: SVGAListener) {
        tryInflate()
        reset()
        this.mSVGAListener = svgaListener
        val list = roundInfoModel?.getsPkRoundInfoModels()
        if (list != null && list.size >= 2) {
            mLeftUserInfoModel = H.micRoomData?.getPlayerOrWaiterInfo(list[0].userID)
            this.mLeftScore = String.format("%.1f", list[0].score)
            this.mLeftOverReason = list[0].overReason
            this.mLeftWin = list[0].isWin

            mRightUserInfoModel = H.micRoomData?.getPlayerOrWaiterInfo(list[1].userID)
            this.mRightScore = String.format("%.1f", list[1].score)
            this.mRightOverReason = list[1].overReason
            this.mRightWin = list[1].isWin
        }
        bindInner()
    }

    private fun bindInner() {
        if (mLeftUserInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mLeftAvatarIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel!!.avatar)
                            .setCircle(true)
                            .build())
            mLeftName?.text = mLeftUserInfoModel!!.nicknameRemark
            mLeftScoreBtv?.setText(mLeftScore)
            showOverReason(mLeftOverReason, mLeftOverReasonIv, mLeftScoreBtv, mLeftTipsTv)
        }

        if (mRightUserInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mRightAvatarIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel!!.avatar)
                            .setCircle(true)
                            .build())
            mRightName?.text = mRightUserInfoModel!!.nicknameRemark
            mRightScoreBtv?.setText(mRightScore)
            showOverReason(mRightOverReason, mRightOverReasonIv, mRightScoreBtv, mRightTipsTv)
        }

        playCardEnterAnimation()
    }

    private fun reset() {
        mLeftUserInfoModel = null
        mRightUserInfoModel = null
        mLeftWinIv?.visibility = View.GONE
        mRightWinIv?.visibility = View.GONE
        if (mLeftSvga != null) {
            mLeftSvga!!.callback = null
            mLeftSvga!!.stopAnimation(true)
        }
        if (mRightSvga != null) {
            mRightSvga!!.callback = null
            mRightSvga!!.stopAnimation(true)
        }
        mLeftWin = false
        mRightWin = false
        mLeftScore = "0.0"
        mRightScore = "0.0"
    }

    private fun showOverReason(overReason: Int, overReasonIv: ImageView?, bitmapTextView: BitmapTextView?, scoreTv: TextView?) {
        if (overReasonIv == null) {
            return
        }
        if (overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
            bitmapTextView?.visibility = View.GONE
            scoreTv?.visibility = View.GONE
            overReasonIv.visibility = View.VISIBLE
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_buchangle)
        } else if (overReason == EQRoundOverReason.ROR_MULTI_NO_PASS.value) {
            bitmapTextView?.visibility = View.GONE
            scoreTv?.visibility = View.GONE
            overReasonIv.visibility = View.VISIBLE
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_miedeng)
        } else if (overReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.value) {
            bitmapTextView?.visibility = View.GONE
            scoreTv?.visibility = View.GONE
            overReasonIv.visibility = View.VISIBLE
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_diaoxianle)
        } else {
            scoreTv?.visibility = View.VISIBLE
            bitmapTextView?.visibility = View.VISIBLE
            overReasonIv.visibility = View.GONE
        }
    }

    /**
     * 入场动画
     */
    private fun playCardEnterAnimation() {
        mParentView!!.visibility = View.VISIBLE
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = TranslateAnimation((-U.getDisplayUtils().screenWidth).toFloat(), 0.0f, 0.0f, 0.0f)
            mEnterTranslateAnimation!!.duration = 200
        }
        mEnterTranslateAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                showPkResult()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        mParentView!!.startAnimation(mEnterTranslateAnimation)

        mUiHandler.postDelayed({ hide() }, 3000)
    }

    private fun showPkResult() {
        if (mLeftWin) {
            mLeftWinIv?.visibility = View.VISIBLE
            playWinSVGA(mLeftSvga)
        }

        if (mRightWin) {
            mRightWinIv?.visibility = View.VISIBLE
            playWinSVGA(mRightSvga)
        }
    }

    private fun playWinSVGA(svgaImageView: SVGAImageView?) {

        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation svgaImageView=$svgaImageView")
            return
        }

        if (svgaImageView != null && svgaImageView.isAnimating) {
            // 正在播放
            return
        }

        svgaImageView.visibility = View.VISIBLE
        svgaImageView.loops = 1

        SvgaParserAdapter.parse("grab_pk_result_win.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                svgaImageView.setImageDrawable(drawable)
                svgaImageView.startAnimation()
            }

            override fun onError() {

            }
        })

        svgaImageView.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                if (svgaImageView != null) {
                    svgaImageView.callback = null
                    svgaImageView.stopAnimation(true)
                }
            }

            override fun onRepeat() {
                if (svgaImageView != null && svgaImageView.isAnimating) {
                    svgaImageView.stopAnimation(false)
                }
            }

            override fun onStep(frame: Int, percentage: Double) {

            }
        }
    }

    /**
     * 离场动画，整个pk结束才执行
     */
    fun hide() {
        mUiHandler.removeCallbacksAndMessages(null)
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
                        mParentView!!.clearAnimation()
                        setVisibility(View.GONE)
                        if (mSVGAListener != null) {
                            mSVGAListener!!.onFinished()
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                mParentView!!.startAnimation(mLeaveTranslateAnimation)
            } else {
                mParentView!!.clearAnimation()
                setVisibility(View.GONE)
            }
        }
    }


    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            destory()
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        destory()
    }

    private fun destory() {
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation!!.setAnimationListener(null)
            mEnterTranslateAnimation!!.cancel()
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation!!.setAnimationListener(null)
            mLeaveTranslateAnimation!!.cancel()
        }
        if (mLeftSvga != null) {
            mLeftSvga!!.callback = null
            mLeftSvga!!.stopAnimation(true)
        }
        if (mRightSvga != null) {
            mRightSvga!!.callback = null
            mLeftSvga!!.stopAnimation(true)
        }
        mUiHandler.removeCallbacksAndMessages(null)
    }
}
