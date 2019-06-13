package com.module.playways.grab.room.view.normal;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.utils.U;
import com.module.playways.grab.room.ui.GrabRoomFragment;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Room.EQRoundOverReason;
import com.zq.live.proto.Room.EQRoundResultType;

/**
 * 轮次结束 合唱和正常结束都用此板
 */
public class NormalRoundOverCardView extends RelativeLayout {

    public final static String TAG = "RoundOverCardView";

    SVGAImageView mSingResultSvga;

    SVGAListener mSVGAListener;

    String assetsName = "";

    public NormalRoundOverCardView(Context context) {
        super(context);
        init();
    }

    public NormalRoundOverCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NormalRoundOverCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_normal_round_over_card_layout, this);
        mSingResultSvga = (SVGAImageView) findViewById(R.id.sing_result_svga);
    }

    public void bindData(GrabRoundInfoModel lastRoundInfo, SVGAListener listener) {
        if (lastRoundInfo == null) {
            return;
        }
        int songId = 0;
        if (lastRoundInfo.getMusic() != null) {
            songId = lastRoundInfo.getMusic().getItemID();
        }
        int reason = lastRoundInfo.getOverReason();
        int resultType = lastRoundInfo.getResultType();
        this.mSVGAListener = listener;
        setVisibility(VISIBLE);

        if (reason == EQRoundOverReason.ROR_NO_ONE_SING.getValue()) {
            // 无人想唱
            assetsName = "grab_none_sing_end.svga";
            startNoneSing(songId);
        } else if (reason == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
            // 自己放弃演唱
            assetsName = "grab_sing_abandon_end.svga";
            startFailed(songId);
        } else if (reason == EQRoundOverReason.ROR_CHO_SUCCESS.getValue()) {
            // 合唱成功
            assetsName = "grab_chorus_sucess.svga";
            startChorusSucess(songId);
        } else if (reason == EQRoundOverReason.ROR_CHO_FAILED.getValue()) {
            // 合唱失败
            assetsName = "grab_chorus_failed.svga";
            startChorusFailed(songId);
        } else if (reason == EQRoundOverReason.ROR_CHO_NOT_ENOUTH_PLAYER.getValue()) {
            // 合唱人数不够失败
            assetsName = "grab_sing_none_with.svga";
            startChorusNoneWith(songId);
        } else if (reason == EQRoundOverReason.ROR_SPK_NOT_ENOUTH_PLAYER.getValue()) {
            // pk人数不够
            assetsName = "grab_sing_none_with.svga";
            startOKNoneWith(songId);
        } else if (reason == EQRoundOverReason.ROR_MIN_GAME_NOT_ENOUTH_PLAYER.getValue()) {
            // 连麦小游戏人数不够
            assetsName = "grab_sing_none_with.svga";
            startOKNoneWith(songId);
        } else {
            // 放弃不用单独处理，看在哪个阶段点击放弃的
            if (resultType == EQRoundResultType.ROT_TYPE_1.getValue()) {
                //有种优秀叫一唱到底（全部唱完）
                assetsName = "grab_sing_perfect_end.svga";
                startPerfect(songId);
            } else if (resultType == EQRoundResultType.ROT_TYPE_2.getValue()) {
                //有种结束叫刚刚开始（t<30%）
                assetsName = "grab_sing_moment_end.svga";
                startFailed(songId);
            } else if (resultType == EQRoundResultType.ROT_TYPE_3.getValue()) {
                //有份悲伤叫都没及格(30%<=t <60%)
                assetsName = "grab_sing_no_pass_end.svga";
                startFailed(songId);
            } else if (resultType == EQRoundResultType.ROT_TYPE_4.getValue()) {
                //有种遗憾叫明明可以（60%<=t<90%）
                assetsName = "grab_sing_pass_end.svga";
                startFailed(songId);
            } else if (resultType == EQRoundResultType.ROT_TYPE_5.getValue()) {
                //有种可惜叫我觉得你行（90%<=t<=100%)
                assetsName = "grab_sing_enough_end.svga";
                startFailed(songId);
            } else if (resultType == EQRoundResultType.ROT_TYPE_6.getValue()) {
                //自己放弃演唱
                assetsName = "grab_sing_abandon_end.svga";
                startFailed(songId);
            } else {
                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }
        }
    }

    private void startOKNoneWith(int songId) {
        LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(190);
        lp.topMargin = U.getDisplayUtils().dip2px(139);
        mSingResultSvga.setLayoutParams(lp);

        // TODO: 2019/4/22 音效和打点？？？

        playAnimation();
    }

    private void startChorusNoneWith(int songId) {
        LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(190);
        lp.topMargin = U.getDisplayUtils().dip2px(139);
        mSingResultSvga.setLayoutParams(lp);

        // TODO: 2019/4/22 音效和打点？？？

        playAnimation();
    }

    private void startNoneSing(int songId) {
        LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(560);
        lp.topMargin = 0;
        mSingResultSvga.setLayoutParams(lp);

//        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_nobodywants);
//        HashMap map = new HashMap();
//        map.put("songId2", String.valueOf(songId));
//        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                StatConstants.KEY_SONG_NO_ONE, map);
        playAnimation();
    }

    private void startChorusSucess(int songId) {
        LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(180);
        lp.topMargin = U.getDisplayUtils().dip2px(150);
        mSingResultSvga.setLayoutParams(lp);
        // TODO: 2019/4/22 音效和打点？？？
        playAnimation();
    }

    private void startChorusFailed(int songId) {
        LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(180);
        lp.topMargin = U.getDisplayUtils().dip2px(139);
        mSingResultSvga.setLayoutParams(lp);

        // TODO: 2019/4/22 音效和打点？？？

        playAnimation();
    }

    // 优秀, 目前缺动画
    private void startPerfect(int songId) {
        LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(180);
        lp.topMargin = U.getDisplayUtils().dip2px(139);
        mSingResultSvga.setLayoutParams(lp);

        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_challengewin);
