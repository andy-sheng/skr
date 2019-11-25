package com.module.playways.grab.room.view.pk.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.common.anim.svga.SvgaParserAdapter
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExRelativeLayout
import com.common.view.ex.ExTextView
import com.component.level.utils.LevelConfigUtils
import com.component.person.event.ShowPersonCardEvent
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.grab.room.top.CircleAnimationView
import com.module.playways.room.data.H
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.zq.live.proto.GrabRoom.EQRoundOverReason
import org.greenrobot.eventbus.EventBus

/**
 * PKOthersSingCardView 和 PKSelfSingCardView 的公用部分
 * 主要保护放大，画圈和声纹动画或过场动画衔接
 */
class PKSingCardView : RelativeLayout {

    val TAG = "PKSingCardView"

    internal var mLeftSingSvga: SVGAImageView? = null
    internal var mRightSingSvga: SVGAImageView? = null
    internal var mPkArea: LinearLayout? = null

    internal var mLeftPkArea: RelativeLayout? = null
    internal var mLeftArea: RelativeLayout? = null
    internal var mLeftIv: SimpleDraweeView? = null
    internal var mLeftStatusArea: ExRelativeLayout? = null
    internal var mLeftName: ExTextView? = null
    internal var mLeftStatus: ExTextView? = null
    internal var mLeftCircleAnimationView: CircleAnimationView? = null
    internal var mLeftLevelBg: ImageView? = null

    internal var mRightPkArea: RelativeLayout? = null
    internal var mRightArea: RelativeLayout? = null
    internal var mRightIv: SimpleDraweeView? = null
    internal var mRightStatusArea: ExRelativeLayout? = null
    internal var mRightName: ExTextView? = null
    internal var mRightStatus: ExTextView? = null
    internal var mRightCircleAnimationView: CircleAnimationView? = null
    internal var mRightLevelBg: ImageView? = null

    internal var mScaleAnimation: ScaleAnimation? = null        // 头像放大动画
    internal var mValueAnimator: ValueAnimator? = null          // 画圆圈的属性动画
    internal var mAnimatorSet: AnimatorSet? = null              // 左右拉开动画
    internal var mIsPlaySVGA: Boolean = false                   // 是否播放SVGA

    internal var mLeftOverReason: Int = 0
    internal var mRightOverReason: Int = 0

