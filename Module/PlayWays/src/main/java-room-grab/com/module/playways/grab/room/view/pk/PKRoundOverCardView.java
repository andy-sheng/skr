package com.module.playways.grab.room.view.pk;

import android.os.Handler;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.anim.svga.SvgaParserAdapter;
import com.common.core.avatar.AvatarUtils;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.SPkRoundInfoModel;
import com.common.view.ExViewStub;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.zq.live.proto.Room.EQRoundOverReason;

import java.util.List;


/**
 * PK 结果卡片
 */
public class PKRoundOverCardView extends ExViewStub {

    public final String TAG = "PKRoundOverCardView";

    LinearLayout mPkArea;
    ImageView mLeftAvatarBg;
    SimpleDraweeView mLeftAvatarIv;
    ExTextView mLeftName;
    ImageView mLeftOverReasonIv;

    ImageView mRightAvatarBg;
    SimpleDraweeView mRightAvatarIv;
    ExTextView mRightName;
    ImageView mRightOverReasonIv;

    LinearLayout mSroreArea;
    TextView mLeftTipsTv;
    BitmapTextView mLeftScoreBtv;
    BitmapTextView mRightScoreBtv;
    TextView mRightTipsTv;

    ImageView mLeftWinIv;
    ImageView mRightWinIv;
    SVGAImageView mLeftSvga;
    SVGAImageView mRightSvga;

    Handler mUiHandler = new Handler();

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    UserInfoModel mLeftUserInfoModel;
    UserInfoModel mRightUserInfoModel;
    int mLeftOverReason;
    int mRightOverReason;
    String mLeftScore;
    String mRightScore;
    boolean mLeftWin = false;     // 标记左边是否胜利
    boolean mRightWin = false;    // 标记右边是否胜利
    private GrabRoomData mRoomData;
    SVGAListener mSVGAListener;

    public PKRoundOverCardView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mPkArea = (LinearLayout) parentView.findViewById(R.id.pk_area);
        mLeftAvatarBg = (ImageView) parentView.findViewById(R.id.left_avatar_bg);
        mLeftAvatarIv = (SimpleDraweeView) parentView.findViewById(R.id.left_avatar_iv);
        mLeftName = (ExTextView) parentView.findViewById(R.id.left_name);
        mLeftOverReasonIv = (ImageView) parentView.findViewById(R.id.left_over_reason_iv);
        mRightAvatarBg = (ImageView) parentView.findViewById(R.id.right_avatar_bg);
        mRightAvatarIv = (SimpleDraweeView) parentView.findViewById(R.id.right_avatar_iv);
        mRightName = (ExTextView) parentView.findViewById(R.id.right_name);
        mRightOverReasonIv = (ImageView) parentView.findViewById(R.id.right_over_reason_iv);
        mSroreArea = (LinearLayout) parentView.findViewById(R.id.srore_area);
        mLeftTipsTv = (TextView) parentView.findViewById(R.id.left_tips_tv);
        mLeftScoreBtv = (BitmapTextView) parentView.findViewById(R.id.left_score_btv);
        mRightScoreBtv = (BitmapTextView) parentView.findViewById(R.id.right_score_btv);
        mRightTipsTv = (TextView) parentView.findViewById(R.id.right_tips_tv);

