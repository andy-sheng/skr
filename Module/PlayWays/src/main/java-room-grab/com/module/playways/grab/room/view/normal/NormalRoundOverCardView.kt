package com.module.playways.grab.room.view.normal

import android.view.View
import android.view.ViewStub
import android.widget.RelativeLayout

import com.common.anim.svga.SvgaParserAdapter
import com.common.utils.U
import com.module.playways.grab.room.ui.GrabRoomFragment
import com.module.playways.listener.SVGAListener
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.R
import com.common.view.ExViewStub
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGADrawable
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.zq.live.proto.GrabRoom.EQRoundOverReason
import com.zq.live.proto.GrabRoom.EQRoundResultType
import com.zq.live.proto.MicRoom.EMRoundOverReason

/**
 * 轮次结束 合唱和正常结束都用此板
 */
class NormalRoundOverCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

    val TAG = "NormalRoundOverCardView"

    internal var mSingResultSvga: SVGAImageView? = null

    internal var mSVGAListener: SVGAListener? = null

    internal var assetsName = ""

    override fun init(parentView: View) {
        mSingResultSvga = parentView.findViewById<View>(R.id.sing_result_svga) as SVGAImageView
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_normal_round_over_card_stub_layout
    }

    fun bindData(lastRoundInfo: GrabRoundInfoModel?, listener: SVGAListener) {
        if (lastRoundInfo == null) {
            return
        }
        tryInflate()
        var songId = 0
        if (lastRoundInfo.music != null) {
            songId = lastRoundInfo.music.itemID
        }
        val reason = lastRoundInfo.overReason
        val resultType = lastRoundInfo.resultType
        this.mSVGAListener = listener
        mParentView!!.visibility = View.VISIBLE

        if (reason == EQRoundOverReason.ROR_NO_ONE_SING.value) {
            // 无人想唱
            assetsName = "grab_none_sing_end.svga"
            startNoneSing(songId)
        } else if (reason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
            // 自己放弃演唱
            assetsName = "grab_sing_abandon_end.svga"
            startFailed(songId)
        } else if (reason == EQRoundOverReason.ROR_CHO_SUCCESS.value) {
            // 合唱成功
            assetsName = "grab_chorus_sucess.svga"
            startChorusSucess(songId)
        } else if (reason == EQRoundOverReason.ROR_CHO_FAILED.value) {
            // 合唱失败
            assetsName = "grab_chorus_failed.svga"
            startChorusFailed(songId)
        } else if (reason == EQRoundOverReason.ROR_CHO_NOT_ENOUTH_PLAYER.value) {
            // 合唱人数不够失败
            assetsName = "grab_sing_none_with.svga"
            startChorusNoneWith(songId)
        } else if (reason == EQRoundOverReason.ROR_SPK_NOT_ENOUTH_PLAYER.value) {
            // pk人数不够
            assetsName = "grab_sing_none_with.svga"
            startOKNoneWith(songId)
        } else if (reason == EQRoundOverReason.ROR_MIN_GAME_NOT_ENOUTH_PLAYER.value) {
            // 连麦小游戏人数不够
            assetsName = "grab_sing_none_with.svga"
            startOKNoneWith(songId)
        } else {
            // 放弃不用单独处理，看在哪个阶段点击放弃的
            if (resultType == EQRoundResultType.ROT_TYPE_1.value) {
                //有种优秀叫一唱到底（全部唱完）
                assetsName = "grab_sing_perfect_end.svga"
                startPerfect(songId)
            } else if (resultType == EQRoundResultType.ROT_TYPE_2.value) {
                //有种结束叫刚刚开始（t<30%）
                assetsName = "grab_sing_moment_end.svga"
                startFailed(songId)
            } else if (resultType == EQRoundResultType.ROT_TYPE_3.value) {
                //有份悲伤叫都没及格(30%<=t <60%)
                assetsName = "grab_sing_no_pass_end.svga"
                startFailed(songId)
            } else if (resultType == EQRoundResultType.ROT_TYPE_4.value) {
                //有种遗憾叫明明可以（60%<=t<90%）
                assetsName = "grab_sing_pass_end.svga"
                startFailed(songId)
            } else if (resultType == EQRoundResultType.ROT_TYPE_5.value) {
                //有种可惜叫我觉得你行（90%<=t<=100%)
                assetsName = "grab_sing_enough_end.svga"
                startFailed(songId)
            } else if (resultType == EQRoundResultType.ROT_TYPE_6.value) {
                //自己放弃演唱
                assetsName = "grab_sing_abandon_end.svga"
                startFailed(songId)
            } else {
                if (mSVGAListener != null) {
                    mSVGAListener!!.onFinished()
                }
            }
        }
    }

    fun bindData(lastRoundInfo: MicRoundInfoModel?, listener: SVGAListener) {
        if (lastRoundInfo == null) {
            return
        }
        tryInflate()
        var songId = lastRoundInfo?.music?.itemID ?: 0
        val reason = lastRoundInfo.overReason
        this.mSVGAListener = listener
        mParentView!!.visibility = View.VISIBLE

        if (reason == EMRoundOverReason.MROR_SELF_GIVE_UP.value) {
            // 自己放弃演唱
            assetsName = "grab_sing_abandon_end.svga"
            startFailed(songId)
        } else if (reason == EMRoundOverReason.MROR_CHO_SUCCESS.value) {
            // 合唱成功
            assetsName = "grab_chorus_sucess.svga"
            startChorusSucess(songId)
        } else if (reason == EMRoundOverReason.MROR_CHO_FAILED.value) {
            // 合唱失败
            assetsName = "grab_chorus_failed.svga"
            startChorusFailed(songId)
        } else {
            if (mSVGAListener != null) {
                mSVGAListener!!.onFinished()
            }
        }
    }

    private fun startOKNoneWith(songId: Int) {
        val lp = mSingResultSvga!!.layoutParams as RelativeLayout.LayoutParams
        lp.height = U.getDisplayUtils().dip2px(190f)
        lp.topMargin = U.getDisplayUtils().dip2px(139f)
        mSingResultSvga!!.layoutParams = lp

        // TODO: 2019/4/22 音效和打点？？？

        playAnimation()
    }

    private fun startChorusNoneWith(songId: Int) {
        val lp = mSingResultSvga!!.layoutParams as RelativeLayout.LayoutParams
        lp.height = U.getDisplayUtils().dip2px(190f)
        lp.topMargin = U.getDisplayUtils().dip2px(139f)
        mSingResultSvga!!.layoutParams = lp

        // TODO: 2019/4/22 音效和打点？？？

        playAnimation()
    }

    private fun startNoneSing(songId: Int) {
        val lp = mSingResultSvga!!.layoutParams as RelativeLayout.LayoutParams
        lp.height = U.getDisplayUtils().dip2px(560f)
        lp.topMargin = 0
        mSingResultSvga!!.layoutParams = lp

        //        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_nobodywants);
        //        HashMap map = new HashMap();
        //        map.put("songId2", String.valueOf(songId));
        //        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
        //                StatConstants.KEY_SONG_NO_ONE, map);
        playAnimation()
    }

    private fun startChorusSucess(songId: Int) {
        val lp = mSingResultSvga!!.layoutParams as RelativeLayout.LayoutParams
        lp.height = U.getDisplayUtils().dip2px(180f)
        lp.topMargin = U.getDisplayUtils().dip2px(150f)
        mSingResultSvga!!.layoutParams = lp
        // TODO: 2019/4/22 音效和打点？？？
        playAnimation()
    }

    private fun startChorusFailed(songId: Int) {
        val lp = mSingResultSvga!!.layoutParams as RelativeLayout.LayoutParams
        lp.height = U.getDisplayUtils().dip2px(180f)
        lp.topMargin = U.getDisplayUtils().dip2px(139f)
        mSingResultSvga!!.layoutParams = lp

        // TODO: 2019/4/22 音效和打点？？？

        playAnimation()
    }

    // 优秀, 目前缺动画
    private fun startPerfect(songId: Int) {
        val lp = mSingResultSvga!!.layoutParams as RelativeLayout.LayoutParams
        lp.height = U.getDisplayUtils().dip2px(180f)
        lp.topMargin = U.getDisplayUtils().dip2px(139f)
        mSingResultSvga!!.layoutParams = lp

        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_challengewin)

        playAnimation()
    }

    // 不够优秀，换字即可，目前缺动画
    private fun startFailed(songId: Int) {

        val lp = mSingResultSvga!!.layoutParams as RelativeLayout.LayoutParams
        lp.height = U.getDisplayUtils().dip2px(180f)
        lp.topMargin = U.getDisplayUtils().dip2px(139f)
        mSingResultSvga!!.layoutParams = lp

        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_challengelose)

        playAnimation()
    }


    private fun playAnimation() {
        mSingResultSvga!!.visibility = View.VISIBLE
        mSingResultSvga!!.loops = 1
        SvgaParserAdapter.parse(assetsName, object : SVGAParser.ParseCompletion {
            override fun onComplete(videoItem: SVGAVideoEntity) {
                val drawable = SVGADrawable(videoItem)
                mSingResultSvga!!.setImageDrawable(drawable)
                mSingResultSvga!!.startAnimation()
            }

            override fun onError() {

            }
        })

        mSingResultSvga!!.callback = object : SVGACallback {
            override fun onPause() {

            }

            override fun onFinished() {
                if (mSingResultSvga != null) {
                    mSingResultSvga!!.callback = null
                    mSingResultSvga!!.stopAnimation(true)
                    mSingResultSvga!!.visibility = View.GONE
                }

                if (mSVGAListener != null) {
                    mSVGAListener!!.onFinished()
                }
            }

            override fun onRepeat() {
                if (mSingResultSvga != null && mSingResultSvga!!.isAnimating) {
                    mSingResultSvga!!.stopAnimation(false)
                }
            }

            override fun onStep(i: Int, v: Double) {

            }
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            this.mSVGAListener = null
            if (mSingResultSvga != null) {
                mSingResultSvga!!.callback = null
                mSingResultSvga!!.stopAnimation(true)
            }
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        this.mSVGAListener = null
        if (mSingResultSvga != null) {
            mSingResultSvga!!.callback = null
            mSingResultSvga!!.stopAnimation(true)
        }
    }
}