    internal var mLeftUserInfoModel: UserInfoModel? = null
    internal var mRightUserInfoModel: UserInfoModel? = null
    internal var mAnimationListerner: AnimationListerner? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.grab_pk_sing_card_layout, this)

        mLeftSingSvga = findViewById<View>(R.id.left_sing_svga) as SVGAImageView
        mRightSingSvga = findViewById<View>(R.id.right_sing_svga) as SVGAImageView
        mPkArea = findViewById<View>(R.id.pk_area) as LinearLayout

        mLeftPkArea = findViewById<View>(R.id.left_pk_area) as RelativeLayout
        mLeftArea = findViewById<View>(R.id.left_area) as RelativeLayout
        mLeftIv = findViewById<View>(R.id.left_iv) as SimpleDraweeView
        mLeftStatusArea = findViewById<View>(R.id.left_status_area) as ExRelativeLayout
        mLeftName = findViewById<View>(R.id.left_name) as ExTextView
        mLeftStatus = findViewById<View>(R.id.left_status) as ExTextView
        mLeftCircleAnimationView = findViewById<View>(R.id.left_circle_animation_view) as CircleAnimationView
        mLeftLevelBg = findViewById(R.id.left_level_bg)

        mRightPkArea = findViewById<View>(R.id.right_pk_area) as RelativeLayout
        mRightArea = findViewById<View>(R.id.right_area) as RelativeLayout
        mRightIv = findViewById<View>(R.id.right_iv) as SimpleDraweeView
        mRightStatusArea = findViewById<View>(R.id.right_status_area) as ExRelativeLayout
        mRightName = findViewById<View>(R.id.right_name) as ExTextView
        mRightStatus = findViewById<View>(R.id.right_status) as ExTextView
        mRightCircleAnimationView = findViewById<View>(R.id.right_circle_animation_view) as CircleAnimationView
        mRightLevelBg = findViewById(R.id.right_level_bg)

        mLeftIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mLeftUserInfoModel != null) {
                    EventBus.getDefault().post(ShowPersonCardEvent(mLeftUserInfoModel!!.userId))
                }
            }
        })

        mRightIv?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (mRightUserInfoModel != null) {
                    EventBus.getDefault().post(ShowPersonCardEvent(mRightUserInfoModel!!.userId))
                }
            }
        })
    }

    fun bindData() {
        if (H.isGrabRoom()) {
            val grabRoundInfoModel = H.grabRoomData!!.realRoundInfo
            if (grabRoundInfoModel == null) {
                MyLog.w(TAG, "setRoomData grabRoundInfoModel=$grabRoundInfoModel")
                return
            }

            reset()

            val list = grabRoundInfoModel.getsPkRoundInfoModels()
            if (list != null && list.size >= 2) {
                mLeftUserInfoModel = H.grabRoomData!!.getPlayerOrWaiterInfo(list[0].userID)
                mLeftOverReason = list[0].overReason

                mRightUserInfoModel = H.grabRoomData!!.getPlayerOrWaiterInfo(list[1].userID)
                mRightOverReason = list[1].overReason
            }
        } else if (H.isMicRoom()) {
            val grabRoundInfoModel = H.micRoomData!!.realRoundInfo
            if (grabRoundInfoModel == null) {
                MyLog.w(TAG, "setRoomData grabRoundInfoModel=$grabRoundInfoModel")
                return
            }

            reset()

            val list = grabRoundInfoModel.getsPkRoundInfoModels()
            if (list != null && list.size >= 2) {
                mLeftUserInfoModel = H.micRoomData!!.getPlayerOrWaiterInfo(list[0].userID)
                mLeftOverReason = list[0].overReason

                mRightUserInfoModel = H.micRoomData!!.getPlayerOrWaiterInfo(list[1].userID)
                mRightOverReason = list[1].overReason
            }
        }


        visibility = View.VISIBLE
        if (mLeftUserInfoModel != null) {
            if (mLeftOverReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
                mLeftStatusArea?.visibility = View.VISIBLE
                mLeftStatus?.visibility = View.VISIBLE
                mLeftStatus?.text = "不唱了"
            } else if (mLeftOverReason == EQRoundOverReason.ROR_MULTI_NO_PASS.value) {
                mLeftStatusArea?.visibility = View.VISIBLE
                mLeftStatus?.visibility = View.VISIBLE
                mLeftStatus?.text = "被灭灯"
            } else if (mLeftOverReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.value) {
                mLeftStatusArea?.visibility = View.VISIBLE
                mLeftStatus?.visibility = View.VISIBLE
                mLeftStatus?.text = "退出了"
            } else {
                mLeftStatusArea?.visibility = View.GONE
                mLeftStatus?.visibility = View.GONE
            }
            AvatarUtils.loadAvatarByUrl(mLeftIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel!!.avatar)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())
            mLeftName?.text = mLeftUserInfoModel!!.nicknameRemark
            mLeftLevelBg?.setBackground(U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(mLeftUserInfoModel!!.ranking.mainRanking)))
        }
        if (mRightUserInfoModel != null) {
            if (mRightOverReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
                mRightStatusArea?.visibility = View.VISIBLE
                mRightStatus?.visibility = View.VISIBLE
                mRightStatus?.text = "不唱了"
            } else if (mRightOverReason == EQRoundOverReason.ROR_MULTI_NO_PASS.value) {
                mRightStatusArea?.visibility = View.VISIBLE
                mRightStatus?.visibility = View.VISIBLE
                mRightStatus?.text = "被灭灯"
            } else if (mRightOverReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.value) {
                mRightStatusArea?.visibility = View.VISIBLE
                mRightStatus?.visibility = View.VISIBLE
                mRightStatus?.text = "退出了"
            } else {
                mRightStatusArea?.visibility = View.GONE
                mRightStatus?.visibility = View.GONE
            }
            AvatarUtils.loadAvatarByUrl(mRightIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel!!.avatar)
                            .setBorderColor(U.getColor(R.color.white))
                            .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                            .setCircle(true)
                            .build())
            mRightName?.text = mRightUserInfoModel!!.nicknameRemark
            mRightLevelBg?.setBackground(U.getDrawable(LevelConfigUtils.getRaceCenterAvatarBg(mRightUserInfoModel!!.ranking.mainRanking)))
        }
    }

    fun reset() {
        mLeftIv?.clearAnimation()
        mRightIv?.clearAnimation()
        mLeftCircleAnimationView?.visibility = View.GONE
        mRightCircleAnimationView?.visibility = View.GONE
        mLeftStatusArea?.visibility = View.GONE
        mLeftStatusArea?.visibility = View.GONE
        mLeftStatus?.visibility = View.GONE
        mRightStatus?.visibility = View.GONE

        mLeftUserInfoModel = null
        mRightUserInfoModel = null
        mLeftOverReason = 0
        mRightOverReason = 0

        if (mScaleAnimation != null) {
            mScaleAnimation!!.setAnimationListener(null)
            mScaleAnimation!!.cancel()
        }
        if (mValueAnimator != null) {
            mValueAnimator!!.removeAllListeners()
            mValueAnimator!!.removeAllUpdateListeners()
            mValueAnimator!!.cancel()
        }
        if (mLeftSingSvga != null) {
            mLeftSingSvga!!.callback = null
            mLeftSingSvga!!.stopAnimation(true)
        }
        if (mRightSingSvga != null) {
            mRightSingSvga!!.callback = null
            mRightSingSvga!!.stopAnimation(true)
        }
        if (mAnimationListerner != null) {
            mAnimationListerner = null
        }

        if (mAnimatorSet != null) {
            mAnimatorSet!!.removeAllListeners()
            mAnimatorSet!!.cancel()
        }
    }

    fun playScaleWithoutAnimation(userId: Int) {
        if (mLeftUserInfoModel != null && userId == mLeftUserInfoModel!!.userId) {
            mLeftArea?.scaleX = 1.35f
            mLeftArea?.scaleY = 1.35f
        } else if (mRightUserInfoModel != null && userId == mRightUserInfoModel!!.userId) {
            mRightArea?.scaleX = 1.35f
            mRightArea?.scaleY = 1.35f
        }
    }

    /**
     * @param uid        播放谁的动画
     * @param isPlaySVGA 是否播放声纹SVGA
     */
    fun playScaleAnimation(uid: Int, isPlaySVGA: Boolean, animationListerner: AnimationListerner) {

        // TODO: 2019/4/23 开始播放动画
        if (mScaleAnimation == null) {
            mScaleAnimation = ScaleAnimation(1.0f, 1.35f, 1f, 1.35f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
            mScaleAnimation!!.interpolator = OvershootInterpolator()
            mScaleAnimation!!.fillAfter = true
            mScaleAnimation!!.duration = 500
        } else {
            mScaleAnimation!!.setAnimationListener(null)
            mScaleAnimation!!.cancel()
        }
        mScaleAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                playCircleAnimation(uid)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        this.mIsPlaySVGA = isPlaySVGA
        this.mAnimationListerner = animationListerner

        if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel!!.userId) {
            mLeftArea?.startAnimation(mScaleAnimation)
        } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel!!.userId) {
            mRightArea?.startAnimation(mScaleAnimation)
        } else {
            MyLog.w(TAG, "playScaleAnimation uid 无效 uid=$uid")
        }
    }


    private fun playCircleAnimation(uid: Int) {
        if (mValueAnimator != null) {
            mValueAnimator!!.removeAllUpdateListeners()
            mValueAnimator!!.cancel()
        }
        if (mValueAnimator == null) {
            mValueAnimator = ValueAnimator()
            mValueAnimator!!.setIntValues(0, 100)
            mValueAnimator!!.duration = 495
        }
        mValueAnimator!!.addUpdateListener { animation ->
            val p = animation.animatedValue as Int
            if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel!!.userId) {
                mLeftCircleAnimationView?.setProgress(p)
            } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel!!.userId) {
                mRightCircleAnimationView?.setProgress(p)
            }
        }

        mValueAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel!!.userId) {
                    mLeftCircleAnimationView?.visibility = View.VISIBLE
                } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel!!.userId) {
                    mRightCircleAnimationView?.visibility = View.VISIBLE
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel!!.userId) {
                    mLeftCircleAnimationView?.visibility = View.GONE
                } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel!!.userId) {
                    mRightCircleAnimationView?.visibility = View.GONE
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel!!.userId) {
                    mLeftCircleAnimationView?.visibility = View.GONE
                } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel!!.userId) {
                    mRightCircleAnimationView?.visibility = View.GONE
                }

                if (mAnimationListerner != null) {
                    mAnimationListerner!!.onAnimationEndExcludeSvga()
                }

                if (mIsPlaySVGA) {
                    playSingAnimation(uid)
                } else {
                    mLeftSingSvga!!.visibility = View.GONE
                    mRightSingSvga!!.visibility = View.GONE
                }
            }
        })
        mValueAnimator!!.start()
    }

    /**
     * 大幕拉开
     */