//        HashMap map = new HashMap();
//        map.put("song_id2", String.valueOf(songId));
//        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                StatConstants.KEY_SONG_SUCCESS, map);

        playAnimation();
    }

    // 不够优秀，换字即可，目前缺动画
    private void startFailed(int songId) {

        LayoutParams lp = (LayoutParams) mSingResultSvga.getLayoutParams();
        lp.height = U.getDisplayUtils().dip2px(180);
        lp.topMargin = U.getDisplayUtils().dip2px(139);
        mSingResultSvga.setLayoutParams(lp);

        U.getSoundUtils().play(GrabRoomFragment.TAG, R.raw.grab_challengelose);
//        HashMap map = new HashMap();
//        map.put("song_id2", String.valueOf(songId));
//        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                StatConstants.KEY_SONG_FAIL, map);

        playAnimation();
    }


    private void playAnimation() {
        mSingResultSvga.setVisibility(VISIBLE);
        mSingResultSvga.setLoops(1);
        SvgaParserAdapter.parse(assetsName, new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mSingResultSvga.setImageDrawable(drawable);
                mSingResultSvga.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        mSingResultSvga.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mSingResultSvga != null) {
                    mSingResultSvga.setCallback(null);
                    mSingResultSvga.stopAnimation(true);
                    mSingResultSvga.setVisibility(GONE);
                }

                if (mSVGAListener != null) {
                    mSVGAListener.onFinished();
                }
            }

            @Override
            public void onRepeat() {
                if (mSingResultSvga != null && mSingResultSvga.isAnimating()) {
                    mSingResultSvga.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            this.mSVGAListener = null;
            if (mSingResultSvga != null) {
                mSingResultSvga.setCallback(null);
                mSingResultSvga.stopAnimation(true);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mSVGAListener = null;
        if (mSingResultSvga != null) {
            mSingResultSvga.setCallback(null);
            mSingResultSvga.stopAnimation(true);
        }
    }
}