        mLeftWinIv = (ImageView) parentView.findViewById(R.id.left_win_iv);
        mRightWinIv = (ImageView) parentView.findViewById(R.id.right_win_iv);
        mLeftSvga = (SVGAImageView) parentView.findViewById(R.id.left_svga);
        mRightSvga = (SVGAImageView) parentView.findViewById(R.id.right_svga);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_pk_round_over_card_stub_layout;
    }

    public void bindData(GrabRoundInfoModel roundInfoModel, SVGAListener svgaListener) {
        tryInflate();
        reset();
        this.mSVGAListener = svgaListener;
        List<SPkRoundInfoModel> list = roundInfoModel.getsPkRoundInfoModels();
        if (list != null && list.size() >= 2) {
            mLeftUserInfoModel = mRoomData.getUserInfo(list.get(0).getUserID());
            this.mLeftScore = String.format("%.1f", list.get(0).getScore());
            this.mLeftOverReason = list.get(0).getOverReason();
            this.mLeftWin = list.get(0).isWin();

            mRightUserInfoModel = mRoomData.getUserInfo(list.get(1).getUserID());
            this.mRightScore = String.format("%.1f", list.get(1).getScore());
            this.mRightOverReason = list.get(1).getOverReason();
            this.mRightWin = list.get(1).isWin();
        }

        if (mLeftUserInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mLeftAvatarIv,
                    AvatarUtils.newParamsBuilder(mLeftUserInfoModel.getAvatar())
                            .setCircle(true)
                            .build());
            mLeftName.setText(mLeftUserInfoModel.getNicknameRemark());
            mLeftScoreBtv.setText(mLeftScore);
            showOverReason(mLeftOverReason, mLeftOverReasonIv, mLeftScoreBtv, mLeftTipsTv);
        }

        if (mRightUserInfoModel != null) {
            AvatarUtils.loadAvatarByUrl(mRightAvatarIv,
                    AvatarUtils.newParamsBuilder(mRightUserInfoModel.getAvatar())
                            .setCircle(true)
                            .build());
            mRightName.setText(mRightUserInfoModel.getNicknameRemark());
            mRightScoreBtv.setText(mRightScore);
            showOverReason(mRightOverReason, mRightOverReasonIv, mRightScoreBtv, mRightTipsTv);
        }

        playCardEnterAnimation();
    }

    private void reset() {
        mLeftUserInfoModel = null;
        mRightUserInfoModel = null;
        mLeftWinIv.setVisibility(View.GONE);
        mRightWinIv.setVisibility(View.GONE);
        if (mLeftSvga != null) {
            mLeftSvga.setCallback(null);
            mLeftSvga.stopAnimation(true);
        }
        if (mRightSvga != null) {
            mRightSvga.setCallback(null);
            mRightSvga.stopAnimation(true);
        }
        mLeftWin = false;
        mRightWin = false;
        mLeftScore = "0.0";
        mRightScore = "0.0";
    }

    private void showOverReason(int overReason, ImageView overReasonIv, BitmapTextView bitmapTextView, TextView scoreTv) {
        if (overReasonIv == null) {
            return;
        }
        if (overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.getValue()) {
            bitmapTextView.setVisibility(View.GONE);
            scoreTv.setVisibility(View.GONE);
            overReasonIv.setVisibility(View.VISIBLE);
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_buchangle);
        } else if (overReason == EQRoundOverReason.ROR_MULTI_NO_PASS.getValue()) {
            bitmapTextView.setVisibility(View.GONE);
            scoreTv.setVisibility(View.GONE);
            overReasonIv.setVisibility(View.VISIBLE);
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_miedeng);
        } else if (overReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.getValue()) {
            bitmapTextView.setVisibility(View.GONE);
            scoreTv.setVisibility(View.GONE);
            overReasonIv.setVisibility(View.VISIBLE);
            overReasonIv.setBackgroundResource(R.drawable.grab_pk_diaoxianle);
        } else {
            scoreTv.setVisibility(View.VISIBLE);
            bitmapTextView.setVisibility(View.VISIBLE);
            overReasonIv.setVisibility(View.GONE);
        }
    }

    /**
     * 入场动画
     */
    private void playCardEnterAnimation() {
        mParentView.setVisibility(View.VISIBLE);
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        mEnterTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showPkResult();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mParentView.startAnimation(mEnterTranslateAnimation);

        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, 3000);
    }

    private void showPkResult() {
        if (mLeftWin) {
            mLeftWinIv.setVisibility(View.VISIBLE);
            playWinSVGA(mLeftSvga);
        }

        if (mRightWin) {
            mRightWinIv.setVisibility(View.VISIBLE);
            playWinSVGA(mRightSvga);
        }
    }

    private void playWinSVGA(SVGAImageView svgaImageView) {

        if (svgaImageView == null) {
            MyLog.w(TAG, "playSingAnimation" + " svgaImageView=" + svgaImageView);
            return;
        }

        if (svgaImageView != null && svgaImageView.isAnimating()) {
            // 正在播放
            return;
        }

        svgaImageView.setVisibility(View.VISIBLE);
        svgaImageView.setLoops(1);

        SvgaParserAdapter.parse("grab_pk_result_win.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity videoItem) {
                SVGADrawable drawable = new SVGADrawable(videoItem);
                svgaImageView.setImageDrawable(drawable);
                svgaImageView.startAnimation();
            }

            @Override
            public void onError() {

            }
        });

        svgaImageView.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (svgaImageView != null) {
                    svgaImageView.setCallback(null);
                    svgaImageView.stopAnimation(true);
                }
            }

            @Override
            public void onRepeat() {
                if (svgaImageView != null && svgaImageView.isAnimating()) {
                    svgaImageView.stopAnimation(false);
                }
            }

            @Override
            public void onStep(int frame, double percentage) {

            }
        });
    }

    /**
     * 离场动画，整个pk结束才执行
     */
    public void hide() {
        mUiHandler.removeCallbacksAndMessages(null);
        if(mParentView != null){
            if (mParentView.getVisibility() == View.VISIBLE) {
                if (mLeaveTranslateAnimation == null) {
                    mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                    mLeaveTranslateAnimation.setDuration(200);
                }
                mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mParentView.clearAnimation();
                        setVisibility(View.GONE);
                        if (mSVGAListener != null) {
                            mSVGAListener.onFinished();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mParentView.startAnimation(mLeaveTranslateAnimation);
            } else {
                mParentView.clearAnimation();
                setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            destory();
        }
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        destory();
    }

    private void destory() {
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
        if (mLeftSvga != null) {
            mLeftSvga.setCallback(null);
            mLeftSvga.stopAnimation(true);
        }
        if (mRightSvga != null) {
            mRightSvga.setCallback(null);
            mLeftSvga.stopAnimation(true);
        }
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