//    fun playWithDraw() {
//        if (mAnimatorSet == null) {
//            mAnimatorSet = AnimatorSet()
//            val left = ObjectAnimator.ofFloat<View>(mLeftPkArea, View.TRANSLATION_X, 0f, -U.getDisplayUtils().screenWidth / 2f)
//            left.setDuration(500)
//
//            val right = ObjectAnimator.ofFloat<View>(mRightPkArea, View.TRANSLATION_X, 0f, U.getDisplayUtils().screenWidth / 2f)
//            right.setDuration(500)
//
//            mAnimatorSet!!.playTogether(left, right)
//        }
//
//        visibility = View.VISIBLE
//        mAnimatorSet!!.removeAllListeners()
//        mAnimatorSet!!.cancel()
//
//        mAnimatorSet!!.addListener(object : Animator.AnimatorListener {
//            override fun onAnimationStart(animation: Animator) {
//
//            }
//
//            override fun onAnimationEnd(animation: Animator) {
//                visibility = View.GONE
//                mLeftPkArea?.translationX = 0f
//                mRightPkArea?.translationX = 0f
//                if (mAnimationListerner != null) {
//                    mAnimationListerner!!.onAnimationEndWithDraw()
//                }
//            }
//
//            override fun onAnimationCancel(animation: Animator) {
//                onAnimationEnd(animation)
//            }
//
//            override fun onAnimationRepeat(animation: Animator) {
//
//            }
//        })
//        mAnimatorSet!!.start()
//    }

    // TODO: 2019/4/23 播放声纹动画，同时倒计时开始计时
    fun playSingAnimation(uid: Int) {
        if (mLeftUserInfoModel != null && uid == mLeftUserInfoModel!!.userId) {
            playSingAnimation(mLeftSingSvga)
        } else if (mRightUserInfoModel != null && uid == mRightUserInfoModel!!.userId) {
            playSingAnimation(mRightSingSvga)
        }
    }

    // 播放声纹动画
    private fun playSingAnimation(svgaImageView: SVGAImageView?) {
        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation svgaImageView=$svgaImageView")
            return
        }

        if (svgaImageView != null && svgaImageView.isAnimating) {
            // 正在播放
            return
        }

        svgaImageView.visibility = View.VISIBLE
        svgaImageView.loops = -1

        SvgaParserAdapter.parse("grab_main_stage.svga", object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                svgaImageView.setImageDrawable(drawable)
                svgaImageView.startAnimation()
            }

            override fun onError() {

            }
        })
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        reset()
    }

    interface AnimationListerner {
        /**
         * 动画播放完毕，不包括svga, 不包括拉开动画
         */
        fun onAnimationEndExcludeSvga()

        /**
         * 拉开动画播放完毕
         */
        fun onAnimationEndWithDraw()
    }
}
